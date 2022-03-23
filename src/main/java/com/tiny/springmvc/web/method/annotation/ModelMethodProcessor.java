package com.tiny.springmvc.web.method.annotation;

import com.tiny.springmvc.web.bind.support.WebDataBinderFactory;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import com.tiny.springmvc.web.method.support.HandlerMethodArgumentResolver;
import com.tiny.springmvc.web.method.support.HandlerMethodReturnValueHandler;
import com.tiny.springmvc.web.method.support.ModelAndViewContainer;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.ui.Model;
import org.springframework.util.Assert;

public class ModelMethodProcessor implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Model.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    @Nullable
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

        Assert.state(mavContainer != null, "ModelAndViewContainer is required for model exposure");
        return mavContainer.getModel();
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return Model.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

        if (returnValue == null) {
            return;
        }
        else if (returnValue instanceof Model) {
            mavContainer.addAllAttributes(((Model) returnValue).asMap());
        }
        else {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " +
                    returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }

}
