package com.wx.wxrpc.core.consumer;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.wx.wxrpc.core.annoation.RpcReference;
import com.wx.wxrpc.core.loadbalance.LoadBalance;
import com.wx.wxrpc.core.loadbalance.Router;
import com.wx.wxrpc.core.loadbalance.RpcContext;
import com.wx.wxrpc.core.meta.ServiceMeta;
import com.wx.wxrpc.core.reflect.reflect;
import com.wx.wxrpc.core.registry.RegisterCenter;
import com.wx.wxrpc.core.utils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

// 1 扫描所有标注reference的字段，注入动态代理对象，代理对象发送http请求完成远程调用
@Component
public class Consumer implements ApplicationContextAware, EnvironmentAware {

    private final Logger log = LoggerFactory.getLogger(Consumer.class);

    private ApplicationContext applicationContext;
    private Environment environment;

    @Autowired
    private reflect reflectHandler ;

    @Autowired
    private Router router;

    @Autowired
    private LoadBalance loadBalance;

    private String app;
    private String namespace;
    private String env;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //获取容器
        this.applicationContext = applicationContext;
    }



    //属性设置之后执行该方法
    public void scanFileds() throws Exception {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        app = environment.getProperty("app.id");
        namespace = environment.getProperty("app.namespace");
        env = environment.getProperty("app.env");
        // 找出所有带有Reference注解的bean,设置代理对象
        log.info("服务消费者开始给所有带有注解的字段设置代理对象.....");
        setFiledsWithReferenceAnnotation(beanDefinitionNames);
    }

    //保存服务接口对应的代理对象，内部通过http请求发起远程调用
    private Map<ServiceMeta,Object> proxyServers = new HashMap<>();
    private void setFiledsWithReferenceAnnotation(String[] beanDefinitionNames) throws IllegalAccessException, ClassNotFoundException {
        //TODO 读取配置的服务地址，准备扩展为注册中心实现
        //String serverUrls = environment.getProperty("wxrpc.providers");

       // List<String> urls = Lists.newArrayList(Splitter.on(",").trimResults().omitEmptyStrings().split(serverUrls));
        //Router router = applicationContext.getBean(Router.class);
        //LoadBalance loadBalance = applicationContext.getBean(LoadBalance.class);
        //保存路由器，选择器
        RegisterCenter registerCenter = applicationContext.getBean(RegisterCenter.class);
        //registerCenter.start();

        RpcContext rpcContext = new RpcContext(loadBalance,router);
        //不空
        for (String def : beanDefinitionNames) {
            //while()
            Object o = applicationContext.getBean(def);
            Class<?> beanClass = o.getClass();
            //while (Object)
            //Field[] fields = beanClass.getDeclaredFields();
            List<Field> withAnnotation = MethodUtils.getAnnoFields(beanClass,RpcReference.class);
            if(withAnnotation.isEmpty()){
                continue;
            }
            //Object o = applicationContext.getBean(def);
            for (Field field : withAnnotation) {
                //和getName没什么区别，只有内部类和数组会有区别
                String serviceName = field.getType().getCanonicalName();
                ServiceMeta serviceMeta = ServiceMeta.builder()
                        .env(env)
                        .namespace(namespace)
                        .app(app)
                        .serviceName(serviceName)
                        .build();
                if(proxyServers.containsKey(serviceName)){
                    //存在，直接给字段赋值
                    Object proxyServer = proxyServers.get(serviceMeta);
                    //强制反射，突破private限制
                    field.setAccessible(true);
                    field.set(o,proxyServer);
                }else{

                    //创建代理对象并放入缓存
                    Object proxyServer = reflectHandler.getProxyInstance(serviceMeta,rpcContext,registerCenter);
                    proxyServers.put(serviceMeta,proxyServer);
                    field.setAccessible(true);
                    field.set(o,proxyServer);
                }
            }
        }
    }


    /**
     * 获取环境变量
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
