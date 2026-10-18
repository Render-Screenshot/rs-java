package com.renderscreenshot.sdk;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookTest {

    private static final String TEST_SECRET = "whsec_test_secret_key";
    private static final String TEST_PAYLOAD = "{\"id\":\"evt_123\",\"type\":\"batch.completed\",\"data\":{}}";

    @Test
    void verify_validSignature_returnsTrue() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signature = computeSignature(timestamp + "." + TEST_PAYLOAD, TEST_SECRET);

        boolean result = Webhook.verify(TEST_PAYLOAD, signature, timestamp, TEST_SECRET);

        assertTrue(result);
    }

    @Test
    void verify_invalidSignature_returnsFalse() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        boolean result = Webhook.verify(TEST_PAYLOAD, "invalid_signature", timestamp, TEST_SECRET);

        assertFalse(result);
    }

    @Test
    void verify_wrongSecret_returnsFalse() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signature = computeSignature(timestamp + "." + TEST_PAYLOAD, TEST_SECRET);

        boolean result = Webhook.verify(TEST_PAYLOAD, signature, timestamp, "wrong_secret");

        assertFalse(result);
    }

    @Test
    void verify_expiredTimestamp_returnsFalse() {
        // Timestamp from 10 minutes ago (beyond default 5 min tolerance)
        String timestamp = String.valueOf(Instant.now().getEpochSecond() - 600);
        String signature = computeSignature(timestamp + "." + TEST_PAYLOAD, TEST_SECRET);

        boolean result = Webhook.verify(TEST_PAYLOAD, signature, timestamp, TEST_SECRET);

        assertFalse(result);
    }

    @Test
    void verify_futureTimestamp_returnsFalse() {
        // Timestamp 10 minutes in the future
        String timestamp = String.valueOf(Instant.now().getEpochSecond() + 600);
        String signature = computeSignature(timestamp + "." + TEST_PAYLOAD, TEST_SECRET);

        boolean result = Webhook.verify(TEST_PAYLOAD, signature, timestamp, TEST_SECRET);

        assertFalse(result);
    }

    @Test
    void verify_customTolerance_respectsTolerance() {
        // Timestamp 8 minutes ago
        String timestamp = String.valueOf(Instant.now().getEpochSecond() - 480);
        String signature = computeSignature(timestamp + "." + TEST_PAYLOAD, TEST_SECRET);

        // Should fail with default 5 min tolerance
        assertFalse(Webhook.verify(TEST_PAYLOAD, signature, timestamp, TEST_SECRET));

        // Should pass with 10 min tolerance
        assertTrue(Webhook.verify(TEST_PAYLOAD, signature, timestamp, TEST_SECRET, 600));
    }

    @Test
    void verify_nullPayload_returnsFalse() {
        assertFalse(Webhook.verify(null, "sig", "123", TEST_SECRET));
    }

    @Test
    void verify_nullSignature_returnsFalse() {
        assertFalse(Webhook.verify(TEST_PAYLOAD, null, "123", TEST_SECRET));
    }

    @Test
    void verify_nullTimestamp_returnsFalse() {
        assertFalse(Webhook.verify(TEST_PAYLOAD, "sig", null, TEST_SECRET));
    }

    @Test
    void verify_nullSecret_returnsFalse() {
        assertFalse(Webhook.verify(TEST_PAYLOAD, "sig", "123", null));
    }

    @Test
    void verify_invalidTimestamp_returnsFalse() {
        assertFalse(Webhook.verify(TEST_PAYLOAD, "sig", "not_a_number", TEST_SECRET));
    }

    @Test
    void verify_tampered_payload_returnsFalse() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signature = computeSignature(timestamp + "." + TEST_PAYLOAD, TEST_SECRET);

        // Try with tampered payload
        boolean result = Webhook.verify(
                TEST_PAYLOAD.replace("evt_123", "evt_456"),
                signature, timestamp, TEST_SECRET);

        assertFalse(result);
    }

    @Test
    void verify_acceptsSha256PrefixedHeader() {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signature = "sha256=" + computeSignature(timestamp + "." + TEST_PAYLOAD, TEST_SECRET);

        assertTrue(Webhook.verify(TEST_PAYLOAD, signature, timestamp, TEST_SECRET));
        assertFalse(Webhook.verify(TEST_PAYLOAD, "sha256=deadbeef", timestamp, TEST_SECRET));
    }

    // ========== parse() Tests ==========

    @Test
    void parse_realScreenshotCompletedPayload() {
        String payload = "{"
                + "\"event\": \"screenshot.completed\","
                + "\"id\": \"req_abc123\","
                + "\"timestamp\": \"2026-10-18T10:30:00Z\","
                + "\"data\": {\"image\": {\"url\": \"https://cdn.example.com/a.png\"}}"
                + "}";

        Webhook.WebhookEvent event = Webhook.parse(payload);

        assertEquals("req_abc123", event.getId());
        assertEquals("screenshot.completed", event.getEvent());
        assertEquals("screenshot.completed", event.getType());
        assertEquals("2026-10-18T10:30:00Z", event.getTimestamp());
        assertNotNull(event.getData());
        assertNull(event.getErrorCode());
    }

    @Test
    void parse_realFailedPayload_exposesErrorCode() {
        String payload = "{"
                + "\"event\": \"screenshot.failed\","
                + "\"id\": \"req_def456\","
                + "\"timestamp\": \"2026-10-18T10:31:00Z\","
                + "\"data\": {\"error\": \"Navigation timed out\", \"error_code\": \"timeout\"}"
                + "}";

        Webhook.WebhookEvent event = Webhook.parse(payload);

        assertEquals("screenshot.failed", event.getType());
        assertEquals("timeout", event.getErrorCode());
    }

    @Test
    void parse_realBatchPayload() {
        String payload = "{\"event\":\"batch.completed\",\"id\":\"batch_1\","
                + "\"timestamp\":\"2026-10-18T10:32:00Z\","
                + "\"data\":{\"summary\":{\"total\":2,\"completed\":2,\"failed\":0},"
                + "\"results_url\":\"https://api.renderscreenshot.com/v1/batch/batch_1\"}}";

        Webhook.WebhookEvent event = Webhook.parse(payload);

        assertEquals("batch_1", event.getId());
        assertEquals("batch.completed", event.getEvent());
    }

    @Test
    void parse_invalidJson_throwsException() {
        assertThrows(RenderScreenshotException.class, () -> Webhook.parse("not json"));
    }

    @Test
    void parse_unknownFields_ignored() {
        String payload = "{"
                + "\"id\": \"evt_123\","
                + "\"type\": \"test\","
                + "\"unknown_field\": \"value\""
                + "}";

        Webhook.WebhookEvent event = Webhook.parse(payload);

        assertEquals("evt_123", event.getId());
    }

    // ========== extractHeaders() Tests ==========

    @Test
    void extractHeaders_findsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Webhook-Signature", "sig_value");
        headers.put("X-Webhook-Timestamp", "12345");
        headers.put("Content-Type", "application/json");

        String[] result = Webhook.extractHeaders(headers);

        assertEquals("sig_value", result[0]);
        assertEquals("12345", result[1]);
    }

    @Test
    void extractHeaders_caseInsensitive() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-webhook-signature", "sig_value");
        headers.put("X-WEBHOOK-TIMESTAMP", "12345");

        String[] result = Webhook.extractHeaders(headers);

        assertEquals("sig_value", result[0]);
        assertEquals("12345", result[1]);
    }

    @Test
    void extractHeaders_missingHeaders_returnsNulls() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String[] result = Webhook.extractHeaders(headers);

        assertNull(result[0]);
        assertNull(result[1]);
    }

    // ========== WebhookEvent Tests ==========

    @Test
    void webhookEvent_toString_works() {
        Webhook.WebhookEvent event = new Webhook.WebhookEvent();
        event.setId("evt_123");
        event.setType("batch.completed");
        event.setCreatedAt("2024-01-15T10:30:00Z");

        String str = event.toString();

        assertTrue(str.contains("evt_123"));
        assertTrue(str.contains("batch.completed"));
    }

    // ========== Helper ==========

    private String computeSignature(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
