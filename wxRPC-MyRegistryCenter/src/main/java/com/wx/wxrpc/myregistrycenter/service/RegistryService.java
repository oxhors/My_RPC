package com.wx.wxrpc.myregistrycenter.service;

import com.wx.wxrpc.myregistrycenter.entity.InstanceMeta;

import java.util.List;
import java.util.Map;

public interface RegistryService {

    InstanceMeta register(String service,InstanceMeta instanceMeta);

    InstanceMeta unregister(String service,InstanceMeta instanceMeta);

    List<InstanceMeta> findAllInstance(String service);

    //查询某个服务的版本
    Long version(String service);

    //查询多个服务的版本号
    Map<String,Long> versions(String... services);

    //刷新某个实例的所有服务的时间戳
    Long renew(InstanceMeta instanceMeta,String... services);
}
