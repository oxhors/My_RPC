package com.wx.wxrpc.core.meta;

import com.google.common.base.Strings;

public class ServiceMeta {
    private String app;

    private String namespace;

    private String serviceName;

    private String env;

    public ServiceMeta() {
    }

    public static Builder builder(){
        return new Builder();
    }

    private ServiceMeta(Builder builder){
        app = builder.app;
        namespace = builder.namespace;
        serviceName = builder.serviceName;
        env = builder.env;
    }

    public String toPath() {
        return Strings.lenientFormat("%s_%s_%s_%s",app,namespace,serviceName,env);
    }

    public static class Builder{
        private String app;

        private String namespace;

        private String serviceName;

        private String env;

        public Builder app(String app){this.app = app ; return this;}
        public Builder namespace(String namespace){this.namespace = namespace ; return this;}
        public Builder serviceName(String serviceName){this.serviceName = serviceName ; return this;}
        public Builder env(String env){this.env = env ; return this;}

        public ServiceMeta build(){
            return new ServiceMeta(this);
        }

    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public String toString() {
        return "ServiceMeta{" +
                "app='" + app + '\'' +
                ", namespace='" + namespace + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", env='" + env + '\'' +
                '}';
    }
}
