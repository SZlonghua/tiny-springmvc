package com.tiny.springmvc.web.servlet.mvc.annotation;

import com.tiny.springmvc.web.context.request.NativeWebRequest;
import com.tiny.springmvc.web.method.support.HandlerMethodReturnValueHandler;
import com.tiny.springmvc.web.method.support.ModelAndViewContainer;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.PatternMatchUtils;

public class ViewNameMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Nullable
    private String[] redirectPatterns;


    /**
     * Configure one more simple patterns (as described in {@link PatternMatchUtils#simpleMatch})
     * to use in order to recognize custom redirect prefixes in addition to "redirect:".
     * <p>Note that simply configuring this property will not make a custom redirect prefix work.
     * There must be a custom View that recognizes the prefix as well.
     * @since 4.1
     */
    public void setRedirectPatterns(@Nullable String... redirectPatterns) {
        this.redirectPatterns = redirectPatterns;
    }

    /**
     * The configured redirect patterns, if any.
     */
    @Nullable
    public String[] getRedirectPatterns() {
        return this.redirectPatterns;
    }


    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> paramType = returnType.getParameterType();
        return (void.class == paramType || CharSequence.class.isAssignableFrom(paramType));
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {

        if (returnValue instanceof CharSequence) {
            String viewName = returnValue.toString();
            mavContainer.setViewName(viewName);
            /*if (isRedirectViewName(viewName)) {
                mavContainer.setRedirectModelScenario(true);
            }*/
        }
        else if (returnValue != null) {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " +
                    returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }

    /**
     * Whether the given view name is a redirect view reference.
     * The default implementation checks the configured redirect patterns and
     * also if the view name starts with the "redirect:" prefix.
     * @param viewName the view name to check, never {@code null}
     * @return "true" if the given view name is recognized as a redirect view
     * reference; "false" otherwise.
     */
    protected boolean isRedirectViewName(String viewName) {
        return (PatternMatchUtils.simpleMatch(this.redirectPatterns, viewName) || viewName.startsWith("redirect:"));
    }

}
