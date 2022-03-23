package com.tiny.springmvc.http.converter.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiny.springmvc.http.MediaType;
import org.springframework.lang.Nullable;

import java.io.IOException;

public class MappingJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    @Nullable
    private String jsonPrefix;


    /**
     * Construct a new {@link MappingJackson2HttpMessageConverter} using default configuration
     * provided by {@link Jackson2ObjectMapperBuilder}.
     */
    public MappingJackson2HttpMessageConverter() {
        this(new ObjectMapper());
    }

    /**
     * Construct a new {@link MappingJackson2HttpMessageConverter} with a custom {@link ObjectMapper}.
     * You can use {@link Jackson2ObjectMapperBuilder} to build it easily.
     */
    public MappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }


    /**
     * Specify a custom prefix to use for this view's JSON output.
     * Default is none.
     * @see #setPrefixJson
     */
    public void setJsonPrefix(String jsonPrefix) {
        this.jsonPrefix = jsonPrefix;
    }

    /**
     * Indicate whether the JSON output by this view should be prefixed with ")]}', ". Default is false.
     * <p>Prefixing the JSON string in this manner is used to help prevent JSON Hijacking.
     * The prefix renders the string syntactically invalid as a script so that it cannot be hijacked.
     * This prefix should be stripped before parsing the string as JSON.
     * @see #setJsonPrefix
     */
    public void setPrefixJson(boolean prefixJson) {
        this.jsonPrefix = (prefixJson ? ")]}', " : null);
    }


    @Override
    protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
        if (this.jsonPrefix != null) {
            generator.writeRaw(this.jsonPrefix);
        }
    }

}