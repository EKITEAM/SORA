package com.ekidevs.sora;

public class LoadError extends Exception {
    public enum ErrorType {
        NETWORK,
        DECODE,
        SVG,
        HTTP,
        CANCELED,
        UNKNOWN
    }

    private final ErrorType type;

    public LoadError(ErrorType type, Throwable cause) {
        super(cause);
        this.type = type;
    }

    public ErrorType getType() { return type; }
}