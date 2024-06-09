package com.wx.wxrpc.core.loadbalance;

import java.util.List;

public interface LoadBalance {
     String choose(List<String> urls);


    //默认实现，返回第一个
     LoadBalance DEFAULT = (urls) ->{
        if(urls.isEmpty()){
            return null;
        }
        //随机返回
        int n = urls.size();
        //返回0-n-1的一个随机整数
        return urls.get((int)(Math.random()*n));
    };

}
