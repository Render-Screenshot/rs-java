package com.renderscreenshot.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Response from a batch screenshot request.
 *
 * <p>Contains the batch ID, status, progress information, and results
 * for each URL in the batch.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchResponse {

    private String id;
    private String status;
    private int total;
    private int completed;
    private int failed;
    private List<BatchResult> results;

    /** Default constructor for Jackson deserialization. */
    public BatchResponse() {
    }

    /**
     * Returns the unique batch ID.
     *
     * @return the batch ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the batch ID.
     *
     * @param id the batch ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the batch status.
     *
     * @return the status (e.g., "pending", "processing", "completed", "failed")
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the batch status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the total number of URLs in the batch.
     *
     * @return the total count
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets the total number of URLs.
     *
     * @param total the total count
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * Returns the number of successfully completed screenshots.
     *
     * @return the completed count
     */
    public int getCompleted() {
        return completed;
    }

    /**
     * Sets the completed count.
     *
     * @param completed the completed count
     */
    public void setCompleted(int completed) {
        this.completed = completed;
    }

    /**
     * Returns the number of failed screenshots.
     *
     * @return the failed count
     */
    public int getFailed() {
        return failed;
    }

    /**
     * Sets the failed count.
     *
     * @param failed the failed count
     */
    public void setFailed(int failed) {
        this.failed = failed;
    }

    /**
     * Returns the results for each URL in the batch.
     *
     * @return the list of results
     */
    public List<BatchResult> getResults() {
        return results;
    }

    /**
     * Sets the results.
     *
     * @param results the list of results
     */
    public void setResults(List<BatchResult> results) {
        this.results = results;
    }

    /**
     * Returns whether the batch has completed (either successfully or with failures).
     *
     * @return true if completed or failed, false if still processing
     */
    public boolean isComplete() {
        return "completed".equals(status) || "failed".equals(status);
    }

    @Override
    public String toString() {
        return "BatchResponse{"
                + "id='" + id + '\''
                + ", status='" + status + '\''
                + ", total=" + total
                + ", completed=" + completed
                + ", failed=" + failed
                + ", results=" + results
                + '}';
    }
}
