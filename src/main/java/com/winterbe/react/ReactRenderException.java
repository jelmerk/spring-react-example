package com.winterbe.react;

public class ReactRenderException extends RuntimeException {

    public ReactRenderException() {
    }

    public ReactRenderException(String message) {
        super(message);
    }

    public ReactRenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReactRenderException(Throwable cause) {
        super(cause);
    }

    public ReactRenderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
