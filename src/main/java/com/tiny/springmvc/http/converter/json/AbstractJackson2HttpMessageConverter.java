package com.tiny.springmvc.http.converter.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.tiny.springmvc.http.HttpInputMessage;
import com.tiny.springmvc.http.HttpOutputMessage;
import com.tiny.springmvc.http.MediaType;
import com.tiny.springmvc.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.core.GenericTypeResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractJackson2HttpMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

    /**
     * The default charset used by the converter.
     */
    @Nullable
    @Deprecated
    public static final Charset DEFAULT_CHARSET = null;


    protected ObjectMapper objectMapper;

    @Nullable
    private Boolean prettyPrint;

    @Nullable
    private PrettyPrinter ssePrettyPrinter;


    protected AbstractJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentObjectsWith(new DefaultIndenter("  ", "\ndata:"));
        this.ssePrettyPrinter = prettyPrinter;
    }

    protected AbstractJackson2HttpMessageConverter(ObjectMapper objectMapper, MediaType supportedMediaType) {
        this(objectMapper);
        setSupportedMediaTypes(Collections.singletonList(supportedMediaType));
    }

    protected AbstractJackson2HttpMessageConverter(ObjectMapper objectMapper, MediaType... supportedMediaTypes) {
        this(objectMapper);
        setSupportedMediaTypes(Arrays.asList(supportedMediaTypes));
    }


    /**
     * Set the {@code ObjectMapper} for this view.
     * If not set, a default {@link ObjectMapper#ObjectMapper() ObjectMapper} is used.
     * <p>Setting a custom-configured {@code ObjectMapper} is one way to take further
     * control of the JSON serialization process. For example, an extended
     * {@link com.fasterxml.jackson.databind.ser.SerializerFactory}
     * can be configured that provides custom serializers for specific types.
     * The other option for refining the serialization process is to use Jackson's
     * provided annotations on the types to be serialized, in which case a
     * custom-configured ObjectMapper is unnecessary.
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        this.objectMapper = objectMapper;
        configurePrettyPrint();
    }

    /**
     * Return the underlying {@code ObjectMapper} for this view.
     */
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /**
     * Whether to use the {@link DefaultPrettyPrinter} when writing JSON.
     * This is a shortcut for setting up an {@code ObjectMapper} as follows:
     * <pre class="code">
     * ObjectMapper mapper = new ObjectMapper();
     * mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
     * converter.setObjectMapper(mapper);
     * </pre>
     */
    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        configurePrettyPrint();
    }

    private void configurePrettyPrint() {
        if (this.prettyPrint != null) {
            this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, this.prettyPrint);
        }
    }


    @Override
    public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
        return canRead(clazz, null, mediaType);
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        if (!canRead(mediaType)) {
            return false;
        }
        JavaType javaType = getJavaType(type, contextClass);
        AtomicReference<Throwable> causeRef = new AtomicReference<>();
        if (this.objectMapper.canDeserialize(javaType, causeRef)) {
            return true;
        }
        logWarningIfNecessary(javaType, causeRef.get());
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
        if (!canWrite(mediaType)) {
            return false;
        }
        AtomicReference<Throwable> causeRef = new AtomicReference<>();
        if (this.objectMapper.canSerialize(clazz, causeRef)) {
            return true;
        }
        logWarningIfNecessary(clazz, causeRef.get());
        return false;
    }

    /**
     * Determine whether to log the given exception coming from a
     * {@link ObjectMapper#canDeserialize} / {@link ObjectMapper#canSerialize} check.
     * @param type the class that Jackson tested for (de-)serializability
     * @param cause the Jackson-thrown exception to evaluate
     * (typically a {@link JsonMappingException})
     * @since 4.3
     */
    protected void logWarningIfNecessary(Type type, @Nullable Throwable cause) {
        if (cause == null) {
            return;
        }

        // Do not log warning for serializer not found (note: different message wording on Jackson 2.9)
        boolean debugLevel = (cause instanceof JsonMappingException && cause.getMessage().startsWith("Cannot find"));

        if (debugLevel ? logger.isDebugEnabled() : logger.isWarnEnabled()) {
            String msg = "Failed to evaluate Jackson " + (type instanceof JavaType ? "de" : "") +
                    "serialization for type [" + type + "]";
            if (debugLevel) {
                logger.debug(msg, cause);
            }
            else if (logger.isDebugEnabled()) {
                logger.warn(msg, cause);
            }
            else {
                logger.warn(msg + ": " + cause);
            }
        }
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException {

        JavaType javaType = getJavaType(clazz, null);
        return readJavaType(javaType, inputMessage);
    }

    @Override
    public Object read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException {

        JavaType javaType = getJavaType(type, contextClass);
        return readJavaType(javaType, inputMessage);
    }

    private Object readJavaType(JavaType javaType, HttpInputMessage inputMessage) throws IOException {
        try {
            if (inputMessage instanceof MappingJacksonInputMessage) {
                Class<?> deserializationView = ((MappingJacksonInputMessage) inputMessage).getDeserializationView();
                if (deserializationView != null) {
                    return this.objectMapper.readerWithView(deserializationView).forType(javaType).
                            readValue(inputMessage.getBody());
                }
            }
            return this.objectMapper.readValue(inputMessage.getBody(), javaType);
        }
        catch (MismatchedInputException ex) {  // specific kind of JsonMappingException
            throw ex;
        }
        catch (InvalidDefinitionException ex) {  // another kind of JsonMappingException
            throw ex;
        }
        catch (JsonMappingException ex) {  // typically ValueInstantiationException
            throw ex;
        }
        catch (JsonProcessingException ex) {
            throw ex;
        }
    }

    @Override
    protected void writeInternal(Object object, @Nullable Type type, HttpOutputMessage outputMessage)
            throws IOException {

        MediaType contentType = outputMessage.getHeaders().getContentType();
        JsonEncoding encoding = getJsonEncoding(contentType);
        JsonGenerator generator = this.objectMapper.getFactory().createGenerator(outputMessage.getBody(), encoding);
        try {
            writePrefix(generator, object);

            Object value = object;
            Class<?> serializationView = null;
            FilterProvider filters = null;
            JavaType javaType = null;

            if (object instanceof MappingJacksonValue) {
                MappingJacksonValue container = (MappingJacksonValue) object;
                value = container.getValue();
                serializationView = container.getSerializationView();
                filters = container.getFilters();
            }
            if (type != null && TypeUtils.isAssignable(type, value.getClass())) {
                javaType = getJavaType(type, null);
            }

            ObjectWriter objectWriter = (serializationView != null ?
                    this.objectMapper.writerWithView(serializationView) : this.objectMapper.writer());
            if (filters != null) {
                objectWriter = objectWriter.with(filters);
            }
            if (javaType != null && javaType.isContainerType()) {
                objectWriter = objectWriter.forType(javaType);
            }
            SerializationConfig config = objectWriter.getConfig();
            if (contentType != null && contentType.isCompatibleWith(MediaType.TEXT_EVENT_STREAM) &&
                    config.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
                objectWriter = objectWriter.with(this.ssePrettyPrinter);
            }
            objectWriter.writeValue(generator, value);

            writeSuffix(generator, object);
            generator.flush();
        }
        catch (MismatchedInputException ex) {  // specific kind of JsonMappingException
            throw ex;
        }
        catch (InvalidDefinitionException ex) {  // another kind of JsonMappingException
            throw ex;
        }
        catch (JsonMappingException ex) {  // typically ValueInstantiationException
            throw ex;
        }
        catch (JsonProcessingException ex) {
            throw ex;
        }
    }

    /**
     * Write a prefix before the main content.
     * @param generator the generator to use for writing content.
     * @param object the object to write to the output message.
     */
    protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
    }

    /**
     * Write a suffix after the main content.
     * @param generator the generator to use for writing content.
     * @param object the object to write to the output message.
     */
    protected void writeSuffix(JsonGenerator generator, Object object) throws IOException {
    }

    /**
     * Return the Jackson {@link JavaType} for the specified type and context class.
     * @param type the generic type to return the Jackson JavaType for
     * @param contextClass a context class for the target type, for example a class
     * in which the target type appears in a method signature (can be {@code null})
     * @return the Jackson JavaType
     */
    protected JavaType getJavaType(Type type, @Nullable Class<?> contextClass) {
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();
        return typeFactory.constructType(GenericTypeResolver.resolveType(type, contextClass));
    }

    /**
     * Determine the JSON encoding to use for the given content type.
     * @param contentType the media type as requested by the caller
     * @return the JSON encoding to use (never {@code null})
     */
    protected JsonEncoding getJsonEncoding(@Nullable MediaType contentType) {
        if (contentType != null && contentType.getCharset() != null) {
            Charset charset = contentType.getCharset();
            for (JsonEncoding encoding : JsonEncoding.values()) {
                if (charset.name().equals(encoding.getJavaName())) {
                    return encoding;
                }
            }
        }
        return JsonEncoding.UTF8;
    }

    @Override
    @Nullable
    protected MediaType getDefaultContentType(Object object) throws IOException {
        if (object instanceof MappingJacksonValue) {
            object = ((MappingJacksonValue) object).getValue();
        }
        return super.getDefaultContentType(object);
    }

    @Override
    protected Long getContentLength(Object object, @Nullable MediaType contentType) throws IOException {
        if (object instanceof MappingJacksonValue) {
            object = ((MappingJacksonValue) object).getValue();
        }
        return super.getContentLength(object, contentType);
    }

}
