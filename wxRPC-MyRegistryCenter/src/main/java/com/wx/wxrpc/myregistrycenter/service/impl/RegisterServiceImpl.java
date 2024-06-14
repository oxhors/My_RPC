package com.wx.wxrpc.myregistrycenter.service.impl;

import com.wx.wxrpc.myregistrycenter.entity.InstanceMeta;
import com.wx.wxrpc.myregistrycenter.service.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

//注册服务的实现类
public class RegisterServiceImpl implements RegistryService {

    private final Logger log = LoggerFactory.getLogger(RegisterServiceImpl.class);

    private final MultiValueMap<String,InstanceMeta> INSTANCE = new LinkedMultiValueMap<>();
    @Override
    public InstanceMeta register(String service, InstanceMeta instanceMeta) {
        List<InstanceMeta> instanceMetaList = INSTANCE.get(service);
        if(Objects.nonNull(instanceMetaList) && !instanceMetaList.isEmpty()){
            if(instanceMetaList.contains(instanceMeta)){
                instanceMeta.setStatus(true);
                return instanceMeta;
            }
        }
        log.debug("注册服务实例：{}",instanceMeta.toUrl());
        instanceMeta.setStatus(true);
        instanceMetaList.add(instanceMeta);
        return instanceMeta;
    }

    @Override
    public InstanceMeta unregister(String service, InstanceMeta instanceMeta) {
        List<InstanceMeta> instanceMetaList = INSTANCE.get(service);
        if(Objects.nonNull(instanceMetaList) && !instanceMetaList.isEmpty()){
            log.debug("移除注册的服务实例：{}",instanceMeta.toUrl());
            instanceMetaList.remove(instanceMeta);
        }
        instanceMeta.setStatus(false);
        return instanceMeta;
    }

    @Override
    public List<InstanceMeta> findAllInstance(String service) {
        return INSTANCE.get(service);
    }
}
