package com.tiny.springmvc.web.method.support;

import com.tiny.springmvc.web.context.request.NativeWebRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

public interface HandlerMethodReturnValueHandler {

    boolean supportsReturnType(MethodParameter returnType);

    void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                           ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception;
}
