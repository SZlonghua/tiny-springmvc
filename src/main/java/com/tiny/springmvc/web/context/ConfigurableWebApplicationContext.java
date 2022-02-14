package com.tiny.springmvc.web.context;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public interface ConfigurableWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

    String APPLICATION_CONTEXT_ID_PREFIX = WebApplicationContext.class.getName() + ":";

    void setServletContext(@Nullable ServletContext servletContext);

    void setConfigLocation(String configLocation);


    /**
     * Set the ServletConfig for this web application context.
     * Only called for a WebApplicationContext that belongs to a specific Servlet.
     * @see #refresh()
     */
    void setServletConfig(@Nullable ServletConfig servletConfig);

    /**
     * Return the ServletConfig for this web application context, if any.
     */
    @Nullable
    ServletConfig getServletConfig();

    /**
     * Set the namespace for this web application context,
     * to be used for building a default context config location.
     * The root web application context does not have a namespace.
     */
    void setNamespace(@Nullable String namespace);

    /**
     * Return the namespace for this web application context, if any.
     */
    @Nullable
    String getNamespace();


    /**
     * Set the config locations for this web application context.
     * <p>If not set, the implementation is supposed to use a default for the
     * given namespace or the root web application context, as appropriate.
     */
    void setConfigLocations(String... configLocations);

    /**
     * Return the config locations for this web application context,
     * or {@code null} if none specified.
     */
    @Nullable
    String[] getConfigLocations();

}
