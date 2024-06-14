package com.wx.wxrpc.myregistrycenter.testcontroller;

import com.wx.wxrpc.myregistrycenter.entity.InstanceMeta;
import com.wx.wxrpc.myregistrycenter.service.RegistryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试类
 */
@RestController
@RequestMapping("/registertest")
public class TestController {

    @Resource
    private RegistryService registryService;
    @PostMapping("/register")
    public InstanceMeta register(@RequestParam(value = "service") String service , @RequestBody InstanceMeta instanceMeta){
        return registryService.register(service,instanceMeta);
    }
    @PostMapping("/unregister")
    public InstanceMeta unregister(@RequestParam(value = "service") String service , @RequestBody InstanceMeta instanceMeta){
        return registryService.unregister(service,instanceMeta);
    }
    @GetMapping("/getall")
    public List<InstanceMeta> getAllInstance(@RequestParam String service){
        return registryService.findAllInstance(service);
    }
}
