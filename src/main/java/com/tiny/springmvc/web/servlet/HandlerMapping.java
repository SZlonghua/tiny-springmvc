package com.tiny.springmvc.web.servlet;

import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;

public interface HandlerMapping {

    String BEST_MATCHING_HANDLER_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingHandler";

    String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinHandlerMapping";

    String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping.class.getName() + ".producibleMediaTypes";

    String LOOKUP_PATH = HandlerMapping.class.getName() + ".lookupPath";

    @Nullable
    HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}
