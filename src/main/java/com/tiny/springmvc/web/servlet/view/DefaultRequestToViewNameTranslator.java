package com.tiny.springmvc.web.servlet.view;

import com.tiny.springmvc.web.servlet.RequestToViewNameTranslator;

import javax.servlet.http.HttpServletRequest;

public class DefaultRequestToViewNameTranslator implements RequestToViewNameTranslator {
    @Override
    public String getViewName(HttpServletRequest request) throws Exception {
        return null;
    }
}
