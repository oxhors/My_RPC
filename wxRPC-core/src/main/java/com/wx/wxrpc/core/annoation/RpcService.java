package com.wx.wxrpc.core.annoation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 标注在类上
public @interface RpcService {
}
