package com.tiny.springmvc.http.converter;

import com.tiny.springmvc.http.HttpHeaders;
import com.tiny.springmvc.http.HttpOutputMessage;
import com.tiny.springmvc.http.MediaType;
import com.tiny.springmvc.http.StreamingHttpOutputMessage;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

public abstract class AbstractGenericHttpMessageConverter<T> extends AbstractHttpMessageConverter<T>
        implements GenericHttpMessageConverter<T> {

    /**
     * Construct an {@code AbstractGenericHttpMessageConverter} with no supported media types.
     * @see #setSupportedMediaTypes
     */
    protected AbstractGenericHttpMessageConverter() {
    }

    /**
     * Construct an {@code AbstractGenericHttpMessageConverter} with one supported media type.
     * @param supportedMediaType the supported media type
     */
    protected AbstractGenericHttpMessageConverter(MediaType supportedMediaType) {
        super(supportedMediaType);
    }

    /**
     * Construct an {@code AbstractGenericHttpMessageConverter} with multiple supported media type.
     * @param supportedMediaTypes the supported media types
     */
    protected AbstractGenericHttpMessageConverter(MediaType... supportedMediaTypes) {
        super(supportedMediaTypes);
    }


    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return (type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType));
    }

    @Override
    public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
        return canWrite(clazz, mediaType);
    }

    /**
     * This implementation sets the default headers by calling {@link #addDefaultHeaders},
     * and then calls {@link #writeInternal}.
     */
    @Override
    public final void write(final T t, @Nullable final Type type, @Nullable MediaType contentType,
                            HttpOutputMessage outputMessage) throws IOException {

        final HttpHeaders headers = outputMessage.getHeaders();
        addDefaultHeaders(headers, t, contentType);

        if (outputMessage instanceof StreamingHttpOutputMessage) {
            StreamingHttpOutputMessage streamingOutputMessage = (StreamingHttpOutputMessage) outputMessage;
            streamingOutputMessage.setBody(outputStream -> writeInternal(t, type, new HttpOutputMessage() {
                @Override
                public OutputStream getBody() {
                    return outputStream;
                }
                @Override
                public HttpHeaders getHeaders() {
                    return headers;
                }
            }));
        }
        else {
            writeInternal(t, type, outputMessage);
            outputMessage.getBody().flush();
        }
    }

    @Override
    protected void writeInternal(T t, HttpOutputMessage outputMessage)
            throws IOException {

        writeInternal(t, null, outputMessage);
    }

    /**
     * Abstract template method that writes the actual body. Invoked from {@link #write}.
     * @param t the object to write to the output message
     * @param type the type of object to write (may be {@code null})
     * @param outputMessage the HTTP output message to write to
     * @throws IOException in case of I/O errors
     */
    protected abstract void writeInternal(T t, @Nullable Type type, HttpOutputMessage outputMessage)
            throws IOException;

}
