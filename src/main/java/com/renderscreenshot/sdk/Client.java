package com.renderscreenshot.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renderscreenshot.sdk.model.BatchResponse;
import com.renderscreenshot.sdk.model.Device;
import com.renderscreenshot.sdk.model.Preset;
import com.renderscreenshot.sdk.model.ScreenshotResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Client for the RenderScreenshot API.
 *
 * <p>This is the main entry point for capturing screenshots. Create a client with your
 * API key and use the {@link #take(TakeOptions)} method to capture screenshots.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Client client = new Client("your_api_key");
 *
 * // Capture a screenshot as binary data
 * byte[] screenshot = client.take(TakeOptions.url("https://example.com")
 *     .width(1200)
 *     .height(630)
 *     .format("png"));
 *
 * // Save to file
 * Files.write(Path.of("screenshot.png"), screenshot);
 * }</pre>
 *
 * @see TakeOptions
 */
public class Client {

    /** The default API base URL. */
    public static final String DEFAULT_BASE_URL = "https://api.renderscreenshot.com";

    /** The current API version. */
    public static final String API_VERSION = "v1";

    /** The SDK version. */
    public static final String SDK_VERSION = "1.0.0";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final String apiKey;
    private final String baseUrl;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /** Cache manager for managing cached screenshots. */
    public final CacheManager cache;

    /**
     * Creates a new client with the specified API key.
     *
     * @param apiKey your RenderScreenshot API key
     * @throws IllegalArgumentException if apiKey is null or empty
     */
    public Client(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new client with custom base URL.
     *
     * @param apiKey  your RenderScreenshot API key
     * @param baseUrl the API base URL
     * @throws IllegalArgumentException if apiKey is null or empty
     */
    public Client(String apiKey, String baseUrl) {
        this(apiKey, baseUrl, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new client with custom configuration.
     *
     * @param apiKey  your RenderScreenshot API key
     * @param baseUrl the API base URL
     * @param timeout request timeout
     * @throws IllegalArgumentException if apiKey is null or empty
     */
    public Client(String apiKey, String baseUrl, Duration timeout) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }

        this.apiKey = apiKey;
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
        this.cache = new CacheManager(httpClient, this.baseUrl, apiKey, objectMapper);
    }

    /**
     * Captures a screenshot and returns the image data.
     *
     * @param options screenshot options
     * @return the screenshot image data
     * @throws RenderScreenshotException if the capture fails
     */
    public byte[] take(TakeOptions options) {
        Map<String, Object> params = options.toParams();
        return postBinary("/v1/screenshot", params);
    }

    /**
     * Captures a screenshot and returns metadata (URL, dimensions, etc.).
     *
     * @param options screenshot options
     * @return screenshot metadata
     * @throws RenderScreenshotException if the capture fails
     */
    public ScreenshotResponse takeJson(TakeOptions options) {
        Map<String, Object> params = options.toParams();
        params.put("response_type", "json");
        String json = postJson("/v1/screenshot", params);
        try {
            return objectMapper.readValue(json, ScreenshotResponse.class);
        } catch (JsonProcessingException e) {
            throw RenderScreenshotException.internal("Failed to parse response: " + e.getMessage());
        }
    }

    /**
     * Generates a signed URL for the screenshot.
     *
     * <p>Signed URLs can be used in img tags or shared without exposing your API key.</p>
     *
     * @param options   screenshot options
     * @param expiresAt when the signed URL expires
     * @return the signed URL
     */
    public String generateUrl(TakeOptions options, Instant expiresAt) {
        String queryString = options.toQueryString();
        long expiresTimestamp = expiresAt.getEpochSecond();

        String url = baseUrl + "/v1/screenshot?" + queryString + "&expires=" + expiresTimestamp;

        // Compute signature
        String signature = computeSignature(url);

        return url + "&signature=" + signature;
    }

    // ========== Batch Methods ==========

    /**
     * Captures screenshots for multiple URLs with the same options.
     *
     * @param urls    list of URLs to capture
     * @param options screenshot options to apply to all URLs
     * @return batch response with status and results
     * @throws RenderScreenshotException if the request fails
     */
    public BatchResponse batch(List<String> urls, TakeOptions options) {
        Map<String, Object> params = new HashMap<>();
        params.put("urls", urls);
        if (options != null) {
            params.put("options", options.toParams());
        }

        String json = postJson("/v1/batch", params);
        try {
            return objectMapper.readValue(json, BatchResponse.class);
        } catch (JsonProcessingException e) {
            throw RenderScreenshotException.internal("Failed to parse batch response: " + e.getMessage());
        }
    }

    /**
     * Captures screenshots for multiple requests with individual options.
     *
     * @param requests list of batch request objects
     * @return batch response with status and results
     * @throws RenderScreenshotException if the request fails
     */
    public BatchResponse batchAdvanced(List<BatchRequest> requests) {
        List<Map<String, Object>> requestMaps = new ArrayList<>();
        for (BatchRequest request : requests) {
            Map<String, Object> map = new HashMap<>();
            map.put("url", request.getUrl());
            if (request.getOptions() != null) {
                map.putAll(request.getOptions().toParams());
            }
            requestMaps.add(map);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("requests", requestMaps);

        String json = postJson("/v1/batch", params);
        try {
            return objectMapper.readValue(json, BatchResponse.class);
        } catch (JsonProcessingException e) {
            throw RenderScreenshotException.internal("Failed to parse batch response: " + e.getMessage());
        }
    }

    /**
     * Gets the status of a batch request.
     *
     * @param batchId the batch ID
     * @return batch response with current status and results
     * @throws RenderScreenshotException if the request fails
     */
    public BatchResponse getBatch(String batchId) {
        String json = get("/v1/batch/" + batchId);
        try {
            return objectMapper.readValue(json, BatchResponse.class);
        } catch (JsonProcessingException e) {
            throw RenderScreenshotException.internal("Failed to parse batch response: " + e.getMessage());
        }
    }

    // ========== Metadata Methods ==========

    /**
     * Lists all available presets.
     *
     * @return list of presets
     * @throws RenderScreenshotException if the request fails
     */
    public List<Preset> presets() {
        String json = get("/v1/presets");
        try {
            return objectMapper.readValue(json, new TypeReference<List<Preset>>() { });
        } catch (JsonProcessingException e) {
            throw RenderScreenshotException.internal("Failed to parse presets: " + e.getMessage());
        }
    }

    /**
     * Gets a specific preset by ID.
     *
     * @param id the preset ID
     * @return the preset
     * @throws RenderScreenshotException if the request fails or preset not found
     */
    public Preset preset(String id) {
        String json = get("/v1/presets/" + id);
        try {
            return objectMapper.readValue(json, Preset.class);
        } catch (JsonProcessingException e) {
            throw RenderScreenshotException.internal("Failed to parse preset: " + e.getMessage());
        }
    }

    /**
     * Lists all available device emulations.
     *
     * @return list of devices
     * @throws RenderScreenshotException if the request fails
     */
    public List<Device> devices() {
        String json = get("/v1/devices");
        try {
            return objectMapper.readValue(json, new TypeReference<List<Device>>() { });
        } catch (JsonProcessingException e) {
            throw RenderScreenshotException.internal("Failed to parse devices: " + e.getMessage());
        }
    }

    // ========== HTTP Methods ==========

    private String get(String path) {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", "renderscreenshot-java/" + SDK_VERSION)
                .get()
                .build();

        return executeForJson(request);
    }

    private String postJson(String path, Map<String, Object> body) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }

        Request request = new Request.Builder()
                .url(baseUrl + path)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", "renderscreenshot-java/" + SDK_VERSION)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, JSON))
                .build();

        return executeForJson(request);
    }

