package com.wx.wxrpc.core.exception;

//自定义异常类
public class RpcException extends RuntimeException{
    private String errorCode;

    // X 技术类异常； Y 业务类异常； Z 未知异常

    public static final String METHOD_NOT_FOUND = "X001_method_not_found";
    public static final String SERVICE_NOT_FOUND = "X003_service_not_found";

    public static final String METHOD_INVOKE_FAILED = "X004_method_invoke_failed";
    public static final String HTTP_INVOKER_TIMEOUT = "X002_http_invoker_timeout";
    public static final String UNKNOWN = "Z001_unknown";

    public RpcException(){}

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(Throwable cause, String errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
