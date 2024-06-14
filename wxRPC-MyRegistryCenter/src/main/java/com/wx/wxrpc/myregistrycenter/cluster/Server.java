package com.wx.wxrpc.myregistrycenter.cluster;

import java.util.Objects;

public class Server {
    private String url;

    private Integer version;

    //是否是集群的leader
    private Boolean isLeader;

    private Boolean status;

    public Server() {
    }

    public Server(String url, Integer version, Boolean isLeader, Boolean status) {
        this.url = url;
        this.version = version;
        this.isLeader = isLeader;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Server server = (Server) o;
        return Objects.equals(url, server.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getLeader() {
        return isLeader;
    }

    public void setLeader(Boolean leader) {
        isLeader = leader;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
