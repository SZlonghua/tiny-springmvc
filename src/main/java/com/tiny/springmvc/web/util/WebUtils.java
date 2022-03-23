package com.tiny.springmvc.web.util;

import org.springframework.lang.Nullable;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;

public class WebUtils {

    public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";

    public static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";

    public static final String ERROR_STATUS_CODE_ATTRIBUTE = "javax.servlet.error.status_code";

    /**
     * Standard Servlet 2.3+ spec request attribute for error page exception type.
     * <p>To be exposed to JSPs that are marked as error pages, when forwarding
     * to them directly rather than through the servlet container's error page
     * resolution mechanism.
     */
    public static final String ERROR_EXCEPTION_TYPE_ATTRIBUTE = "javax.servlet.error.exception_type";

    /**
     * Standard Servlet 2.3+ spec request attribute for error page message.
     * <p>To be exposed to JSPs that are marked as error pages, when forwarding
     * to them directly rather than through the servlet container's error page
     * resolution mechanism.
     */
    public static final String ERROR_MESSAGE_ATTRIBUTE = "javax.servlet.error.message";

    /**
     * Standard Servlet 2.3+ spec request attribute for error page exception.
     * <p>To be exposed to JSPs that are marked as error pages, when forwarding
     * to them directly rather than through the servlet container's error page
     * resolution mechanism.
     */
    public static final String ERROR_EXCEPTION_ATTRIBUTE = "javax.servlet.error.exception";

    /**
     * Standard Servlet 2.3+ spec request attribute for error page request URI.
     * <p>To be exposed to JSPs that are marked as error pages, when forwarding
     * to them directly rather than through the servlet container's error page
     * resolution mechanism.
     */
    public static final String ERROR_REQUEST_URI_ATTRIBUTE = "javax.servlet.error.request_uri";

    /**
     * Standard Servlet 2.3+ spec request attribute for error page servlet name.
     * <p>To be exposed to JSPs that are marked as error pages, when forwarding
     * to them directly rather than through the servlet container's error page
     * resolution mechanism.
     */
    public static final String ERROR_SERVLET_NAME_ATTRIBUTE = "javax.servlet.error.servlet_name";

    public static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";


    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";



    public static void clearErrorRequestAttributes(HttpServletRequest request) {
        request.removeAttribute(ERROR_STATUS_CODE_ATTRIBUTE);
        request.removeAttribute(ERROR_EXCEPTION_TYPE_ATTRIBUTE);
        request.removeAttribute(ERROR_MESSAGE_ATTRIBUTE);
        request.removeAttribute(ERROR_EXCEPTION_ATTRIBUTE);
        request.removeAttribute(ERROR_REQUEST_URI_ATTRIBUTE);
        request.removeAttribute(ERROR_SERVLET_NAME_ATTRIBUTE);
    }

    @Nullable
    public static <T> T getNativeRequest(ServletRequest request, @Nullable Class<T> requiredType) {
        if (requiredType != null) {
            if (requiredType.isInstance(request)) {
                return (T) request;
            }
            else if (request instanceof ServletRequestWrapper) {
                return getNativeRequest(((ServletRequestWrapper) request).getRequest(), requiredType);
            }
        }
        return null;
    }

    /**
     * Return an appropriate response object of the specified type, if available,
     * unwrapping the given response as far as necessary.
     * @param response the servlet response to introspect
     * @param requiredType the desired type of response object
     * @return the matching response object, or {@code null} if none
     * of that type is available
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T getNativeResponse(ServletResponse response, @Nullable Class<T> requiredType) {
        if (requiredType != null) {
            if (requiredType.isInstance(response)) {
                return (T) response;
            }
            else if (response instanceof ServletResponseWrapper) {
                return getNativeResponse(((ServletResponseWrapper) response).getResponse(), requiredType);
            }
        }
        return null;
    }
}
