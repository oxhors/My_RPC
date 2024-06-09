package com.wx.wxrpc.demo.provider;

import com.example.wxrpc.demo.api.User;
import com.example.wxrpc.demo.api.UserService;
import com.wx.wxrpc.core.config.BootConfig;
import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;

//demo 提供者，引入core依赖，提供rpc服务，通过http请求
@SpringBootApplication
@Import(BootConfig.class)
public class WxRpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxRpcDemoProviderApplication.class, args);
    }


}
