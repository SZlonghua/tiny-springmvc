package com.tiny.springmvc.web.bind;

import org.springframework.validation.DataBinder;

public class WebDataBinder extends DataBinder {

    public WebDataBinder(Object target) {
        super(target);
    }

    public WebDataBinder(Object target, String objectName) {
        super(target, objectName);
    }
}
