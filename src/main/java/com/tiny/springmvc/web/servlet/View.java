package com.tiny.springmvc.web.servlet;

import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface View {

    String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";

    /**
     * Name of the {@link HttpServletRequest} attribute that contains a Map with path variables.
     * The map consists of String-based URI template variable names as keys and their corresponding
     * Object-based values -- extracted from segments of the URL and type converted.
     * <p>Note: This attribute is not required to be supported by all View implementations.
     * @since 3.1
     */
    String PATH_VARIABLES = View.class.getName() + ".pathVariables";

    /**
     * which may be more specific than the one the View is configured with. For example:
     * "application/vnd.example-v1+xml" vs "application/*+xml".
     * @since 3.2
     */
    String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";


    /**
     * Return the content type of the view, if predetermined.
     * <p>Can be used to check the view's content type upfront,
     * i.e. before an actual rendering attempt.
     * @return the content type String (optionally including a character set),
     * or {@code null} if not predetermined
     */
    @Nullable
    default String getContentType() {
        return null;
    }

    /**
     * Render the view given the specified model.
     * <p>The first step will be preparing the request: In the JSP case, this would mean
     * setting model objects as request attributes. The second step will be the actual
     * rendering of the view, for example including the JSP via a RequestDispatcher.
     * @param model a Map with name Strings as keys and corresponding model
     * objects as values (Map can also be {@code null} in case of empty model)
     * @param request current HTTP request
     * @param response he HTTP response we are building
     * @throws Exception if rendering failed
     */
    void render(@Nullable Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception;
}
