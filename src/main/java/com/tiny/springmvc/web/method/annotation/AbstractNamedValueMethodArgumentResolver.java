package com.tiny.springmvc.web.method.annotation;

import com.tiny.springmvc.web.bind.WebDataBinder;
import com.tiny.springmvc.web.bind.support.WebDataBinderFactory;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import com.tiny.springmvc.web.method.support.HandlerMethodArgumentResolver;
import com.tiny.springmvc.web.method.support.ModelAndViewContainer;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractNamedValueMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Nullable
    private final ConfigurableBeanFactory configurableBeanFactory;

    @Nullable
    private final BeanExpressionContext expressionContext;

    private final Map<MethodParameter, NamedValueInfo> namedValueInfoCache = new ConcurrentHashMap<>(256);


    public AbstractNamedValueMethodArgumentResolver() {
        this.configurableBeanFactory = null;
        this.expressionContext = null;
    }

    /**
     * Create a new {@link AbstractNamedValueMethodArgumentResolver} instance.
     * @param beanFactory a bean factory to use for resolving ${...} placeholder
     * and #{...} SpEL expressions in default values, or {@code null} if default
     * values are not expected to contain expressions
     */
    public AbstractNamedValueMethodArgumentResolver(@Nullable ConfigurableBeanFactory beanFactory) {
        this.configurableBeanFactory = beanFactory;
        /*this.expressionContext =
                (beanFactory != null ? new BeanExpressionContext(beanFactory, new RequestScope()) : null);*/
        this.expressionContext = null;
    }


    @Override
    @Nullable
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        NamedValueInfo namedValueInfo = getNamedValueInfo(parameter);
        MethodParameter nestedParameter = parameter.nestedIfOptional();

        Object resolvedName = namedValueInfo.name;
        if (resolvedName == null) {
            throw new IllegalArgumentException(
                    "Specified name must not resolve to null: [" + namedValueInfo.name + "]");
        }

        Object arg = resolveName(resolvedName.toString(), nestedParameter, webRequest);


        if (binderFactory != null) {
            WebDataBinder binder = binderFactory.createBinder(webRequest, null, namedValueInfo.name);
            arg = binder.convertIfNecessary(arg, parameter.getParameterType(), parameter);
        }

        //handleResolvedValue(arg, namedValueInfo.name, parameter, mavContainer, webRequest);

        return arg;
    }

    @Nullable
    protected abstract Object resolveName(String name, MethodParameter parameter, NativeWebRequest request)
            throws Exception;

    private NamedValueInfo getNamedValueInfo(MethodParameter parameter) {
        NamedValueInfo namedValueInfo = this.namedValueInfoCache.get(parameter);
        if (namedValueInfo == null) {
            namedValueInfo = createNamedValueInfo(parameter);
            namedValueInfo = updateNamedValueInfo(parameter, namedValueInfo);
            this.namedValueInfoCache.put(parameter, namedValueInfo);
        }
        return namedValueInfo;
    }

    protected abstract NamedValueInfo createNamedValueInfo(MethodParameter parameter);

    /**
     * Create a new NamedValueInfo based on the given NamedValueInfo with sanitized values.
     */
    private NamedValueInfo updateNamedValueInfo(MethodParameter parameter, NamedValueInfo info) {
        String name = info.name;
        if (info.name.isEmpty()) {
            name = parameter.getParameterName();
            if (name == null) {
                throw new IllegalArgumentException(
                        "Name for argument type [" + parameter.getNestedParameterType().getName() +
                                "] not available, and parameter name information not found in class file either.");
            }
        }
        String defaultValue = info.defaultValue;
        return new NamedValueInfo(name, info.required, defaultValue);
    }





    protected static class NamedValueInfo {

        private final String name;

        private final boolean required;

        @Nullable
        private final String defaultValue;

        public NamedValueInfo(String name, boolean required, @Nullable String defaultValue) {
            this.name = name;
            this.required = required;
            this.defaultValue = defaultValue;
        }
    }
}
