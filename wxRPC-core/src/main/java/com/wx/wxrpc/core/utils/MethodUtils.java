package com.wx.wxrpc.core.utils;

import com.wx.wxrpc.core.annoation.RpcReference;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MethodUtils {

    //判断是否是toString这类方法，这类方法不向外提供
    public static boolean checkLocalMethod(Method method){
        return method.getDeclaringClass().equals(Object.class);
    }

    public static List<Field> getAnnoFields(Class<?> clazz,Class<? extends Annotation> annoClazz) {
        List<Field> res = new ArrayList<>();
        while(Objects.nonNull(clazz)){
            List<Field> list = Arrays.stream(clazz.getDeclaredFields()).filter(field -> {
                return field.isAnnotationPresent(annoClazz);
            }).toList();
            if(!list.isEmpty()){
                res.addAll(list);
            }
            clazz = clazz.getSuperclass();
        }
        return res;
    }
}
