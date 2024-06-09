package com.wx.wxrpc.demo.provider;

import com.example.wxrpc.demo.api.User;
import com.example.wxrpc.demo.api.UserService;
import com.wx.wxrpc.core.annoation.RpcService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.KeyStore;

//demo 提供者 提供的方法

@Component
@RpcService //提供者标注这是一个提供外部调用的服务实例
public class UserServiceImpl implements UserService {


    @Resource
    private Environment environment;
    @Override

    public User findById(Integer uid) {
        return new User(uid,environment.getProperty("server.port") + " " + System.currentTimeMillis());
    }

    @Override
    public User findById(Integer uid, String name) {
        return new User(uid,"方法重载:"+ name);
    }

    @Override
    public long findLongId(long id) {
        return id;
    }
}
