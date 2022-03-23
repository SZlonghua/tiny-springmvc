package com.tiny.springmvc.web.servlet.theme;

import com.tiny.springmvc.web.servlet.ThemeResolver;

public abstract class AbstractThemeResolver implements ThemeResolver {


    /**
     * Out-of-the-box value for the default theme name: "theme".
     */
    public static final String ORIGINAL_DEFAULT_THEME_NAME = "theme";

    private String defaultThemeName = ORIGINAL_DEFAULT_THEME_NAME;


    /**
     * Set the name of the default theme.
     * Out-of-the-box value is "theme".
     */
    public void setDefaultThemeName(String defaultThemeName) {
        this.defaultThemeName = defaultThemeName;
    }

    /**
     * Return the name of the default theme.
     */
    public String getDefaultThemeName() {
        return this.defaultThemeName;
    }

}
