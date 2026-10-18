package com.renderscreenshot.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void takeJson_parsesRealResponseShape() throws Exception {
        String jsonResponse = "{"
                + "\"id\": \"req_ab12cd34\","
                + "\"status\": \"completed\","
                + "\"image\": {\"url\": \"https://cdn.example.com/screenshot.png\","
                + "\"width\": 1200, \"height\": 630, \"size\": 45678, \"format\": \"png\"},"
                + "\"metadata\": {\"title\": \"Screenshot\"},"
                + "\"cache\": {\"hit\": false, \"key\": \"screenshots/abc123.png\","
                + "\"url\": \"https://cdn.example.com/screenshot.png\","
                + "\"expires_at\": \"2026-10-19T12:00:00Z\"},"
                + "\"usage\": {\"credits\": 1, \"remaining\": 99}"
                + "}";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        ScreenshotResponse response = client.takeJson(TakeOptions.url("https://example.com"));

        assertEquals("req_ab12cd34", response.getId());
        assertEquals("completed", response.getStatus());
        assertEquals("https://cdn.example.com/screenshot.png", response.getImage().getUrl());
        assertEquals(1200, response.getImage().getWidth());
        assertEquals(630, response.getImage().getHeight());
        assertEquals(45678L, response.getImage().getSize());
        assertEquals("png", response.getImage().getFormat());
        assertEquals("Screenshot", response.getMetadata().get("title"));
        assertFalse(response.getCache().isHit());
        assertEquals("screenshots/abc123.png", response.getCache().getKey());
        assertEquals("https://cdn.example.com/screenshot.png", response.getCache().getUrl());
        assertEquals("2026-10-19T12:00:00Z", response.getCache().getExpiresAt());
        assertEquals(1, response.getUsage().getCredits());
        assertEquals(99, response.getUsage().getRemaining());

        // Shortcut getters read the nested objects
        assertEquals("https://cdn.example.com/screenshot.png", response.getUrl());
        assertEquals("https://cdn.example.com/screenshot.png", response.getCacheUrl());
        assertEquals(1200, response.getWidth());
        assertEquals(630, response.getHeight());
        assertEquals(45678L, response.getSize());
        assertEquals("png", response.getFormat());
        assertEquals("screenshots/abc123.png", response.getCacheKey());
        assertFalse(response.isCached());
    }

    @Test
    void takeJson_handlesMissingOptionalSections() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"id\":\"job_1\",\"status\":\"processing\",\"webhook\":{\"url\":\"https://h.example\"}}")
                .addHeader("Content-Type", "application/json"));

        ScreenshotResponse response = client.takeJson(TakeOptions.url("https://example.com"));

        assertEquals("job_1", response.getId());
        assertEquals("processing", response.getStatus());
        assertNull(response.getImage());
        assertNull(response.getCache());
        assertNull(response.getUsage());
        assertNull(response.getUrl());
        assertEquals(0, response.getWidth());
        assertFalse(response.isCached());
    }

    @Test
    void takeJson_sendsAcceptJsonHeader() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        client.takeJson(TakeOptions.url("https://example.com").fullPage());

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("application/json", request.getHeader("Accept"));
        String body = request.getBody().readUtf8();
        assertFalse(body.contains("response_type"));
        assertTrue(body.contains("\"capture\":{\"mode\":\"full_page\"}"));
    }

    @Test
    void take_doesNotSendAcceptJsonHeader() throws Exception {
        server.enqueue(new MockResponse().setBody("image data"));

        client.take(TakeOptions.url("https://example.com"));

        RecordedRequest request = server.takeRequest();
        assertFalse("application/json".equals(request.getHeader("Accept")));
    }

    @Test
    void take_sendsNestedJsonBody() throws Exception {
        server.enqueue(new MockResponse().setBody("image data"));

        client.take(TakeOptions.url("https://example.com")
                .width(1200)
                .height(630)
                .fullPage()
                .element("#main")
                .format("jpeg")
                .quality(80)
                .waitForSelector(".ready")
                .blockAds()
                .blockCookieBanners()
                .darkMode()
                .cacheTtl(3600)
                .cacheRefresh());

        RecordedRequest request = server.takeRequest();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = new ObjectMapper().readValue(request.getBody().readUtf8(), Map.class);

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("url", "https://example.com");
        expected.put("viewport", map("width", 1200, "height", 630));
        expected.put("capture", map("mode", "full_page", "selector", "#main"));
        expected.put("output", map("format", "jpeg", "quality", 80));
        expected.put("wait", map("for_selector", ".ready"));
        expected.put("block", map("ads", true, "cookie_banners", true));
        expected.put("browser", map("dark_mode", true));
        expected.put("cache", map("ttl", 3600, "refresh", true));
        assertEquals(expected, body);
    }

    @Test
    void batch_sendsNestedSharedOptions() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"id\":\"batch_1\",\"status\":\"processing\"}")
                .addHeader("Content-Type", "application/json"));

        client.batch(Arrays.asList("https://a.com", "https://b.com"),
                TakeOptions.url("dummy").width(800).format("webp").blockAds());

        RecordedRequest request = server.takeRequest();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = new ObjectMapper().readValue(request.getBody().readUtf8(), Map.class);
        Map<String, Object> expectedOptions = new LinkedHashMap<>();
        expectedOptions.put("viewport", map("width", 800));
        expectedOptions.put("output", map("format", "webp"));
        expectedOptions.put("block", map("ads", true));
        assertEquals(Arrays.asList("https://a.com", "https://b.com"), body.get("urls"));
        assertEquals(expectedOptions, body.get("options"));
    }

    @Test
    void batchAdvanced_sendsNestedPerRequestOptions() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"id\":\"batch_2\",\"status\":\"processing\"}")
                .addHeader("Content-Type", "application/json"));

        client.batchAdvanced(Arrays.asList(
                new Client.BatchRequest("https://a.com", TakeOptions.url("ignored").width(1200).fullPage()),
                new Client.BatchRequest("https://b.com")));

        RecordedRequest request = server.takeRequest();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = new ObjectMapper().readValue(request.getBody().readUtf8(), Map.class);
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("url", "https://a.com");
        first.put("viewport", map("width", 1200));
        first.put("capture", map("mode", "full_page"));
        assertEquals(Arrays.asList(first, map("url", "https://b.com")), body.get("requests"));
    }

    private static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    // ========== generateUrl() Tests ==========

    private static final Instant VECTOR_EXPIRES = Instant.ofEpochSecond(1893456000L);

    private static Client signingClient() {
        return new Client("rs_live_unused", "https://api.renderscreenshot.com", Duration.ofSeconds(30),
                "rs_secret_vector456", "rs_pub_vector123");
    }

    @Test
    void generateUrl_matchesCrossCheckVectorV1() {
        TakeOptions options = TakeOptions.url("https://example.com/path?a=1&b=two words")
                .width(1200)
                .height(630)
                .format("png")
                .fullPage();

        String url = signingClient().generateUrl(options, VECTOR_EXPIRES);

        assertEquals("https://api.renderscreenshot.com/v1/screenshot?expires=1893456000&format=png"
                + "&full_page=true&height=630&key_id=rs_pub_vector123"
                + "&url=https%3A%2F%2Fexample.com%2Fpath%3Fa%3D1%26b%3Dtwo+words&width=1200"
                + "&signature=ff30f019a228e751b71f4bdca8c5341c33eaac0985a18426882525de00ba329a", url);
    }

    @Test
    void generateUrl_matchesCrossCheckVectorV2() {
        TakeOptions options = TakeOptions.url("https://example.com/caf\u00e9?q=a b&x=~*()!'")
                .width(1200);

        String url = signingClient().generateUrl(options, VECTOR_EXPIRES);

        assertEquals("https://api.renderscreenshot.com/v1/screenshot?expires=1893456000"
                + "&key_id=rs_pub_vector123"
                + "&url=https%3A%2F%2Fexample.com%2Fcaf%C3%A9%3Fq%3Da+b%26x%3D~%2A%28%29%21%27&width=1200"
                + "&signature=8c53d62d192cd5423ea612eb08bbe2df25e22ee89b8f52634be4b8f0812a0e14", url);
    }

    @Test
    void generateUrl_explicitCredentialsOverload() {
        Client c = new Client("rs_live_unused", "https://api.renderscreenshot.com", Duration.ofSeconds(30));
        TakeOptions options = TakeOptions.url("https://example.com/caf\u00e9?q=a b&x=~*()!'")
                .width(1200);

        String url = c.generateUrl(options, VECTOR_EXPIRES, "rs_secret_vector456", "rs_pub_vector123");

        assertTrue(url.endsWith(
                "&signature=8c53d62d192cd5423ea612eb08bbe2df25e22ee89b8f52634be4b8f0812a0e14"));
    }

    @Test
    void generateUrl_requiresSigningCredentials() {
        Client c = new Client("rs_live_key", "https://api.renderscreenshot.com", Duration.ofSeconds(30));
        TakeOptions options = TakeOptions.url("https://example.com");

        RenderScreenshotException e = assertThrows(RenderScreenshotException.class,
                () -> c.generateUrl(options, VECTOR_EXPIRES));
        assertEquals("invalid_request", e.getCode());
        assertThrows(RenderScreenshotException.class,
                () -> c.generateUrl(options, VECTOR_EXPIRES, "rs_secret_only", null));
        assertThrows(RenderScreenshotException.class,
                () -> c.generateUrl(options, VECTOR_EXPIRES, null, "rs_pub_only"));
    }

    @Test
    void generateUrl_includesAllOptions() {
        Client c = signingClient();
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
                + "{\"url\": \"https://a.com\", \"status\": \"completed\", \"image\": {\"image_url\": \"https://cdn/a.png\"}, \"error\": null},"
                + "{\"url\": \"https://b.com\", \"status\": \"completed\", \"image\": {\"image_url\": \"https://cdn/b.png\"}, \"error\": null}"
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
