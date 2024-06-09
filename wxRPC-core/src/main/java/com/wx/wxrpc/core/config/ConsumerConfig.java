package com.wx.wxrpc.core.config;

import com.wx.wxrpc.core.consumer.Consumer;
import com.wx.wxrpc.core.reflect.api.impl.CglibReflect;
import com.wx.wxrpc.core.reflect.api.impl.JdkReflect;
import com.wx.wxrpc.core.reflect.api.reflect;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig  {


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
}
