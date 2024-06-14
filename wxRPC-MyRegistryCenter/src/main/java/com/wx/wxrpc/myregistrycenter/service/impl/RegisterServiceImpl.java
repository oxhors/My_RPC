package com.wx.wxrpc.myregistrycenter.service.impl;

import com.google.common.base.Strings;
import com.wx.wxrpc.myregistrycenter.entity.InstanceMeta;
import com.wx.wxrpc.myregistrycenter.service.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


//注册服务的实现类
public class RegisterServiceImpl implements RegistryService {

    private final Logger log = LoggerFactory.getLogger(RegisterServiceImpl.class);

    private final MultiValueMap<String,InstanceMeta> INSTANCE = new LinkedMultiValueMap<>();

    //service -> long 版本号
    private final Map<String, Long> VERSIONS = new ConcurrentHashMap<>();

    //每一个服务+实例的时间戳 service_instance ---> 时间戳
    public static final Map<String, Long> TIMESTAMP = new ConcurrentHashMap<>();



    private final AtomicLong counter = new AtomicLong(0);
    @Override
    public InstanceMeta register(String service, InstanceMeta instanceMeta) {
        List<InstanceMeta> instanceMetaList = INSTANCE.get(service);
        //服务注册时更新版本号和时间戳
        VERSIONS.put(service,counter.getAndIncrement());
        renew(instanceMeta,service);
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
        VERSIONS.put(service,counter.getAndIncrement());
        // renew(instanceMeta,service);
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

    @Override
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> versions(String... services) {
        //对每一个service,取出版本号
        Map<String,Long> versionMap = new HashMap<>();

        for (String service : services) {
            versionMap.put(service,VERSIONS.get(service));
        }
        return versionMap;
    }

    @Override
    public Long renew(InstanceMeta instanceMeta, String... services) {
        //%s_%s
        long now = System.currentTimeMillis();
        for (String service : services) {
            String key = Strings.lenientFormat("%s_%s",service,instanceMeta.toUrl());
            TIMESTAMP.put(key,now);
        }
        return now;
    }

    public Map<String, Long> getTIMESTAMP() {
        return TIMESTAMP;
    }
}
