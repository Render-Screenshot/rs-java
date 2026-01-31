package com.renderscreenshot.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A preset configuration for common screenshot use cases.
 *
 * <p>Presets provide pre-configured settings for scenarios like Open Graph cards,
 * Twitter cards, full page captures, etc.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Preset {

    private String id;
    private String name;
    private String description;
    private int width;
    private int height;
    private Double scale;
    private String format;
    private Integer quality;

    @JsonProperty("full_page")
    private Boolean fullPage;

    @JsonProperty("block_ads")
    private Boolean blockAds;

    @JsonProperty("block_trackers")
    private Boolean blockTrackers;

    /** Default constructor for Jackson deserialization. */
    public Preset() {
    }

    /**
     * Returns the unique preset ID.
     *
     * @return the preset ID (e.g., "og_card", "twitter_card")
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the preset ID.
     *
     * @param id the preset ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the human-readable preset name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the preset name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the preset description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the preset description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the viewport width.
     *
     * @return the width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the viewport width.
     *
     * @param width the width in pixels
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the viewport height.
     *
     * @return the height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the viewport height.
     *
     * @param height the height in pixels
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the device scale factor.
     *
     * @return the scale factor, or null if default
     */
    public Double getScale() {
        return scale;
    }

    /**
     * Sets the device scale factor.
     *
     * @param scale the scale factor
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }

    /**
     * Returns the output format.
     *
     * @return the format (e.g., "png", "jpeg", "webp"), or null if default
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the output format.
     *
     * @param format the format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Returns the image quality for lossy formats.
     *
     * @return the quality (0-100), or null if default
     */
    public Integer getQuality() {
        return quality;
    }

    /**
     * Sets the image quality.
     *
     * @param quality the quality (0-100)
     */
    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    /**
     * Returns whether full page capture is enabled.
     *
     * @return true if full page, false if viewport only, null if default
     */
    public Boolean getFullPage() {
        return fullPage;
    }

    /**
     * Sets the full page flag.
     *
     * @param fullPage the full page flag
     */
    public void setFullPage(Boolean fullPage) {
        this.fullPage = fullPage;
    }

    /**
     * Returns whether ad blocking is enabled.
     *
     * @return true if enabled, null if default
     */
    public Boolean getBlockAds() {
        return blockAds;
    }

    /**
     * Sets the block ads flag.
     *
     * @param blockAds the block ads flag
     */
    public void setBlockAds(Boolean blockAds) {
        this.blockAds = blockAds;
    }

    /**
     * Returns whether tracker blocking is enabled.
     *
     * @return true if enabled, null if default
     */
    public Boolean getBlockTrackers() {
        return blockTrackers;
    }

    /**
     * Sets the block trackers flag.
     *
     * @param blockTrackers the block trackers flag
     */
    public void setBlockTrackers(Boolean blockTrackers) {
        this.blockTrackers = blockTrackers;
    }

    @Override
    public String toString() {
        return "Preset{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", width=" + width
                + ", height=" + height
                + ", scale=" + scale
                + ", format='" + format + '\''
                + '}';
    }
}
