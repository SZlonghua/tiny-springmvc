package com.tiny.springmvc.web.context.request;

import org.springframework.lang.Nullable;

public interface WebRequest extends RequestAttributes {


    @Nullable
    String[] getParameterValues(String paramName);
}
