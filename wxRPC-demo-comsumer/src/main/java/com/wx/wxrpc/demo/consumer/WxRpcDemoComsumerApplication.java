package com.wx.wxrpc.demo.consumer;

import com.example.wxrpc.demo.api.User;
import com.example.wxrpc.demo.api.UserService;
import com.wx.wxrpc.core.annoation.RpcReference;
import com.wx.wxrpc.core.config.ConsumerConfig;
import com.wx.wxrpc.core.consumer.Consumer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@SpringBootApplication
@Import(ConsumerConfig.class) //导入Consumer配置
public class WxRpcDemoComsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxRpcDemoComsumerApplication.class, args);
        //ApplicationContext applicationContext =
    }

    @RpcReference
    public UserService userService;

    @Bean
    //@Order()
    public ApplicationRunner applicationRunner2(){
        return x ->{
            User userServiceById = userService.findById(1000);
            System.out.println(userServiceById);
        };
    }


}
