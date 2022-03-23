package com.tiny.springmvc.web.servlet.mvc.method;

import com.tiny.springmvc.web.bind.annotation.RequestMethod;
import com.tiny.springmvc.web.servlet.mvc.condition.PatternsRequestCondition;
import com.tiny.springmvc.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import com.tiny.springmvc.web.util.UrlPathHelper;
import org.springframework.lang.Nullable;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class RequestMappingInfo {

    @Nullable
    private final String name;

    private final PatternsRequestCondition patternsCondition;

    private final RequestMethodsRequestCondition methodsCondition;

    public RequestMappingInfo(@Nullable String name, @Nullable PatternsRequestCondition patterns,
                              @Nullable RequestMethodsRequestCondition methods) {

        this.name = (StringUtils.hasText(name) ? name : null);
        this.patternsCondition = (patterns != null ? patterns : new PatternsRequestCondition());
        this.methodsCondition = (methods != null ? methods : new RequestMethodsRequestCondition());
    }

    public RequestMappingInfo combine(RequestMappingInfo other) {
        String name = combineNames(other);
        RequestMethodsRequestCondition methods = this.methodsCondition.combine(other.methodsCondition);
        PatternsRequestCondition patterns = this.patternsCondition.combine(other.patternsCondition);
        return new RequestMappingInfo(name,patterns,methods);
    }


    @Nullable
    public RequestMappingInfo getMatchingCondition(HttpServletRequest request) {
        RequestMethodsRequestCondition methods = this.methodsCondition.getMatchingCondition(request);
        if (methods == null) {
            return null;
        }
        PatternsRequestCondition patterns = this.patternsCondition.getMatchingCondition(request);
        if (patterns == null) {
            return null;
        }
        return new RequestMappingInfo(this.name, patterns, methods);
    }


    public PatternsRequestCondition getPatternsCondition() {
        return this.patternsCondition;
    }

    @Nullable
    private String combineNames(RequestMappingInfo other) {
        if (this.name != null && other.name != null) {
            String separator = "#";
            return this.name + separator + other.name;
        }
        else if (this.name != null) {
            return this.name;
        }
        else {
            return other.name;
        }
    }

    @Nullable
    public String getName() {
        return this.name;
    }



    public static Builder paths(String... paths) {
        return new DefaultBuilder(paths);
    }


    /**
     * Defines a builder for creating a RequestMappingInfo.
     * @since 4.2
     */
    public interface Builder {

        /**
         * Set the path patterns.
         */
        Builder paths(String... paths);

        /**
         * Set the request method conditions.
         */
        Builder methods(RequestMethod... methods);

        /**
         * Set the mapping name.
         */
        Builder mappingName(String name);

        Builder options(BuilderConfiguration options);

        /**
         * Build the RequestMappingInfo.
         */
        RequestMappingInfo build();
    }


    private static class DefaultBuilder implements Builder {

        private String[] paths = new String[0];

        private RequestMethod[] methods = new RequestMethod[0];


        @Nullable
        private String mappingName;


        private BuilderConfiguration options = new BuilderConfiguration();

        public DefaultBuilder(String... paths) {
            this.paths = paths;
        }

        @Override
        public Builder paths(String... paths) {
            this.paths = paths;
            return this;
        }

        @Override
        public DefaultBuilder methods(RequestMethod... methods) {
            this.methods = methods;
            return this;
        }


        @Override
        public DefaultBuilder mappingName(String name) {
            this.mappingName = name;
            return this;
        }


        @Override
        public Builder options(BuilderConfiguration options) {
            this.options = options;
            return this;
        }

        @Override
        @SuppressWarnings("deprecation")
        public RequestMappingInfo build() {

            PatternsRequestCondition patternsCondition = new PatternsRequestCondition(
                    this.paths, this.options.getUrlPathHelper(), this.options.getPathMatcher(),
                    this.options.useSuffixPatternMatch(), this.options.useTrailingSlashMatch(),
                    this.options.getFileExtensions());

            return new RequestMappingInfo(this.mappingName,patternsCondition,
                    new RequestMethodsRequestCondition(this.methods));
        }
    }




    public static class BuilderConfiguration {

        @Nullable
        private UrlPathHelper urlPathHelper;

        @Nullable
        private PathMatcher pathMatcher;

        private boolean trailingSlashMatch = true;

        private boolean suffixPatternMatch = true;

        private boolean registeredSuffixPatternMatch = false;


        /**
         * Set a custom UrlPathHelper to use for the PatternsRequestCondition.
         * <p>By default this is not set.
         * @since 4.2.8
         */
        public void setUrlPathHelper(@Nullable UrlPathHelper urlPathHelper) {
            this.urlPathHelper = urlPathHelper;
        }

        /**
         * Return a custom UrlPathHelper to use for the PatternsRequestCondition, if any.
         */
        @Nullable
        public UrlPathHelper getUrlPathHelper() {
            return this.urlPathHelper;
        }

        /**
         * Set a custom PathMatcher to use for the PatternsRequestCondition.
         * <p>By default this is not set.
         */
        public void setPathMatcher(@Nullable PathMatcher pathMatcher) {
            this.pathMatcher = pathMatcher;
        }

        /**
         * Return a custom PathMatcher to use for the PatternsRequestCondition, if any.
         */
        @Nullable
        public PathMatcher getPathMatcher() {
            return this.pathMatcher;
        }

        /**
         * Set whether to apply trailing slash matching in PatternsRequestCondition.
         * <p>By default this is set to 'true'.
         */
        public void setTrailingSlashMatch(boolean trailingSlashMatch) {
            this.trailingSlashMatch = trailingSlashMatch;
        }

        /**
         * Return whether to apply trailing slash matching in PatternsRequestCondition.
         */
        public boolean useTrailingSlashMatch() {
            return this.trailingSlashMatch;
        }

        /**
         * Set whether to apply suffix pattern matching in PatternsRequestCondition.
         * <p>By default this is set to 'true'.
         * @see #setRegisteredSuffixPatternMatch(boolean)
         * @deprecated as of 5.2.4. See class-level note in
         * extension config options.
         */
        @Deprecated
        public void setSuffixPatternMatch(boolean suffixPatternMatch) {
            this.suffixPatternMatch = suffixPatternMatch;
        }

        /**
         * Return whether to apply suffix pattern matching in PatternsRequestCondition.
         * @deprecated as of 5.2.4. See class-level note in
         * extension config options.
         */
        @Deprecated
        public boolean useSuffixPatternMatch() {
            return this.suffixPatternMatch;
        }

        /**
         * Set whether suffix pattern matching should be restricted to registered
         * file extensions only. Setting this property also sets
         * {@code suffixPatternMatch=true} and requires that a
         * obtain the registered file extensions.
         * @deprecated as of 5.2.4. See class-level note in
         * extension config options; note also that in 5.3 the default for this
         * property switches from {@code false} to {@code true}.
         */
        @Deprecated
        public void setRegisteredSuffixPatternMatch(boolean registeredSuffixPatternMatch) {
            this.registeredSuffixPatternMatch = registeredSuffixPatternMatch;
            this.suffixPatternMatch = (registeredSuffixPatternMatch || this.suffixPatternMatch);
        }

        /**
         * Return whether suffix pattern matching should be restricted to registered
         * file extensions only.
         * @deprecated as of 5.2.4. See class-level note in
         * extension config options.
         */
        @Deprecated
        public boolean useRegisteredSuffixPatternMatch() {
            return this.registeredSuffixPatternMatch;
        }

        /**
         * Return the file extensions to use for suffix pattern matching. If
         * {@code registeredSuffixPatternMatch=true}, the extensions are obtained
         * from the configured {@code contentNegotiationManager}.
         * @deprecated as of 5.2.4. See class-level note in
         * extension config options.
         */
        @Nullable
        @Deprecated
        public List<String> getFileExtensions() {
            /*if (useRegisteredSuffixPatternMatch() && this.contentNegotiationManager != null) {
                return this.contentNegotiationManager.getAllFileExtensions();
            }*/
            return null;
        }

    }
}
