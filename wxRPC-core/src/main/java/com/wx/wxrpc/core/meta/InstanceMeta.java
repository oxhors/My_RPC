package com.wx.wxrpc.core.meta;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Map;

public class InstanceMeta {
    private String schema;

    private  String host;
    private  Integer port;

    private  String context;

    private  Boolean status;
    private  Map<String,String> parameters = Maps.newHashMap();

    private InstanceMeta(Builder builder){
        schema = builder.schema;
        host = builder.host;
        port = builder.port;
        context = builder.context;
        status = builder.status;
        parameters = builder.parameters;
    }

    public static Builder builder(){
        return new Builder();
    }

    public String toPath() {
        return Strings.lenientFormat("%s_%s",host,port);
    }

    public static class Builder{
       private  String schema;

       private  String host;
       private  Integer port;

       private  String context;

       private  Boolean status;
       private  Map<String,String> parameters = Maps.newHashMap();

       public Builder schema(String schema){this.schema = schema; return this;}
       public Builder host(String host){this.host = host; return this;}
       public Builder port(Integer port){this.port = port; return this;}
       public Builder context(String context){this.context = context; return this;}
       public Builder status(Boolean status){this.status = status; return this;}
       public Builder parameters(Map<String,String> parameters){this.parameters = parameters; return this;}

       public InstanceMeta build(){
           return new InstanceMeta(this);
       }
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

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
