package com.tiny.springmvc.web.method.support;

import com.tiny.springmvc.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;
import org.springframework.validation.support.BindingAwareModelMap;

import java.util.Map;

public class ModelAndViewContainer {

    @Nullable
    private Object view;

    private final ModelMap defaultModel = new BindingAwareModelMap();

    @Nullable
    private HttpStatus status;

    private boolean requestHandled = false;


    public void setRequestHandled(boolean requestHandled) {
        this.requestHandled = requestHandled;
    }

    public boolean isRequestHandled() {
        return this.requestHandled;
    }

    public ModelMap getModel() {
        return this.defaultModel;
    }

    @Nullable
    public String getViewName() {
        return (this.view instanceof String ? (String) this.view : null);
    }

    public void setView(@Nullable Object view) {
        this.view = view;
    }

    @Nullable
    public Object getView() {
        return this.view;
    }

    @Nullable
    public HttpStatus getStatus() {
        return this.status;
    }


    public boolean isViewReference() {
        return (this.view instanceof String);
    }

    public void setViewName(@Nullable String viewName) {
        this.view = viewName;
    }

    public ModelAndViewContainer addAllAttributes(@Nullable Map<String, ?> attributes) {
        getModel().addAllAttributes(attributes);
        return this;
    }
}
