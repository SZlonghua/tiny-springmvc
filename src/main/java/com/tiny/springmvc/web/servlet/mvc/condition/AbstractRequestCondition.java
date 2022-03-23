package com.tiny.springmvc.web.servlet.mvc.condition;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.StringJoiner;

public abstract class AbstractRequestCondition<T extends AbstractRequestCondition<T>> implements RequestCondition<T> {

    public boolean isEmpty() {
        return getContent().isEmpty();
    }

    /**
     * Return the discrete items a request condition is composed of.
     * <p>For example URL patterns, HTTP request methods, param expressions, etc.
     * @return a collection of objects (never {@code null})
     */
    protected abstract Collection<?> getContent();

    /**
     * The notation to use when printing discrete items of content.
     * <p>For example {@code " || "} for URL patterns or {@code " && "}
     * for param expressions.
     */
    protected abstract String getToStringInfix();


    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return getContent().equals(((AbstractRequestCondition<?>) other).getContent());
    }

    @Override
    public int hashCode() {
        return getContent().hashCode();
    }

    @Override
    public String toString() {
        String infix = getToStringInfix();
        StringJoiner joiner = new StringJoiner(infix, "[", "]");
        for (Object expression : getContent()) {
            joiner.add(expression.toString());
        }
        return joiner.toString();
    }
}
