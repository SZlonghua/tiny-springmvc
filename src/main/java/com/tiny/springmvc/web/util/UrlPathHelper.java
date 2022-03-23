package com.tiny.springmvc.web.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Properties;

public class UrlPathHelper {

    private static final String WEBSPHERE_URI_ATTRIBUTE = "com.ibm.websphere.servlet.uri_non_decoded";

    private static final Log logger = LogFactory.getLog(UrlPathHelper.class);

    @Nullable
    static volatile Boolean websphereComplianceFlag;


    private boolean alwaysUseFullPath = false;

    private boolean urlDecode = true;

    private boolean removeSemicolonContent = true;

    private String defaultEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;

    public String getLookupPathForRequest(HttpServletRequest request, @Nullable String lookupPathAttributeName) {
        if (lookupPathAttributeName != null) {
            String result = (String) request.getAttribute(lookupPathAttributeName);
            if (result != null) {
                return result;
            }
        }
        return getLookupPathForRequest(request);
    }

    public String getLookupPathForRequest(HttpServletRequest request) {
        // Always use full path within current servlet context?
        if (this.alwaysUseFullPath) {
            return getPathWithinApplication(request);
        }
        // Else, use path within current servlet mapping if applicable
        String rest = getPathWithinServletMapping(request);
        if (!"".equals(rest)) {
            return rest;
        }
        else {
            return getPathWithinApplication(request);
        }
    }

    public String getPathWithinServletMapping(HttpServletRequest request) {
        String pathWithinApp = getPathWithinApplication(request);
        String servletPath = getServletPath(request);
        String sanitizedPathWithinApp = getSanitizedPath(pathWithinApp);
        String path;

        // If the app container sanitized the servletPath, check against the sanitized version
        if (servletPath.contains(sanitizedPathWithinApp)) {
            path = getRemainingPath(sanitizedPathWithinApp, servletPath, false);
        }
        else {
            path = getRemainingPath(pathWithinApp, servletPath, false);
        }

        if (path != null) {
            // Normal case: URI contains servlet path.
            return path;
        }
        else {
            // Special case: URI is different from servlet path.
            String pathInfo = request.getPathInfo();
            if (pathInfo != null) {
                // Use path info if available. Indicates index page within a servlet mapping?
                // e.g. with index page: URI="/", servletPath="/index.html"
                return pathInfo;
            }
            if (!this.urlDecode) {
                // No path info... (not mapped by prefix, nor by extension, nor "/*")
                // For the default servlet mapping (i.e. "/"), urlDecode=false can
                // cause issues since getServletPath() returns a decoded path.
                // If decoding pathWithinApp yields a match just use pathWithinApp.
                path = getRemainingPath(decodeInternal(request, pathWithinApp), servletPath, false);
                if (path != null) {
                    return pathWithinApp;
                }
            }
            // Otherwise, use the full servlet path.
            return servletPath;
        }
    }

    public String getServletPath(HttpServletRequest request) {
        String servletPath = (String) request.getAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
        if (servletPath == null) {
            servletPath = request.getServletPath();
        }
        if (servletPath.length() > 1 && servletPath.endsWith("/") && shouldRemoveTrailingServletPathSlash(request)) {
            // On WebSphere, in non-compliant mode, for a "/foo/" case that would be "/foo"
            // on all other servlet containers: removing trailing slash, proceeding with
            // that remaining slash as final lookup path...
            servletPath = servletPath.substring(0, servletPath.length() - 1);
        }
        return servletPath;
    }

