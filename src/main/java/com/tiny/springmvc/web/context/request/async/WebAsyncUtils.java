package com.tiny.springmvc.web.context.request.async;

import javax.servlet.ServletRequest;

public class WebAsyncUtils {
    public static final String WEB_ASYNC_MANAGER_ATTRIBUTE =
            WebAsyncManager.class.getName() + ".WEB_ASYNC_MANAGER";


    /**
     * Obtain the {@link WebAsyncManager} for the current request, or if not
     * found, create and associate it with the request.
     */
    public static WebAsyncManager getAsyncManager(ServletRequest servletRequest) {
        WebAsyncManager asyncManager = null;
        Object asyncManagerAttr = servletRequest.getAttribute(WEB_ASYNC_MANAGER_ATTRIBUTE);
        if (asyncManagerAttr instanceof WebAsyncManager) {
            asyncManager = (WebAsyncManager) asyncManagerAttr;
        }
        if (asyncManager == null) {
            asyncManager = new WebAsyncManager();
            servletRequest.setAttribute(WEB_ASYNC_MANAGER_ATTRIBUTE, asyncManager);
        }
        return asyncManager;
    }
}
