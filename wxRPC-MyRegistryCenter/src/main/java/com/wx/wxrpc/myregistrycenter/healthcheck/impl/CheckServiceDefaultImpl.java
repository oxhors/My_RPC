package com.wx.wxrpc.myregistrycenter.healthcheck.impl;

import com.wx.wxrpc.myregistrycenter.entity.InstanceMeta;
import com.wx.wxrpc.myregistrycenter.healthcheck.CheckHealth;
import com.wx.wxrpc.myregistrycenter.service.RegistryService;
import com.wx.wxrpc.myregistrycenter.service.impl.RegisterServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CheckServiceDefaultImpl implements CheckHealth {

    private final Integer TIMEOUT = 30 * 1000;
    //单线程线程池
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    /**
     * 需要通过注册服务注销服务
     */
    private final RegistryService registryService;

    public CheckServiceDefaultImpl(RegistryService registryService) {
        this.registryService = registryService;
    }
    @Override
    //@PostConstruct
    public void start() {
        // 开启一个定时任务，定时去移除超时的服务
        scheduledExecutorService.scheduleAtFixedRate(() ->{
            Map<String, Long> timestamp = RegisterServiceImpl.TIMESTAMP;
            for (Map.Entry<String, Long> entry : timestamp.entrySet()) {
                // 某个服务实例超时,把该实例下线
                if(entry.getValue() - System.currentTimeMillis() > TIMEOUT){
                    String[] split = entry.getKey().split("_");
                    String service = split[0];
                    InstanceMeta instanceMeta = InstanceMeta.fromUrl(split[1]);
                    registryService.unregister(service,instanceMeta);
                    // 从时间表中移除
                    timestamp.remove(entry.getKey());
                }
            }
        },10,20, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        scheduledExecutorService.shutdown();
    }
}
