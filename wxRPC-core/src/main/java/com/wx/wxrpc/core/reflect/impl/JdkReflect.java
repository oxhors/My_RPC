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
import com.wx.wxrpc.core.loadbalance.RpcContext;
import com.wx.wxrpc.core.meta.InstanceMeta;
import com.wx.wxrpc.core.meta.ServiceMeta;
import com.wx.wxrpc.core.reflect.reflect;
import com.wx.wxrpc.core.registry.RegisterCenter;
import com.wx.wxrpc.core.utils.MethodUtils;
import com.wx.wxrpc.core.utils.TypeUtils;
import lombok.SneakyThrows;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

        //上下文保存了路由器和选择器
        private RpcContext rpcContext;

        public JdkInvocationHandler(String serviceName, List<InstanceMeta> nodes, RpcContext rpcContext) {
            this.serviceName = serviceName;
            this.nodes = nodes;
            this.rpcContext = rpcContext;
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
//            }
                    log.info("消费者发送http请求，请求消息为：{}", request.toString());
                    RpcResponse response = getResponse(request, method);
                    if (!response.getStatus()) {
                        throw new RpcException(response.getErrorCode());
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

        /**
         * 发送http请求的实际方法，拿到http响应
         * @param request 请求对象
         * @return
         */
        private RpcResponse getResponse(RpcRequest request,Method method) throws SocketTimeoutException {
            Gson gson = new GsonBuilder().
                    registerTypeAdapter(Class.class, new ClassCodec())
                    .create();
            String requsetJson = gson.toJson(request);
            //转换原信息为url
            List<String> urls = nodes.stream().map(node -> Strings.lenientFormat("http://%s:%s", node.getHost(), String.valueOf(node.getPort()))).toList();
            log.info("消费者获取到的提供者的服务列表为：{}",urls);
            String url = rpcContext.getLoadBalance().choose(rpcContext.getRouter().rout(urls));
            log.info("消费者选择的提供者的地址为：{}",url);
            try {
                Response response = okHttpClient.newCall(new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(requsetJson, MediaType.get("application/json; charset=utf-8")))
                        .build()).execute();
                // 获取结果，做一下类型处理
                //return TypeUtils.getRpcResponse(method, response);
                RpcResponse rpcResponse = JSONObject.parseObject(response.body().string(), RpcResponse.class);
                if (rpcResponse.getErrorCode().equals(RpcExceptionEnum.X002.getErrorCode())) {
                    throw new SocketTimeoutException("服务调用超时");
                }
                else return rpcResponse;
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
