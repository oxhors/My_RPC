package com.wx.wxrpc.myregistrycenter.config;

import com.wx.wxrpc.myregistrycenter.cluster.Cluster;
import com.wx.wxrpc.myregistrycenter.healthcheck.CheckHealth;
import com.wx.wxrpc.myregistrycenter.healthcheck.impl.CheckServiceDefaultImpl;
import com.wx.wxrpc.myregistrycenter.service.RegistryService;
import com.wx.wxrpc.myregistrycenter.service.impl.RegisterServiceImpl;
import org.checkerframework.checker.units.qual.C;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 装配beans的配置类
 */

@Configuration
@EnableConfigurationProperties(RegisterCenterConfigProperties.class)
public class RegisterCenterConfig {

    @Bean
    public RegistryService registryService(){
        return new RegisterServiceImpl();
    }

    @Bean(initMethod = "start",destroyMethod = "stop")
    public CheckHealth checkHealth(RegistryService registryService){
        return new CheckServiceDefaultImpl(registryService);
    }

    @Bean(initMethod = "init")
    public Cluster cluster(RegisterCenterConfigProperties properties){
        return new Cluster(properties);
    }
}
