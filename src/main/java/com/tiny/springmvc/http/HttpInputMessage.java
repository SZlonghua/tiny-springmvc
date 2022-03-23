package com.tiny.springmvc.http;

import java.io.IOException;
import java.io.InputStream;

public interface HttpInputMessage extends HttpMessage {

    /**
     * Return the body of the message as an input stream.
     * @return the input stream body (never {@code null})
     * @throws IOException in case of I/O errors
     */
    InputStream getBody() throws IOException;

}
