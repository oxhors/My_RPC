package com.wx.wxrpc.myregistrycenter.http.impl;

import com.wx.wxrpc.myregistrycenter.http.HttpInvoker;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

public class OkHttpInvoker implements HttpInvoker {
    private static final Logger log = LoggerFactory.getLogger(OkHttpInvoker.class);

    private final OkHttpClient client;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public OkHttpInvoker(long timeout) {
        this.client = new OkHttpClient.Builder()
                //  .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
                .connectTimeout(10,TimeUnit.SECONDS)
                .readTimeout(10,TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS).build();
    }

    @Override
    public String get(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            checkState(response.isSuccessful(), "Unexpected code " + response);
            ResponseBody body = response.body();
            if (Objects.nonNull(body)) {
                String result = body.string();
                log.debug("===> get url: {}, success: {}", url, result);
                return result;
            }
            return null;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String post(String url, String requestString) {
        Request request = new Request.Builder().url(url).post(RequestBody.create(requestString, JSON)).build();
        try (Response response = client.newCall(request).execute()) {
            checkState(response.isSuccessful(), "Unexpected code " + response);
            ResponseBody body = response.body();
            if (Objects.nonNull(body)) {
                String result = body.string();
                log.debug("===> post url: {}, success: {}", url, result);
                return result;
            }
            return null;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
