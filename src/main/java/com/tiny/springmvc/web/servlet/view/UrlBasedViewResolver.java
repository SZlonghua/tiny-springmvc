package com.tiny.springmvc.web.servlet.view;

import com.tiny.springmvc.web.servlet.View;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Locale;

public class UrlBasedViewResolver extends AbstractCachingViewResolver {

    @Nullable
    private Class<?> viewClass;

    private String prefix = "";

    private String suffix = "";

    @Nullable
    private String contentType;


    @Override
    protected View loadView(String viewName, Locale locale) throws Exception {
        AbstractUrlBasedView view = buildView(viewName);
        View result = applyLifecycleMethods(viewName, view);
        return (view.checkResource(locale) ? result : null);
    }

    protected View applyLifecycleMethods(String viewName, AbstractUrlBasedView view) {
        ApplicationContext context = getApplicationContext();
        if (context != null) {
            Object initialized = context.getAutowireCapableBeanFactory().initializeBean(view, viewName);
            if (initialized instanceof View) {
                return (View) initialized;
            }
        }
        return view;
    }

    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        Class<?> viewClass = getViewClass();
        Assert.state(viewClass != null, "No view class");

        AbstractUrlBasedView view = (AbstractUrlBasedView) BeanUtils.instantiateClass(viewClass);
        view.setUrl(getPrefix() + viewName + getSuffix());

        String contentType = getContentType();
        if (contentType != null) {
            view.setContentType(contentType);
        }

        return view;
    }


    public void setViewClass(@Nullable Class<?> viewClass) {
        this.viewClass = viewClass;
    }

    /**
     * Return the view class to be used to create views.
     */
    @Nullable
    protected Class<?> getViewClass() {
        return this.viewClass;
    }


    public void setPrefix(@Nullable String prefix) {
        this.prefix = (prefix != null ? prefix : "");
    }

    /**
     * Return the prefix that gets prepended to view names when building a URL.
     */
    protected String getPrefix() {
        return this.prefix;
    }

    /**
     * Set the suffix that gets appended to view names when building a URL.
     */
    public void setSuffix(@Nullable String suffix) {
        this.suffix = (suffix != null ? suffix : "");
    }

    /**
     * Return the suffix that gets appended to view names when building a URL.
     */
    protected String getSuffix() {
        return this.suffix;
    }

    public void setContentType(@Nullable String contentType) {
        this.contentType = contentType;
    }

    /**
     * Return the content type for all views, if any.
     */
    @Nullable
    protected String getContentType() {
        return this.contentType;
    }
}
