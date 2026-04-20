package com.streamflix.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
    public ResourceNotFoundException(String type, Object id) {
        super(type + " not found with id " + id);
    }
}
