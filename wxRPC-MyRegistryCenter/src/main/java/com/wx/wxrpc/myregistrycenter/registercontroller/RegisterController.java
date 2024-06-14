package com.wx.wxrpc.myregistrycenter.registercontroller;

import com.wx.wxrpc.myregistrycenter.cluster.Cluster;
import com.wx.wxrpc.myregistrycenter.cluster.RegisterCenterSnapshot;
import com.wx.wxrpc.myregistrycenter.cluster.Server;
import com.wx.wxrpc.myregistrycenter.entity.InstanceMeta;
import com.wx.wxrpc.myregistrycenter.service.RegistryService;
import com.wx.wxrpc.myregistrycenter.service.impl.RegisterServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试类
 */

/**
 * 注册中心暴露的接口，提供服务
 */
@RestController
@RequestMapping("/register")
public class RegisterController {

    @Resource
    private RegistryService registryService;

    @Resource
    private Cluster cluster;
    //服务注册
    // 只能在主节点
    @PostMapping("/register")
    public InstanceMeta register(@RequestParam(value = "service") String service , @RequestBody InstanceMeta instanceMeta){
        return registryService.register(service,instanceMeta);
    }
    //服务注销
    @PostMapping("/unregister")
    public InstanceMeta unregister(@RequestParam(value = "service") String service , @RequestBody InstanceMeta instanceMeta){
        return registryService.unregister(service,instanceMeta);
    }
    //服务发现
    @GetMapping("/getall")
    public List<InstanceMeta> getAllInstance(@RequestParam String service){
        return registryService.findAllInstance(service);
    }
    //获取当前注册中心节点信息
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Server info() {
       // log.debug("==> info: {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping(value = "/snapshot" , method = RequestMethod.GET)
    public RegisterCenterSnapshot getSnapshot(){
        return RegisterServiceImpl.createSnapshot();
    }


}
