package com.mall.common.i18n.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class I18nMessage {

    private static MessageSource messageSource;

    public I18nMessage(MessageSource messageSource) {
        I18nMessage.messageSource = messageSource;
    }

    public static String get(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, key, locale);
    }
}
