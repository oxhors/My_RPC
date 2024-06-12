package com.wx.wxrpc.core.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wx.wxrpc.core.entity.RpcResponse;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Method;

public class TypeUtils {

    public static RpcResponse<?> getRpcResponse(Method method, Response response) throws IOException {
        String body = response.body().string();
        //反序列化
        Gson gson = new Gson();
        // 如果 retType 是 String.class，那么你创建的 TypeToken 表示 RpcResponse<String> 类型。
        TypeToken<?> parameterized = TypeToken.getParameterized(RpcResponse.class, method.getReturnType());
        RpcResponse<?> rpcResponse = gson.fromJson(body,parameterized.getType());
        return rpcResponse;
    }
}
