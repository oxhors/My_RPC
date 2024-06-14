package com.wx.wxrpc.myregistrycenter.service;

import com.wx.wxrpc.myregistrycenter.entity.InstanceMeta;

import java.util.List;

public interface RegistryService {

    InstanceMeta register(String service,InstanceMeta instanceMeta);

    InstanceMeta unregister(String service,InstanceMeta instanceMeta);

    List<InstanceMeta> findAllInstance(String service);

}
