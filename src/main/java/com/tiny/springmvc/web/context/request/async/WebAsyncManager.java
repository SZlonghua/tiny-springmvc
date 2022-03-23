package com.tiny.springmvc.web.context.request.async;

import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;

public final class WebAsyncManager {

    private final Map<Object, CallableProcessingInterceptor> callableInterceptors = new LinkedHashMap<>();

    WebAsyncManager() {
    }

    public void registerCallableInterceptor(Object key, CallableProcessingInterceptor interceptor) {
        Assert.notNull(key, "Key is required");
        Assert.notNull(interceptor, "CallableProcessingInterceptor  is required");
        this.callableInterceptors.put(key, interceptor);
    }
}
