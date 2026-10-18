package com.renderscreenshot.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * JSON response from a screenshot capture request ({@code Accept: application/json}).
 *
 * <p>The API returns this shape:</p>
 * <pre>{@code
 * {
 *   "id": "req_ab12...",
 *   "status": "completed",
 *   "image": { "url": "...", "width": 1200, "height": 630, "size": 12345, "format": "png" },
 *   "metadata": { "title": "Screenshot" },
 *   "cache": { "hit": false, "key": "...", "url": "...", "expires_at": "2026-10-19T12:00:00Z" },
 *   "usage": { "credits": 1, "remaining": 99 }
 * }
 * }</pre>
 *
 * <p>{@code image}, {@code cache} and {@code usage} can be missing. The nested getters
 * return null in that case. The shortcut getters ({@link #getUrl()}, {@link #getWidth()},
 * {@link #isCached()}, ...) read the nested objects and return null, 0 or false.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScreenshotResponse {

    private String id;
    private String status;
    private Image image;
    private Map<String, Object> metadata;
    private Cache cache;
    private Usage usage;

    @JsonIgnore
    private String storagePath;

    @JsonIgnore
    private int ttl;

    /** Default constructor for Jackson deserialization. */
    public ScreenshotResponse() {
    }

    // ========== Top-level fields ==========

    /**
     * Returns the request ID (for example "req_ab12...").
     *
     * @return the request ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the request ID.
     *
     * @param id the request ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the request status ("completed", or "processing" for webhook requests).
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the request status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the image details.
     *
     * @return the image details, or null if not present
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the image details.
     *
     * @param image the image details
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * Returns the page metadata.
     *
     * @return the metadata map, or null if not present
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the page metadata.
     *
     * @param metadata the metadata map
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the cache details.
     *
     * @return the cache details, or null if not present
     */
    public Cache getCache() {
        return cache;
    }

    /**
     * Sets the cache details.
     *
     * @param cache the cache details
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**
     * Returns the credit usage.
     *
     * @return the usage, or null if not present
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * Sets the credit usage.
     *
     * @param usage the usage
     */
    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    // ========== Shortcut getters ==========

    /**
     * Returns the screenshot URL ({@code image.url}).
     *
     * @return the screenshot URL, or null
     */
    @JsonIgnore
    public String getUrl() {
        return image != null ? image.getUrl() : null;
    }

    /**
     * Returns the public cache URL ({@code cache.url}).
     *
     * @return the cache URL, or null
     */
    @JsonIgnore
    public String getCacheUrl() {
        return cache != null ? cache.getUrl() : null;
    }

    /**
     * Returns the screenshot width in pixels ({@code image.width}).
     *
     * @return the width, or 0 if not present
     */
    @JsonIgnore
    public int getWidth() {
        return image != null && image.getWidth() != null ? image.getWidth() : 0;
    }

    /**
     * Returns the screenshot height in pixels ({@code image.height}).
     *
     * @return the height, or 0 if not present
     */
    @JsonIgnore
    public int getHeight() {
        return image != null && image.getHeight() != null ? image.getHeight() : 0;
    }

    /**
     * Returns the image format ({@code image.format}).
     *
     * @return the format, or null
     */
    @JsonIgnore
    public String getFormat() {
        return image != null ? image.getFormat() : null;
    }

    /**
     * Returns the file size in bytes ({@code image.size}).
     *
     * @return the size, or 0 if not present
     */
    @JsonIgnore
    public long getSize() {
        return image != null && image.getSize() != null ? image.getSize() : 0L;
    }

    /**
     * Returns the cache key ({@code cache.key}).
     *
     * @return the cache key, or null
     */
    @JsonIgnore
    public String getCacheKey() {
        return cache != null ? cache.getKey() : null;
    }

    /**
     * Returns whether the screenshot was served from cache ({@code cache.hit}).
     *
     * @return true if served from cache
     */
    @JsonIgnore
    public boolean isCached() {
        return cache != null && cache.isHit();
    }

    /**
     * Returns the storage path.
     *
     * @return always null for API responses; the screenshot API response has no storage path
     * @deprecated the API response does not contain this field
     */
    @Deprecated
    @JsonIgnore
    public String getStoragePath() {
        return storagePath;
    }

    /**
     * Sets the storage path.
     *
     * @param storagePath the storage path
     * @deprecated the API response does not contain this field
     */
    @Deprecated
    @JsonIgnore
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Returns the cache TTL.
     *
     * @return always 0 for API responses; use {@code getCache().getExpiresAt()} instead
     * @deprecated the API response does not contain this field
     */
    @Deprecated
    @JsonIgnore
    public int getTtl() {
        return ttl;
    }

    /**
     * Sets the cache TTL.
     *
     * @param ttl the TTL
     * @deprecated the API response does not contain this field
     */
    @Deprecated
    @JsonIgnore
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return "ScreenshotResponse{"
                + "id='" + id + '\''
                + ", status='" + status + '\''
                + ", image=" + image
                + ", cache=" + cache
                + ", usage=" + usage
                + '}';
    }

    /**
     * Image details ({@code image} object).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private String url;
        private Integer width;
        private Integer height;
        private Long size;
        private String format;

        /** Default constructor for Jackson deserialization. */
        public Image() {
        }

        /**
         * Returns the image URL.
         *
         * @return the URL, or null
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the image URL.
         *
         * @param url the URL
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Returns the width in pixels.
         *
         * @return the width, or null
         */
        public Integer getWidth() {
            return width;
        }

        /**
         * Sets the width in pixels.
         *
         * @param width the width
         */
        public void setWidth(Integer width) {
            this.width = width;
        }

        /**
         * Returns the height in pixels.
         *
         * @return the height, or null
         */
        public Integer getHeight() {
            return height;
        }

        /**
         * Sets the height in pixels.
         *
         * @param height the height
         */
        public void setHeight(Integer height) {
            this.height = height;
        }

        /**
         * Returns the file size in bytes.
         *
         * @return the size, or null
         */
        public Long getSize() {
            return size;
        }

        /**
         * Sets the file size in bytes.
         *
         * @param size the size
         */
        public void setSize(Long size) {
            this.size = size;
        }

        /**
         * Returns the image format.
         *
         * @return the format, or null
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

        @Override
        public String toString() {
            return "Image{url='" + url + "', width=" + width + ", height=" + height
                    + ", size=" + size + ", format='" + format + "'}";
        }
    }

    /**
     * Cache details ({@code cache} object).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cache {
        private boolean hit;
        private String key;
        private String url;

        @JsonProperty("expires_at")
        private String expiresAt;

        /** Default constructor for Jackson deserialization. */
        public Cache() {
        }

        /**
         * Returns whether the screenshot was served from cache.
         *
         * @return true on a cache hit
         */
        public boolean isHit() {
            return hit;
        }

        /**
         * Sets the cache hit flag.
         *
         * @param hit the cache hit flag
         */
        public void setHit(boolean hit) {
            this.hit = hit;
        }

        /**
         * Returns the cache key.
         *
         * @return the key, or null
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the cache key.
         *
         * @param key the key
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * Returns the public cache URL.
         *
         * @return the URL, or null
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the public cache URL.
         *
         * @param url the URL
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Returns when the cache entry expires (ISO 8601).
         *
         * @return the expiry timestamp, or null
         */
        public String getExpiresAt() {
            return expiresAt;
        }

        /**
         * Sets when the cache entry expires.
         *
         * @param expiresAt the expiry timestamp (ISO 8601)
         */
        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }

        @Override
        public String toString() {
            return "Cache{hit=" + hit + ", key='" + key + "', url='" + url
                    + "', expiresAt='" + expiresAt + "'}";
        }
    }

    /**
     * Credit usage ({@code usage} object).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        private Integer credits;
        private Integer remaining;

        /** Default constructor for Jackson deserialization. */
        public Usage() {
        }

        /**
         * Returns the credits used by this request.
         *
         * @return the credits, or null
         */
        public Integer getCredits() {
            return credits;
        }

        /**
         * Sets the credits used.
         *
         * @param credits the credits
         */
        public void setCredits(Integer credits) {
            this.credits = credits;
        }

        /**
         * Returns the remaining credits.
         *
         * @return the remaining credits, or null
         */
        public Integer getRemaining() {
            return remaining;
        }

        /**
         * Sets the remaining credits.
         *
         * @param remaining the remaining credits
         */
        public void setRemaining(Integer remaining) {
            this.remaining = remaining;
        }

        @Override
        public String toString() {
            return "Usage{credits=" + credits + ", remaining=" + remaining + "}";
        }
    }
}
