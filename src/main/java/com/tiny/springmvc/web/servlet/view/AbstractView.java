package com.tiny.springmvc.web.servlet.view;

import com.tiny.springmvc.web.context.support.WebApplicationObjectSupport;
import com.tiny.springmvc.web.servlet.View;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractView  extends WebApplicationObjectSupport implements View, BeanNameAware {

    public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";

    /** Initial size for the temporary output byte array (if any). */
    //private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;


    @Nullable
    private String contentType = DEFAULT_CONTENT_TYPE;

    @Nullable
    private String beanName;

    @Override
    public void setBeanName(@Nullable String beanName) {
        this.beanName = beanName;
    }

    /**
     * Return the view's name. Should never be {@code null},
     * if the view was correctly configured.
     */
    @Nullable
    public String getBeanName() {
        return this.beanName;
    }

    public void setContentType(@Nullable String contentType) {
        this.contentType = contentType;
    }

    /**
     * Return the content type for this view.
     */
    @Override
    @Nullable
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> mergedModel = createMergedOutputModel(model, request, response);
        renderMergedOutputModel(mergedModel, request, response);
    }


    protected abstract void renderMergedOutputModel(
            Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception;

    protected Map<String, Object> createMergedOutputModel(@Nullable Map<String, ?> model,
                                                          HttpServletRequest request, HttpServletResponse response) {


        Map<String, Object> mergedModel = new LinkedHashMap<>(model.size());
        if (model != null) {
            mergedModel.putAll(model);
        }

        // Expose RequestContext?
        /*if (this.requestContextAttribute != null) {
            mergedModel.put(this.requestContextAttribute, createRequestContext(request, response, mergedModel));
        }*/

        return mergedModel;
    }

    protected void exposeModelAsRequestAttributes(Map<String, Object> model,
                                                  HttpServletRequest request) throws Exception {

        model.forEach((name, value) -> {
            if (value != null) {
                request.setAttribute(name, value);
            }
            else {
                request.removeAttribute(name);
            }
        });
    }
}
