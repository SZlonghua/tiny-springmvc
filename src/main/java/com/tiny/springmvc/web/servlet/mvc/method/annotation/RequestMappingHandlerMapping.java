package com.tiny.springmvc.web.servlet.mvc.method.annotation;

import com.tiny.springmvc.web.bind.annotation.RequestMapping;
import com.tiny.springmvc.web.servlet.handler.AbstractHandlerMethodMapping;
import com.tiny.springmvc.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class RequestMappingHandlerMapping extends AbstractHandlerMethodMapping<RequestMappingInfo> {



    private Map<String, Predicate<Class<?>>> pathPrefixes = new LinkedHashMap<>();

    private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

    public void setPathPrefixes(Map<String, Predicate<Class<?>>> prefixes) {
        this.pathPrefixes = Collections.unmodifiableMap(new LinkedHashMap<>(prefixes));
    }

    public Map<String, Predicate<Class<?>>> getPathPrefixes() {
        return this.pathPrefixes;
    }

    @Override
    protected boolean isHandler(Class beanType) {
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
                AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
    }

    @Override
    @Nullable
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
            String prefix = getPathPrefix(handlerType);
            if (prefix != null) {
                info = RequestMappingInfo.paths(prefix).options(this.config).build().combine(info);
            }
        }
        return info;
    }

    @Override
    protected RequestMappingInfo getMatchingMapping(RequestMappingInfo info, HttpServletRequest request) {
        return info.getMatchingCondition(request);
    }

    @Nullable
    String getPathPrefix(Class<?> handlerType) {
        for (Map.Entry<String, Predicate<Class<?>>> entry : this.pathPrefixes.entrySet()) {
            if (entry.getValue().test(handlerType)) {
                String prefix = entry.getKey();
                return prefix;
            }
        }
        return null;
    }


    @Nullable
    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
        return (requestMapping != null ? createRequestMappingInfo(requestMapping) : null);
    }

    protected RequestMappingInfo createRequestMappingInfo(RequestMapping requestMapping) {

        RequestMappingInfo.Builder builder = RequestMappingInfo
                .paths(requestMapping.path())
                .methods(requestMapping.method())
                .mappingName(requestMapping.name());
        return builder.options(this.config).build();
    }


    @Override
    protected Set<String> getMappingPathPatterns(RequestMappingInfo info) {
        return info.getPatternsCondition().getPatterns();
    }

}
