package com.tiny.springmvc.web.method.support;

import com.tiny.springmvc.web.context.request.NativeWebRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HandlerMethodReturnValueHandlerComposite implements HandlerMethodReturnValueHandler{

    protected final Log logger = LogFactory.getLog(getClass());

    private final List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();


    /**
     * Return a read-only list with the registered handlers, or an empty list.
     */
    public List<HandlerMethodReturnValueHandler> getHandlers() {
        return Collections.unmodifiableList(this.returnValueHandlers);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return getReturnValueHandler(returnType) != null;
    }

    @Nullable
    private HandlerMethodReturnValueHandler getReturnValueHandler(MethodParameter returnType) {
        for (HandlerMethodReturnValueHandler handler : this.returnValueHandlers) {
            if (handler.supportsReturnType(returnType)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Iterate over registered {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers} and invoke the one that supports it.
     * @throws IllegalStateException if no suitable {@link HandlerMethodReturnValueHandler} is found.
     */
    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

        HandlerMethodReturnValueHandler handler = getReturnValueHandler(returnType);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown return value type: " + returnType.getParameterType().getName());
        }
        handler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    public HandlerMethodReturnValueHandlerComposite addHandlers(
            @Nullable List<? extends HandlerMethodReturnValueHandler> handlers) {

        if (handlers != null) {
            this.returnValueHandlers.addAll(handlers);
        }
        return this;
    }
}
