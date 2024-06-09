package com.wx.wxrpc.core.registry.impl;

import com.google.common.base.Strings;
import com.wx.wxrpc.core.event.Event;
import com.wx.wxrpc.core.event.EventListener;
import com.wx.wxrpc.core.registry.RegisterCenter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Objects;

public class ZkRegisterCenter implements RegisterCenter {

    private final CuratorFramework client;

    public ZkRegisterCenter() {
        final RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3,1000);
        this.client = CuratorFrameworkFactory.builder()
                .connectString("192.168.91.100:2181")
                .retryPolicy(retryPolicy)
                .namespace("wxrpc")
                .build();
        this.client.start();
    }

    @Override
    public void start() {
        this.client.start();
    }

    @Override
    public void stop() {
        client.close();
    }

    @Override
    public void register(String service, String instance) {
        try {
            String servicePath = Strings.lenientFormat("/%s", service);
            if(Objects.isNull(client.checkExists().forPath(servicePath))){
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            }
            String instancePath = Strings.lenientFormat("/%s", instance);
            if(Objects.isNull(client.checkExists().forPath(instancePath))){
                client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void unregister(String service, String instance) {
        try {
            String servicePath = Strings.lenientFormat("/%s", service);
            if(Objects.isNull(client.checkExists().forPath(servicePath))){
                return ;
            }
            String instancePath = Strings.lenientFormat("/%s", instance);
            if(Objects.nonNull(client.checkExists().forPath(instancePath))){
                client.delete().quietly().forPath(instancePath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> findAll(String service) {
        String servicePath = Strings.lenientFormat("/%s", service);

        try {
            List<String> nodes = client.getChildren().forPath(servicePath);
            return nodes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribe(String service, EventListener eventListener) {
        try {
            String servicePath = Strings.lenientFormat("/%s", service);
            final TreeCache treeCache = TreeCache.newBuilder(client,servicePath)
                    .setCacheData(true)
                    .setMaxDepth(2)
                    .build();
            treeCache.getListenable().addListener(new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                    System.out.println("subscribe ===>");
                    //监听服务列表的变化，通过fire方法重新获取服务列表
                    List<String> nodes = findAll(service);
                    eventListener.fire(new Event(nodes));
                }
            });
            treeCache.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
