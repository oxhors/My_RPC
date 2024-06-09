package com.wx.wxrpc.core.config;

import com.wx.wxrpc.core.consumer.Consumer;
import com.wx.wxrpc.core.loadbalance.LoadBalance;
import com.wx.wxrpc.core.loadbalance.Router;
import com.wx.wxrpc.core.loadbalance.impl.RoundRobinLoadBalance;
import com.wx.wxrpc.core.reflect.api.impl.CglibReflect;
import com.wx.wxrpc.core.reflect.api.impl.JdkReflect;
import com.wx.wxrpc.core.reflect.api.reflect;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

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
    public Router router(){
        return Router.DEFAULT;
    }

    /**
     * 配置一个默认的负载均衡器，固定选择第一个
     * @return
     */
    private Environment environment;
    @Bean
    public LoadBalance loadBalance(){
        String property = environment.getProperty("wxrpc.loadbalance");
        if(property==null || property.isBlank()|| property.equals("random")){
            return LoadBalance.DEFAULT;
        } else if (property.equals("roundrobin")) {
            return new RoundRobinLoadBalance();
        }
        return LoadBalance.DEFAULT;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
