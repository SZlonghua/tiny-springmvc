package com.tiny.springmvc.http.server;

import com.tiny.springmvc.http.HttpInputMessage;
import com.tiny.springmvc.http.HttpRequest;
import org.springframework.lang.Nullable;

import java.net.InetSocketAddress;
import java.security.Principal;

public interface ServerHttpRequest extends HttpRequest, HttpInputMessage {

    /**
     * Return a {@link java.security.Principal} instance containing the name of the
     * authenticated user.
     * <p>If the user has not been authenticated, the method returns <code>null</code>.
     */
    @Nullable
    Principal getPrincipal();

    /**
     * Return the address on which the request was received.
     */
    InetSocketAddress getLocalAddress();

    /**
     * Return the address of the remote client.
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Return a control that allows putting the request in asynchronous mode so the
     * response remains open until closed explicitly from the current or another thread.
     */
    ServerHttpAsyncRequestControl getAsyncRequestControl(ServerHttpResponse response);

}
