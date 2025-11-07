package com.processmonster.bpm.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Arrays;

/**
 * Internationalization (i18n) Configuration
 *
 * Configures message sources and locale resolution for French and English support.
 * Uses Accept-Language header for automatic locale detection.
 *
 * Supported locales: FR (français), EN (English)
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Configuration
public class I18nConfig {

    /**
     * Configures the message source for internationalization.
     * Messages are loaded from i18n/messages_XX.properties files.
     *
     * @return Configured ResourceBundleMessageSource
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.ENGLISH);
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        return messageSource;
    }

    /**
     * Configures the locale resolver to use Accept-Language header.
     * Defaults to English if no supported locale is found.
     *
     * @return Configured AcceptHeaderLocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setSupportedLocales(Arrays.asList(
            Locale.ENGLISH,
            Locale.FRENCH
        ));
        return localeResolver;
    }
}
