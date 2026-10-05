package com.renderscreenshot.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result for a single URL in a batch request.
 *
 * <p>{@link #getImage()} is set when the status is {@code "completed"}.
 * {@link #getError()} holds the error message when the status is {@code "failed"}.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchResult {

    private Integer position;
    private String url;
    private String status;
    private BatchImage image;
    private String error;
    @JsonProperty("response_time_ms")
    private Integer responseTimeMs;

    /** Default constructor for Jackson deserialization. */
    public BatchResult() {
    }

    /**
     * Returns the index of this URL in the original request.
     *
     * @return the position, or null if not provided
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets the position.
     *
     * @param position the position
     */
    public void setPosition(Integer position) {
        this.position = position;
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
     * Returns the item status.
     *
     * @return the status (e.g., "pending", "processing", "completed", "failed")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the item status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns whether the screenshot was captured successfully.
     *
     * @return true if the status is "completed", false otherwise
     */
    public boolean isSuccess() {
        return "completed".equals(status);
    }

    /**
     * Returns the screenshot details if successful.
     *
     * @return the image, or null if not completed
     */
    public BatchImage getImage() {
        return image;
    }

    /**
     * Sets the screenshot details.
     *
     * @param image the image
     */
    public void setImage(BatchImage image) {
        this.image = image;
    }

    /**
     * Returns the error message if failed.
     *
     * @return the error message, or null if not failed
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

    /**
     * Returns the capture time in milliseconds.
     *
     * @return the response time, or null if unknown
     */
    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }

    /**
     * Sets the capture time.
     *
     * @param responseTimeMs the response time in milliseconds
     */
    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    @Override
    public String toString() {
        return "BatchResult{"
                + "position=" + position
                + ", url='" + url + '\''
                + ", status='" + status + '\''
                + ", image=" + image
                + ", error='" + error + '\''
                + '}';
    }
}
