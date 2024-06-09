package com.wx.wxrpc.demo.provider;

import com.example.wxrpc.demo.api.User;
import com.example.wxrpc.demo.api.UserService;
import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import com.wx.wxrpc.core.server.BootStrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "")
public class TestProvider {
    @Autowired
    UserService userService;

    //接受客户端发起的rpc请求，返回Json形式的字符串
    @PostMapping()
    public RpcResponse<?> findUserById(@RequestBody RpcRequest request){
        RpcResponse<?> response = invoke(request);
        System.out.println(response.toString());
        return response;
    }

    @Autowired
    BootStrap client;
    private RpcResponse<?> invoke(RpcRequest request){
        //反射调用
        // String serviceName = request.getServiceName();
        //handler.
        RpcResponse<?> response = client.invoke(request);
        return response;
    }

}
