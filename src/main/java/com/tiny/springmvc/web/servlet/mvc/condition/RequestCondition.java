package com.tiny.springmvc.web.servlet.mvc.condition;

import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;

public interface RequestCondition<T> {
    T combine(T other);

    @Nullable
    T getMatchingCondition(HttpServletRequest request);

    int compareTo(T other, HttpServletRequest request);
}
