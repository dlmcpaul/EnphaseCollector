package com.hz.exceptions;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String reason) {
        super(reason);
    }
}
