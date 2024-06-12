package com.wx.wxrpc.core.registry.impl;

import com.google.common.base.Strings;
import com.wx.wxrpc.core.event.Event;
import com.wx.wxrpc.core.event.EventListener;
import com.wx.wxrpc.core.meta.InstanceMeta;
import com.wx.wxrpc.core.meta.ServiceMeta;
import com.wx.wxrpc.core.registry.RegisterCenter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ZkRegisterCenter implements RegisterCenter {

    private final CuratorFramework client;

    private TreeCache treeCache;

    private Environment env;

    @Value("${wxrpc.zkservers}")
    private String zkServers;

    @Value("${wxrpc.zkroot}")
    private String root;

    public ZkRegisterCenter() {
        //String zkservers = env.getProperty("wxrpc.zkservers");
        //String root = env.getProperty("wxrpc.zkroot");
        final RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3,1000);
        this.client = CuratorFrameworkFactory.builder()
                .connectString(zkServers)
                .retryPolicy(retryPolicy)
                .namespace(root)
                .build();
        //this.client.start();
    }

    @Override
    public void start() {
        this.client.start();
    }

    @Override
    public void stop() {
        if(Objects.nonNull(treeCache)){
            try {
                treeCache.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        client.close();
    }

    @Override
    public void register(ServiceMeta serviceMeta, InstanceMeta instanceMeta) {
        try {
            String servicePath = Strings.lenientFormat("/%s", serviceMeta.toPath());
            if(Objects.isNull(client.checkExists().forPath(servicePath))){
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            }
            String instancePath = Strings.lenientFormat("/%s/%s", serviceMeta.toPath(),instanceMeta.toPath());
            if(Objects.isNull(client.checkExists().forPath(instancePath))){
                client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void unregister(ServiceMeta serviceMeta, InstanceMeta instanceMeta) {
        try {
            String servicePath = Strings.lenientFormat("/%s", serviceMeta.toPath());
            if(Objects.isNull(client.checkExists().forPath(servicePath))){
                return ;
            }
            String instancePath = Strings.lenientFormat("/%s/%s",serviceMeta.toPath(), instanceMeta.toPath());
            if(Objects.nonNull(client.checkExists().forPath(instancePath))){
                client.delete().quietly().forPath(instancePath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InstanceMeta> findAll(ServiceMeta serviceMeta) {
        String servicePath = Strings.lenientFormat("/%s",serviceMeta.toPath());

        try {
            List<String> nodes = client.getChildren().forPath(servicePath);
            List<InstanceMeta> res = nodes.stream().map(node -> {
                String[] hostAndPort = node.split("_");
                return InstanceMeta.builder()
                        .schema("http")
                        .host(hostAndPort[0])
                        .port(Integer.valueOf(hostAndPort[1]))
                        .build();
            }).collect(Collectors.toList());
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, EventListener eventListener) {
        try {
            String servicePath = Strings.lenientFormat("/%s", serviceMeta.toPath());
            treeCache = TreeCache.newBuilder(client,servicePath)
                    .setCacheData(true)
                    .setMaxDepth(2)
                    .build();
            treeCache.getListenable().addListener(new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                    System.out.println("subscribe ===>:" + serviceMeta);
                    //监听服务列表的变化，通过fire方法重新获取服务列表
                    List<InstanceMeta> nodes = findAll(serviceMeta);
                    eventListener.fire(new Event(nodes));
                }
            });
            treeCache.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
