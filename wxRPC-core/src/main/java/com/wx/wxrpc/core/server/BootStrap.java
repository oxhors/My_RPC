package com.wx.wxrpc.core.server;

import com.google.common.base.Strings;
import com.wx.wxrpc.core.annoation.RpcService;
import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import com.wx.wxrpc.core.exception.RpcException;
import com.wx.wxrpc.core.meta.InstanceMeta;
import com.wx.wxrpc.core.meta.ServiceMeta;
import com.wx.wxrpc.core.registry.RegisterCenter;
import jakarta.annotation.PostConstruct;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//加载时把所有标注RpcService的类添加到map中
@Component
public class BootStrap implements ApplicationContextAware, EnvironmentAware {

    private final Logger log = LoggerFactory.getLogger(BootStrap.class);
    public static Map<String,Object> serviceMap = new HashMap<>();
    //拿到容器
    private ApplicationContext applicationContext;

    private Environment environment;

    private String app;
    private String namespace;
    private String env;

    //服务一启动，对于所有的对外提供的方法，元信息都是一样的
    private InstanceMeta instanceMeta;
    @PostConstruct
    public void initServiceMap(){
        try {
            String port = environment.getProperty("server.port");
            app = environment.getProperty("app.id");
            namespace = environment.getProperty("app.namespace");
            env = environment.getProperty("app.env");

            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            instanceMeta = InstanceMeta.builder()
                    .schema("http")
                    .host(hostAddress)
                    .port(Integer.valueOf(port))
                    .build();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> services = applicationContext.getBeansWithAnnotation(RpcService.class);

        for (Map.Entry<String, Object> entry : services.entrySet()) {
            Object serviceImpl = entry.getValue();
            Class<?>[] interfaces = serviceImpl.getClass().getInterfaces();

            for (Class<?> anInterface : interfaces) {

                //对于实现类的每一个接口，都要对应这个实现类对象，多对一的关系
                log.info("服务提供者开始注册服务,服务名为：{}，实例对象为：{}",anInterface.getCanonicalName(),serviceImpl);
                serviceMap.put(anInterface.getName(),entry.getValue());
            }
        }
        //registerService();
    }

    public void start(){
        log.info("服务提供者开始将服务列表注册到zk.....");
        RegisterCenter registerCenter = applicationContext.getBean(RegisterCenter.class);
        registerCenter.start();
        registerService();
    }

    //注销所有服务何其对应的地址
    @PreDestroy
    private void unRegisterService(){
        log.info("服务提供者开始将服务从zk注销.....");
        RegisterCenter registerCenter = applicationContext.getBean(RegisterCenter.class);
        serviceMap.keySet().forEach(service ->{
            ServiceMeta serviceMeta = ServiceMeta.builder()
                    .app(app)
                    .namespace(namespace)
                    .env(env)
                    .serviceName(service)
                    .build();
            registerCenter.unregister(serviceMeta,instanceMeta);
        });
        registerCenter.stop();
    }
    //注册到注册中心,k-v --> userService - 127.0.0.1_8080 etc

    private void registerService() {
        RegisterCenter registerCenter = applicationContext.getBean(RegisterCenter.class);
        serviceMap.keySet().forEach(service ->{
            ServiceMeta serviceMeta = ServiceMeta.builder()
                    .serviceName(service)
                    .app(app)
                    .namespace(namespace)
                    .env(env)
                    .build();
            registerCenter.register(serviceMeta,instanceMeta);
        });
    }




    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    //提供反射的方法
    public RpcResponse<?> invoke(RpcRequest request){
        /*Class<?> interfaces = null;
        try {
            interfaces = Class.forName(request.getServiceName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String serviceName = interfaces.getName();*/
        String serviceName = request.getServiceName();
        Object service = serviceMap.get(serviceName);

        RpcResponse<Object> response = new RpcResponse<>();
        response.setStatus(false);
        response.setData(null);

        //拿到实现对象
        if(service == null){
            RpcException ex = new RpcException("no such service");
            return new RpcResponse<>(false,"no such service",ex);
        }
        String methodName = request.getMethodName();
        Class<?>[] paras = request.getParas();
        log.info("提供者接受远程调用的方法名为：{}，参数类型为：{}",methodName,Arrays.toString(paras));
        /*List<Class<?>> paramTypes = Arrays.stream(paras).map((str) -> {
            try {
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());*/
        Object[] args = request.getArgs();
        try {
            Class<?> serviceClass = Class.forName(serviceName);

            Method method = serviceClass.getMethod(methodName,paras/*支持方法重载*/);
            // 有对应的服务实例，以及方法,反射调用
            Object ret = method.invoke(service, args/*如果不实现重载，在这里肯定会出现反射调用失败，因为参数不一致*/);
            log.info("提供者接受远程调用的方法名为：{}，参数类型为：{}，调用结果为：{}",methodName,Arrays.toString(paras),ret.toString());
            response.setData(ret);
            response.setEx(null);
            response.setStatus(true);
        } catch (ClassNotFoundException e) {
           // response.setData(null);
            response.setEx(new RpcException(e.getException(),RpcException.SERVICE_NOT_FOUND));
        }  catch (NoSuchMethodException e) {
            response.setEx(new RpcException(e,RpcException.METHOD_NOT_FOUND));
        } catch (InvocationTargetException | IllegalAccessException e) {
            response.setEx(new RpcException(e,RpcException.METHOD_INVOKE_FAILED));
        }
        response.setEx(new RpcException(RpcException.UNKNOWN));
        return response;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
