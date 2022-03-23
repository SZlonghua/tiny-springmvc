package com.tiny.springmvc.web.servlet.handler;

import com.tiny.springmvc.web.context.support.WebApplicationObjectSupport;
import com.tiny.springmvc.web.servlet.HandlerExecutionChain;
import com.tiny.springmvc.web.servlet.HandlerInterceptor;
import com.tiny.springmvc.web.servlet.HandlerMapping;
import com.tiny.springmvc.web.util.UrlPathHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport
        implements HandlerMapping, Ordered, BeanNameAware {

    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private Object defaultHandler;

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    private PathMatcher pathMatcher = new AntPathMatcher();

    private final List<Object> interceptors = new ArrayList<>();

    private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<>();

    private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

    @Nullable
    private String beanName;


    @Override
    protected void initApplicationContext() throws BeansException {
        detectMappedInterceptors(this.adaptedInterceptors);
        //initInterceptors();
    }

    protected void detectMappedInterceptors(List<HandlerInterceptor> mappedInterceptors) {
        mappedInterceptors.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        obtainApplicationContext(), MappedInterceptor.class, true, false).values());
    }

    /*protected void initInterceptors() {
        if (!this.interceptors.isEmpty()) {
            for (int i = 0; i < this.interceptors.size(); i++) {
                Object interceptor = this.interceptors.get(i);
                if (interceptor == null) {
                    throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
                }
                this.adaptedInterceptors.add(adaptInterceptor(interceptor));
            }
        }
    }

    protected HandlerInterceptor adaptInterceptor(Object interceptor) {
        if (interceptor instanceof HandlerInterceptor) {
            return (HandlerInterceptor) interceptor;
        }
        else if (interceptor instanceof WebRequestInterceptor) {
            return new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor);
        }
        else {
            throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
        }
    }*/

    @Override
    @Nullable
    public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        Object handler = getHandlerInternal(request);
        if (handler == null) {
            handler = getDefaultHandler();
        }
        if (handler == null) {
            return null;
        }
        // Bean name or resolved handler?
        if (handler instanceof String) {
            String handlerName = (String) handler;
            handler = obtainApplicationContext().getBean(handlerName);
        }

        HandlerExecutionChain executionChain = getHandlerExecutionChain(handler, request);

        if (logger.isTraceEnabled()) {
            logger.trace("Mapped to " + handler);
        }
        else if (logger.isDebugEnabled() && !request.getDispatcherType().equals(DispatcherType.ASYNC)) {
            logger.debug("Mapped to " + executionChain.getHandler());
        }

        return executionChain;
    }

    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
        HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ?
                (HandlerExecutionChain) handler : new HandlerExecutionChain(handler));

        String lookupPath = this.urlPathHelper.getLookupPathForRequest(request, LOOKUP_PATH);
        for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
            if (interceptor instanceof MappedInterceptor) {
                MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptor;
                if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
                    chain.addInterceptor(mappedInterceptor.getInterceptor());
                }
            }
            else {
                chain.addInterceptor(interceptor);
            }
        }
        return chain;
    }


    public void setDefaultHandler(@Nullable Object defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    @Nullable
    public Object getDefaultHandler() {
        return this.defaultHandler;
    }

    @Nullable
    protected abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;


    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    protected String formatMappingName() {
        return this.beanName != null ? "'" + this.beanName + "'" : "<unknown>";
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public UrlPathHelper getUrlPathHelper() {
        return this.urlPathHelper;
    }
}
