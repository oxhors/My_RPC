package com.wx.wxrpc.core.config;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.wx.wxrpc.core.consumer.Consumer;
import com.wx.wxrpc.core.loadbalance.LoadBalance;
import com.wx.wxrpc.core.loadbalance.Router;
import com.wx.wxrpc.core.loadbalance.impl.RoundRobinLoadBalance;
import com.wx.wxrpc.core.reflect.impl.CglibReflect;
import com.wx.wxrpc.core.reflect.impl.JdkReflect;
import com.wx.wxrpc.core.reflect.reflect;
import com.wx.wxrpc.core.registry.RegisterCenter;
import com.wx.wxrpc.core.registry.impl.SimpleRegisterCenter;
import com.wx.wxrpc.core.registry.impl.ZkRegisterCenter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
public class ConsumerConfig implements EnvironmentAware {


   /* @ConditionalOnProperty(prefix = "wxrpc.reflect.type" , value = "jdk")
    public reflect getJdkReflect(){
        return new JdkReflect();
    }
    @ConditionalOnProperty(prefix = "wxrpc.reflect.type" , value = "cglib")
    public reflect getCglibReflect(){
        return new CglibReflect();
    }*/
    @Bean
    public Consumer consumer() {
        return new Consumer();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner applicationRunner(Consumer consumer){
        return x ->{
            consumer.scanFileds();
        };
    }


    /**
     * 配置一个默认的路由器
     * @return
     */
    @Bean
    public Router getRouter(){
        return Router.DEFAULT;
    }

    /**
     * 配置一个默认的负载均衡器，固定选择第一个
     * @return
     */
    private Environment environment;
    @Bean
    public LoadBalance getLoadBalance(){
        String property = environment.getProperty("wxrpc.loadbalance");
        if(property==null || property.isBlank()|| property.equals("random")){
            return LoadBalance.DEFAULT;
        } else if (property.equals("roundrobin")) {
            return new RoundRobinLoadBalance();
        }
        return LoadBalance.DEFAULT;
    }

    @Bean
    public reflect getReflect(){
        String property = environment.getProperty("wxrpc.reflect.type");
        if(property == null || property.isBlank() || property.equals("jdk")){
            return new JdkReflect();
        } else if (property.equals("cglib")) {
            return new CglibReflect();
        }
        return new JdkReflect();
    }

    @Bean(initMethod = "start" , destroyMethod = "stop")
    public RegisterCenter registerCenter(){
        //String serverUrls = environment.getProperty("wxrpc.providers");

        //List<String> urls = Lists.newArrayList(Splitter.on(",").trimResults().omitEmptyStrings().split(serverUrls));
        ZkRegisterCenter registerCenter = new ZkRegisterCenter();
        //registerCenter.start();
        return registerCenter;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
