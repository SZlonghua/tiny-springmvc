package com.tiny.springmvc.web.context.support;

import com.tiny.springmvc.web.context.ConfigurableWebApplicationContext;
import com.tiny.springmvc.web.context.ServletContextAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public abstract class AbstractRefreshableWebApplicationContext extends AbstractRefreshableConfigApplicationContext
    implements ConfigurableWebApplicationContext, ThemeSource {

    private ServletContext servletContext;

    @Nullable
    private ServletConfig servletConfig;

    /** Namespace of this context, or {@code null} if root. */
    @Nullable
    private String namespace;

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
    public void setServletConfig(@Nullable ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        if (servletConfig != null && this.servletContext == null) {
            setServletContext(servletConfig.getServletContext());
        }
    }

    @Override
    @Nullable
    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    @Override
    public void setNamespace(@Nullable String namespace) {
        this.namespace = namespace;
        if (namespace != null) {
            setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
        }
    }

    @Override
    @Nullable
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public String[] getConfigLocations() {
        return super.getConfigLocations();
    }



    @Override
    @Nullable
    public Theme getTheme(String themeName) {
        return null;
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));
        beanFactory.ignoreDependencyInterface(ServletContextAware.class);
        /*beanFactory.ignoreDependencyInterface(ServletConfigAware.class);

        WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
        WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext, this.servletConfig);*/
    }
}
