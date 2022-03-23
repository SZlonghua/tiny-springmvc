package com.tiny.springmvc.web.method.support;

import com.tiny.springmvc.web.bind.support.WebDataBinderFactory;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

public interface HandlerMethodArgumentResolver {


    boolean supportsParameter(MethodParameter parameter);

    @Nullable
    Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                           NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception;
}
