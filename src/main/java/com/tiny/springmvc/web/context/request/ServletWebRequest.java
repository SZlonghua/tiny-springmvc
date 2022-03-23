package com.tiny.springmvc.web.context.request;

import com.tiny.springmvc.web.util.WebUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletWebRequest extends ServletRequestAttributes implements NativeWebRequest {

    public ServletWebRequest(HttpServletRequest request) {
        super(request);
    }

    public ServletWebRequest(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Override
    public Object getNativeRequest() {
        return getRequest();
    }

    @Override
    public Object getNativeResponse() {
        return getResponse();
    }

    @Override
    public <T> T getNativeRequest(@Nullable Class<T> requiredType) {
        return WebUtils.getNativeRequest(getRequest(), requiredType);
    }

    @Override
    public <T> T getNativeResponse(@Nullable Class<T> requiredType) {
        HttpServletResponse response = getResponse();
        return (response != null ? WebUtils.getNativeResponse(response, requiredType) : null);
    }

    @Override
    @Nullable
    public String[] getParameterValues(String paramName) {
        return getRequest().getParameterValues(paramName);
    }

    @Override
    @Nullable
    public String[] getHeaderValues(String headerName) {
        String[] headerValues = StringUtils.toStringArray(getRequest().getHeaders(headerName));
        return (!ObjectUtils.isEmpty(headerValues) ? headerValues : null);
    }
}
