package com.wx.wxrpc.myregistrycenter.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wx.wxrpc.myregistrycenter.http.impl.OkHttpInvoker;

import java.util.Objects;

public interface HttpInvoker {

    HttpInvoker DEFAULT = new OkHttpInvoker(5000);
    String get(String url);
    String post(String url, String requestString);


   static  <T> T getHttp(String url,Class<T> clazz){
        //T http = DEFAULT.getHttp(url, clazz);
        try {
            String res = DEFAULT.get(url);
            if(res == null){
                return null;
            }
            return new Gson().fromJson(res,clazz);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        }
    }



    static <T> T postHttp(String url, String requestString, Class<T> clazz) {
        try {
            String result = DEFAULT.post(url, requestString);
            if (Objects.isNull(result)) {
                return null;
            }
            return new Gson().fromJson(result, clazz);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}