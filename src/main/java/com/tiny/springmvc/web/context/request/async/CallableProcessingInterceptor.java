package com.tiny.springmvc.web.context.request.async;

import com.tiny.springmvc.web.context.request.NativeWebRequest;

import java.util.concurrent.Callable;

public interface CallableProcessingInterceptor {

    Object RESULT_NONE = new Object();

    Object RESPONSE_HANDLED = new Object();

    default <T> void preProcess(NativeWebRequest request, Callable<T> task) throws Exception {
    }

    default <T> void postProcess(NativeWebRequest request, Callable<T> task,
                                 Object concurrentResult) throws Exception {
    }

    default <T> void afterCompletion(NativeWebRequest request, Callable<T> task) throws Exception {
    }
}
