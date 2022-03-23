package com.tiny.springmvc.web.context.request;

import org.springframework.core.NamedInheritableThreadLocal;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

public class RequestContextHolder {

    private static final ThreadLocal<RequestAttributes> requestAttributesHolder =
            new NamedThreadLocal<>("Request attributes");

    private static final ThreadLocal<RequestAttributes> inheritableRequestAttributesHolder =
            new NamedInheritableThreadLocal<>("Request context");


    @Nullable
    public static RequestAttributes getRequestAttributes() {
        RequestAttributes attributes = requestAttributesHolder.get();
        if (attributes == null) {
            attributes = inheritableRequestAttributesHolder.get();
        }
        return attributes;
    }

    public static void setRequestAttributes(@Nullable RequestAttributes attributes, boolean inheritable) {
        if (attributes == null) {
            resetRequestAttributes();
        }
        else {
            if (inheritable) {
                inheritableRequestAttributesHolder.set(attributes);
                requestAttributesHolder.remove();
            }
            else {
                requestAttributesHolder.set(attributes);
                inheritableRequestAttributesHolder.remove();
            }
        }
    }

    public static void resetRequestAttributes() {
        requestAttributesHolder.remove();
        inheritableRequestAttributesHolder.remove();
    }
}
