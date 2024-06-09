package com.wx.wxrpc.core.consumer;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.wx.wxrpc.core.annoation.RpcReference;
import com.wx.wxrpc.core.loadbalance.LoadBalance;
import com.wx.wxrpc.core.loadbalance.Router;
import com.wx.wxrpc.core.loadbalance.RpcContext;
import com.wx.wxrpc.core.reflect.reflect;
import com.wx.wxrpc.core.registry.RegisterCenter;
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

    private ApplicationContext applicationContext;
    private Environment environment;

    @Autowired
    private reflect reflectHandler ;

    @Autowired
    private Router router;

    @Autowired
    private LoadBalance loadBalance;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //获取容器
        this.applicationContext = applicationContext;
    }



    //属性设置之后执行该方法
    public void scanFileds() throws Exception {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();

        // 找出所有带有Reference注解的bean,设置代理对象
        setFiledsWithReferenceAnnotation(beanDefinitionNames);
    }

    //保存服务接口对应的代理对象，内部通过http请求发起远程调用
    private Map<String,Object> proxyServers = new HashMap<>();
    private void setFiledsWithReferenceAnnotation(String[] beanDefinitionNames) throws IllegalAccessException, ClassNotFoundException {
        //TODO 读取配置的服务地址，准备扩展为注册中心实现
        //String serverUrls = environment.getProperty("wxrpc.providers");

       // List<String> urls = Lists.newArrayList(Splitter.on(",").trimResults().omitEmptyStrings().split(serverUrls));
        //Router router = applicationContext.getBean(Router.class);
        //LoadBalance loadBalance = applicationContext.getBean(LoadBalance.class);
        //保存路由器，选择器
        RegisterCenter registerCenter = applicationContext.getBean(RegisterCenter.class);

        RpcContext rpcContext = new RpcContext(loadBalance,router);
        //不空
        for (String def : beanDefinitionNames) {
            //while()
            Object o = applicationContext.getBean(def);
            Class<?> beanClass = o.getClass();
            //while (Object)
            //Field[] fields = beanClass.getDeclaredFields();
            List<Field> withAnnotation = getTargetFileds(beanClass);
            if(withAnnotation.isEmpty()){
                continue;
            }
            //Object o = applicationContext.getBean(def);
            for (Field field : withAnnotation) {
                //和getName没什么区别，只有内部类和数组会有区别
                String serviceName = field.getType().getCanonicalName();
                if(proxyServers.containsKey(serviceName)){
                    //存在，直接给字段赋值
                    Object proxyServer = proxyServers.get(serviceName);
                    //强制反射，突破private限制
                    field.setAccessible(true);
                    field.set(o,proxyServer);
                }else{
                    //创建代理对象并放入缓存
                    Object proxyServer = reflectHandler.getProxyInstance(serviceName,rpcContext,registerCenter);
                    proxyServers.put(serviceName,proxyServer);
                    field.setAccessible(true);
                    field.set(o,proxyServer);
                }
            }
        }
    }

    /**
     * 找出所有带有目标注解的字段
     * @param
     * @return
     */
    private List<Field> getTargetFileds(Class<?> clazz) {
       List<Field> res = new ArrayList<>();
       while(Objects.nonNull(clazz)){
           List<Field> list = Arrays.stream(clazz.getDeclaredFields()).filter(field -> {
               return field.isAnnotationPresent(RpcReference.class);
           }).toList();
           if(!list.isEmpty()){
               res.addAll(list);
           }
           clazz = clazz.getSuperclass();
       }
        return res;
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
