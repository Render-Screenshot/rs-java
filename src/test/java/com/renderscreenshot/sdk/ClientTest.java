package com.renderscreenshot.sdk;

import com.renderscreenshot.sdk.model.BatchResponse;
import com.renderscreenshot.sdk.model.Device;
import com.renderscreenshot.sdk.model.Preset;
import com.renderscreenshot.sdk.model.ScreenshotResponse;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientTest {

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

    // ========== Constructor Tests ==========

    @Test
    void constructor_requiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new Client(null));
        assertThrows(IllegalArgumentException.class, () -> new Client(""));
        assertThrows(IllegalArgumentException.class, () -> new Client("   "));
    }

    @Test
    void constructor_acceptsValidApiKey() {
        Client c = new Client("valid_key");
        assertNotNull(c);
    }

    // ========== take() Tests ==========

    @Test
    void take_returnsBinaryData() throws Exception {
        byte[] imageData = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
        server.enqueue(new MockResponse()
                .setBody(new okio.Buffer().write(imageData))
                .addHeader("Content-Type", "image/png"));

        byte[] result = client.take(TakeOptions.url("https://example.com"));

        assertArrayEquals(imageData, result);

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/v1/screenshot", request.getPath());
        assertTrue(request.getHeader("Authorization").contains("Bearer test_api_key"));
        assertTrue(request.getHeader("User-Agent").contains("renderscreenshot-java"));
    }

    @Test
    void take_sendsCorrectBody() throws Exception {
        server.enqueue(new MockResponse().setBody("image data"));

        client.take(TakeOptions.url("https://example.com")
                .width(1200)
                .height(630)
                .format("png"));

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"url\":\"https://example.com\""));
        assertTrue(body.contains("\"format\":\"png\""));
        assertTrue(body.contains("\"viewport\":{"));
        assertTrue(body.contains("\"width\":1200"));
        assertTrue(body.contains("\"height\":630"));
    }

    // ========== takeJson() Tests ==========

    @Test
    void takeJson_returnsScreenshotResponse() throws Exception {
        String jsonResponse = "{"
                + "\"url\": \"https://cdn.example.com/screenshot.png\","
                + "\"cache_url\": \"https://cache.example.com/abc123.png\","
                + "\"width\": 1200,"
                + "\"height\": 630,"
                + "\"format\": \"png\","
                + "\"size\": 45678,"
                + "\"cache_key\": \"abc123\","
                + "\"ttl\": 86400,"
                + "\"cached\": false"
                + "}";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        ScreenshotResponse response = client.takeJson(TakeOptions.url("https://example.com"));

        assertEquals("https://cdn.example.com/screenshot.png", response.getUrl());
        assertEquals("https://cache.example.com/abc123.png", response.getCacheUrl());
        assertEquals(1200, response.getWidth());
        assertEquals(630, response.getHeight());
        assertEquals("png", response.getFormat());
        assertEquals(45678, response.getSize());
        assertEquals("abc123", response.getCacheKey());
        assertEquals(86400, response.getTtl());
        assertFalse(response.isCached());
    }

    @Test
    void takeJson_sendsResponseTypeJson() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        client.takeJson(TakeOptions.url("https://example.com"));

        RecordedRequest request = server.takeRequest();
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"response_type\":\"json\""));
    }

    // ========== generateUrl() Tests ==========

    @Test
    void generateUrl_createsSignedUrl() {
        Client c = new Client("secret_key", "https://api.renderscreenshot.com", Duration.ofSeconds(30));
        Instant expires = Instant.ofEpochSecond(1700000000);

        String url = c.generateUrl(TakeOptions.url("https://example.com"), expires);

        assertTrue(url.startsWith("https://api.renderscreenshot.com/v1/screenshot?"));
        assertTrue(url.contains("url=https%3A%2F%2Fexample.com"));
        assertTrue(url.contains("expires=1700000000"));
        assertTrue(url.contains("signature="));
    }

    @Test
    void generateUrl_includesAllOptions() {
        Client c = new Client("secret_key", "https://api.renderscreenshot.com", Duration.ofSeconds(30));
        Instant expires = Instant.ofEpochSecond(1700000000);

        String url = c.generateUrl(
                TakeOptions.url("https://example.com").width(800).height(600),
                expires);

        assertTrue(url.contains("width=800"));
        assertTrue(url.contains("height=600"));
    }

    // ========== batch() Tests ==========

    @Test
    void batch_returnsResponse() throws Exception {
        String jsonResponse = "{"
                + "\"id\": \"batch_123\","
                + "\"status\": \"processing\","
                + "\"total\": 3,"
                + "\"completed\": 1,"
                + "\"failed\": 0,"
                + "\"results\": []"
                + "}";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        BatchResponse response = client.batch(
                Arrays.asList("https://a.com", "https://b.com", "https://c.com"),
                TakeOptions.url("dummy").width(800));

        assertEquals("batch_123", response.getId());
        assertEquals("processing", response.getStatus());
        assertEquals(3, response.getTotal());
        assertEquals(1, response.getCompleted());
        assertEquals(0, response.getFailed());
        assertFalse(response.isComplete());
    }

    @Test
    void batch_sendsCorrectBody() throws Exception {
        server.enqueue(new MockResponse().setBody("{}"));

        client.batch(Arrays.asList("https://a.com", "https://b.com"), null);

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/v1/batch", request.getPath());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"urls\":[\"https://a.com\",\"https://b.com\"]"));
    }

    // ========== getBatch() Tests ==========

    @Test
    void getBatch_returnsBatchStatus() throws Exception {
        String jsonResponse = "{"
                + "\"id\": \"batch_123\","
                + "\"status\": \"completed\","
                + "\"total\": 2,"
                + "\"completed\": 2,"
                + "\"failed\": 0,"
                + "\"results\": ["
                + "{\"url\": \"https://a.com\", \"success\": true, \"response\": {\"url\": \"https://cdn/a.png\"}},"
                + "{\"url\": \"https://b.com\", \"success\": true, \"response\": {\"url\": \"https://cdn/b.png\"}}"
                + "]"
                + "}";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        BatchResponse response = client.getBatch("batch_123");

        assertEquals("batch_123", response.getId());
        assertEquals("completed", response.getStatus());
        assertTrue(response.isComplete());
        assertEquals(2, response.getResults().size());
        assertTrue(response.getResults().get(0).isSuccess());
    }

    @Test
    void getBatch_sendsCorrectRequest() throws Exception {
        server.enqueue(new MockResponse().setBody("{}"));

        client.getBatch("batch_xyz");

        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v1/batch/batch_xyz", request.getPath());
    }

    // ========== presets() Tests ==========

    @Test
    void presets_returnsList() throws Exception {
        String jsonResponse = "["
                + "{\"id\": \"og_card\", \"name\": \"Open Graph Card\", \"width\": 1200, \"height\": 630},"
                + "{\"id\": \"twitter_card\", \"name\": \"Twitter Card\", \"width\": 800, \"height\": 418}"
                + "]";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        List<Preset> presets = client.presets();

        assertEquals(2, presets.size());
        assertEquals("og_card", presets.get(0).getId());
        assertEquals("Open Graph Card", presets.get(0).getName());
        assertEquals(1200, presets.get(0).getWidth());
        assertEquals(630, presets.get(0).getHeight());
    }

    // ========== preset() Tests ==========

    @Test
    void preset_returnsSinglePreset() throws Exception {
        String jsonResponse = "{\"id\": \"og_card\", \"name\": \"Open Graph Card\", "
                + "\"width\": 1200, \"height\": 630, \"format\": \"png\"}";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        Preset preset = client.preset("og_card");

        assertEquals("og_card", preset.getId());
        assertEquals("png", preset.getFormat());
    }

    // ========== devices() Tests ==========

    @Test
    void devices_returnsList() throws Exception {
        String jsonResponse = "["
                + "{\"id\": \"iphone_14\", \"name\": \"iPhone 14\", \"width\": 390, "
                + "\"height\": 844, \"scale\": 3.0, \"mobile\": true},"
                + "{\"id\": \"pixel_7\", \"name\": \"Pixel 7\", \"width\": 412, "
                + "\"height\": 915, \"scale\": 2.625, \"mobile\": true}"
                + "]";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        List<Device> devices = client.devices();

        assertEquals(2, devices.size());
        assertEquals("iphone_14", devices.get(0).getId());
        assertEquals("iPhone 14", devices.get(0).getName());
        assertEquals(390, devices.get(0).getWidth());
        assertEquals(3.0, devices.get(0).getScale());
        assertTrue(devices.get(0).isMobile());
    }

    // ========== Error Handling Tests ==========

    @Test
    void take_handles400Error() {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"message\": \"Invalid URL\", \"code\": \"invalid_url\"}")
                .addHeader("Content-Type", "application/json"));

        RenderScreenshotException e = assertThrows(
                RenderScreenshotException.class,
                () -> client.take(TakeOptions.url("invalid")));

        assertEquals(400, e.getHttpStatus());
        assertEquals("Invalid URL", e.getMessage());
        assertEquals("invalid_url", e.getCode());
        assertFalse(e.isRetryable());
    }

    @Test
    void take_handles401Error() {
        server.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"message\": \"Unauthorized\"}"));

        RenderScreenshotException e = assertThrows(
                RenderScreenshotException.class,
                () -> client.take(TakeOptions.url("https://example.com")));

        assertEquals(401, e.getHttpStatus());
        assertFalse(e.isRetryable());
    }

    @Test
    void take_handles429ErrorWithRetryAfter() {
        server.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("{\"message\": \"Rate limited\"}")
                .addHeader("Retry-After", "60"));

        RenderScreenshotException e = assertThrows(
                RenderScreenshotException.class,
                () -> client.take(TakeOptions.url("https://example.com")));

        assertEquals(429, e.getHttpStatus());
        assertTrue(e.isRetryable());
        assertEquals(60, e.getRetryAfter());
    }

    @Test
    void take_handles500Error() {
        server.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"message\": \"Internal server error\"}"));

        RenderScreenshotException e = assertThrows(
                RenderScreenshotException.class,
                () -> client.take(TakeOptions.url("https://example.com")));

        assertEquals(500, e.getHttpStatus());
        assertTrue(e.isRetryable());
    }

    @Test
    void take_handlesNonJsonError() {
        server.enqueue(new MockResponse()
                .setResponseCode(502)
                .setBody("Bad Gateway"));

        RenderScreenshotException e = assertThrows(
                RenderScreenshotException.class,
                () -> client.take(TakeOptions.url("https://example.com")));

        assertEquals(502, e.getHttpStatus());
        assertEquals("Bad Gateway", e.getMessage());
    }

    // ========== Headers Tests ==========

    @Test
    void take_sendsCorrectHeaders() throws Exception {
        server.enqueue(new MockResponse().setBody("data"));

        client.take(TakeOptions.url("https://example.com"));

        RecordedRequest request = server.takeRequest();
        assertEquals("Bearer test_api_key", request.getHeader("Authorization"));
        assertTrue(request.getHeader("User-Agent").startsWith("renderscreenshot-java/"));
        assertTrue(request.getHeader("Content-Type").startsWith("application/json"));
    }
}
