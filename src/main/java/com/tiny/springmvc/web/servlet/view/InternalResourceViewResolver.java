package com.tiny.springmvc.web.servlet.view;

public class InternalResourceViewResolver extends UrlBasedViewResolver {

    public InternalResourceViewResolver() {
        setViewClass(JstlView.class);
    }

    public InternalResourceViewResolver(String prefix, String suffix) {
        this();
        setPrefix(prefix);
        setSuffix(suffix);
    }

}
