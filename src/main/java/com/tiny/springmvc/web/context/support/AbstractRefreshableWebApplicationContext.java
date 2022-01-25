package com.tiny.springmvc.web.context.support;

import com.tiny.springmvc.web.context.ConfigurableWebApplicationContext;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;

import javax.servlet.ServletContext;

public abstract class AbstractRefreshableWebApplicationContext extends AbstractRefreshableConfigApplicationContext
    implements ConfigurableWebApplicationContext, ThemeSource {

    private ServletContext servletContext;

    public AbstractRefreshableWebApplicationContext() {
        setDisplayName("Root WebApplicationContext");
    }

    @Override
    public void setServletContext(@Nullable ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    @Nullable
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    @Nullable
    public Theme getTheme(String themeName) {
        return null;
    }
}
