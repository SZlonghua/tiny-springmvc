package com.tiny.springmvc.web.servlet.mvc.method;

import com.tiny.springmvc.web.method.HandlerMethod;
import com.tiny.springmvc.web.servlet.HandlerAdapter;
import com.tiny.springmvc.web.servlet.ModelAndView;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractHandlerMethodAdapter implements HandlerAdapter, Ordered {

    private int order = Ordered.LOWEST_PRECEDENCE;


    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }


    @Override
    public final boolean supports(Object handler) {
        return (handler instanceof HandlerMethod && supportsInternal((HandlerMethod) handler));
    }

    protected abstract boolean supportsInternal(HandlerMethod handlerMethod);

    @Override
    @Nullable
    public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        return handleInternal(request, response, (HandlerMethod) handler);
    }

    @Nullable
    protected abstract ModelAndView handleInternal(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   HandlerMethod handlerMethod)
            throws Exception;
}
