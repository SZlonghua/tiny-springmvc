package com.tiny.springmvc.http;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpOutputMessage extends HttpMessage {

    /**
     * Return the body of the message as an output stream.
     * @return the output stream body (never {@code null})
     * @throws IOException in case of I/O errors
     */
    OutputStream getBody() throws IOException;

}
