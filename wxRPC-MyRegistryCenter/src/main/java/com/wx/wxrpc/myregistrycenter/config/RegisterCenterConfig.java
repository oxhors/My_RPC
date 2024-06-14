package com.wx.wxrpc.myregistrycenter.config;

import com.wx.wxrpc.myregistrycenter.service.RegistryService;
import com.wx.wxrpc.myregistrycenter.service.impl.RegisterServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 装配beans的配置类
 */

@Configuration
public class RegisterCenterConfig {

    @Bean
    public RegistryService registryService(){
        return new RegisterServiceImpl();

    }
}
