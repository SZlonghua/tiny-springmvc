package com.tiny.springmvc.web.servlet.mvc.method.annotation;

import com.tiny.springmvc.http.MediaType;
import com.tiny.springmvc.http.converter.GenericHttpMessageConverter;
import com.tiny.springmvc.http.converter.HttpMessageConverter;
import com.tiny.springmvc.http.server.ServletServerHttpRequest;
import com.tiny.springmvc.http.server.ServletServerHttpResponse;
import com.tiny.springmvc.web.accept.ContentNegotiationManager;
import com.tiny.springmvc.web.context.request.ServletWebRequest;
import com.tiny.springmvc.web.method.support.HandlerMethodReturnValueHandler;
import com.tiny.springmvc.web.servlet.HandlerMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.Resource;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

public abstract class AbstractMessageConverterMethodProcessor implements HandlerMethodReturnValueHandler {


    protected final Log logger = LogFactory.getLog(getClass());

    private final ContentNegotiationManager contentNegotiationManager;

    protected final List<HttpMessageConverter<?>> messageConverters;

    protected final List<MediaType> allSupportedMediaTypes;

    private static final List<MediaType> ALL_APPLICATION_MEDIA_TYPES =
            Arrays.asList(MediaType.ALL, new MediaType("application"));

    protected AbstractMessageConverterMethodProcessor(List<HttpMessageConverter<?>> converters,
                                                      @Nullable ContentNegotiationManager contentNegotiationManager) {

        this(converters, contentNegotiationManager, null);
    }

    /**
     * Constructor with list of converters and ContentNegotiationManager as well
     * as request/response body advice instances.
     */
    protected AbstractMessageConverterMethodProcessor(List<HttpMessageConverter<?>> converters,
                                                      @Nullable ContentNegotiationManager manager, @Nullable List<Object> requestResponseBodyAdvice) {

        Assert.notEmpty(converters, "'messageConverters' must not be empty");
        this.messageConverters = converters;
        this.allSupportedMediaTypes = getAllSupportedMediaTypes(converters);

        this.contentNegotiationManager = (manager != null ? manager : new ContentNegotiationManager());
    }

