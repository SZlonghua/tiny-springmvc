package com.tiny.springmvc.web.accept;

import com.tiny.springmvc.http.HttpHeaders;
import com.tiny.springmvc.http.MediaType;
import com.tiny.springmvc.web.context.request.NativeWebRequest;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

public class HeaderContentNegotiationStrategy implements ContentNegotiationStrategy {

    @Override
    public List<MediaType> resolveMediaTypes(NativeWebRequest request)
            throws Exception {

        String[] headerValueArray = request.getHeaderValues(HttpHeaders.ACCEPT);
        if (headerValueArray == null) {
            return MEDIA_TYPE_ALL_LIST;
        }

        List<String> headerValues = Arrays.asList(headerValueArray);
        List<MediaType> mediaTypes = MediaType.parseMediaTypes(headerValues);
        MediaType.sortBySpecificityAndQuality(mediaTypes);
        return !CollectionUtils.isEmpty(mediaTypes) ? mediaTypes : MEDIA_TYPE_ALL_LIST;
    }

}
