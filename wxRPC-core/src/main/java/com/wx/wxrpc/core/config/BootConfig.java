package com.wx.wxrpc.core.config;

import com.wx.wxrpc.core.server.BootStrap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BootConfig {
    @Bean
    public BootStrap bootstrap(){
        return new BootStrap();
    }
}
