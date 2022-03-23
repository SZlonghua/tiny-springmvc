package com.tiny.springmvc.web.method.support;

import com.tiny.springmvc.web.util.UriComponentsBuilder;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;

import java.util.Map;

public interface UriComponentsContributor {

    boolean supportsParameter(MethodParameter parameter);

    void contributeMethodArgument(MethodParameter parameter, Object value, UriComponentsBuilder builder,
                                  Map<String, Object> uriVariables, ConversionService conversionService);
}
