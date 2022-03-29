package com.tiny.springmvc.web.servlet.view;

import com.tiny.springmvc.web.context.support.WebApplicationObjectSupport;
import com.tiny.springmvc.web.servlet.View;
import com.tiny.springmvc.web.servlet.ViewResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

    protected final Log logger = LogFactory.getLog(getClass());

    public static final int DEFAULT_CACHE_LIMIT = 1024;

    private static final View UNRESOLVED_VIEW = new View() {
        @Override
        @Nullable
        public String getContentType() {
            return null;
        }
        @Override
        public void render(@Nullable Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
        }
    };

    private final Map<Object, View> viewAccessCache = new ConcurrentHashMap<>(DEFAULT_CACHE_LIMIT);

    private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

    private final Map<Object, View> viewCreationCache =
            new LinkedHashMap<Object, View>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Object, View> eldest) {
                    if (size() > getCacheLimit()) {
                        viewAccessCache.remove(eldest.getKey());
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            };

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        Object cacheKey = viewName;
        View view = this.viewAccessCache.get(cacheKey);
        if (view == null) {
            synchronized (this.viewCreationCache) {
                view = this.viewCreationCache.get(cacheKey);
                if (view == null) {
                    // Ask the subclass to create the View object.
                    view = createView(viewName, locale);
                    if (view == null) {
                        view = UNRESOLVED_VIEW;
                    }
                    if (view != null) {
                        this.viewAccessCache.put(cacheKey, view);
                        this.viewCreationCache.put(cacheKey, view);
                    }
                }
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace(cacheKey + "served from cache");
            }
        }
        return (view != UNRESOLVED_VIEW ? view : null);
    }

    @Nullable
    protected View createView(String viewName, Locale locale) throws Exception {
        return loadView(viewName, locale);
    }

    @Nullable
    protected abstract View loadView(String viewName, Locale locale) throws Exception;

    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
    }

    /**
     * Return the maximum number of entries for the view cache.
     */
    public int getCacheLimit() {
        return this.cacheLimit;
    }
}
