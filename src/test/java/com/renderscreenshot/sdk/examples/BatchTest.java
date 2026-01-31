package com.renderscreenshot.sdk.examples;

import com.renderscreenshot.sdk.Client;
import com.renderscreenshot.sdk.Client.BatchRequest;
import com.renderscreenshot.sdk.TakeOptions;
import com.renderscreenshot.sdk.model.BatchResponse;
import com.renderscreenshot.sdk.model.BatchResult;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that demonstrate batch screenshot examples.
 */
class BatchTest {

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
     * Example: Simple batch with same options for all URLs.
     */
    @Test
    void simpleBatch() {
        String jsonResponse = "{"
                + "\"id\": \"batch_123\","
                + "\"status\": \"processing\","
                + "\"total\": 3,"
                + "\"completed\": 0,"
                + "\"failed\": 0,"
                + "\"results\": []"
                + "}";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Example code from README
        List<String> urls = Arrays.asList(
                "https://example.com",
                "https://example.org",
                "https://example.net"
        );

        TakeOptions options = TakeOptions.url("dummy") // URL not used for batch
                .width(1200)
                .height(630)
                .format("png");

        BatchResponse response = client.batch(urls, options);

        assertEquals("batch_123", response.getId());
        assertEquals("processing", response.getStatus());
        assertEquals(3, response.getTotal());
    }

    /**
     * Example: Advanced batch with individual options per URL.
     */
    @Test
    void advancedBatch() {
        String jsonResponse = "{"
                + "\"id\": \"batch_456\","
                + "\"status\": \"processing\","
                + "\"total\": 2,"
                + "\"completed\": 0,"
                + "\"failed\": 0,"
                + "\"results\": []"
                + "}";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Example code from README
        List<BatchRequest> requests = Arrays.asList(
                new BatchRequest("https://example.com",
                        TakeOptions.url("https://example.com").width(1200).height(630)),
                new BatchRequest("https://example.org",
                        TakeOptions.url("https://example.org").width(800).height(600).darkMode())
        );

        BatchResponse response = client.batchAdvanced(requests);

        assertEquals("batch_456", response.getId());
        assertEquals(2, response.getTotal());
    }

    /**
     * Example: Polling for batch completion.
     */
    @Test
    void pollBatchCompletion() throws InterruptedException {
        // First call - still processing
        String processingResponse = "{"
                + "\"id\": \"batch_789\","
                + "\"status\": \"processing\","
                + "\"total\": 2,"
                + "\"completed\": 1,"
                + "\"failed\": 0,"
                + "\"results\": []"
                + "}";
        server.enqueue(new MockResponse()
                .setBody(processingResponse)
                .addHeader("Content-Type", "application/json"));

        // Second call - completed
        String completedResponse = "{"
                + "\"id\": \"batch_789\","
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
                .setBody(completedResponse)
                .addHeader("Content-Type", "application/json"));

        // Example code from README
        BatchResponse batch = client.getBatch("batch_789");
        assertEquals("processing", batch.getStatus());

        // Poll until complete (in real code you'd add delays)
        while (!batch.isComplete()) {
            // Thread.sleep(1000); // In real code
            batch = client.getBatch("batch_789");
        }

        // Process results
        assertTrue(batch.isComplete());
        assertEquals("completed", batch.getStatus());
        assertEquals(2, batch.getCompleted());

        for (BatchResult result : batch.getResults()) {
            if (result.isSuccess()) {
                assertNotNull(result.getResponse());
                assertNotNull(result.getResponse().getUrl());
            }
        }
    }

    /**
     * Example: Handling batch with failures.
     */
    @Test
    void batchWithFailures() {
        String jsonResponse = "{"
                + "\"id\": \"batch_fail\","
                + "\"status\": \"completed\","
                + "\"total\": 3,"
                + "\"completed\": 2,"
                + "\"failed\": 1,"
                + "\"results\": ["
                + "{\"url\": \"https://good1.com\", \"success\": true, \"response\": {\"url\": \"https://cdn/1.png\"}},"
                + "{\"url\": \"https://good2.com\", \"success\": true, \"response\": {\"url\": \"https://cdn/2.png\"}},"
                + "{\"url\": \"https://bad.invalid\", \"success\": false, \"error\": \"DNS resolution failed\"}"
                + "]"
                + "}";
        server.enqueue(new MockResponse()
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        // Example code from README
        BatchResponse batch = client.getBatch("batch_fail");

        int successCount = 0;
        int failCount = 0;

        for (BatchResult result : batch.getResults()) {
            if (result.isSuccess()) {
                successCount++;
                // Use result.getResponse().getUrl() for the screenshot
            } else {
                failCount++;
                // Log result.getError()
                assertNotNull(result.getError());
            }
        }

        assertEquals(2, successCount);
        assertEquals(1, failCount);
        assertEquals(batch.getCompleted(), successCount);
        assertEquals(batch.getFailed(), failCount);
    }
}
