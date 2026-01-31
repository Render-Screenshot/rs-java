package com.renderscreenshot.sdk;

/**
 * Exception thrown by the RenderScreenshot SDK for API and network errors.
 *
 * <p>This exception provides detailed information about failures including HTTP status codes,
 * API error codes, and retry information for rate-limited requests.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * try {
 *     byte[] screenshot = client.take(options);
 * } catch (RenderScreenshotException e) {
 *     if (e.isRetryable()) {
 *         // Wait and retry
 *         Thread.sleep(e.getRetryAfter() * 1000L);
 *     }
 * }
 * }</pre>
 */
public class RenderScreenshotException extends RuntimeException {

    private final int httpStatus;
    private final String code;
    private final boolean retryable;
    private final Integer retryAfter;

    /**
     * Constructs a new RenderScreenshotException.
     *
     * @param message    the error message
     * @param httpStatus the HTTP status code (0 for non-HTTP errors)
     * @param code       the API error code (may be null)
     * @param retryable  whether the request can be retried
     * @param retryAfter seconds to wait before retrying (may be null)
     */
    public RenderScreenshotException(String message, int httpStatus, String code,
                                     boolean retryable, Integer retryAfter) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.retryable = retryable;
        this.retryAfter = retryAfter;
    }

    /**
     * Constructs a new RenderScreenshotException with a cause.
     *
     * @param message    the error message
     * @param cause      the underlying cause
     * @param httpStatus the HTTP status code (0 for non-HTTP errors)
     * @param code       the API error code (may be null)
     * @param retryable  whether the request can be retried
     * @param retryAfter seconds to wait before retrying (may be null)
     */
    public RenderScreenshotException(String message, Throwable cause, int httpStatus, String code,
                                     boolean retryable, Integer retryAfter) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.code = code;
        this.retryable = retryable;
        this.retryAfter = retryAfter;
    }

    /**
     * Returns the HTTP status code associated with this error.
     *
     * @return the HTTP status code, or 0 for non-HTTP errors (e.g., timeout, network)
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * Returns the API error code.
     *
     * @return the error code (e.g., "invalid_url", "rate_limited"), or null if not available
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns whether this error is retryable.
     *
     * @return true if the request can be retried, false otherwise
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Returns the number of seconds to wait before retrying.
     *
     * @return seconds to wait, or null if not applicable
     */
    public Integer getRetryAfter() {
        return retryAfter;
    }

    /**
     * Creates an exception for invalid URL errors (400).
     *
     * @param url the invalid URL
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException invalidUrl(String url) {
        return new RenderScreenshotException("Invalid URL provided: " + url, 400, "invalid_url", false, null);
    }

    /**
     * Creates an exception for invalid request errors (400).
     *
     * @param message the error message
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException invalidRequest(String message) {
        return new RenderScreenshotException(message, 400, "invalid_request", false, null);
    }

    /**
     * Creates an exception for invalid request errors with a specific code.
     *
     * @param message the error message
     * @param code    the specific error code
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException invalidRequest(String message, String code) {
        return new RenderScreenshotException(message, 400, code, false, null);
    }

    /**
     * Creates an exception for unauthorized errors (401).
     *
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException unauthorized() {
        return new RenderScreenshotException("Invalid or missing API key", 401, "unauthorized", false, null);
    }

    /**
     * Creates an exception for forbidden errors (403).
     *
     * @param message the error message
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException forbidden(String message) {
        return new RenderScreenshotException(message, 403, "forbidden", false, null);
    }

    /**
     * Creates an exception for not found errors (404).
     *
     * @param message the error message
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException notFound(String message) {
        return new RenderScreenshotException(message, 404, "not_found", false, null);
    }

    /**
     * Creates an exception for rate limit errors (429).
     *
     * @param retryAfter seconds to wait before retrying
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException rateLimited(int retryAfter) {
        return new RenderScreenshotException(
                "Rate limit exceeded. Retry after " + retryAfter + " seconds",
                429,
                "rate_limited",
                true,
                retryAfter
        );
    }

    /**
     * Creates an exception for rate limit errors without retry information.
     *
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException rateLimited() {
        return new RenderScreenshotException(
                "Rate limit exceeded",
                429,
                "rate_limited",
                true,
                null
        );
    }

    /**
     * Creates an exception for timeout errors.
     *
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException timeout() {
        return new RenderScreenshotException("Request timed out", 0, "timeout", true, null);
    }

    /**
     * Creates an exception for timeout errors with a cause.
     *
     * @param cause the underlying timeout exception
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException timeout(Throwable cause) {
        return new RenderScreenshotException("Request timed out", cause, 0, "timeout", true, null);
    }

    /**
     * Creates an exception for network errors.
     *
     * @param message the error message
     * @param cause   the underlying network exception
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException network(String message, Throwable cause) {
        return new RenderScreenshotException(message, cause, 0, "network_error", true, null);
    }

    /**
     * Creates an exception for render failures (500).
     *
     * @param message the error message (optional, defaults to "Browser rendering failed")
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException renderFailed(String message) {
        String msg = message != null ? message : "Browser rendering failed";
        return new RenderScreenshotException(msg, 500, "render_failed", true, null);
    }

    /**
     * Creates an exception for render failures (500) with default message.
     *
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException renderFailed() {
        return renderFailed(null);
    }

    /**
     * Creates an exception for internal server errors (500).
     *
     * @param message the error message
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException internal(String message) {
        return new RenderScreenshotException(message, 500, "internal_error", true, null);
    }

    /**
     * Creates an exception from an HTTP response.
     *
     * @param status     the HTTP status code
     * @param message    the error message
     * @param code       the error code (may be null)
     * @param retryAfter seconds to wait before retrying (may be null)
     * @return a new RenderScreenshotException
     */
    public static RenderScreenshotException fromResponse(int status, String message, String code, Integer retryAfter) {
        boolean retryable = status == 429 || status >= 500;
        return new RenderScreenshotException(message, status, code, retryable, retryAfter);
    }
}
