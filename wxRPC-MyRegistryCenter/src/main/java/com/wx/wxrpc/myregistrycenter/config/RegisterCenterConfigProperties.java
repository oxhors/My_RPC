package com.wx.wxrpc.myregistrycenter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "registry")
public class RegisterCenterConfigProperties {
    private List<String> cluster = new ArrayList<>();


    public List<String> getCluster() {
        return cluster;
    }
    public void setCluster(List<String> cluster) {
        this.cluster = cluster;
    }
}
