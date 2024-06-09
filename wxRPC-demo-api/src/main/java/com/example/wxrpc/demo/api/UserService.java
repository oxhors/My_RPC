package com.example.wxrpc.demo.api;


//暴露出去的api，提供给消费者使用

import org.springframework.stereotype.Service;


public interface UserService {

    public User findById(Integer uid);

    public User findById(Integer uid,String name);

    public long findLongId(long id);
}
