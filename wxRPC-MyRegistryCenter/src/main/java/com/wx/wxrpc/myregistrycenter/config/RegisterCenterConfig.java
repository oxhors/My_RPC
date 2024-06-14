package com.wx.wxrpc.myregistrycenter.config;

import com.wx.wxrpc.myregistrycenter.healthcheck.CheckHealth;
import com.wx.wxrpc.myregistrycenter.healthcheck.impl.CheckServiceDefaultImpl;
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

    @Bean
    public CheckHealth checkHealth(RegistryService registryService){
        return new CheckServiceDefaultImpl(registryService);
    }
}
