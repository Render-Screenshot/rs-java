package com.renderscreenshot.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Error details for a failed batch item.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchError {

    private String code;
    private String message;

    /** Default constructor for Jackson deserialization. */
    public BatchError() {
    }

    /**
     * Creates a new BatchError with the given code and message.
     *
     * @param code    the error code
     * @param message the error message
     */
    public BatchError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the error code.
     *
     * @param code the error code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     *
     * @param message the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "BatchError{"
                + "code='" + code + '\''
                + ", message='" + message + '\''
                + '}';
    }
}
