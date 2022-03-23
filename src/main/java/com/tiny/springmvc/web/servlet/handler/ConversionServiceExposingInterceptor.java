package com.tiny.springmvc.web.servlet.handler;

import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConversionServiceExposingInterceptor extends HandlerInterceptorAdapter {

    private final ConversionService conversionService;


    /**
     * Creates a new {@link ConversionServiceExposingInterceptor}.
     * @param conversionService the conversion service to export to request scope when this interceptor is invoked
     */
    public ConversionServiceExposingInterceptor(ConversionService conversionService) {
        Assert.notNull(conversionService, "The ConversionService may not be null");
        this.conversionService = conversionService;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException, IOException {

        request.setAttribute(ConversionService.class.getName(), this.conversionService);
        return true;
    }
}
