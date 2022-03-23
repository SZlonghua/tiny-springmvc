package com.tiny.springmvc.web.context.support;

import com.tiny.springmvc.web.context.ServletContextAware;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.lang.Nullable;

import javax.servlet.ServletContext;

public class WebApplicationObjectSupport extends ApplicationObjectSupport implements ServletContextAware {

    @Nullable
    private ServletContext servletContext;


    @Override
    public final void setServletContext(ServletContext servletContext) {
        if (servletContext != this.servletContext) {
            this.servletContext = servletContext;
            initServletContext(servletContext);
        }
    }

    protected void initServletContext(ServletContext servletContext) {
    }
}
