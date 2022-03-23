package com.tiny.springmvc.web.servlet;

import com.tiny.springmvc.web.util.NestedServletException;
import com.tiny.springmvc.web.util.WebUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.ThemeSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class DispatcherServlet extends FrameworkServlet {

    public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

    public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";

    public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";

    public static final String THEME_SOURCE_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_SOURCE";

    public static final String INPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".INPUT_FLASH_MAP";

    /*@Nullable
    private MultipartResolver multipartResolver;*/

    /** LocaleResolver used by this servlet. */
    @Nullable
    private LocaleResolver localeResolver;

    /** ThemeResolver used by this servlet. */
    @Nullable
    private ThemeResolver themeResolver;

    @Nullable
    private List<HandlerMapping> handlerMappings;

    @Nullable
    private List<HandlerAdapter> handlerAdapters;

    @Nullable
    private List<HandlerExceptionResolver> handlerExceptionResolvers;

    @Nullable
    private RequestToViewNameTranslator viewNameTranslator;

    /*@Nullable
    private FlashMapManager flashMapManager;*/

    @Nullable
    private List<ViewResolver> viewResolvers;

    private static final Properties defaultStrategies;

    public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

    public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

    public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

    public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

    public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

    public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";

    public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";



    private boolean detectAllHandlerMappings = true;
    private boolean detectAllHandlerAdapters = true;
    private boolean detectAllHandlerExceptionResolvers = true;
    private boolean detectAllViewResolvers = true;
    private boolean throwExceptionIfNoHandlerFound = false;
    private boolean cleanupAfterInclude = true;



    private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";

    static {
        // Load default strategy implementations from properties file.
        // This is currently strictly internal and not meant to be customized
        // by application developers.
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Could not load '" + DEFAULT_STRATEGIES_PATH + "': " + ex.getMessage());
        }
    }

    @Override
    protected void onRefresh(ApplicationContext context) {
        initStrategies(context);
    }

    @Override
    protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Make framework objects available to handlers and view objects.
        request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
        //request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
        //request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
        request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());

        doDispatch(request, response);
    }

    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpServletRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;


        try {
            ModelAndView mv = null;
            Exception dispatchException = null;

            try {

                // Determine handler for the current request.
                mappedHandler = getHandler(processedRequest);
                if (mappedHandler == null) {
                    noHandlerFound(processedRequest, response);
                    return;
                }

                // Determine handler adapter for the current request.
                HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

                if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                    return;
                }

                // Actually invoke the handler.
                mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

                applyDefaultViewName(processedRequest, mv);
                mappedHandler.applyPostHandle(processedRequest, response, mv);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                dispatchException = ex;
            }
            catch (Throwable err) {
                // As of 4.3, we're processing Errors thrown from handler methods as well,
                // making them available for @ExceptionHandler methods and other scenarios.
                dispatchException = new NestedServletException("Handler dispatch failed", err);
            }
            processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
        }
        catch (Exception ex) {
            triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
        }
        catch (Throwable err) {
            triggerAfterCompletion(processedRequest, response, mappedHandler,
                    new NestedServletException("Handler processing failed", err));
        }

    }

    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
                                       @Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
                                       @Nullable Exception exception) throws Exception {

        boolean errorView = false;

        /*if (exception != null) {
            if (exception instanceof ModelAndViewDefiningException) {
                logger.debug("ModelAndViewDefiningException encountered", exception);
                mv = ((ModelAndViewDefiningException) exception).getModelAndView();
            }
            else {
                Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
                mv = processHandlerException(request, response, handler, exception);
                errorView = (mv != null);
            }
        }*/

        // Did the handler return a view to render?
        if (mv != null && !mv.wasCleared()) {
            render(mv, request, response);
            if (errorView) {
                WebUtils.clearErrorRequestAttributes(request);
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("No view rendering, null ModelAndView returned.");
            }
        }


        if (mappedHandler != null) {
            // Exception (if any) is already handled..
            mappedHandler.triggerAfterCompletion(request, response, null);
        }
    }

    protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Determine locale for request and apply it to the response.
        Locale locale =
                (this.localeResolver != null ? this.localeResolver.resolveLocale(request) : request.getLocale());
        response.setLocale(locale);

        View view;
        String viewName = mv.getViewName();
        if (viewName != null) {
            // We need to resolve the view name.
            view = resolveViewName(viewName, mv.getModelInternal(), locale, request);
            if (view == null) {
                throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
                        "' in servlet with name '" + getServletName() + "'");
            }
        }
        else {
            // No need to lookup: the ModelAndView object contains the actual View object.
            view = mv.getView();
            if (view == null) {
                throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
                        "View object in servlet with name '" + getServletName() + "'");
            }
        }

        // Delegate to the View object for rendering.
        if (logger.isTraceEnabled()) {
            logger.trace("Rendering view [" + view + "] ");
        }
        try {
            if (mv.getStatus() != null) {
                response.setStatus(mv.getStatus().value());
            }
            view.render(mv.getModelInternal(), request, response);
        }
        catch (Exception ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error rendering view [" + view + "]", ex);
            }
            throw ex;
        }
    }

    @Nullable
    protected View resolveViewName(String viewName, @Nullable Map<String, Object> model,
                                   Locale locale, HttpServletRequest request) throws Exception {

        if (this.viewResolvers != null) {
            for (ViewResolver viewResolver : this.viewResolvers) {
                View view = viewResolver.resolveViewName(viewName, locale);
                if (view != null) {
                    return view;
                }
            }
        }
        return null;
    }

    private void applyDefaultViewName(HttpServletRequest request, @Nullable ModelAndView mv) throws Exception {
        if (mv != null && !mv.hasView()) {
            String defaultViewName = getDefaultViewName(request);
            if (defaultViewName != null) {
                mv.setViewName(defaultViewName);
            }
        }
    }

    @Nullable
    protected String getDefaultViewName(HttpServletRequest request) throws Exception {
        return (this.viewNameTranslator != null ? this.viewNameTranslator.getViewName(request) : null);
    }

    private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
                                        @Nullable HandlerExecutionChain mappedHandler, Exception ex) throws Exception {

        if (mappedHandler != null) {
            mappedHandler.triggerAfterCompletion(request, response, ex);
        }
        throw ex;
    }

    protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        if (this.handlerAdapters != null) {
            for (HandlerAdapter adapter : this.handlerAdapters) {
                if (adapter.supports(handler)) {
                    return adapter;
                }
            }
        }
        throw new ServletException("No adapter for handler [" + handler +
                "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
    }

    protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Nullable
    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        if (this.handlerMappings != null) {
            for (HandlerMapping mapping : this.handlerMappings) {
                HandlerExecutionChain handler = mapping.getHandler(request);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }

    @Nullable
    public final ThemeSource getThemeSource() {
        return (getWebApplicationContext() instanceof ThemeSource ? (ThemeSource) getWebApplicationContext() : null);
    }



    protected void initStrategies(ApplicationContext context) {
        //initMultipartResolver(context);
        initLocaleResolver(context);
        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
        //initFlashMapManager(context);
    }

    private void initViewResolvers(ApplicationContext context) {
        this.viewResolvers = null;

        if (this.detectAllViewResolvers) {
            // Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, ViewResolver> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.viewResolvers = new ArrayList<>(matchingBeans.values());
                // We keep ViewResolvers in sorted order.
                AnnotationAwareOrderComparator.sort(this.viewResolvers);
            }
        }
        else {
            try {
                ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
                this.viewResolvers = Collections.singletonList(vr);
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default ViewResolver later.
            }
        }

        // Ensure we have at least one ViewResolver, by registering
        // a default ViewResolver if no other resolvers are found.
        if (this.viewResolvers == null) {
            this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
            if (logger.isTraceEnabled()) {
                logger.trace("No ViewResolvers declared for servlet '" + getServletName() +
                        "': using default strategies from DispatcherServlet.properties");
            }
        }
    }

    private void initRequestToViewNameTranslator(ApplicationContext context) {
        try {
            this.viewNameTranslator =
                    context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator.class);
            if (logger.isTraceEnabled()) {
                logger.trace("Detected " + this.viewNameTranslator.getClass().getSimpleName());
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("Detected " + this.viewNameTranslator);
            }
        }
        catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
            if (logger.isTraceEnabled()) {
                logger.trace("No RequestToViewNameTranslator '" + REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME +
                        "': using default [" + this.viewNameTranslator.getClass().getSimpleName() + "]");
            }
        }
    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {
        this.handlerExceptionResolvers = null;

        if (this.detectAllHandlerExceptionResolvers) {
            // Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
                // We keep HandlerExceptionResolvers in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
            }
        }
        else {
            try {
                HandlerExceptionResolver her =
                        context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
                this.handlerExceptionResolvers = Collections.singletonList(her);
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Ignore, no HandlerExceptionResolver is fine too.
            }
        }

        // Ensure we have at least some HandlerExceptionResolvers, by registering
        // default HandlerExceptionResolvers if no other resolvers are found.
        if (this.handlerExceptionResolvers == null) {
            this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
            if (logger.isTraceEnabled()) {
                logger.trace("No HandlerExceptionResolvers declared in servlet '" + getServletName() +
                        "': using default strategies from DispatcherServlet.properties");
            }
        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        this.handlerAdapters = null;

        if (this.detectAllHandlerAdapters) {
            // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerAdapter> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerAdapters = new ArrayList<>(matchingBeans.values());
                // We keep HandlerAdapters in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerAdapters);
            }
        }
        else {
            try {
                HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
                this.handlerAdapters = Collections.singletonList(ha);
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerAdapter later.
            }
        }

        // Ensure we have at least some HandlerAdapters, by registering
        // default HandlerAdapters if no other adapters are found.
        if (this.handlerAdapters == null) {
            this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
            if (logger.isTraceEnabled()) {
                logger.trace("No HandlerAdapters declared for servlet '" + getServletName() +
                        "': using default strategies from DispatcherServlet.properties");
            }
        }
    }

    private void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = null;

        if (this.detectAllHandlerMappings) {
            // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerMapping> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<>(matchingBeans.values());
                // We keep HandlerMappings in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerMappings);
            }
        }
        else {
            try {
                HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
                this.handlerMappings = Collections.singletonList(hm);
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Ignore, we'll add a default HandlerMapping later.
            }
        }

        // Ensure we have at least one HandlerMapping, by registering
        // a default HandlerMapping if no other mappings are found.
        if (this.handlerMappings == null) {
            this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
            if (logger.isTraceEnabled()) {
                logger.trace("No HandlerMappings declared for servlet '" + getServletName() +
                        "': using default strategies from DispatcherServlet.properties");
            }
        }
    }

    private void initThemeResolver(ApplicationContext context) {
        try {
            this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
        }
        catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
            if (logger.isTraceEnabled()) {
                logger.trace("No ThemeResolver '" + THEME_RESOLVER_BEAN_NAME +
                        "': using default [" + this.themeResolver.getClass().getSimpleName() + "]");
            }
        }
    }

    private void initLocaleResolver(ApplicationContext context) {
        try {
            this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
        }
        catch (NoSuchBeanDefinitionException ex) {
            // We need to use the default.
            this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
            if (logger.isTraceEnabled()) {
                logger.trace("No LocaleResolver '" + LOCALE_RESOLVER_BEAN_NAME +
                        "': using default [" + this.localeResolver.getClass().getSimpleName() + "]");
            }
        }
    }

    protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
        List<T> strategies = getDefaultStrategies(context, strategyInterface);
        if (strategies.size() != 1) {
            throw new BeanInitializationException(
                    "DispatcherServlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
        }
        return strategies.get(0);
    }

    protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
        String key = strategyInterface.getName();
        String value = defaultStrategies.getProperty(key);
        if (value != null) {
            String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
            List<T> strategies = new ArrayList<>(classNames.length);
            for (String className : classNames) {
                try {
                    Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
                    Object strategy = createDefaultStrategy(context, clazz);
                    strategies.add((T) strategy);
                }
                catch (ClassNotFoundException ex) {
                    throw new BeanInitializationException(
                            "Could not find DispatcherServlet's default strategy class [" + className +
                                    "] for interface [" + key + "]", ex);
                }
                catch (LinkageError err) {
                    throw new BeanInitializationException(
                            "Unresolvable class definition for DispatcherServlet's default strategy class [" +
                                    className + "] for interface [" + key + "]", err);
                }
            }
            return strategies;
        }
        else {
            return new LinkedList<>();
        }
    }

    protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
        return context.getAutowireCapableBeanFactory().createBean(clazz);
    }
}
