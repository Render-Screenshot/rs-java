package com.renderscreenshot.sdk;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheManagerTest {

    private MockWebServer server;
    private Client client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        String baseUrl = server.url("/").toString();
        // Remove trailing slash to avoid double-slash in paths
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        client = new Client("test_api_key", baseUrl, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    // ========== get() Tests ==========

    @Test
    void get_returnsCachedData() throws Exception {
        byte[] imageData = new byte[]{1, 2, 3, 4, 5};
        server.enqueue(new MockResponse()
                .setBody(new okio.Buffer().write(imageData))
                .addHeader("Content-Type", "image/png"));

        byte[] result = client.cache.get("cache_key_123");

        assertArrayEquals(imageData, result);

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v1/cache/cache_key_123", request.getPath());
        assertTrue(request.getHeader("Authorization").contains("Bearer test_api_key"));
    }

    @Test
    void get_returns_null_for404() {
        server.enqueue(new MockResponse().setResponseCode(404));

        byte[] result = client.cache.get("nonexistent_key");

        assertNull(result);
    }

    @Test
    void get_throwsOnOtherErrors() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"message\": \"Server error\"}"));

        RenderScreenshotException e = assertThrows(
                RenderScreenshotException.class,
                () -> client.cache.get("key"));

        assertEquals(500, e.getHttpStatus());
    }

    // ========== delete() Tests ==========

    @Test
    void delete_returnsTrueOnSuccess() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(204));

        boolean result = client.cache.delete("cache_key_123");

        assertTrue(result);

        RecordedRequest request = server.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/v1/cache/cache_key_123", request.getPath());
    }

    @Test
    void delete_returnsFalseFor404() {
        server.enqueue(new MockResponse().setResponseCode(404));

        boolean result = client.cache.delete("nonexistent_key");

        assertFalse(result);
    }

    @Test
    void delete_throwsOnOtherErrors() {
        server.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("{\"message\": \"Forbidden\"}"));

        RenderScreenshotException e = assertThrows(
                RenderScreenshotException.class,
                () -> client.cache.delete("key"));

        assertEquals(403, e.getHttpStatus());
    }

    // ========== purge() Tests ==========

    @Test
    void purge_byKeys_returnsResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"purged\": 3, \"keys\": [\"key1\", \"key2\", \"key3\"]}")
                .addHeader("Content-Type", "application/json"));

        CacheManager.PurgeResponse result = client.cache.purge(
                Arrays.asList("key1", "key2", "key3"));

        assertEquals(3, result.getPurged());
        assertEquals(Arrays.asList("key1", "key2", "key3"), result.getKeys());

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/v1/cache/purge", request.getPath());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"keys\":[\"key1\",\"key2\",\"key3\"]"));
    }

    @Test
    void purgeUrl_byPattern_returnsResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"purged\": 10, \"keys\": []}")
                .addHeader("Content-Type", "application/json"));

        CacheManager.PurgeResponse result = client.cache.purgeUrl("https://example.com/*");

        assertEquals(10, result.getPurged());

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"url\":\"https://example.com/*\""));
    }

    @Test
    void purgeBefore_byDate_returnsResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"purged\": 50, \"keys\": []}")
                .addHeader("Content-Type", "application/json"));

        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        CacheManager.PurgeResponse result = client.cache.purgeBefore(before);

        assertEquals(50, result.getPurged());

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"before\":\"2024-01-01T00:00:00Z\""));
    }

    @Test
    void purgePattern_byStoragePath_returnsResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"purged\": 25, \"keys\": []}")
                .addHeader("Content-Type", "application/json"));

        CacheManager.PurgeResponse result = client.cache.purgePattern("screenshots/2024/*");

        assertEquals(25, result.getPurged());

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"pattern\":\"screenshots/2024/*\""));
    }

    @Test
    void purge_throwsOnError() {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid keys\"}"));

        RenderScreenshotException e = assertThrows(
                RenderScreenshotException.class,
                () -> client.cache.purge(Arrays.asList("invalid")));

        assertEquals(400, e.getHttpStatus());
        assertEquals("Invalid keys", e.getMessage());
    }

    // ========== PurgeResponse Tests ==========

    @Test
    void purgeResponse_toString_works() {
        CacheManager.PurgeResponse response = new CacheManager.PurgeResponse();
        response.setPurged(5);
        response.setKeys(Arrays.asList("key1", "key2"));

        String str = response.toString();

        assertTrue(str.contains("5"));
        assertTrue(str.contains("key1"));
    }
}