    private boolean shouldRemoveTrailingServletPathSlash(HttpServletRequest request) {
        if (request.getAttribute(WEBSPHERE_URI_ATTRIBUTE) == null) {
            // Regular servlet container: behaves as expected in any case,
            // so the trailing slash is the result of a "/" url-pattern mapping.
            // Don't remove that slash.
            return false;
        }
        Boolean flagToUse = websphereComplianceFlag;
        if (flagToUse == null) {
            ClassLoader classLoader = UrlPathHelper.class.getClassLoader();
            String className = "com.ibm.ws.webcontainer.WebContainer";
            String methodName = "getWebContainerProperties";
            String propName = "com.ibm.ws.webcontainer.removetrailingservletpathslash";
            boolean flag = false;
            try {
                Class<?> cl = classLoader.loadClass(className);
                Properties prop = (Properties) cl.getMethod(methodName).invoke(null);
                flag = Boolean.parseBoolean(prop.getProperty(propName));
            }
            catch (Throwable ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not introspect WebSphere web container properties: " + ex);
                }
            }
            flagToUse = flag;
            websphereComplianceFlag = flag;
        }
        // Don't bother if WebSphere is configured to be fully Servlet compliant.
        // However, if it is not compliant, do remove the improper trailing slash!
        return !flagToUse;
    }

    public String getPathWithinApplication(HttpServletRequest request) {
        String contextPath = getContextPath(request);
        String requestUri = getRequestUri(request);
        String path = getRemainingPath(requestUri, contextPath, true);
        if (path != null) {
            // Normal case: URI contains context path.
            return (StringUtils.hasText(path) ? path : "/");
        }
        else {
            return requestUri;
        }
    }

    @Nullable
    private String getRemainingPath(String requestUri, String mapping, boolean ignoreCase) {
        int index1 = 0;
        int index2 = 0;
        for (; (index1 < requestUri.length()) && (index2 < mapping.length()); index1++, index2++) {
            char c1 = requestUri.charAt(index1);
            char c2 = mapping.charAt(index2);
            if (c1 == ';') {
                index1 = requestUri.indexOf('/', index1);
                if (index1 == -1) {
                    return null;
                }
                c1 = requestUri.charAt(index1);
            }
            if (c1 == c2 || (ignoreCase && (Character.toLowerCase(c1) == Character.toLowerCase(c2)))) {
                continue;
            }
            return null;
        }
        if (index2 != mapping.length()) {
            return null;
        }
        else if (index1 == requestUri.length()) {
            return "";
        }
        else if (requestUri.charAt(index1) == ';') {
            index1 = requestUri.indexOf('/', index1);
        }
        return (index1 != -1 ? requestUri.substring(index1) : "");
    }

    public String getRequestUri(HttpServletRequest request) {
        String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
        if (uri == null) {
            uri = request.getRequestURI();
        }
        return decodeAndCleanUriString(request, uri);
    }
    private String decodeAndCleanUriString(HttpServletRequest request, String uri) {
        uri = removeSemicolonContent(uri);
        uri = decodeRequestString(request, uri);
        uri = getSanitizedPath(uri);
        return uri;
    }

    private String getSanitizedPath(final String path) {
        String sanitized = path;
        while (true) {
            int index = sanitized.indexOf("//");
            if (index < 0) {
                break;
            }
            else {
                sanitized = sanitized.substring(0, index) + sanitized.substring(index + 1);
            }
        }
        return sanitized;
    }

    public String removeSemicolonContent(String requestUri) {
        return (this.removeSemicolonContent ?
                removeSemicolonContentInternal(requestUri) : removeJsessionid(requestUri));
    }

    private String removeSemicolonContentInternal(String requestUri) {
        int semicolonIndex = requestUri.indexOf(';');
        while (semicolonIndex != -1) {
            int slashIndex = requestUri.indexOf('/', semicolonIndex);
            String start = requestUri.substring(0, semicolonIndex);
            requestUri = (slashIndex != -1) ? start + requestUri.substring(slashIndex) : start;
            semicolonIndex = requestUri.indexOf(';', semicolonIndex);
        }
        return requestUri;
    }

    private String removeJsessionid(String requestUri) {
        int startIndex = requestUri.toLowerCase().indexOf(";jsessionid=");
        if (startIndex != -1) {
            int endIndex = requestUri.indexOf(';', startIndex + 12);
            String start = requestUri.substring(0, startIndex);
            requestUri = (endIndex != -1) ? start + requestUri.substring(endIndex) : start;
        }
        return requestUri;
    }


    public String getContextPath(HttpServletRequest request) {
        String contextPath = (String) request.getAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
        if (contextPath == null) {
            contextPath = request.getContextPath();
        }
        if ("/".equals(contextPath)) {
            // Invalid case, but happens for includes on Jetty: silently adapt it.
            contextPath = "";
        }
        return decodeRequestString(request, contextPath);
    }

    public String decodeRequestString(HttpServletRequest request, String source) {
        if (this.urlDecode) {
            return decodeInternal(request, source);
        }
        return source;
    }

    @SuppressWarnings("deprecation")
    private String decodeInternal(HttpServletRequest request, String source) {
        String enc = determineEncoding(request);
        try {
            return StringUtils.uriDecode(source, Charset.forName(enc));
        }
        catch (UnsupportedCharsetException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not decode request string [" + source + "] with encoding '" + enc +
                        "': falling back to platform default encoding; exception message: " + ex.getMessage());
            }
            return URLDecoder.decode(source);
        }
    }

    protected String determineEncoding(HttpServletRequest request) {
        String enc = request.getCharacterEncoding();
        if (enc == null) {
            enc = getDefaultEncoding();
        }
        return enc;
    }

    protected String getDefaultEncoding() {
        return this.defaultEncoding;
    }
}
