package com.renderscreenshot.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from a screenshot capture request.
 *
 * <p>Contains metadata about the captured screenshot including dimensions,
 * format, file size, and caching information.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScreenshotResponse {

    private String url;

    @JsonProperty("cache_url")
    private String cacheUrl;

    private int width;
    private int height;
    private String format;
    private long size;

    @JsonProperty("cache_key")
    private String cacheKey;

    @JsonProperty("storage_path")
    private String storagePath;

    private int ttl;
    private boolean cached;

    /** Default constructor for Jackson deserialization. */
    public ScreenshotResponse() {
    }

    /**
     * Returns the URL of the captured screenshot.
     *
     * @return the screenshot URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the captured screenshot.
     *
     * @param url the screenshot URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the public cache URL that doesn't require authentication.
     *
     * @return the cache URL
     */
    public String getCacheUrl() {
        return cacheUrl;
    }

    /**
     * Sets the public cache URL.
     *
     * @param cacheUrl the cache URL
     */
    public void setCacheUrl(String cacheUrl) {
        this.cacheUrl = cacheUrl;
    }

    /**
     * Returns the width of the screenshot in pixels.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the screenshot in pixels.
     *
     * @param width the width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the height of the screenshot in pixels.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the screenshot in pixels.
     *
     * @param height the height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the image format (e.g., "png", "jpeg", "webp").
     *
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the image format.
     *
     * @param format the format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Returns the file size in bytes.
     *
     * @return the file size
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the file size in bytes.
     *
     * @param size the file size
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Returns the cache key for this screenshot.
     *
     * @return the cache key
     */
    public String getCacheKey() {
        return cacheKey;
    }

    /**
     * Sets the cache key.
     *
     * @param cacheKey the cache key
     */
    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    /**
     * Returns the storage path if custom storage is enabled.
     *
     * @return the storage path, or null if not stored
     */
    public String getStoragePath() {
        return storagePath;
    }

    /**
     * Sets the storage path.
     *
     * @param storagePath the storage path
     */
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Returns the cache TTL in seconds.
     *
     * @return the TTL
     */
    public int getTtl() {
        return ttl;
    }

    /**
     * Sets the cache TTL in seconds.
     *
     * @param ttl the TTL
     */
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    /**
     * Returns whether this response was served from cache.
     *
     * @return true if cached, false otherwise
     */
    public boolean isCached() {
        return cached;
    }

    /**
     * Sets whether this response was served from cache.
     *
     * @param cached the cached flag
     */
    public void setCached(boolean cached) {
        this.cached = cached;
    }

    @Override
    public String toString() {
        return "ScreenshotResponse{"
                + "url='" + url + '\''
                + ", cacheUrl='" + cacheUrl + '\''
                + ", width=" + width
                + ", height=" + height
                + ", format='" + format + '\''
                + ", size=" + size
                + ", cacheKey='" + cacheKey + '\''
                + ", storagePath='" + storagePath + '\''
                + ", ttl=" + ttl
                + ", cached=" + cached
                + '}';
    }
}
