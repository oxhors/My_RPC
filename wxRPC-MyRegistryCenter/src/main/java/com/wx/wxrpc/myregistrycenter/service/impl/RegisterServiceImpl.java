package com.wx.wxrpc.myregistrycenter.service.impl;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.wx.wxrpc.myregistrycenter.cluster.Cluster;
import com.wx.wxrpc.myregistrycenter.cluster.RegisterCenterSnapshot;
import com.wx.wxrpc.myregistrycenter.cluster.Server;
import com.wx.wxrpc.myregistrycenter.entity.InstanceMeta;
import com.wx.wxrpc.myregistrycenter.http.HttpInvoker;
import com.wx.wxrpc.myregistrycenter.service.RegistryService;
import jakarta.annotation.Resource;
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

    private static final MultiValueMap<String,InstanceMeta> INSTANCE = new LinkedMultiValueMap<>();

    //service -> long 版本号
    private static final Map<String, Long> VERSIONS = new ConcurrentHashMap<>();

    //每一个服务+实例的时间戳 service_instance ---> 时间戳
    public static final Map<String, Long> TIMESTAMP = new ConcurrentHashMap<>();

    private static final AtomicLong counter = new AtomicLong(0);
    private final Cluster  cluster;

    public RegisterServiceImpl(Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public InstanceMeta register(String service, InstanceMeta instanceMeta) {
        int retryTimes = 3;
        //只能往主节点写
        while (retryTimes -- > 0){
            Server leader = cluster.leader();
            Server self = cluster.self();
            if(leader != null && !leader.equals(self)) {
                Gson gson = new Gson();
                String requestBody = gson.toJson(instanceMeta);
                return HttpInvoker.postHttp(Strings.lenientFormat("%s/register?%s", leader.getUrl(), service), requestBody, InstanceMeta.class);
            } else if (leader == null) {
                // 没有主节点，等待一会进行重试
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // 当前节点就是主节点
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
        int retryTimes = 3;
        while(retryTimes-- > 0){
            Server leader = cluster.leader();
            Server self = cluster.self();
            if(leader != null && !leader.equals(self)) {
                Gson gson = new Gson();
                String requestBody = gson.toJson(instanceMeta);
                return HttpInvoker.postHttp(Strings.lenientFormat("%s/register?%s", leader.getUrl(), service), requestBody, InstanceMeta.class);
            } else if (leader == null) {
                // 没有主节点，客户端重试 TODO 返回特殊的值，告诉客户端重试
                // return null;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // 注销服务
        List<InstanceMeta> instanceMetaList = INSTANCE.get(service);
        VERSIONS.remove(service);
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

    //创建当前的快照

    public static RegisterCenterSnapshot createSnapshot() {
        MultiValueMap<String,InstanceMeta> instances = new LinkedMultiValueMap<>(INSTANCE);
        Map<String,Long> timeStamp = new HashMap<>(TIMESTAMP);
        Map<String, Long> versions = new HashMap<>(VERSIONS);
        long nodeVersion = counter.get();
        return new RegisterCenterSnapshot(instances,timeStamp,versions,nodeVersion);
    }

    //读取数据快照
    public static Long recoverDataFromSnapshot(RegisterCenterSnapshot snapshot){
        INSTANCE.clear();
        INSTANCE.addAll(snapshot.getINSTANCE());

        VERSIONS.clear();
        VERSIONS.putAll(snapshot.getVERSIONS());

        TIMESTAMP.clear();
        TIMESTAMP.putAll(snapshot.getTIMESTAMP());
        //当前注册中心节点的版本
        counter.set(snapshot.getVersion());
        return snapshot.getVersion();
    }


    public Map<String, Long> getTIMESTAMP() {
        return TIMESTAMP;
    }
}
