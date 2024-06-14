package com.wx.wxrpc.myregistrycenter.cluster;

import com.wx.wxrpc.myregistrycenter.entity.InstanceMeta;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 注册中心数据快照
 */
public class RegisterCenterSnapshot {
    private  MultiValueMap<String, InstanceMeta> INSTANCE;

    //service -> long 版本号
    private  Map<String, Long> VERSIONS ;

    //每一个服务+实例的时间戳 service_instance ---> 时间戳
    private  Map<String, Long> TIMESTAMP ;
    private  long version ;
    public RegisterCenterSnapshot(MultiValueMap<String, InstanceMeta> INSTANCE, Map<String, Long> VERSIONS, Map<String, Long> TIMESTAMP, long version) {
        this.INSTANCE = INSTANCE;
        this.VERSIONS = VERSIONS;
        this.TIMESTAMP = TIMESTAMP;
        this.version = version;
    }

    public RegisterCenterSnapshot() {
    }



    public MultiValueMap<String, InstanceMeta> getINSTANCE() {
        return INSTANCE;
    }

    public void setINSTANCE(MultiValueMap<String, InstanceMeta> INSTANCE) {
        this.INSTANCE = INSTANCE;
    }

    public Map<String, Long> getVERSIONS() {
        return VERSIONS;
    }

    public void setVERSIONS(Map<String, Long> VERSIONS) {
        this.VERSIONS = VERSIONS;
    }

    public Map<String, Long> getTIMESTAMP() {
        return TIMESTAMP;
    }

    public void setTIMESTAMP(Map<String, Long> TIMESTAMP) {
        this.TIMESTAMP = TIMESTAMP;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }



    @Override
    public String toString() {
        return "RegisterCenterSnapshot{" +
                "INSTANCE=" + INSTANCE +
                ", VERSIONS=" + VERSIONS +
                ", TIMESTAMP=" + TIMESTAMP +
                ", version=" + version +
                '}';
    }


}
