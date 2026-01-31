package com.renderscreenshot.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages cached screenshots.
 *
 * <p>Provides methods to retrieve, delete, and purge cached screenshots.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Client client = new Client("your_api_key");
 *
 * // Get cached screenshot
 * byte[] data = client.cache.get("cache_key_here");
 *
 * // Delete specific cache entry
 * client.cache.delete("cache_key_here");
 *
 * // Purge multiple keys
 * PurgeResponse response = client.cache.purge(List.of("key1", "key2"));
 * }</pre>
 */
public class CacheManager {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new CacheManager.
     *
     * <p>This constructor is typically called internally by {@link Client}.</p>
     *
     * @param httpClient   the HTTP client to use
     * @param baseUrl      the API base URL
     * @param apiKey       the API key
     * @param objectMapper the JSON mapper
     */
    public CacheManager(OkHttpClient httpClient, String baseUrl, String apiKey, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves a cached screenshot by key.
     *
     * @param key the cache key
     * @return the cached screenshot data, or null if not found
     * @throws RenderScreenshotException if the request fails
     */
    public byte[] get(String key) {
        Request request = new Request.Builder()
                .url(baseUrl + "/v1/cache/" + key)
                .addHeader("Authorization", "Bearer " + apiKey)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 404) {
                return null;
            }
            if (!response.isSuccessful()) {
                throw handleErrorResponse(response);
            }
            if (response.body() == null) {
                return null;
            }
            return response.body().bytes();
        } catch (IOException e) {
            throw RenderScreenshotException.network("Failed to get cached screenshot: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a cached screenshot by key.
     *
     * @param key the cache key
     * @return true if deleted, false if not found
     * @throws RenderScreenshotException if the request fails
     */
    public boolean delete(String key) {
        Request request = new Request.Builder()
                .url(baseUrl + "/v1/cache/" + key)
                .addHeader("Authorization", "Bearer " + apiKey)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 404) {
                return false;
            }
            if (!response.isSuccessful()) {
                throw handleErrorResponse(response);
            }
            return true;
        } catch (IOException e) {
            throw RenderScreenshotException.network("Failed to delete cached screenshot: " + e.getMessage(), e);
        }
    }

    /**
     * Purges multiple cached screenshots by keys.
     *
     * @param keys list of cache keys to purge
     * @return purge response with deleted count
     * @throws RenderScreenshotException if the request fails
     */
    public PurgeResponse purge(List<String> keys) {
        Map<String, Object> body = new HashMap<>();
        body.put("keys", keys);
        return purgeRequest(body);
    }

    /**
     * Purges cached screenshots matching a URL pattern.
     *
     * @param pattern URL pattern (supports wildcards)
     * @return purge response with deleted count
     * @throws RenderScreenshotException if the request fails
     */
    public PurgeResponse purgeUrl(String pattern) {
        Map<String, Object> body = new HashMap<>();
        body.put("url", pattern);
        return purgeRequest(body);
    }

    /**
     * Purges cached screenshots created before a specific date.
     *
     * @param before purge entries created before this instant
     * @return purge response with deleted count
     * @throws RenderScreenshotException if the request fails
     */
    public PurgeResponse purgeBefore(Instant before) {
        Map<String, Object> body = new HashMap<>();
        body.put("before", before.toString());
        return purgeRequest(body);
    }

    /**
     * Purges cached screenshots matching a storage path pattern.
     *
     * @param pattern glob pattern for storage paths
     * @return purge response with deleted count
     * @throws RenderScreenshotException if the request fails
     */
    public PurgeResponse purgePattern(String pattern) {
        Map<String, Object> body = new HashMap<>();
        body.put("pattern", pattern);
        return purgeRequest(body);
    }

    private PurgeResponse purgeRequest(Map<String, Object> body) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }

        Request request = new Request.Builder()
                .url(baseUrl + "/v1/cache/purge")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(json, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw handleErrorResponse(response);
            }
            if (response.body() == null) {
                throw RenderScreenshotException.internal("Empty response body");
            }
            return objectMapper.readValue(response.body().string(), PurgeResponse.class);
        } catch (IOException e) {
            throw RenderScreenshotException.network("Failed to purge cache: " + e.getMessage(), e);
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
        if (retryAfterHeader != null) {
            try {
                retryAfter = Integer.parseInt(retryAfterHeader);
            } catch (NumberFormatException ignored) {
                // Ignore invalid header
            }
        }

        return RenderScreenshotException.fromResponse(response.code(), message, code, retryAfter);
    }

    /**
     * Response from a cache purge operation.
     */
    public static class PurgeResponse {
        private int purged;
        private List<String> keys;

        /** Default constructor for Jackson. */
        public PurgeResponse() {
        }

        /**
         * Returns the number of cache entries purged.
         *
         * @return purged count
         */
        public int getPurged() {
            return purged;
        }

        /**
         * Sets the purged count.
         *
         * @param purged purged count
         */
        public void setPurged(int purged) {
            this.purged = purged;
        }

        /**
         * Returns the list of purged cache keys.
         *
         * @return list of keys
         */
        public List<String> getKeys() {
            return keys;
        }

        /**
         * Sets the keys.
         *
         * @param keys list of keys
         */
        public void setKeys(List<String> keys) {
            this.keys = keys;
        }

        @Override
        public String toString() {
            return "PurgeResponse{purged=" + purged + ", keys=" + keys + "}";
        }
    }
}
