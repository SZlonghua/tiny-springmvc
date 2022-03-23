package com.tiny.springmvc.web.servlet.mvc.condition;

import com.tiny.springmvc.http.HttpMethod;
import com.tiny.springmvc.web.bind.annotation.RequestMethod;
import org.springframework.lang.Nullable;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class RequestMethodsRequestCondition extends AbstractRequestCondition<RequestMethodsRequestCondition> {

    private static final Map<String, RequestMethodsRequestCondition> requestMethodConditionCache;

    static {
        requestMethodConditionCache = new HashMap<>(RequestMethod.values().length);
        for (RequestMethod method : RequestMethod.values()) {
            requestMethodConditionCache.put(method.name(), new RequestMethodsRequestCondition(method));
        }
    }


    private final Set<RequestMethod> methods;


    /**
     * Create a new instance with the given request methods.
     * @param requestMethods 0 or more HTTP request methods;
     * if, 0 the condition will match to every request
     */
    public RequestMethodsRequestCondition(RequestMethod... requestMethods) {
        this(Arrays.asList(requestMethods));
    }

    private RequestMethodsRequestCondition(Collection<RequestMethod> requestMethods) {
        this.methods = Collections.unmodifiableSet(new LinkedHashSet<>(requestMethods));
    }


    /**
     * Returns all {@link RequestMethod RequestMethods} contained in this condition.
     */
    public Set<RequestMethod> getMethods() {
        return this.methods;
    }

    @Override
    protected Collection<RequestMethod> getContent() {
        return this.methods;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    /**
     * Returns a new instance with a union of the HTTP request methods
     * from "this" and the "other" instance.
     */
    @Override
    public RequestMethodsRequestCondition combine(RequestMethodsRequestCondition other) {
        Set<RequestMethod> set = new LinkedHashSet<>(this.methods);
        set.addAll(other.methods);
        return new RequestMethodsRequestCondition(set);
    }

    /**
     * Check if any of the HTTP request methods match the given request and
     * return an instance that contains the matching HTTP request method only.
     * @param request the current request
     * @return the same instance if the condition is empty (unless the request
     * method is HTTP OPTIONS), a new condition with the matched request method,
     * or {@code null} if there is no match or the condition is empty and the
     * request method is OPTIONS.
     */
    @Override
    @Nullable
    public RequestMethodsRequestCondition getMatchingCondition(HttpServletRequest request) {
        /*if (CorsUtils.isPreFlightRequest(request)) {
            return matchPreFlight(request);
        }*/

        if (getMethods().isEmpty()) {
            if (RequestMethod.OPTIONS.name().equals(request.getMethod()) &&
                    !DispatcherType.ERROR.equals(request.getDispatcherType())) {

                return null; // We handle OPTIONS transparently, so don't match if no explicit declarations
            }
            return this;
        }

        return matchRequestMethod(request.getMethod());
    }

    /**
     * On a pre-flight request match to the would-be, actual request.
     * Hence empty conditions is a match, otherwise try to match to the HTTP
     * method in the "Access-Control-Request-Method" header.
     */
    /*@Nullable
    private RequestMethodsRequestCondition matchPreFlight(HttpServletRequest request) {
        if (getMethods().isEmpty()) {
            return this;
        }
        String expectedMethod = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        return matchRequestMethod(expectedMethod);
    }*/

    @Nullable
    private RequestMethodsRequestCondition matchRequestMethod(String httpMethodValue) {
        RequestMethod requestMethod;
        try {
            requestMethod = RequestMethod.valueOf(httpMethodValue);
            if (getMethods().contains(requestMethod)) {
                return requestMethodConditionCache.get(httpMethodValue);
            }
            if (requestMethod.equals(RequestMethod.HEAD) && getMethods().contains(RequestMethod.GET)) {
                return requestMethodConditionCache.get(HttpMethod.GET.name());
            }
        }
        catch (IllegalArgumentException ex) {
            // Custom request method
        }
        return null;
    }

    /**
     * Returns:
     * <ul>
     * <li>0 if the two conditions contain the same number of HTTP request methods
     * <li>Less than 0 if "this" instance has an HTTP request method but "other" doesn't
     * <li>Greater than 0 "other" has an HTTP request method but "this" doesn't
     * </ul>
     * <p>It is assumed that both instances have been obtained via
     * {@link #getMatchingCondition(HttpServletRequest)} and therefore each instance
     * contains the matching HTTP request method only or is otherwise empty.
     */
    @Override
    public int compareTo(RequestMethodsRequestCondition other, HttpServletRequest request) {
        if (other.methods.size() != this.methods.size()) {
            return other.methods.size() - this.methods.size();
        }
        else if (this.methods.size() == 1) {
            if (this.methods.contains(RequestMethod.HEAD) && other.methods.contains(RequestMethod.GET)) {
                return -1;
            }
            else if (this.methods.contains(RequestMethod.GET) && other.methods.contains(RequestMethod.HEAD)) {
                return 1;
            }
        }
        return 0;
    }
}