    private static List<MediaType> getAllSupportedMediaTypes(List<HttpMessageConverter<?>> messageConverters) {
        Set<MediaType> allSupportedMediaTypes = new LinkedHashSet<>();
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
        }
        List<MediaType> result = new ArrayList<>(allSupportedMediaTypes);
        MediaType.sortBySpecificity(result);
        return Collections.unmodifiableList(result);
    }

    protected <T> void writeWithMessageConverters(@Nullable T value, MethodParameter returnType,
                                                  ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage)
            throws Exception {

        Object body;
        Class<?> valueType;
        Type targetType;

        if (value instanceof CharSequence) {
            body = value.toString();
            valueType = String.class;
            targetType = String.class;
        }
        else {
            body = value;
            valueType = getReturnValueType(body, returnType);
            targetType = GenericTypeResolver.resolveType(getGenericType(returnType), returnType.getContainingClass());
        }

        MediaType selectedMediaType = null;
        MediaType contentType = outputMessage.getHeaders().getContentType();
        boolean isContentTypePreset = contentType != null && contentType.isConcrete();
        if (isContentTypePreset) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found 'Content-Type:" + contentType + "' in response");
            }
            selectedMediaType = contentType;
        }
        else {
            HttpServletRequest request = inputMessage.getServletRequest();

            List<MediaType> acceptableTypes = getAcceptableMediaTypes(request);
            List<MediaType> producibleTypes = getProducibleMediaTypes(request, valueType, targetType);

            if (body != null && producibleTypes.isEmpty()) {
                throw new RuntimeException(
                        "No converter found for return value of type: " + valueType);
            }
            List<MediaType> mediaTypesToUse = new ArrayList<>();
            for (MediaType requestedType : acceptableTypes) {
                for (MediaType producibleType : producibleTypes) {
                    if (requestedType.isCompatibleWith(producibleType)) {
                        mediaTypesToUse.add(getMostSpecificMediaType(requestedType, producibleType));
                    }
                }
            }
            if (mediaTypesToUse.isEmpty()) {
                if (body != null) {
                    throw new RuntimeException("not support media type"+producibleTypes);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("No match for " + acceptableTypes + ", supported: " + producibleTypes);
                }
                return;
            }

            MediaType.sortBySpecificityAndQuality(mediaTypesToUse);

            for (MediaType mediaType : mediaTypesToUse) {
                if (mediaType.isConcrete()) {
                    selectedMediaType = mediaType;
                    break;
                }
                else if (mediaType.isPresentIn(ALL_APPLICATION_MEDIA_TYPES)) {
                    selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
                    break;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Using '" + selectedMediaType + "', given " +
                        acceptableTypes + " and supported " + producibleTypes);
            }
        }

        if (selectedMediaType != null) {
            selectedMediaType = selectedMediaType.removeQualityValue();
            for (HttpMessageConverter<?> converter : this.messageConverters) {
                GenericHttpMessageConverter genericConverter = (converter instanceof GenericHttpMessageConverter ?
                        (GenericHttpMessageConverter<?>) converter : null);
                if (genericConverter != null ?
                        ((GenericHttpMessageConverter) converter).canWrite(targetType, valueType, selectedMediaType) :
                        converter.canWrite(valueType, selectedMediaType)) {

                    if (body != null) {
                        Object theBody = body;
                        LogFormatUtils.traceDebug(logger, traceOn ->
                                "Writing [" + LogFormatUtils.formatValue(theBody, !traceOn) + "]");
                        //addContentDispositionHeader(inputMessage, outputMessage);
                        if (genericConverter != null) {
                            genericConverter.write(body, targetType, selectedMediaType, outputMessage);
                        }
                        else {
                            ((HttpMessageConverter) converter).write(body, selectedMediaType, outputMessage);
                        }
                    }
                    else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Nothing to write: null body");
                        }
                    }
                    return;
                }
            }
        }

        if (body != null) {
            Set<MediaType> producibleMediaTypes =
                    (Set<MediaType>) inputMessage.getServletRequest()
                            .getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);

            if (isContentTypePreset || !CollectionUtils.isEmpty(producibleMediaTypes)) {
                throw new RuntimeException(
                        "No converter for [" + valueType + "] with preset Content-Type '" + contentType + "'");
            }
            throw new RuntimeException();
        }
    }

    private MediaType getMostSpecificMediaType(MediaType acceptType, MediaType produceType) {
        MediaType produceTypeToUse = produceType.copyQualityValue(acceptType);
        return (MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceTypeToUse) <= 0 ? acceptType : produceTypeToUse);
    }

    protected List<MediaType> getProducibleMediaTypes(
            HttpServletRequest request, Class<?> valueClass, @Nullable Type targetType) {

        Set<MediaType> mediaTypes =
                (Set<MediaType>) request.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            return new ArrayList<>(mediaTypes);
        }
        else if (!this.allSupportedMediaTypes.isEmpty()) {
            List<MediaType> result = new ArrayList<>();
            for (HttpMessageConverter<?> converter : this.messageConverters) {
                if (converter instanceof GenericHttpMessageConverter && targetType != null) {
                    if (((GenericHttpMessageConverter<?>) converter).canWrite(targetType, valueClass, null)) {
                        result.addAll(converter.getSupportedMediaTypes());
                    }
                }
                else if (converter.canWrite(valueClass, null)) {
                    result.addAll(converter.getSupportedMediaTypes());
                }
            }
            return result;
        }
        else {
            return Collections.singletonList(MediaType.ALL);
        }
    }

    private List<MediaType> getAcceptableMediaTypes(HttpServletRequest request)
            throws Exception {

        return this.contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));
    }

    protected Class<?> getReturnValueType(@Nullable Object value, MethodParameter returnType) {
        return (value != null ? value.getClass() : returnType.getParameterType());
    }

    private Type getGenericType(MethodParameter returnType) {
        /*if (HttpEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return ResolvableType.forType(returnType.getGenericParameterType()).getGeneric().getType();
        }
        else {
            return returnType.getGenericParameterType();
        }*/

        return returnType.getGenericParameterType();
    }
}
