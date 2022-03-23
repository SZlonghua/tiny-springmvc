package com.tiny.springmvc.web.servlet.theme;

import com.tiny.springmvc.web.servlet.ThemeResolver;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FixedThemeResolver extends AbstractThemeResolver {
    @Override
    public String resolveThemeName(HttpServletRequest request) {
        return getDefaultThemeName();
    }

    @Override
    public void setThemeName(
            HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable String themeName) {

        throw new UnsupportedOperationException("Cannot change theme - use a different theme resolution strategy");
    }
}
