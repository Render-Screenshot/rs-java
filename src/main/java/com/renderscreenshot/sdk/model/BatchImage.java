package com.renderscreenshot.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Screenshot details for a completed item in a batch.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchImage {

    @JsonProperty("image_url")
    private String imageUrl;
    private Integer width;
    private Integer height;
    private Long size;
    private String format;

    /** Default constructor for Jackson deserialization. */
    public BatchImage() {
    }

    /**
     * Returns the URL of the captured screenshot.
     *
     * @return the image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image URL.
     *
     * @param imageUrl the image URL
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Returns the image width in pixels.
     *
     * @return the width, or null if unknown
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Sets the image width.
     *
     * @param width the width
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * Returns the image height in pixels.
     *
     * @return the height, or null if unknown
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Sets the image height.
     *
     * @param height the height
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * Returns the file size in bytes.
     *
     * @return the size, or null if unknown
     */
    public Long getSize() {
        return size;
    }

    /**
     * Sets the file size.
     *
     * @param size the size in bytes
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * Returns the image format.
     *
     * @return the format (e.g., "png"), or null if unknown
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
        return "BatchImage{"
                + "imageUrl='" + imageUrl + '\''
                + ", width=" + width
                + ", height=" + height
                + ", size=" + size
                + ", format='" + format + '\''
                + '}';
    }
}