    private byte[] postBinary(String path, Map<String, Object> body) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }

        Request request = new Request.Builder()
                .url(baseUrl + path)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("User-Agent", "renderscreenshot-java/" + SDK_VERSION)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, JSON))
                .build();

        return executeForBinary(request);
    }

    private String executeForJson(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw handleErrorResponse(response);
            }
            if (response.body() == null) {
                throw RenderScreenshotException.internal("Empty response body");
            }
            return response.body().string();
        } catch (InterruptedIOException e) {
            throw RenderScreenshotException.timeout(e);
        } catch (IOException e) {
            throw RenderScreenshotException.network("Request failed: " + e.getMessage(), e);
        }
    }

    private byte[] executeForBinary(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw handleErrorResponse(response);
            }
            if (response.body() == null) {
                throw RenderScreenshotException.internal("Empty response body");
            }
            return response.body().bytes();
        } catch (InterruptedIOException e) {
            throw RenderScreenshotException.timeout(e);
        } catch (IOException e) {
            throw RenderScreenshotException.network("Request failed: " + e.getMessage(), e);
        }
    }

    private RenderScreenshotException handleErrorResponse(Response response) throws IOException {
        String body = response.body() != null ? response.body().string() : "";
        String message = "API request failed with status " + response.code();
        String code = null;
        Integer retryAfter = null;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> errorBody = objectMapper.readValue(body, Map.class);
            if (errorBody.containsKey("message")) {
                message = (String) errorBody.get("message");
            }
            if (errorBody.containsKey("error")) {
                message = (String) errorBody.get("error");
            }
            if (errorBody.containsKey("code")) {
                code = (String) errorBody.get("code");
            }
        } catch (Exception ignored) {
            // Use raw body if not JSON
            if (!body.isEmpty()) {
                message = body;
            }
        }

        String retryAfterHeader = response.header("Retry-After");
        if (retryAfterHeader == null) {
            retryAfterHeader = response.header("retry-after");
        }
        if (retryAfterHeader != null) {
            try {
                retryAfter = Integer.parseInt(retryAfterHeader);
            } catch (NumberFormatException ignored) {
                // Ignore invalid header
            }
        }

        return RenderScreenshotException.fromResponse(response.code(), message, code, retryAfter);
    }

    private String computeSignature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute signature", e);
        }
    }

    /**
     * A request in a batch operation with individual options.
     */
    public static class BatchRequest {
        private final String url;
        private final TakeOptions options;

        /**
         * Creates a batch request with only a URL.
         *
         * @param url the URL to capture
         */
        public BatchRequest(String url) {
            this(url, null);
        }

        /**
         * Creates a batch request with URL and options.
         *
         * @param url     the URL to capture
         * @param options screenshot options for this URL
         */
        public BatchRequest(String url, TakeOptions options) {
            this.url = url;
            this.options = options;
        }

        /**
         * Returns the URL.
         *
         * @return the URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * Returns the options.
         *
         * @return the options, or null if using defaults
         */
        public TakeOptions getOptions() {
            return options;
        }
    }
}
