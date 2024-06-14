package com.wx.wxrpc.myregistrycenter.entity;


import com.google.common.base.Strings;

import java.net.URI;
import java.nio.channels.Pipe;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

//注册的实体类
public class InstanceMeta {

    //协议类型
    private String schema;
    // ip
    private String host;

    private Integer port;

    private String context;

    private Boolean status;


    private Map<String,String> parameters = new HashMap<>();

    public InstanceMeta() {

    }

    public InstanceMeta(String schema, String host, Integer port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath(){
        return Strings.lenientFormat("%s_%s_%s",host,port,context);
    }

    public String toUrl(){
        // http://localhost:8080/Context
        return Strings.lenientFormat("%s://%s:%s/%s",schema,host,port,context);
    }

    public InstanceMeta addParameters(Map<String,String> paras){
        parameters.putAll(paras);
        return this;
    }
    public static InstanceMeta fromUrl(String url){
        URI uri = URI.create(url);
        return new InstanceMeta(uri.getScheme(), uri.getHost(),  uri.getPort(), uri.getPath().substring(1));
    }

    @Override
    public String toString() {
        return "InstanceMeta{" +
                "schema='" + schema + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", context='" + context + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceMeta that = (InstanceMeta) o;
        return Objects.equals(schema, that.schema) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, host, port, context);
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
