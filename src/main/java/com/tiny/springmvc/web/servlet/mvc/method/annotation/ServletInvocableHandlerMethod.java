package com.tiny.springmvc.web.servlet.mvc.method.annotation;

import com.tiny.springmvc.web.context.request.ServletWebRequest;
import com.tiny.springmvc.web.method.HandlerMethod;
import com.tiny.springmvc.web.method.support.HandlerMethodReturnValueHandlerComposite;
import com.tiny.springmvc.web.method.support.InvocableHandlerMethod;
import com.tiny.springmvc.web.method.support.ModelAndViewContainer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

public class ServletInvocableHandlerMethod extends InvocableHandlerMethod {

    @Nullable
    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    public ServletInvocableHandlerMethod(HandlerMethod handlerMethod) {
        super(handlerMethod);
    }

    public ServletInvocableHandlerMethod(Object bean, Method method) {
        super(bean, method);
    }

    public ServletInvocableHandlerMethod(String beanName, BeanFactory beanFactory, Method method) {
        super(beanName, beanFactory, method);
    }

    public void setHandlerMethodReturnValueHandlers(HandlerMethodReturnValueHandlerComposite returnValueHandlers) {
        this.returnValueHandlers = returnValueHandlers;
    }

    public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer,
                                Object... providedArgs) throws Exception {

        Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);

        if (returnValue == null) {
            mavContainer.setRequestHandled(true);
            return;
        }
        mavContainer.setRequestHandled(false);
        Assert.state(this.returnValueHandlers != null, "No return value handlers");
        try {
            this.returnValueHandlers.handleReturnValue(
                    returnValue, getReturnValueType(returnValue), mavContainer, webRequest);
        }
        catch (Exception ex) {
            throw ex;
        }
    }
}
