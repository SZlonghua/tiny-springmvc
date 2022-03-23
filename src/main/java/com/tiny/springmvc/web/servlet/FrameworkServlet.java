package com.tiny.springmvc.web.servlet;

import com.tiny.springmvc.web.context.ConfigurableWebApplicationContext;
import com.tiny.springmvc.web.context.WebApplicationContext;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import com.tiny.springmvc.web.context.request.RequestAttributes;
import com.tiny.springmvc.web.context.request.RequestContextHolder;
import com.tiny.springmvc.web.context.request.ServletRequestAttributes;
import com.tiny.springmvc.web.context.request.async.CallableProcessingInterceptor;
import com.tiny.springmvc.web.context.request.async.WebAsyncManager;
import com.tiny.springmvc.web.context.request.async.WebAsyncUtils;
import com.tiny.springmvc.web.context.support.ServletRequestHandledEvent;
import com.tiny.springmvc.web.context.support.XmlWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.Callable;

public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware {

    @Nullable
    private WebApplicationContext webApplicationContext;

    /** If the WebApplicationContext was injected via {@link #setApplicationContext}. */
    private boolean webApplicationContextInjected = false;

    @Nullable
    private String contextConfigLocation;

    @Nullable
    private String namespace;

    public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";


    /** Flag used to detect whether onRefresh has already been called. */
    private volatile boolean refreshEventReceived = false;

    /** Monitor for synchronized onRefresh execution. */
    private final Object onRefreshMonitor = new Object();

    private boolean publishContext = true;

    private boolean publishEvents = true;

    public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (this.webApplicationContext == null && applicationContext instanceof WebApplicationContext) {
            this.webApplicationContext = (WebApplicationContext) applicationContext;
            this.webApplicationContextInjected = true;
        }
    }
    public void setContextConfigLocation(@Nullable String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /**
     * Return the explicit context config location, if any.
     */
    @Nullable
    public String getContextConfigLocation() {
        return this.contextConfigLocation;
    }

    @Override
    protected void initServletBean() throws ServletException {
        getServletContext().log("Initializing Spring " + getClass().getSimpleName() + " '" + getServletName() + "'");
        if (logger.isInfoEnabled()) {
            logger.info("Initializing Servlet '" + getServletName() + "'");
        }
        long startTime = System.currentTimeMillis();

        try {
            this.webApplicationContext = initWebApplicationContext();
            initFrameworkServlet();
        }
        catch (ServletException | RuntimeException ex) {
            logger.error("Context initialization failed", ex);
            throw ex;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Completed initialization in " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    protected void initFrameworkServlet() throws ServletException {
    }

    protected WebApplicationContext initWebApplicationContext() {
        WebApplicationContext rootContext =
                (WebApplicationContext)getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        WebApplicationContext wac = null;

        if (this.webApplicationContext != null) {
            // A context instance was injected at construction time -> use it
            wac = this.webApplicationContext;
            if (wac instanceof ConfigurableWebApplicationContext) {
                ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
                if (!cwac.isActive()) {
                    // The context has not yet been refreshed -> provide services such as
                    // setting the parent context, setting the application context id, etc
                    if (cwac.getParent() == null) {
                        // The context instance was injected without an explicit parent -> set
                        // the root application context (if any; may be null) as the parent
                        cwac.setParent(rootContext);
                    }
                    configureAndRefreshWebApplicationContext(cwac);
                }
            }
        }

        if (wac == null) {
            // No context instance is defined for this servlet -> create a local one
            wac = createWebApplicationContext(rootContext);
        }

        if (!this.refreshEventReceived) {
            // Either the context is not a ConfigurableApplicationContext with refresh
            // support or the context injected at construction time had already been
            // refreshed -> trigger initial onRefresh manually here.
            synchronized (this.onRefreshMonitor) {
                onRefresh(wac);
            }
        }

        if (this.publishContext) {
            // Publish the context as a servlet context attribute.
            String attrName = getServletContextAttributeName();
            getServletContext().setAttribute(attrName, wac);
        }

        return wac;
    }

    public String getServletContextAttributeName() {
        return SERVLET_CONTEXT_PREFIX + getServletName();
    }

    protected WebApplicationContext createWebApplicationContext(@Nullable WebApplicationContext parent) {

        ConfigurableWebApplicationContext wac = new XmlWebApplicationContext();
        wac.setParent(parent);
        String configLocation = getContextConfigLocation();
        if (configLocation != null) {
            wac.setConfigLocation(configLocation);
        }
        configureAndRefreshWebApplicationContext(wac);
        return wac;
    }

    protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {

        // Generate default id...
        wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
                ObjectUtils.getDisplayString(getServletContext().getContextPath()) + '/' + getServletName());

        wac.setServletContext(getServletContext());
        wac.setServletConfig(getServletConfig());
        wac.setNamespace(getNamespace());
        wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

        // The wac environment's #initPropertySources will be called in any case when the context
        // is refreshed; do it eagerly here to ensure servlet property sources are in place for
        // use in any post-processing or initialization that occurs below prior to #refresh
        /*ConfigurableEnvironment env = wac.getEnvironment();
        if (env instanceof ConfigurableWebEnvironment) {
            ((ConfigurableWebEnvironment) env).initPropertySources(getServletContext(), getServletConfig());
        }*/

        wac.refresh();
    }

    public String getNamespace() {
        return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.refreshEventReceived = true;
        synchronized (this.onRefreshMonitor) {
            onRefresh(event.getApplicationContext());
        }
    }

    protected void onRefresh(ApplicationContext context) {
        // For subclasses: do nothing by default.
    }


    private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

        @Override
        public void onApplicationEvent(ContextRefreshedEvent event) {
            FrameworkServlet.this.onApplicationEvent(event);
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }


    protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        Throwable failureCause = null;

        LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
        LocaleContext localeContext = buildLocaleContext(request);

        RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());

        initContextHolders(request, localeContext, requestAttributes);

        try {
            doService(request, response);
        }
        catch (ServletException | IOException ex) {
            failureCause = ex;
            throw ex;
        }
        catch (Throwable ex) {
            failureCause = ex;
            throw new ServletException("Request processing failed");
        }

        finally {
            resetContextHolders(request, previousLocaleContext, previousAttributes);
            if (requestAttributes != null) {
                requestAttributes.requestCompleted();
            }
            publishRequestHandledEvent(request, response, startTime, failureCause);
        }
    }

    @Nullable
    public final WebApplicationContext getWebApplicationContext() {
        return this.webApplicationContext;
    }

    protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
            throws Exception;

    private void publishRequestHandledEvent(HttpServletRequest request, HttpServletResponse response,
                                            long startTime, @Nullable Throwable failureCause) {

        if (this.publishEvents && this.webApplicationContext != null) {
            // Whether or not we succeeded, publish an event.
            long processingTime = System.currentTimeMillis() - startTime;
            HttpSession session = request.getSession(false);
            Principal userPrincipal = request.getUserPrincipal();
            this.webApplicationContext.publishEvent(
                    new ServletRequestHandledEvent(this,
                            request.getRequestURI(), request.getRemoteAddr(),
                            request.getMethod(), getServletConfig().getServletName(),
                            session != null ? session.getId() : null,
                            userPrincipal != null ? userPrincipal.getName() : null,
                            processingTime, failureCause, response.getStatus()));
        }
    }

    private void resetContextHolders(HttpServletRequest request,
                                     @Nullable LocaleContext prevLocaleContext, @Nullable RequestAttributes previousAttributes) {

        LocaleContextHolder.setLocaleContext(prevLocaleContext, false);
        RequestContextHolder.setRequestAttributes(previousAttributes, false);
    }

    private void initContextHolders(HttpServletRequest request,
                                    @Nullable LocaleContext localeContext, @Nullable RequestAttributes requestAttributes) {

        if (localeContext != null) {
            LocaleContextHolder.setLocaleContext(localeContext, false);
        }
        if (requestAttributes != null) {
            RequestContextHolder.setRequestAttributes(requestAttributes, false);
        }
    }
    private class RequestBindingInterceptor implements CallableProcessingInterceptor {

        @Override
        public <T> void preProcess(NativeWebRequest webRequest, Callable<T> task) {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (request != null) {
                HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
                initContextHolders(request, buildLocaleContext(request),
                        buildRequestAttributes(request, response, null));
            }
        }
        @Override
        public <T> void postProcess(NativeWebRequest webRequest, Callable<T> task, Object concurrentResult) {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (request != null) {
                resetContextHolders(request, null, null);
            }
        }
    }

    @Nullable
    protected ServletRequestAttributes buildRequestAttributes(HttpServletRequest request,
                                                              @Nullable HttpServletResponse response, @Nullable RequestAttributes previousAttributes) {

        if (previousAttributes == null || previousAttributes instanceof ServletRequestAttributes) {
            return new ServletRequestAttributes(request, response);
        }
        else {
            return null;  // preserve the pre-bound RequestAttributes instance
        }
    }

    @Nullable
    protected LocaleContext buildLocaleContext(HttpServletRequest request) {
        return new SimpleLocaleContext(request.getLocale());
    }
}
