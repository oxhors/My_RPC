package com.wx.wxrpc.core.reflect.api.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.wx.wxrpc.core.entity.RpcRequest;
import com.wx.wxrpc.core.entity.RpcResponse;
import com.wx.wxrpc.core.reflect.api.reflect;
import lombok.SneakyThrows;
import okhttp3.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.ObjectError;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

//@ConditionalOnProperty(prefix = "wxrpc.reflect.type" , value = "jdk")
public class JdkReflect implements reflect {
    @Override
    public Object getProxyInstance(String serviceName) {
        Object res = null;
        try {
            Class<?> interfaceClass = Class.forName(serviceName);
            res = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{interfaceClass},new JdkInvocationHandler(serviceName));
            return res;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
          //  .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
            .connectTimeout(10,TimeUnit.SECONDS)
            .readTimeout(10,TimeUnit.SECONDS)
            .writeTimeout(10,TimeUnit.SECONDS).build();

    class JdkInvocationHandler implements InvocationHandler {
        private final String serviceName;

        JdkInvocationHandler(String serviceName) {
            this.serviceName = serviceName;
        }

        //真正发送http请求的地方
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            RpcRequest request = new RpcRequest();
            request.setServiceName(serviceName);
            Type[] types = method.getGenericParameterTypes();
            Class<?>[] paras = new Class<?>[types.length];
            for(int i=0;i<types.length;i++){
                paras[i] = Class.forName(types[i].getTypeName());
            }
            request.setParas(paras);
            request.setArgs(args);
            request.setMethodName(method.getName());
            //发送http请求，请求体为request
            RpcResponse response = getResponse(request,method.getReturnType());
            if(response != null && response.getStatus()){
                return response.getData();
            }else {
                return null;
            }
        }
        private RpcResponse getResponse(RpcRequest request,Class<?> retType) {
            Gson gson = new GsonBuilder().
                    registerTypeAdapter(Class.class, new ClassCodec())
                    .create();
            String requsetJson = gson.toJson(request);
            try {
                Response response = okHttpClient.newCall(new Request.Builder().url("http://localhost:8080")
                        .post(RequestBody.create(requsetJson, MediaType.get("application/json; charset=utf-8")))
                        .build()).execute();
                String body = response.body().string();
                //反序列化
                System.out.println(body);
                TypeToken<?> parameterized = TypeToken.getParameterized(RpcResponse.class, retType);
                RpcResponse<?> rpcResponse = gson.fromJson(body,parameterized.getType());
                return rpcResponse;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>>{

        // 反序列化
        @SneakyThrows
        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String clazz = jsonElement.getAsString();
            return Class.forName(clazz);
        }

        // 序列化
        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            // 将 Class 变为 json
            return new JsonPrimitive(aClass.getName());
        }
    }


}
