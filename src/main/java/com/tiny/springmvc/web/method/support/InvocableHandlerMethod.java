package com.tiny.springmvc.web.method.support;

import com.tiny.springmvc.web.bind.support.WebDataBinderFactory;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import com.tiny.springmvc.web.method.HandlerMethod;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class InvocableHandlerMethod extends HandlerMethod {

    @Nullable
    private WebDataBinderFactory dataBinderFactory;

    private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();

    private static final Object[] EMPTY_ARGS = new Object[0];

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();


    public InvocableHandlerMethod(HandlerMethod handlerMethod) {
        super(handlerMethod);
    }

    public InvocableHandlerMethod(Object bean, Method method) {
        super(bean, method);
    }

    public InvocableHandlerMethod(String beanName, BeanFactory beanFactory, Method method) {
        super(beanName, beanFactory, method);
    }

    public void setHandlerMethodArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.resolvers = argumentResolvers;
    }
    public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    public void setDataBinderFactory(WebDataBinderFactory dataBinderFactory) {
        this.dataBinderFactory = dataBinderFactory;
    }

    @Nullable
    public Object invokeForRequest(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
                                   Object... providedArgs) throws Exception {

        Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
        if (logger.isTraceEnabled()) {
            logger.trace("Arguments: " + Arrays.toString(args));
        }
        return doInvoke(args);
    }

    protected Object[] getMethodArgumentValues(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
                                               Object... providedArgs) throws Exception {

        MethodParameter[] parameters = getMethodParameters();
        if (ObjectUtils.isEmpty(parameters)) {
            return EMPTY_ARGS;
        }

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            args[i] = findProvidedArgument(parameter, providedArgs);
            if (args[i] != null) {
                continue;
            }
            if (!this.resolvers.supportsParameter(parameter)) {
                throw new IllegalStateException("No suitable resolver");
            }
            try {
                args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);
            }
            catch (Exception ex) {
                // Leave stack trace for later, exception may actually be resolved and handled...
                throw ex;
            }
        }
        return args;
    }

    @Nullable
    protected Object doInvoke(Object... args) throws Exception {
        ReflectionUtils.makeAccessible(getBridgedMethod());
        try {
            return getBridgedMethod().invoke(getBean(), args);
        }
        catch (IllegalArgumentException ex) {
            String text = (ex.getMessage() != null ? ex.getMessage() : "Illegal argument");
            throw new IllegalStateException(text);
        }
        catch (InvocationTargetException ex) {
            // Unwrap for HandlerExceptionResolvers ...
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            else if (targetException instanceof Error) {
                throw (Error) targetException;
            }
            else if (targetException instanceof Exception) {
                throw (Exception) targetException;
            }
            else {
                throw new IllegalStateException("Invocation failure");
            }
        }
    }

    @Nullable
    protected static Object findProvidedArgument(MethodParameter parameter, @Nullable Object... providedArgs) {
        if (!ObjectUtils.isEmpty(providedArgs)) {
            for (Object providedArg : providedArgs) {
                if (parameter.getParameterType().isInstance(providedArg)) {
                    return providedArg;
                }
            }
        }
        return null;
    }

}
