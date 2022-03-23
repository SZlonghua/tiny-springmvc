package com.tiny.springmvc.web.accept;

import com.tiny.springmvc.http.MediaType;

import java.util.List;

public interface MediaTypeFileExtensionResolver {

    List<String> resolveFileExtensions(MediaType mediaType);

    /**
     * Get all registered file extensions.
     * @return a list of extensions or an empty list (never {@code null})
     */
    List<String> getAllFileExtensions();
}
