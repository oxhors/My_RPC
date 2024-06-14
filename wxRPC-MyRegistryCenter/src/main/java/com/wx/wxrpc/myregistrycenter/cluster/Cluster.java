package com.wx.wxrpc.myregistrycenter.cluster;

import com.google.common.base.Strings;
import com.wx.wxrpc.myregistrycenter.config.RegisterCenterConfigProperties;
import com.wx.wxrpc.myregistrycenter.http.HttpInvoker;
import com.wx.wxrpc.myregistrycenter.service.impl.RegisterServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Cluster {

    private final RegisterCenterConfigProperties properties;

    private final Logger log = LoggerFactory.getLogger(Cluster.class);
    private List<Server> serverList = new ArrayList<>();
    public Cluster(RegisterCenterConfigProperties properties) {
        this.properties = properties;
    }

    private Server MYSELF;
    @Value("${server.port}")
    private Integer port;
    private String host;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private void init() {
        try (InetUtils inetUtils = new InetUtils(new InetUtilsProperties())){
            this.host = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        }catch (Exception e) {
            this.host = "127.0.0.1";
        }
        this.MYSELF = new Server(Strings.lenientFormat("http://%s:%s", this.host, port), 0, false, true);
        //从配置文件中读取集群信息
        List<String> serversUrl = properties.getCluster();
        for (String url : serversUrl) {
            Server server = new Server();
            server.setUrl(url);
            server.setLeader(false);
            server.setStatus(false);
            server.setVersion(0);
            serverList.add(server);
        }
        //集群探活
        scheduledExecutorService.scheduleAtFixedRate(()->{
            checkServersAlive();
            //选主
            selectLeader();
            //从leader同步数据快照
            syncSnapshotFromLeader();
        },0,1, TimeUnit.SECONDS);

    }

    //从节点从主节点获取数据快照
    private void syncSnapshotFromLeader() {

        Server self = self();
        Server leader = leader();
        if(self != null && leader != null && !self.getLeader() &&!self.equals(leader) && self.getVersion() <= leader.getVersion()){
            //当前节点不空，主节点不空，当前节点不是主节点，版本号小于主节点的版本号
            RegisterCenterSnapshot snapshot = HttpInvoker.getHttp(Strings.lenientFormat("%s/register/snapshot", leader.getUrl()), RegisterCenterSnapshot.class);
            if(snapshot != null){
                log.debug("开始数据同步,从节点为：{},主节点为：{}", self.getUrl(),leader.getUrl());
                RegisterServiceImpl.recoverDataFromSnapshot(snapshot);
            }
        } else if (self.getVersion() > leader.getVersion()) {
            // 当前节点应该为主节点
            self.setLeader(true);
            self.setVersion(self.getVersion() + 1);
            leader.setLeader(false);
        }
    }

    private void selectLeader() {
        List<Server> servers = serverList.stream().filter(Server::getStatus/*首先要存活*/).filter(Server::getLeader).toList();
        if(servers.isEmpty()){
            //需要选主
            List<Server> aliveServers = serverList.stream().filter(Server::getStatus).toList();
            if(aliveServers.isEmpty()) return;
            Server leader = selectLeaderFromAliveServers(aliveServers);
            log.debug("没有主节点，选取版本号最大的节点的作为主节点,{}",leader.getUrl());
        } else if (servers.size() > 1) {
            //选取版本号最大的作为主节点
            //其他节点设置位false
            Server leader = selectLeaderFromAliveServers(servers);
            log.debug("超过一个主节点,选取版本号最大的节点的作为主节点,{}",leader.getUrl());
        } else{
            //只有一个主节点，不用选主
            log.debug("主节点为：{}",servers.get(0).getUrl());
        }
    }

    //每次当选主节点，version加一
    private Server  selectLeaderFromAliveServers(List<Server> servers) {
        int version = Integer.MIN_VALUE;
        Server leader = null;
        for (Server server : servers) {
            if(server.getVersion() > version){
                version = server.getVersion();
                leader = server;
            }
        }
        leader.setLeader(true);
        for (Server server : servers) {
            if(!server.equals(leader)){
                server.setLeader(false);
            }
        }
        leader.setVersion(leader.getVersion() + 1);
        return leader;
    }

    /**
     * 优化点：对列表的遍历如果先后顺序不影响，可以使用并行流
     */
    private void checkServersAlive() {
        serverList.parallelStream().forEach(server -> {
            if(server.equals(MYSELF)) {
                server.setStatus(true);
                return;
            }
            try {
                Server info = HttpInvoker.getHttp(Strings.lenientFormat("%s/register/info",server.getUrl()), Server.class);
                if(info != null){
                    server.setStatus(true);
                    server.setLeader(info.getLeader());
                    server.setVersion(info.getVersion());
                }else {
                    server.setStatus(false);
                    server.setLeader(false);
                    server.setVersion(0);
                }
            } catch (Exception e) {
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(0);
            }
        });
    }

    public Server self() {
        return MYSELF;
    }
    
    //取出当前集群的主节点
    public Server leader(){
        return serverList.stream().filter(Server::getLeader).findAny().orElse(null);
    }
}
