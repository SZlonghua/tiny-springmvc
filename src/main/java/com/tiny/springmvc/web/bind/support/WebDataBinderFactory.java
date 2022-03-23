package com.tiny.springmvc.web.bind.support;

import com.tiny.springmvc.web.bind.WebDataBinder;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import org.springframework.lang.Nullable;

public interface WebDataBinderFactory {

    WebDataBinder createBinder(NativeWebRequest webRequest, @Nullable Object target, String objectName)
            throws Exception;
}
