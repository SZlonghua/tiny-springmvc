package com.tiny.springmvc.web.servlet.handler;

import com.tiny.springmvc.web.method.HandlerMethod;
import com.tiny.springmvc.web.servlet.HandlerMapping;
import com.tiny.springmvc.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Nullable;
import org.springframework.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean {

    private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";

    private final MappingRegistry mappingRegistry = new MappingRegistry();

    private PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void afterPropertiesSet() throws Exception {
        initHandlerMethods();
    }

    protected void initHandlerMethods() {
        for (String beanName : getCandidateBeanNames()) {
            if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
                processCandidateBean(beanName);
            }
        }
    }



    protected void processCandidateBean(String beanName) {
        Class<?> beanType = null;
        try {
            beanType = obtainApplicationContext().getType(beanName);
        }
        catch (Throwable ex) {
            // An unresolvable bean type, probably from a lazy bean - let's ignore it.
            if (logger.isTraceEnabled()) {
                logger.trace("Could not resolve type for bean '" + beanName + "'", ex);
            }
        }
        if (beanType != null && isHandler(beanType)) {
            detectHandlerMethods(beanName);
        }
    }

    protected void detectHandlerMethods(Object handler) {
        Class<?> handlerType = (handler instanceof String ?
                obtainApplicationContext().getType((String) handler) : handler.getClass());

        if (handlerType != null) {
            Class<?> userType = ClassUtils.getUserClass(handlerType);
            Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
                    (MethodIntrospector.MetadataLookup<T>) method -> {
                        try {
                            return getMappingForMethod(method, userType);
                        }
                        catch (Throwable ex) {
                            throw new IllegalStateException("Invalid mapping on handler class [" +
                                    userType.getName() + "]: " + method, ex);
                        }
                    });

            methods.forEach((method, mapping) -> {
                Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                registerHandlerMethod(handler, invocableMethod, mapping);
            });
        }
    }

    protected void registerHandlerMethod(Object handler, Method method, T mapping) {
        this.mappingRegistry.register(mapping, handler, method);
    }

    @Nullable
    protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

    protected abstract boolean isHandler(Class<?> beanType);

    protected String[] getCandidateBeanNames() {
        return obtainApplicationContext().getBeanNamesForType(Object.class);
    }

    @Override
    protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
        String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
        request.setAttribute(LOOKUP_PATH, lookupPath);
        this.mappingRegistry.acquireReadLock();
        try {
            HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
            return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
        }
        finally {
            this.mappingRegistry.releaseReadLock();
        }
    }

    @Nullable
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        List<Match> matches = new ArrayList<>();
        List<T> directPathMatches = this.mappingRegistry.getMappingsByUrl(lookupPath);
        if (directPathMatches != null) {
            addMatchingMappings(directPathMatches, matches, request);
        }
        if (matches.isEmpty()) {
            // No choice but to go through all mappings...
            addMatchingMappings(this.mappingRegistry.getMappings().keySet(), matches, request);
        }

        if (!matches.isEmpty()) {
            Match bestMatch = matches.get(0);
            if (matches.size() > 1) {
                throw new RuntimeException("Many match,please check!");
            }
            request.setAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE, bestMatch.handlerMethod);
            handleMatch(bestMatch.mapping, lookupPath, request);
            return bestMatch.handlerMethod;
        }
        else {
            return handleNoMatch(this.mappingRegistry.getMappings().keySet(), lookupPath, request);
        }
    }

    protected void handleMatch(T mapping, String lookupPath, HttpServletRequest request) {
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
    }

    @Nullable
    protected HandlerMethod handleNoMatch(Set<T> mappings, String lookupPath, HttpServletRequest request)
            throws Exception {

        return null;
    }

    private void addMatchingMappings(Collection<T> mappings, List<Match> matches, HttpServletRequest request) {
        for (T mapping : mappings) {
            T match = getMatchingMapping(mapping, request);
            if (match != null) {
                matches.add(new Match(match, this.mappingRegistry.getMappings().get(mapping)));
            }
        }
    }

    @Nullable
    protected abstract T getMatchingMapping(T mapping, HttpServletRequest request);


    private class Match {

        private final T mapping;

        private final HandlerMethod handlerMethod;

        public Match(T mapping, HandlerMethod handlerMethod) {
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
        }

        @Override
        public String toString() {
            return this.mapping.toString();
        }
    }

    protected HandlerMethod createHandlerMethod(Object handler, Method method) {
        if (handler instanceof String) {
            return new HandlerMethod((String) handler,
                    obtainApplicationContext().getAutowireCapableBeanFactory(), method);
        }
        return new HandlerMethod(handler, method);
    }

    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    protected abstract Set<String> getMappingPathPatterns(T mapping);

    class MappingRegistry {

        private final Map<T, MappingRegistration<T>> registry = new HashMap<>();

        private final Map<T, HandlerMethod> mappingLookup = new LinkedHashMap<>();

        private final MultiValueMap<String, T> urlLookup = new LinkedMultiValueMap<>();

        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        /**
         * Return all mappings and handler methods. Not thread-safe.
         * @see #acquireReadLock()
         */
        public Map<T, HandlerMethod> getMappings() {
            return this.mappingLookup;
        }

        /**
         * Return matches for the given URL path. Not thread-safe.
         * @see #acquireReadLock()
         */
        @Nullable
        public List<T> getMappingsByUrl(String urlPath) {
            return this.urlLookup.get(urlPath);
        }

        /**
         * Acquire the read lock when using getMappings and getMappingsByUrl.
         */
        public void acquireReadLock() {
            this.readWriteLock.readLock().lock();
        }

        /**
         * Release the read lock after using getMappings and getMappingsByUrl.
         */
        public void releaseReadLock() {
            this.readWriteLock.readLock().unlock();
        }

        public void register(T mapping, Object handler, Method method) {

            this.readWriteLock.writeLock().lock();
            try {
                HandlerMethod handlerMethod = createHandlerMethod(handler, method);
                validateMethodMapping(handlerMethod, mapping);
                this.mappingLookup.put(mapping, handlerMethod);

                List<String> directUrls = getDirectUrls(mapping);
                for (String url : directUrls) {
                    this.urlLookup.add(url, mapping);
                }

                String name = handler.toString()+"#"+method.getName();
                if(mapping instanceof RequestMappingInfo){
                    RequestMappingInfo info = (RequestMappingInfo) mapping;
                    name = info.getName();
                }

                this.registry.put(mapping, new MappingRegistration<>(mapping, handlerMethod, directUrls, name));
            }
            finally {
                this.readWriteLock.writeLock().unlock();
            }
        }

        private void validateMethodMapping(HandlerMethod handlerMethod, T mapping) {
            // Assert that the supplied mapping is unique.
            HandlerMethod existingHandlerMethod = this.mappingLookup.get(mapping);
            if (existingHandlerMethod != null && !existingHandlerMethod.equals(handlerMethod)) {
                throw new IllegalStateException(
                        "Ambiguous mapping. Cannot map '" + handlerMethod.getBean() + "' method \n" +
                                handlerMethod + "\nto " + mapping + ": There is already '" +
                                existingHandlerMethod.getBean() + "' bean method\n" + existingHandlerMethod + " mapped.");
            }
        }

        private List<String> getDirectUrls(T mapping) {
            List<String> urls = new ArrayList<>(1);
            for (String path : getMappingPathPatterns(mapping)) {
                if (!getPathMatcher().isPattern(path)) {
                    urls.add(path);
                }
            }
            return urls;
        }

        public void unregister(T mapping) {
            this.readWriteLock.writeLock().lock();
            try {
                MappingRegistration<T> definition = this.registry.remove(mapping);
                if (definition == null) {
                    return;
                }

                this.mappingLookup.remove(definition.getMapping());

                for (String url : definition.getDirectUrls()) {
                    List<T> list = this.urlLookup.get(url);
                    if (list != null) {
                        list.remove(definition.getMapping());
                        if (list.isEmpty()) {
                            this.urlLookup.remove(url);
                        }
                    }
                }

            }
            finally {
                this.readWriteLock.writeLock().unlock();
            }
        }

    }

    private static class MappingRegistration<T> {

        private final T mapping;

        private final HandlerMethod handlerMethod;

        private final List<String> directUrls;

        @Nullable
        private final String mappingName;

        public MappingRegistration(T mapping, HandlerMethod handlerMethod,
                                   @Nullable List<String> directUrls, @Nullable String mappingName) {

            Assert.notNull(mapping, "Mapping must not be null");
            Assert.notNull(handlerMethod, "HandlerMethod must not be null");
            this.mapping = mapping;
            this.handlerMethod = handlerMethod;
            this.directUrls = (directUrls != null ? directUrls : Collections.emptyList());
            this.mappingName = mappingName;
        }

        public T getMapping() {
            return this.mapping;
        }

        public HandlerMethod getHandlerMethod() {
            return this.handlerMethod;
        }

        public List<String> getDirectUrls() {
            return this.directUrls;
        }

        @Nullable
        public String getMappingName() {
            return this.mappingName;
        }
    }
}
