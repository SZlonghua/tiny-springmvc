package com.tiny.springmvc.web.context.request;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractRequestAttributes implements RequestAttributes {

    private volatile boolean requestActive = true;

    protected final Map<String, Runnable> requestDestructionCallbacks = new LinkedHashMap<>(8);

    public void requestCompleted() {
        executeRequestDestructionCallbacks();
        updateAccessedSessionAttributes();
        this.requestActive = false;
    }

    protected final boolean isRequestActive() {
        return this.requestActive;
    }

    private void executeRequestDestructionCallbacks() {
        synchronized (this.requestDestructionCallbacks) {
            for (Runnable runnable : this.requestDestructionCallbacks.values()) {
                runnable.run();
            }
            this.requestDestructionCallbacks.clear();
        }
    }

    /**
     * Update all session attributes that have been accessed during request processing,
     * to expose their potentially updated state to the underlying session manager.
     */
    protected abstract void updateAccessedSessionAttributes();
}
