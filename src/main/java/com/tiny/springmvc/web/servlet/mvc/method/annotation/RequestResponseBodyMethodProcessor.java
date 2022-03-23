package com.tiny.springmvc.web.servlet.mvc.method.annotation;

import com.tiny.springmvc.http.converter.HttpMessageConverter;
import com.tiny.springmvc.http.server.ServletServerHttpRequest;
import com.tiny.springmvc.http.server.ServletServerHttpResponse;
import com.tiny.springmvc.web.accept.ContentNegotiationManager;
import com.tiny.springmvc.web.bind.annotation.ResponseBody;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import com.tiny.springmvc.web.method.support.ModelAndViewContainer;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class RequestResponseBodyMethodProcessor extends AbstractMessageConverterMethodProcessor {
    protected RequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters, ContentNegotiationManager contentNegotiationManager) {
        super(converters, contentNegotiationManager);
    }

    protected RequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager, List<Object> requestResponseBodyAdvice) {
        super(converters, manager, requestResponseBodyAdvice);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBody.class) ||
                returnType.hasMethodAnnotation(ResponseBody.class));
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);
        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        // Try even with null return value. ResponseBodyAdvice could get involved.
        writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
    }

    protected ServletServerHttpRequest createInputMessage(NativeWebRequest webRequest) {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.state(servletRequest != null, "No HttpServletRequest");
        return new ServletServerHttpRequest(servletRequest);
    }
    protected ServletServerHttpResponse createOutputMessage(NativeWebRequest webRequest) {
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        Assert.state(response != null, "No HttpServletResponse");
        return new ServletServerHttpResponse(response);
    }
}
