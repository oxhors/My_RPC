package com.wx.wxrpc.core.config;

import com.wx.wxrpc.core.registry.RegisterCenter;
import com.wx.wxrpc.core.registry.impl.ZkRegisterCenter;
import com.wx.wxrpc.core.server.BootStrap;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BootConfig {
    @Bean
    public BootStrap bootstrap(){
        return new BootStrap();
    }

    @Bean//(initMethod = "start" , destroyMethod = "stop")
    public RegisterCenter registerCenter(){
        return new ZkRegisterCenter();
    }

    //容器初始化完成之后，启动注册中心，然后开始注册服务
    @Bean
    ApplicationRunner startRunner(BootStrap bootStrap){
        return (x)->{
             bootStrap.start();
        };
    }
}
