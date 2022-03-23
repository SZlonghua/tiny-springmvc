package com.tiny.springmvc.web.method.annotation;

import com.tiny.springmvc.web.bind.annotation.RequestParam;
import com.tiny.springmvc.web.bind.annotation.ValueConstants;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import com.tiny.springmvc.web.method.support.UriComponentsContributor;
import com.tiny.springmvc.web.util.UriComponentsBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class RequestParamMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver
                implements UriComponentsContributor {


    private final boolean useDefaultResolution;

    public RequestParamMethodArgumentResolver(@Nullable ConfigurableBeanFactory beanFactory,
                                              boolean useDefaultResolution) {

        super(beanFactory);
        this.useDefaultResolution = useDefaultResolution;
    }


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestParam.class)||
                BeanUtils.isSimpleProperty(parameter.getNestedParameterType());
    }

    @Override
    public void contributeMethodArgument(MethodParameter parameter, Object value, UriComponentsBuilder builder, Map<String, Object> uriVariables, ConversionService conversionService) {

    }

    @Override
    @Nullable
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {

        Object arg = null;
        String[] paramValues = request.getParameterValues(name);
        if (paramValues != null) {
            arg = (paramValues.length == 1 ? paramValues[0] : paramValues);
        }
        return arg;
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        RequestParam ann = parameter.getParameterAnnotation(RequestParam.class);
        return (ann != null ? new RequestParamNamedValueInfo(ann) : new RequestParamNamedValueInfo());
    }

    private static class RequestParamNamedValueInfo extends NamedValueInfo {

        public RequestParamNamedValueInfo() {
            super("", false, ValueConstants.DEFAULT_NONE);
        }

        public RequestParamNamedValueInfo(RequestParam annotation) {
            super(annotation.name(), annotation.required(), annotation.defaultValue());
        }
    }
}
