package com.renderscreenshot.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Result for a single URL in a batch request.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchResult {

    private String url;
    private boolean success;
    private ScreenshotResponse response;
    private String error;

    /** Default constructor for Jackson deserialization. */
    public BatchResult() {
    }

    /**
     * Returns the URL that was captured.
     *
     * @return the URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL.
     *
     * @param url the URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns whether the screenshot was captured successfully.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the success flag.
     *
     * @param success the success flag
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Returns the screenshot response if successful.
     *
     * @return the response, or null if failed
     */
    public ScreenshotResponse getResponse() {
        return response;
    }

    /**
     * Sets the screenshot response.
     *
     * @param response the response
     */
    public void setResponse(ScreenshotResponse response) {
        this.response = response;
    }

    /**
     * Returns the error message if failed.
     *
     * @return the error message, or null if successful
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message.
     *
     * @param error the error message
     */
    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "BatchResult{"
                + "url='" + url + '\''
                + ", success=" + success
                + ", response=" + response
                + ", error='" + error + '\''
                + '}';
    }
}
