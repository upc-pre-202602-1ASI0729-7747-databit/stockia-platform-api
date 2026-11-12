package com.databit.stockia.platform.shared.application.result;

import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * A message key used as an argument of another localized message (resolved with the request
 * language when the problem response is built).
 */
public final class LocalizedText {
    private LocalizedText() {
    }

    public static DefaultMessageSourceResolvable of(String key, Object... args) {
        return new DefaultMessageSourceResolvable(new String[]{key}, args, key);
    }
}