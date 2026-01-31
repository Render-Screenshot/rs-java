package com.renderscreenshot.sdk.examples;

import com.renderscreenshot.sdk.Client;
import com.renderscreenshot.sdk.RenderScreenshotException;
import com.renderscreenshot.sdk.TakeOptions;
import com.renderscreenshot.sdk.model.ScreenshotResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests that demonstrate quick start examples from the README.
 */
class QuickStartTest {

    private MockWebServer server;
    private Client client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        String baseUrl = server.url("/").toString();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        client = new Client("test_api_key", baseUrl, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    /**
     * Example: Basic screenshot capture.
     */
    @Test
    void basicScreenshot() {
        // Mock response
        server.enqueue(new MockResponse()
                .setBody(new okio.Buffer().write(new byte[]{(byte) 0x89, 'P', 'N', 'G'}))
                .addHeader("Content-Type", "image/png"));

        // Example code from README
        byte[] screenshot = client.take(TakeOptions.url("https://example.com"));

        // Would normally save: Files.write(Path.of("screenshot.png"), screenshot);
        assertNotNull(screenshot);
        assertTrue(screenshot.length > 0);
    }

    /**
     * Example: Screenshot with custom dimensions.
     */
    @Test
    void screenshotWithDimensions() {
        server.enqueue(new MockResponse()
                .setBody("image data")
                .addHeader("Content-Type", "image/png"));

        // Example code from README
        byte[] screenshot = client.take(TakeOptions.url("https://example.com")
                .width(1200)
                .height(630)
                .format("png"));

        assertNotNull(screenshot);
    }

    /**
     * Example: Open Graph card preset.
     */
    @Test
    void ogCardScreenshot() {
        server.enqueue(new MockResponse()
                .setBody("image data")
                .addHeader("Content-Type", "image/png"));

        // Example code from README
        byte[] screenshot = client.take(TakeOptions.url("https://example.com")
                .preset("og_card")
                .blockAds()
                .blockCookieBanners());

        assertNotNull(screenshot);
    }

    /**
     * Example: Getting JSON response with metadata.
     */
    @Test
    void jsonResponse() {
        String jsonResponse = "{"
                + "\"url\": \"https://cdn.example.com/screenshot.png\","
                + "\"cache_url\": \"https://cache.example.com/abc.png\","
                + "\"width\": 1200,"
                + "\"height\": 630,"
                + "\"format\": \"png\","
                + "\"size\": 45678,"
                + "\"cached\": true"
                + "}";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Example code from README
        ScreenshotResponse response = client.takeJson(TakeOptions.url("https://example.com")
                .width(1200)
                .height(630));

        assertEquals("https://cdn.example.com/screenshot.png", response.getUrl());
        assertEquals(1200, response.getWidth());
        assertEquals(630, response.getHeight());
        assertTrue(response.isCached());
    }

    /**
     * Example: Error handling.
     */
    @Test
    void errorHandling() {
        server.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("{\"message\": \"Rate limited\"}")
                .addHeader("Retry-After", "60"));

        // Example code from README
        try {
            client.take(TakeOptions.url("https://example.com"));
            fail("Should have thrown exception");
        } catch (RenderScreenshotException e) {
            if (e.isRetryable()) {
                Integer retryAfter = e.getRetryAfter();
                assertNotNull(retryAfter);
                assertEquals(60, retryAfter);
                // In real code: Thread.sleep(retryAfter * 1000L);
            }
        }
    }

    /**
     * Example: Full page capture.
     */
    @Test
    void fullPageScreenshot() {
        server.enqueue(new MockResponse()
                .setBody("image data")
                .addHeader("Content-Type", "image/png"));

        // Example code from README
        byte[] screenshot = client.take(TakeOptions.url("https://example.com")
                .fullPage()
                .width(1280));

        assertNotNull(screenshot);
    }

    /**
     * Example: Mobile device emulation.
     */
    @Test
    void mobileScreenshot() {
        server.enqueue(new MockResponse()
                .setBody("image data")
                .addHeader("Content-Type", "image/png"));

        // Example code from README
        byte[] screenshot = client.take(TakeOptions.url("https://example.com")
                .device("iphone_14"));

        assertNotNull(screenshot);
    }

    /**
     * Example: Dark mode screenshot.
     */
    @Test
    void darkModeScreenshot() {
        server.enqueue(new MockResponse()
                .setBody("image data")
                .addHeader("Content-Type", "image/png"));

        // Example code from README
        byte[] screenshot = client.take(TakeOptions.url("https://example.com")
                .darkMode()
                .width(1200)
                .height(630));

        assertNotNull(screenshot);
    }

    /**
     * Example: HTML to screenshot.
     */
    @Test
    void htmlScreenshot() {
        server.enqueue(new MockResponse()
                .setBody("image data")
                .addHeader("Content-Type", "image/png"));

        // Example code from README
        String html = "<!DOCTYPE html>"
                + "<html>"
                + "<head><title>Test</title></head>"
                + "<body><h1>Hello World</h1></body>"
                + "</html>";

        byte[] screenshot = client.take(TakeOptions.html(html)
                .width(800)
                .height(600));

        assertNotNull(screenshot);
    }
}
