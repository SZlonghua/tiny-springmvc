package com.tiny.springmvc.web.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class HttpServletBean extends HttpServlet implements EnvironmentCapable, EnvironmentAware {

    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "ConfigurableEnvironment required");
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public Environment getEnvironment() {
        /*if (this.environment == null) {
            this.environment = createEnvironment();
        }*/
        return this.environment;
    }

    /*protected ConfigurableEnvironment createEnvironment() {
        return new StandardServletEnvironment();
    }*/


    @Override
    public final void init() throws ServletException {

        String contextConfigLocation = getServletConfig().getInitParameter("contextConfigLocation");

        if(this instanceof FrameworkServlet){
            ((FrameworkServlet)this).setContextConfigLocation(contextConfigLocation);
        }
        // Let subclasses do whatever initialization they like.
        initServletBean();
    }

    protected void initServletBean() throws ServletException {
    }
}
