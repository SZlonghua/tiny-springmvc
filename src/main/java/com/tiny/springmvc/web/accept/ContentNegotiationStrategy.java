package com.tiny.springmvc.web.accept;

import com.tiny.springmvc.http.MediaType;
import com.tiny.springmvc.web.context.request.NativeWebRequest;

import java.util.Collections;
import java.util.List;


@FunctionalInterface
public interface ContentNegotiationStrategy {
    List<MediaType> MEDIA_TYPE_ALL_LIST = Collections.singletonList(MediaType.ALL);

    List<MediaType> resolveMediaTypes(NativeWebRequest webRequest)
            throws Exception;
}
