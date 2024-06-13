package com.wx.wxrpc.core.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wx.wxrpc.core.entity.RpcResponse;
import okhttp3.Response;
import org.springframework.validation.ObjectError;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class TypeUtils {

    public static RpcResponse<?> getRpcResponse(Method method, Response response) throws IOException {
        String body = response.body().string();
        //反序列化
       // Gson gson = new Gson();
        // 如果 retType 是 String.class，那么你创建的 TypeToken 表示 RpcResponse<String> 类型。
        //TypeToken<?> parameterized = TypeToken.getParameterized(RpcResponse.class, method.getReturnType());
       // RpcResponse<?> rpcResponse = gson.fromJson(body,parameterized.getType());
        return JSONObject.parseObject(body,RpcResponse.class);
    }

    public static Object castFastJsonRetObject(Object origin, Method method) {
        if(null == origin){
            return null;
        }
        Class<?> returnType = method.getReturnType();

        //返回值的泛型类型
        Type genericReturnType = method.getGenericReturnType();

        return castFastJsonObject(origin,returnType,genericReturnType);
    }

    //类型 + 泛型 转换
    private static Object castFastJsonObject(Object origin, Class<?> aClazz, Type genericType) {
        if(origin == null){
            return null;
        }
        // 先判断是否是基本类型
        if(Short.class.equals(aClazz) || Short.TYPE.equals(aClazz)){
            return Short.valueOf(origin.toString());
        } else if (Integer.class.equals(aClazz) || Integer.TYPE.equals(aClazz)) {
            return Integer.valueOf(origin.toString());
        }else if (Character.class.equals(aClazz) || Character.TYPE.equals(aClazz)) {
            return origin.toString().charAt(0);
        }else if (Float.class.equals(aClazz) || Float.TYPE.equals(aClazz)) {
            return Float.valueOf(origin.toString());
        }else if (Double.class.equals(aClazz) || Double.TYPE.equals(aClazz)) {
            return Double.valueOf(origin.toString());
        }else if (Long.class.equals(aClazz) || Long.TYPE.equals(aClazz)) {
            return Long.valueOf(origin.toString());
        }else if (Boolean.class.equals(aClazz) || Boolean.TYPE.equals(aClazz)) {
            return Boolean.valueOf(origin.toString());
        }
        if (origin instanceof JSONObject jsonObject) {
            if (Map.class.isAssignableFrom(aClazz)) {
                Map<Object, Object> mapResult = new HashMap<>();
                if (Objects.nonNull(genericType) && genericType instanceof ParameterizedType parameterizedType) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    Type keyType = actualTypeArguments[0];
                    Type valueType = actualTypeArguments[1];
                    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                        Object keyValue = castFastJsonObject(entry.getKey(), (Class<?>)keyType, ((Class<?>)keyType).getGenericSuperclass());
                        Object valValue = castFastJsonObject(entry.getValue(), (Class<?>)valueType, ((Class<?>)valueType).getGenericSuperclass());
                        mapResult.put(keyValue, valValue);
                    }
                } else {
                    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                        mapResult.put(entry.getKey(), entry.getValue());
                    }
                }
                return mapResult;
            }else {
                return jsonObject.toJavaObject(aClazz);
            }
        } else if (origin instanceof JSONArray jsonArray) {
            if (aClazz.isArray()) {
                Class<?> componentType = aClazz.getComponentType();
                Object arrResult = Array.newInstance(componentType, jsonArray.size());
                for (int i = 0; i < jsonArray.size(); i++) {
                    Array.set(arrResult, i, castFastJsonObject(jsonArray.get(i), componentType, componentType.getGenericSuperclass()));
                }
                return arrResult;
            } else if (List.class.isAssignableFrom(aClazz)) {
                List<Object> listResult = new ArrayList<>(jsonArray.size());
                if (Objects.nonNull(genericType) && genericType instanceof ParameterizedType parameterizedType) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    Type itemType = actualTypeArguments[0];
                    for (int i = 0; i < jsonArray.size(); i++) {
                        listResult.add(castFastJsonObject(jsonArray.get(i), (Class<?>) itemType, ((Class<?>)itemType).getGenericSuperclass()));
                    }
                } else {
                    for (Object o : jsonArray) {
                        listResult.add(o);
                    }
                }
                return listResult;
            }
        }
        if (origin.getClass().isAssignableFrom(aClazz)) {
            return origin;
        }
        return JSONObject.parseObject(origin.toString(), aClazz);
    }
}
