package com.tiny.springmvc.web.servlet.mvc.method.annotation;

import com.tiny.springmvc.http.converter.HttpMessageConverter;
import com.tiny.springmvc.http.converter.json.MappingJackson2HttpMessageConverter;
import com.tiny.springmvc.web.accept.ContentNegotiationManager;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import com.tiny.springmvc.web.context.request.ServletWebRequest;
import com.tiny.springmvc.web.method.HandlerMethod;
import com.tiny.springmvc.web.method.annotation.ModelMethodProcessor;
import com.tiny.springmvc.web.method.annotation.RequestParamMethodArgumentResolver;
import com.tiny.springmvc.web.method.support.*;
import com.tiny.springmvc.web.servlet.ModelAndView;
import com.tiny.springmvc.web.servlet.View;
import com.tiny.springmvc.web.servlet.mvc.annotation.ViewNameMethodReturnValueHandler;
import com.tiny.springmvc.web.servlet.mvc.method.AbstractHandlerMethodAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter implements BeanFactoryAware, InitializingBean {

    @Nullable
    private ConfigurableBeanFactory beanFactory;

    @Nullable
    private HandlerMethodArgumentResolverComposite argumentResolvers;

    @Nullable
    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    /*@Nullable
    private List<ModelAndViewResolver> modelAndViewResolvers;*/

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    private List<HttpMessageConverter<?>> messageConverters;

    public RequestMappingHandlerAdapter() {
        this.messageConverters = new ArrayList<>(4);
        //this.messageConverters.add(new StringHttpMessageConverter());
        this.messageConverters.add(new MappingJackson2HttpMessageConverter());
        //this.messageConverters.add(new GsonHttpMessageConverter());
    }

    @Override
    protected boolean supportsInternal(HandlerMethod handlerMethod) {
        return true;
    }

    @Override
    protected ModelAndView handleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        ModelAndView mav = invokeHandlerMethod(request, response, handlerMethod);
        return mav;
    }

    @Nullable
    protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
                                               HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        try {

            //WebDataBinderFactory binderFactory = null;

            ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
            if (this.argumentResolvers != null) {
                invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
            }
            if (this.returnValueHandlers != null) {
                invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
            }
            //invocableMethod.setDataBinderFactory(binderFactory);
            invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

            ModelAndViewContainer mavContainer = new ModelAndViewContainer();
            //mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

            invocableMethod.invokeAndHandle(webRequest, mavContainer);

            return getModelAndView(mavContainer, webRequest);
        }
        finally {
            webRequest.requestCompleted();
        }
    }

    protected ServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
        return new ServletInvocableHandlerMethod(handlerMethod);
    }

    @Nullable
    private ModelAndView getModelAndView(ModelAndViewContainer mavContainer,
                                         NativeWebRequest webRequest) throws Exception {

        if (mavContainer.isRequestHandled()) {
            return null;
        }
        ModelMap model = mavContainer.getModel();
        ModelAndView mav = new ModelAndView(mavContainer.getViewName(), model, mavContainer.getStatus());
        if (!mavContainer.isViewReference()) {
            mav.setView((View) mavContainer.getView());
        }
        return mav;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    @Nullable
    protected ConfigurableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.argumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
            this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }
        /*if (this.initBinderArgumentResolvers == null) {
            List<HandlerMethodArgumentResolver> resolvers = getDefaultInitBinderArgumentResolvers();
            this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
        }*/
        if (this.returnValueHandlers == null) {
            List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
            this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
        }
    }

    private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        // Annotation-based argument resolution
        resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));

        // Type-based argument resolution
        resolvers.add(new ModelMethodProcessor());

        return resolvers;
    }

    private List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
        List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

        // Annotation-based return value types
        handlers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(),
                this.contentNegotiationManager,null));


        // Multi-purpose return value types
        handlers.add(new ViewNameMethodReturnValueHandler());
        return handlers;
    }

    public List<HttpMessageConverter<?>> getMessageConverters() {
        return this.messageConverters;
    }
}
