package com.wx.wxrpc.demo.provider;

import com.example.wxrpc.demo.api.User;
import com.example.wxrpc.demo.api.UserService;
import com.wx.wxrpc.core.annoation.RpcService;
import org.springframework.stereotype.Component;

//demo 提供者 提供的方法

@Component
@RpcService //提供者标注这是一个提供外部调用的服务实例
public class UserServiceImpl implements UserService {
    @Override

    public User findById(Integer uid) {
        return new User(uid,"Wx" + System.currentTimeMillis());
    }
}
