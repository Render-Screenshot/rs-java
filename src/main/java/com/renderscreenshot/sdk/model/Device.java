package com.renderscreenshot.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A device configuration for emulating specific devices.
 *
 * <p>Device emulation includes viewport dimensions, device scale factor,
 * mobile flag, and user agent string.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {

    private String id;
    private String name;
    private int width;
    private int height;
    private double scale;
    private boolean mobile;

    @JsonProperty("user_agent")
    private String userAgent;

    /** Default constructor for Jackson deserialization. */
    public Device() {
    }

    /**
     * Returns the unique device ID.
     *
     * @return the device ID (e.g., "iphone_14", "pixel_7")
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the device ID.
     *
     * @param id the device ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the human-readable device name.
     *
     * @return the name (e.g., "iPhone 14", "Pixel 7")
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the device name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
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
     * Returns the device pixel ratio.
     *
     * @return the scale factor
     */
    public double getScale() {
        return scale;
    }

    /**
     * Sets the device pixel ratio.
     *
     * @param scale the scale factor
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * Returns whether this is a mobile device.
     *
     * @return true for mobile devices, false otherwise
     */
    public boolean isMobile() {
        return mobile;
    }

    /**
     * Sets the mobile flag.
     *
     * @param mobile the mobile flag
     */
    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    /**
     * Returns the device user agent string.
     *
     * @return the user agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the device user agent string.
     *
     * @param userAgent the user agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        return "Device{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", width=" + width
                + ", height=" + height
                + ", scale=" + scale
                + ", mobile=" + mobile
                + '}';
    }
}
