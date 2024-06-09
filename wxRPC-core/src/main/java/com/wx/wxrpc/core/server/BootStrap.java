package com.wx.wxrpc.core.server;

import com.google.common.base.Strings;
import com.wx.wxrpc.core.annoation.RpcService;
import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import com.wx.wxrpc.core.registry.RegisterCenter;
import jakarta.annotation.PostConstruct;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//加载时把所有标注RpcService的类添加到map中
@Component
public class BootStrap implements ApplicationContextAware, EnvironmentAware {
    public static Map<String,Object> serviceMap = new HashMap<>();
    //拿到容器
    private ApplicationContext applicationContext;

    private Environment environment;

    private String serviceAddr;
    @PostConstruct
    public void initServiceMap(){
        try {
            String port = environment.getProperty("server.port");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            serviceAddr = Strings.lenientFormat("%s_%s",hostAddress,port);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> services = applicationContext.getBeansWithAnnotation(RpcService.class);

        for (Map.Entry<String, Object> entry : services.entrySet()) {

            Class<?>[] interfaces = entry.getValue().getClass().getInterfaces();

            for (Class<?> anInterface : interfaces) {
                //对于实现类的每一个接口，都要对应这个实现类对象，多对一的关系
                serviceMap.put(anInterface.getName(),entry.getValue());
            }
        }
        //registerService();
    }

    public void start(){
        RegisterCenter registerCenter = applicationContext.getBean(RegisterCenter.class);
        registerCenter.start();
        registerService();
    }

    //注销所有服务何其对应的地址
    @PreDestroy
    private void unRegisterService(){
        RegisterCenter registerCenter = applicationContext.getBean(RegisterCenter.class);
        serviceMap.keySet().forEach(service ->{
            registerCenter.unregister(service,serviceAddr);
        });
        registerCenter.stop();
    }
    //注册到注册中心,k-v --> userService - 127.0.0.1_8080 etc

    private void registerService() {
        RegisterCenter registerCenter = applicationContext.getBean(RegisterCenter.class);
        serviceMap.keySet().forEach(service ->{
            registerCenter.register(service,serviceAddr);
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
        //拿到实现对象
        if(service == null){
            return new RpcResponse<>(false,"no such service");
        }

        String methodName = request.getMethodName();
        Class<?>[] paras = request.getParas();
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
            return new RpcResponse<>(true,ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RpcResponse<>(false,"服务调用失败");
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
