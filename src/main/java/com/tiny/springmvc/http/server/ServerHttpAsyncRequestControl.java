package com.tiny.springmvc.http.server;

public interface ServerHttpAsyncRequestControl {

    void start();

    /**
     * A variation on {@link #start()} that allows specifying a timeout value to use to
     * use for asynchronous processing. If {@link #complete()} is not called within the
     * specified value, the request times out.
     */
    void start(long timeout);

    /**
     * Return whether asynchronous request processing has been started.
     */
    boolean isStarted();

    /**
     * Mark asynchronous request processing as completed.
     */
    void complete();

    /**
     * Return whether asynchronous request processing has been completed.
     */
    boolean isCompleted();
}
