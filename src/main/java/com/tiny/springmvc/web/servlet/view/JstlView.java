package com.tiny.springmvc.web.servlet.view;

import javax.servlet.http.HttpServletRequest;

public class JstlView extends InternalResourceView {

    public JstlView() {
    }

    public JstlView(String url) {
        super(url);
    }


    @Override
    protected void exposeHelpers(HttpServletRequest request) throws Exception {
        /*if (this.messageSource != null) {
            JstlUtils.exposeLocalizationContext(request, this.messageSource);
        }
        else {
            JstlUtils.exposeLocalizationContext(new RequestContext(request, getServletContext()));
        }*/
    }
}
