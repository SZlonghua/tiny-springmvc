package com.tiny.springmvc.web.context.request;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServletRequestAttributes extends AbstractRequestAttributes {

    private final HttpServletRequest request;

    @Nullable
    private HttpServletResponse response;

    @Nullable
    private volatile HttpSession session;

    private final Map<String, Object> sessionAttributesToUpdate = new ConcurrentHashMap<>(1);

    protected static final Set<Class<?>> immutableValueTypes = new HashSet<>(16);

    static {
        immutableValueTypes.addAll(NumberUtils.STANDARD_NUMBER_TYPES);
        immutableValueTypes.add(Boolean.class);
        immutableValueTypes.add(Character.class);
        immutableValueTypes.add(String.class);
    }


    public ServletRequestAttributes(HttpServletRequest request) {
        Assert.notNull(request, "Request must not be null");
        this.request = request;
    }

    public ServletRequestAttributes(HttpServletRequest request, @Nullable HttpServletResponse response) {
        this(request);
        this.response = response;
    }

    @Override
    protected void updateAccessedSessionAttributes() {
        if (!this.sessionAttributesToUpdate.isEmpty()) {
            // Update all affected session attributes.
            HttpSession session = getSession(false);
            if (session != null) {
                try {
                    for (Map.Entry<String, Object> entry : this.sessionAttributesToUpdate.entrySet()) {
                        String name = entry.getKey();
                        Object newValue = entry.getValue();
                        Object oldValue = session.getAttribute(name);
                        if (oldValue == newValue && !isImmutableSessionAttribute(name, newValue)) {
                            session.setAttribute(name, newValue);
                        }
                    }
                }
                catch (IllegalStateException ex) {
                    // Session invalidated - shouldn't usually happen.
                }
            }
            this.sessionAttributesToUpdate.clear();
        }
    }

    protected boolean isImmutableSessionAttribute(String name, @Nullable Object value) {
        return (value == null || immutableValueTypes.contains(value.getClass()));
    }

    @Nullable
    protected final HttpSession getSession(boolean allowCreate) {
        if (isRequestActive()) {
            HttpSession session = this.request.getSession(allowCreate);
            this.session = session;
            return session;
        }
        else {
            // Access through stored session reference, if any...
            HttpSession session = this.session;
            if (session == null) {
                if (allowCreate) {
                    throw new IllegalStateException(
                            "No session found and request already completed - cannot create new session!");
                }
                else {
                    session = this.request.getSession(false);
                    this.session = session;
                }
            }
            return session;
        }
    }

    public final HttpServletRequest getRequest() {
        return this.request;
    }

    @Nullable
    public final HttpServletResponse getResponse() {
        return this.response;
    }


}
