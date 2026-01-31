package com.renderscreenshot.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenderScreenshotExceptionTest {

    @Test
    void invalidUrl_createsNonRetryableException() {
        RenderScreenshotException e = RenderScreenshotException.invalidUrl("not-a-url");

        assertEquals("Invalid URL provided: not-a-url", e.getMessage());
        assertEquals(400, e.getHttpStatus());
        assertEquals("invalid_url", e.getCode());
        assertFalse(e.isRetryable());
        assertNull(e.getRetryAfter());
    }

    @Test
    void invalidRequest_createsNonRetryableException() {
        RenderScreenshotException e = RenderScreenshotException.invalidRequest("Invalid URL");

        assertEquals("Invalid URL", e.getMessage());
        assertEquals(400, e.getHttpStatus());
        assertEquals("invalid_request", e.getCode());
        assertFalse(e.isRetryable());
        assertNull(e.getRetryAfter());
    }

    @Test
    void invalidRequestWithCode_setsCustomCode() {
        RenderScreenshotException e = RenderScreenshotException.invalidRequest("Missing url parameter", "missing_url");

        assertEquals("Missing url parameter", e.getMessage());
        assertEquals(400, e.getHttpStatus());
        assertEquals("missing_url", e.getCode());
        assertFalse(e.isRetryable());
    }

    @Test
    void unauthorized_createsUnauthorizedException() {
        RenderScreenshotException e = RenderScreenshotException.unauthorized();

        assertEquals("Invalid or missing API key", e.getMessage());
        assertEquals(401, e.getHttpStatus());
        assertEquals("unauthorized", e.getCode());
        assertFalse(e.isRetryable());
    }

    @Test
    void forbidden_createsForbiddenException() {
        RenderScreenshotException e = RenderScreenshotException.forbidden("Access denied");

        assertEquals("Access denied", e.getMessage());
        assertEquals(403, e.getHttpStatus());
        assertEquals("forbidden", e.getCode());
        assertFalse(e.isRetryable());
    }

    @Test
    void notFound_createsNotFoundException() {
        RenderScreenshotException e = RenderScreenshotException.notFound("Resource not found");

        assertEquals("Resource not found", e.getMessage());
        assertEquals(404, e.getHttpStatus());
        assertEquals("not_found", e.getCode());
        assertFalse(e.isRetryable());
    }

    @Test
    void rateLimited_createsRetryableExceptionWithRetryAfter() {
        RenderScreenshotException e = RenderScreenshotException.rateLimited(60);

        assertEquals("Rate limit exceeded. Retry after 60 seconds", e.getMessage());
        assertEquals(429, e.getHttpStatus());
        assertEquals("rate_limited", e.getCode());
        assertTrue(e.isRetryable());
        assertEquals(60, e.getRetryAfter());
    }

    @Test
    void rateLimitedWithoutRetryAfter_createsRetryableException() {
        RenderScreenshotException e = RenderScreenshotException.rateLimited();

        assertEquals("Rate limit exceeded", e.getMessage());
        assertEquals(429, e.getHttpStatus());
        assertEquals("rate_limited", e.getCode());
        assertTrue(e.isRetryable());
        assertNull(e.getRetryAfter());
    }

    @Test
    void timeout_createsRetryableException() {
        RenderScreenshotException e = RenderScreenshotException.timeout();

        assertEquals("Request timed out", e.getMessage());
        assertEquals(0, e.getHttpStatus());
        assertEquals("timeout", e.getCode());
        assertTrue(e.isRetryable());
    }

    @Test
    void timeoutWithCause_preservesCause() {
        Exception cause = new RuntimeException("Connection timeout");
        RenderScreenshotException e = RenderScreenshotException.timeout(cause);

        assertEquals("Request timed out", e.getMessage());
        assertEquals(cause, e.getCause());
        assertTrue(e.isRetryable());
    }

    @Test
    void network_createsRetryableException() {
        Exception cause = new RuntimeException("Connection refused");
        RenderScreenshotException e = RenderScreenshotException.network("Network error", cause);

        assertEquals("Network error", e.getMessage());
        assertEquals(cause, e.getCause());
        assertEquals(0, e.getHttpStatus());
        assertEquals("network_error", e.getCode());
        assertTrue(e.isRetryable());
    }

    @Test
    void renderFailed_createsRetryableException() {
        RenderScreenshotException e = RenderScreenshotException.renderFailed("Browser crashed");

        assertEquals("Browser crashed", e.getMessage());
        assertEquals(500, e.getHttpStatus());
        assertEquals("render_failed", e.getCode());
        assertTrue(e.isRetryable());
    }

    @Test
    void renderFailedNoMessage_usesDefaultMessage() {
        RenderScreenshotException e = RenderScreenshotException.renderFailed();

        assertEquals("Browser rendering failed", e.getMessage());
        assertEquals(500, e.getHttpStatus());
        assertEquals("render_failed", e.getCode());
        assertTrue(e.isRetryable());
    }

    @Test
    void internal_createsRetryableException() {
        RenderScreenshotException e = RenderScreenshotException.internal("Server error");

        assertEquals("Server error", e.getMessage());
        assertEquals(500, e.getHttpStatus());
        assertEquals("internal_error", e.getCode());
        assertTrue(e.isRetryable());
    }

    @Test
    void fromResponse_4xxNotRetryable() {
        RenderScreenshotException e = RenderScreenshotException.fromResponse(
                400, "Bad request", "bad_request", null);

        assertEquals("Bad request", e.getMessage());
        assertEquals(400, e.getHttpStatus());
        assertEquals("bad_request", e.getCode());
        assertFalse(e.isRetryable());
    }

    @Test
    void fromResponse_429IsRetryable() {
        RenderScreenshotException e = RenderScreenshotException.fromResponse(
                429, "Too many requests", "rate_limited", 30);

        assertEquals("Too many requests", e.getMessage());
        assertEquals(429, e.getHttpStatus());
        assertEquals("rate_limited", e.getCode());
        assertTrue(e.isRetryable());
        assertEquals(30, e.getRetryAfter());
    }

    @Test
    void fromResponse_5xxIsRetryable() {
        RenderScreenshotException e = RenderScreenshotException.fromResponse(
                502, "Bad gateway", null, null);

        assertEquals("Bad gateway", e.getMessage());
        assertEquals(502, e.getHttpStatus());
        assertNull(e.getCode());
        assertTrue(e.isRetryable());
    }

    @Test
    void constructor_preservesAllFields() {
        RenderScreenshotException e = new RenderScreenshotException(
                "Test message", 418, "teapot", true, 10);

        assertEquals("Test message", e.getMessage());
        assertEquals(418, e.getHttpStatus());
        assertEquals("teapot", e.getCode());
        assertTrue(e.isRetryable());
        assertEquals(10, e.getRetryAfter());
    }

    @Test
    void constructorWithCause_preservesCause() {
        Exception cause = new RuntimeException("Original error");
        RenderScreenshotException e = new RenderScreenshotException(
                "Wrapped message", cause, 500, "wrapped", true, null);

        assertEquals("Wrapped message", e.getMessage());
        assertEquals(cause, e.getCause());
        assertNotNull(e.getCause());
    }
}
