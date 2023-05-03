package com.hz.exceptions;

public class ConnectionException extends RuntimeException {
    public ConnectionException(String reason) {
        super(reason);
    }

    public ConnectionException(Exception e) {
        super(e);
    }
}
