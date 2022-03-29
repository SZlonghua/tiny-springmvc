package com.tiny.springmvc.web.servlet.view;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;

import java.util.Locale;

public abstract class AbstractUrlBasedView  extends AbstractView implements InitializingBean {


    @Nullable
    private String url;


    /**
     * Constructor for use as a bean.
     */
    protected AbstractUrlBasedView() {
    }

    /**
     * Create a new AbstractUrlBasedView with the given URL.
     * @param url the URL to forward to
     */
    protected AbstractUrlBasedView(String url) {
        this.url = url;
    }



    public void setUrl(@Nullable String url) {
        this.url = url;
    }

    /**
     * Return the URL of the resource that this view wraps.
     */
    @Nullable
    public String getUrl() {
        return this.url;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (isUrlRequired() && getUrl() == null) {
            throw new IllegalArgumentException("Property 'url' is required");
        }
    }

    protected boolean isUrlRequired() {
        return true;
    }


    public boolean checkResource(Locale locale) throws Exception {
        return true;
    }
}
