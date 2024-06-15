package com.wx.wxrpc.core.reflect.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import com.wx.wxrpc.core.exception.RpcException;
import com.wx.wxrpc.core.exception.RpcExceptionEnum;
import com.wx.wxrpc.core.filter.Filter;
import com.wx.wxrpc.core.govern.SlidingTimeWindow;
import com.wx.wxrpc.core.loadbalance.RpcContext;
import com.wx.wxrpc.core.meta.InstanceMeta;
import com.wx.wxrpc.core.meta.ServiceMeta;
import com.wx.wxrpc.core.reflect.reflect;
import com.wx.wxrpc.core.registry.RegisterCenter;
import com.wx.wxrpc.core.utils.MethodUtils;
import com.wx.wxrpc.core.utils.TypeUtils;
import lombok.SneakyThrows;
import lombok.Synchronized;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@ConditionalOnProperty(prefix = "wxrpc.reflect.type" , value = "jdk")
public class JdkReflect implements reflect {

    private final Logger log = LoggerFactory.getLogger(JdkReflect.class);
    @Override
    public Object getProxyInstance(ServiceMeta serviceMeta, RpcContext rpcContext, RegisterCenter registerCenter) {
        Object res = null;
        try {
            //给每一个服务名对应的服务列表注册了一个回调函数，当发生变化时候回调用更新urls，即更新handler中持有的列表引用
            List<InstanceMeta> nodes = registerCenter.findAll(serviceMeta);
            //从元信息中解析出url
            //List<String> urls = createProviders(nodes);
            //注册回调函数，invoke方法会使用这个urls，所以urls变化会被感知
            registerCenter.subscribe(serviceMeta,(event) ->{
                //订阅服务变化，更新
                nodes.clear();
                nodes.addAll(event.getNodes());
            });

            Class<?> interfaceClass = Class.forName(serviceMeta.getServiceName());
            res = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{interfaceClass},
                    new JdkInvocationHandler(serviceMeta.getServiceName(), nodes, rpcContext));
            return res;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //从元信息中解析出url
    private List<String> createProviders(List<InstanceMeta> nodes){
        return nodes.stream().map(node -> Strings.lenientFormat("http://%s:%s",node.getHost(),node.getPort())).toList();
    }

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
          //  .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
            .connectTimeout(10,TimeUnit.SECONDS)
            .readTimeout(10,TimeUnit.SECONDS)
            .writeTimeout(10,TimeUnit.SECONDS).build();

    /**
     * 创建动态代理对象，实际调用方法会走这个handler里面的Invoke
     */
    class JdkInvocationHandler implements InvocationHandler {
        private final String serviceName;

        //服务列表
        private List<InstanceMeta> nodes;
        //隔离的节点
        private List<InstanceMeta> isolatedNodes = new ArrayList<>();
        //探活列表
        private List<InstanceMeta> halfOpenNodes = new ArrayList<>();
        //每个服务实例对应的滑动窗口 实例地址 + 服务名
        private Map<String, SlidingTimeWindow> slidingTimeWindowMap = new HashMap<>();

        private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        //上下文保存了路由器和选择器
        private RpcContext rpcContext;

        private final Lock recordFaultLock = new ReentrantLock();

        public JdkInvocationHandler(String serviceName, List<InstanceMeta> nodes, RpcContext rpcContext) {
            this.serviceName = serviceName;
            this.nodes = nodes;
            this.rpcContext = rpcContext;
            //定期从隔离列表加入探活列表
            scheduledExecutorService.scheduleAtFixedRate(()->{
                synchronized (nodes){
                    halfOpenNodes.addAll(isolatedNodes);
                    isolatedNodes.clear();
                }
            },10,10,TimeUnit.SECONDS);
        }

        //真正发送http请求的地方
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            RpcRequest request = new RpcRequest();
            if(MethodUtils.checkLocalMethod(method)){
                return null;
            }
            Object result = null;
            int retryTimes = 2;
            while(retryTimes-- > 0) {
                try {
                    request.setServiceName(serviceName);
                    Type[] types = method.getGenericParameterTypes();
                    Class<?>[] paras = new Class<?>[types.length];
                    for (int i = 0; i < types.length; i++) {

                        paras[i] = Class.forName(types[i].getTypeName());
                    }
                    request.setParas(paras);
                    request.setArgs(args);
                    request.setMethodName(method.getName());
                    //发送http请求，请求体为request
                    List<Filter> filters = rpcContext.getFilters();
                    // 过滤器前置处理逻辑
//            Object o = null;
//            for (Filter filter : filters) {
//                o = filter.preFilter(request);
//            }
//            if(Objects.nonNull(o)){
//                log.info("消费者使用了缓存结果....{}",o);
//                return o;
                    InstanceMeta node = null;
                    synchronized (nodes){
                        if(halfOpenNodes.isEmpty()){
                            node = rpcContext.getLoadBalance().choose(rpcContext.getRouter().rout(nodes));
                        }else {
                            //从探活列表中取出节点
                            node = halfOpenNodes.get(0);
                        }
                    }
                    String url = Strings.lenientFormat("http://%s:%s", node.getHost(), String.valueOf(node.getPort()));
                    log.info("消费者选择的提供者的地址为：{}",url);
                    RpcResponse response = null;
                    try {
                        log.info("消费者发送http请求，请求消息为：{}", request.toString());
                        response = getResponse(request, method,url);
                        if (!response.getStatus()) {
                            throw new RpcException(response.getErrorCode());
                        }
                    } catch (Exception e) {
                        //发生故障了，异常继续抛出，做故障计数
                       slidingTimeWindowMap.putIfAbsent(url,new SlidingTimeWindow());
                        SlidingTimeWindow window = slidingTimeWindowMap.get(url);
                        int sum = window.getSum();
                        recordFaultLock.lock();
                        try {
                            window.record(System.currentTimeMillis());
                        }finally {
                            recordFaultLock.unlock();
                        }
                        log.info("服务{}出现了{}次故障",url,sum);
                        if(sum >= 10){
                            //隔离故障节点
                            isoloate(node);
                        }
                        throw e;
                    }
                    //探活成功
                    synchronized (nodes){
                        halfOpenNodes.remove(node);
                        isolatedNodes.remove(node);
                        nodes.add(node);
                    }
                   //接受到的响应
                    result = response.getData();
                    //获取结果后要进行类型转换
                    result = TypeUtils.castFastJsonRetObject(result, method);

                    // 过滤器后置逻辑
                    //  for (Filter filter : filters) {
//              //主要做了缓存的逻辑
//              filter.postFilter(request, response, response.getData());
                    return result;
                } catch (Exception e) {
                    if ( !(e instanceof SocketTimeoutException)) {
                        throw e;
                    }
                    else log.info("=======>请求超时");
                }
            }
            return result;
        }

        //隔离故障节点
        private void isoloate(InstanceMeta node) {
            synchronized (nodes){
                if(nodes.contains(node)){
                    nodes.remove(node);
                }
                if(halfOpenNodes.contains(node)){
                    halfOpenNodes.remove(node);
                }
                isolatedNodes.add(node);
            }
        }

        /**
         * 发送http请求的实际方法，拿到http响应
         * @param request 请求对象
         * @return
         */
        private RpcResponse getResponse(RpcRequest request,Method method,String url) throws SocketTimeoutException {
            Gson gson = new GsonBuilder().
                    registerTypeAdapter(Class.class, new ClassCodec())
                    .create();
            String requsetJson = gson.toJson(request);
            //转换原信息为url

            try {
                Response response = okHttpClient.newCall(new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(requsetJson, MediaType.get("application/json; charset=utf-8")))
                        .build()).execute();
                // 获取结果，做一下类型处理
                //return TypeUtils.getRpcResponse(method, response);
                RpcResponse rpcResponse = JSONObject.parseObject(response.body().string(), RpcResponse.class);
                if (rpcResponse.getErrorCode().equals(RpcExceptionEnum.X002.getErrorCode())) {
                    //throw new SocketTimeoutException("服务调用超时");
                    log.info("服务调用超时，url:{}",url);
                }
                return rpcResponse;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>>{

        // 反序列化
        @SneakyThrows
        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String clazz = jsonElement.getAsString();
            return Class.forName(clazz);
        }

        // 序列化
        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            // 将 Class 变为 json
            return new JsonPrimitive(aClass.getName());
        }
    }


}
