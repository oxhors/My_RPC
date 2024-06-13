package com.wx.wxrpc.core.exception;

import java.util.Objects;

public enum RpcExceptionEnum {
    X001("X001", "method_not_found"),
    X002("X002", "http_invoker_timeout"),
    X003("X003", "limitRequest"),
    Z001("Z001", "unknown"),
            ;

    private final String errorCode;
    private final String errorMessage;

    RpcExceptionEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static  RpcExceptionEnum findHhRpcExceptionEnum(String errorCode) {
        if (Objects.isNull(errorCode)) {
            return Z001;
        }
        for (RpcExceptionEnum enumItem : values()) {
            if (enumItem.errorCode.equals(errorCode)) {
                return enumItem;
            }
        }
        return Z001;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
