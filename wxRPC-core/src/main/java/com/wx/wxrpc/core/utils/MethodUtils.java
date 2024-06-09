package com.wx.wxrpc.core.utils;

import java.lang.reflect.Method;

public class MethodUtils {

    //判断是否是toString这类方法，这类方法不向外提供
    public static boolean checkLocalMethod(Method method){
        return method.getDeclaringClass().equals(Object.class);
    }
}
