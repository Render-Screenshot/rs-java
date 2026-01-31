package com.renderscreenshot.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;

/**
 * Utilities for verifying and parsing webhook payloads.
 *
 * <p>RenderScreenshot sends webhooks for batch completion and other events.
 * This class helps verify that webhooks are authentic and parse their payloads.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // In your webhook handler
 * String payload = request.getBody();
 * String signature = request.getHeader("X-RenderScreenshot-Signature");
 * String timestamp = request.getHeader("X-RenderScreenshot-Timestamp");
 *
 * if (Webhook.verify(payload, signature, timestamp, webhookSecret)) {
 *     WebhookEvent event = Webhook.parse(payload);
 *     System.out.println("Received event: " + event.getType());
 * }
 * }</pre>
 */
public final class Webhook {

    private static final int DEFAULT_TOLERANCE_SECONDS = 300; // 5 minutes
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Webhook() {
        // Utility class, prevent instantiation
    }

    /**
     * Verifies a webhook signature.
     *
     * @param payload   the raw request body
     * @param signature the X-RenderScreenshot-Signature header
     * @param timestamp the X-RenderScreenshot-Timestamp header
     * @param secret    your webhook secret
     * @return true if the signature is valid, false otherwise
     */
    public static boolean verify(String payload, String signature, String timestamp, String secret) {
        return verify(payload, signature, timestamp, secret, DEFAULT_TOLERANCE_SECONDS);
    }

    /**
     * Verifies a webhook signature with custom timestamp tolerance.
     *
     * @param payload          the raw request body
     * @param signature        the X-RenderScreenshot-Signature header
     * @param timestamp        the X-RenderScreenshot-Timestamp header
     * @param secret           your webhook secret
     * @param toleranceSeconds maximum allowed age of the webhook in seconds
     * @return true if the signature is valid, false otherwise
     */
    public static boolean verify(String payload, String signature, String timestamp, String secret,
                                 int toleranceSeconds) {
        if (payload == null || signature == null || timestamp == null || secret == null) {
            return false;
        }

        // Verify timestamp is within tolerance
        try {
            long timestampSeconds = Long.parseLong(timestamp);
            long nowSeconds = Instant.now().getEpochSecond();
            if (Math.abs(nowSeconds - timestampSeconds) > toleranceSeconds) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        // Compute expected signature
        String signedPayload = timestamp + "." + payload;
        String expectedSignature = computeHmac(signedPayload, secret);

        if (expectedSignature == null) {
            return false;
        }

        // Constant-time comparison to prevent timing attacks
        return constantTimeEquals(signature, expectedSignature);
    }

    /**
     * Parses a webhook payload into an event object.
     *
     * @param payload the raw request body
     * @return the parsed webhook event
     * @throws RenderScreenshotException if parsing fails
     */
    public static WebhookEvent parse(String payload) {
        try {
            return OBJECT_MAPPER.readValue(payload, WebhookEvent.class);
        } catch (Exception e) {
            throw RenderScreenshotException.invalidRequest("Failed to parse webhook payload: " + e.getMessage());
        }
    }

    /**
     * Extracts webhook headers from a map of HTTP headers.
     *
     * <p>This helper handles case-insensitive header lookup.</p>
     *
     * @param headers map of HTTP headers
     * @return array of [signature, timestamp], with nulls for missing headers
     */
    public static String[] extractHeaders(Map<String, String> headers) {
        String signature = null;
        String timestamp = null;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.equals("x-renderscreenshot-signature")) {
                signature = entry.getValue();
            } else if (key.equals("x-renderscreenshot-timestamp")) {
                timestamp = entry.getValue();
            }
        }

        return new String[]{signature, timestamp};
    }

    private static String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * A webhook event from RenderScreenshot.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookEvent {

        private String id;
        private String type;
        private Object data;

        @JsonProperty("created_at")
        private String createdAt;

        /** Default constructor for Jackson. */
        public WebhookEvent() {
        }

        /**
         * Returns the unique event ID.
         *
         * @return event ID
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the event ID.
         *
         * @param id event ID
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Returns the event type.
         *
         * @return event type (e.g., "batch.completed", "screenshot.ready")
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the event type.
         *
         * @param type event type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Returns the event data.
         *
         * <p>The structure depends on the event type. Cast to Map or use Jackson
         * to deserialize to a specific type.</p>
         *
         * @return event data
         */
        public Object getData() {
            return data;
        }

        /**
         * Sets the event data.
         *
         * @param data event data
         */
        public void setData(Object data) {
            this.data = data;
        }

        /**
         * Returns when the event was created.
         *
         * @return ISO 8601 timestamp
         */
        public String getCreatedAt() {
            return createdAt;
        }

        /**
         * Sets the created at timestamp.
         *
         * @param createdAt ISO 8601 timestamp
         */
        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        @Override
        public String toString() {
            return "WebhookEvent{id='" + id + "', type='" + type + "', createdAt='" + createdAt + "'}";
        }
    }
}
