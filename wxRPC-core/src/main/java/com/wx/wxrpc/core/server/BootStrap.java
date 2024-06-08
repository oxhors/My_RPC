package com.wx.wxrpc.core.server;

import com.wx.wxrpc.core.annoation.RpcService;
import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//加载时把所有标注RpcService的类添加到map中
@Component
public class BootStrap implements ApplicationContextAware {
    public static Map<String,Object> serviceMap = new HashMap<>();
    //拿到容器
    private ApplicationContext applicationContext;

    @PostConstruct
    public void initServiceMap(){
        Map<String, Object> services = applicationContext.getBeansWithAnnotation(RpcService.class);

        for (Map.Entry<String, Object> entry : services.entrySet()) {

            Class<?>[] interfaces = entry.getValue().getClass().getInterfaces();

            for (Class<?> anInterface : interfaces) {
                //对于实现类的每一个接口，都要对应这个实现类对象，多对一的关系
                serviceMap.put(anInterface.getName(),entry.getValue());
            }
        }
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
            Method method = serviceClass.getMethod(methodName,paras);
            Object ret = method.invoke(service, args);
            return new RpcResponse<>(true,ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RpcResponse<>(false,null);
    }

}
