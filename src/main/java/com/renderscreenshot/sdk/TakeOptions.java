package com.renderscreenshot.sdk;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Fluent builder for screenshot capture options.
 *
 * <p>Use the static factory methods {@link #url(String)} or {@link #html(String)}
 * to create a new instance, then chain option methods to configure the capture.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * TakeOptions options = TakeOptions.url("https://example.com")
 *     .width(1200)
 *     .height(630)
 *     .format("png")
 *     .blockAds()
 *     .darkMode();
 * }</pre>
 *
 * <p>TakeOptions instances are immutable - each method returns a new instance
 * with the updated configuration.</p>
 */
public final class TakeOptions {

    private final Map<String, Object> config;

    private TakeOptions(Map<String, Object> config) {
        this.config = new HashMap<>(config);
    }

    /**
     * Creates options for capturing a URL.
     *
     * @param url the URL to capture
     * @return a new TakeOptions instance
     */
    public static TakeOptions url(String url) {
        Map<String, Object> config = new HashMap<>();
        config.put("url", url);
        return new TakeOptions(config);
    }

    /**
     * Creates options for rendering HTML content.
     *
     * @param html the HTML content to render
     * @return a new TakeOptions instance
     */
    public static TakeOptions html(String html) {
        Map<String, Object> config = new HashMap<>();
        config.put("html", html);
        return new TakeOptions(config);
    }

    private TakeOptions copyWith(String key, Object value) {
        Map<String, Object> newConfig = new HashMap<>(this.config);
        newConfig.put(key, value);
        return new TakeOptions(newConfig);
    }

    // ========== Viewport Options ==========

    /**
     * Sets the viewport width.
     *
     * @param width width in pixels
     * @return a new TakeOptions instance
     */
    public TakeOptions width(int width) {
        return copyWith("width", width);
    }

    /**
     * Sets the viewport height.
     *
     * @param height height in pixels
     * @return a new TakeOptions instance
     */
    public TakeOptions height(int height) {
        return copyWith("height", height);
    }

    /**
     * Sets the device scale factor.
     *
     * @param scale scale factor (e.g., 2.0 for retina)
     * @return a new TakeOptions instance
     */
    public TakeOptions scale(double scale) {
        return copyWith("scale", scale);
    }

    /**
     * Enables mobile emulation mode.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions mobile() {
        return copyWith("mobile", true);
    }

    // ========== Capture Options ==========

    /**
     * Captures the full scrollable page instead of just the viewport.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions fullPage() {
        return copyWith("full_page", true);
    }

    /**
     * Captures only a specific CSS selector element.
     *
     * @param selector CSS selector for the element to capture
     * @return a new TakeOptions instance
     */
    public TakeOptions element(String selector) {
        return copyWith("element", selector);
    }

    /**
     * Sets the output format.
     *
     * @param format "png", "jpeg", "webp", or "pdf"
     * @return a new TakeOptions instance
     */
    public TakeOptions format(String format) {
        return copyWith("format", format);
    }

    /**
     * Sets the image quality for lossy formats (jpeg, webp).
     *
     * @param quality quality level 0-100
     * @return a new TakeOptions instance
     */
    public TakeOptions quality(int quality) {
        return copyWith("quality", quality);
    }

    // ========== Wait Options ==========

    /**
     * Wait strategy: "load", "domcontentloaded", "networkidle".
     *
     * @param strategy wait strategy
     * @return a new TakeOptions instance
     */
    public TakeOptions waitFor(String strategy) {
        return copyWith("wait_for", strategy);
    }

    /**
     * Additional delay in milliseconds after page load.
     *
     * @param ms delay in milliseconds
     * @return a new TakeOptions instance
     */
    public TakeOptions delay(int ms) {
        return copyWith("delay", ms);
    }

    /**
     * Wait for a CSS selector to appear before capturing.
     *
     * @param selector CSS selector to wait for
     * @return a new TakeOptions instance
     */
    public TakeOptions waitForSelector(String selector) {
        return copyWith("wait_for_selector", selector);
    }

    /**
     * Maximum time to wait in milliseconds.
     *
     * @param ms timeout in milliseconds
     * @return a new TakeOptions instance
     */
    public TakeOptions waitForTimeout(int ms) {
        return copyWith("wait_for_timeout", ms);
    }

    // ========== Presets ==========

    /**
     * Applies a preset configuration.
     *
     * @param preset preset ID (e.g., "og_card", "twitter_card")
     * @return a new TakeOptions instance
     */
    public TakeOptions preset(String preset) {
        return copyWith("preset", preset);
    }

    /**
     * Emulates a specific device.
     *
     * @param device device ID (e.g., "iphone_14", "pixel_7")
     * @return a new TakeOptions instance
     */
    public TakeOptions device(String device) {
        return copyWith("device", device);
    }

    // ========== Blocking Options ==========

    /**
     * Blocks advertisements.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions blockAds() {
        return copyWith("block_ads", true);
    }

    /**
     * Blocks tracking scripts.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions blockTrackers() {
        return copyWith("block_trackers", true);
    }

    /**
     * Blocks cookie consent banners.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions blockCookieBanners() {
        return copyWith("block_cookie_banners", true);
    }

    /**
     * Blocks chat widgets.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions blockChatWidgets() {
        return copyWith("block_chat_widgets", true);
    }

    /**
     * Blocks specific URLs.
     *
     * @param urls list of URL patterns to block
     * @return a new TakeOptions instance
     */
    public TakeOptions blockUrls(List<String> urls) {
        return copyWith("block_urls", new ArrayList<>(urls));
    }

    /**
     * Blocks specific resource types.
     *
     * @param types list of resource types to block (e.g., "image", "stylesheet")
     * @return a new TakeOptions instance
     */
    public TakeOptions blockResources(List<String> types) {
        return copyWith("block_resources", new ArrayList<>(types));
    }

    // ========== Page Manipulation ==========

    /**
     * Injects JavaScript to execute before capture.
     *
     * @param script JavaScript code
     * @return a new TakeOptions instance
     */
    public TakeOptions injectScript(String script) {
        return copyWith("inject_script", script);
    }

    /**
     * Injects CSS styles before capture.
     *
     * @param css CSS code
     * @return a new TakeOptions instance
     */
    public TakeOptions injectStyle(String css) {
        return copyWith("inject_style", css);
    }

    /**
     * Clicks an element before capture.
     *
     * @param selector CSS selector of element to click
     * @return a new TakeOptions instance
     */
    public TakeOptions click(String selector) {
        return copyWith("click", selector);
    }

    /**
     * Hides elements matching the selectors (visibility: hidden).
     *
     * @param selectors list of CSS selectors
     * @return a new TakeOptions instance
     */
    public TakeOptions hide(List<String> selectors) {
        return copyWith("hide", selectors);
    }

    /**
     * Removes elements matching the selectors from the DOM.
     *
     * @param selectors list of CSS selectors
     * @return a new TakeOptions instance
     */
    public TakeOptions remove(List<String> selectors) {
        return copyWith("remove", selectors);
    }

    // ========== Browser Emulation ==========

    /**
     * Enables dark mode (prefers-color-scheme: dark).
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions darkMode() {
        return copyWith("dark_mode", true);
    }

    /**
     * Enables reduced motion (prefers-reduced-motion: reduce).
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions reducedMotion() {
        return copyWith("reduced_motion", true);
    }

    /**
     * Sets the CSS media type.
     *
     * @param type "screen" or "print"
     * @return a new TakeOptions instance
     */
    public TakeOptions mediaType(String type) {
        return copyWith("media_type", type);
    }

    /**
     * Sets a custom user agent string.
     *
     * @param userAgent user agent string
     * @return a new TakeOptions instance
     */
    public TakeOptions userAgent(String userAgent) {
        return copyWith("user_agent", userAgent);
    }

    /**
     * Sets the browser timezone.
     *
     * @param timezone timezone ID (e.g., "America/New_York")
     * @return a new TakeOptions instance
     */
    public TakeOptions timezone(String timezone) {
        return copyWith("timezone", timezone);
    }

    /**
     * Sets the browser locale.
     *
     * @param locale locale code (e.g., "en-US")
     * @return a new TakeOptions instance
     */
    public TakeOptions locale(String locale) {
        return copyWith("locale", locale);
    }

    /**
     * Sets the browser geolocation.
     *
     * @param latitude  latitude
     * @param longitude longitude
     * @return a new TakeOptions instance
     */
    public TakeOptions geolocation(double latitude, double longitude) {
        Map<String, Object> geo = new HashMap<>();
        geo.put("latitude", latitude);
        geo.put("longitude", longitude);
        return copyWith("geolocation", geo);
    }

    /**
     * Sets the browser geolocation with accuracy.
     *
     * @param latitude  latitude
     * @param longitude longitude
     * @param accuracy  accuracy in meters
     * @return a new TakeOptions instance
     */
    public TakeOptions geolocation(double latitude, double longitude, double accuracy) {
        Map<String, Object> geo = new HashMap<>();
        geo.put("latitude", latitude);
        geo.put("longitude", longitude);
        geo.put("accuracy", accuracy);
        return copyWith("geolocation", geo);
    }

    // ========== Network Options ==========

    /**
     * Sets custom request headers.
     *
     * @param headers map of header names to values
     * @return a new TakeOptions instance
     */
    public TakeOptions headers(Map<String, String> headers) {
        return copyWith("headers", new HashMap<>(headers));
    }

    /**
     * Sets cookies for the request.
     *
     * @param cookies list of cookie maps (name, value, domain, etc.)
     * @return a new TakeOptions instance
     */
    public TakeOptions cookies(List<Map<String, Object>> cookies) {
        return copyWith("cookies", new ArrayList<>(cookies));
    }

    /**
     * Sets HTTP Basic authentication credentials.
     *
     * @param username username
     * @param password password
     * @return a new TakeOptions instance
     */
    public TakeOptions authBasic(String username, String password) {
        Map<String, String> auth = new HashMap<>();
        auth.put("username", username);
        auth.put("password", password);
        return copyWith("auth_basic", auth);
    }

    /**
     * Sets Bearer token authentication.
     *
     * @param token bearer token
     * @return a new TakeOptions instance
     */
    public TakeOptions authBearer(String token) {
        return copyWith("auth_bearer", token);
    }

    /**
     * Bypasses Content Security Policy.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions bypassCsp() {
        return copyWith("bypass_csp", true);
    }

    // ========== Cache Options ==========

    /**
     * Sets the cache TTL in seconds.
     *
     * @param seconds TTL in seconds
     * @return a new TakeOptions instance
     */
    public TakeOptions cacheTtl(int seconds) {
        return copyWith("cache_ttl", seconds);
    }

    /**
     * Forces a cache refresh (bypass cache for this request).
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions cacheRefresh() {
        return copyWith("cache_refresh", true);
    }

    // ========== PDF Options ==========

    /**
     * Sets the PDF paper size.
     *
     * @param size paper size ("letter", "legal", "a4", etc.)
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfPaperSize(String size) {
        return copyWith("pdf_paper_size", size);
    }

    /**
     * Sets the PDF page width.
     *
     * @param width width with units (e.g., "8.5in", "210mm")
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfWidth(String width) {
        return copyWith("pdf_width", width);
    }

    /**
     * Sets the PDF page height.
     *
     * @param height height with units (e.g., "11in", "297mm")
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfHeight(String height) {
        return copyWith("pdf_height", height);
    }

    /**
     * Sets the PDF to landscape orientation.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfLandscape() {
        return copyWith("pdf_landscape", true);
    }

    /**
     * Sets all PDF margins.
     *
     * @param margin margin with units (e.g., "1in", "25mm")
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfMargin(String margin) {
        return copyWith("pdf_margin", margin);
    }

    /**
     * Sets the PDF top margin.
     *
     * @param margin margin with units
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfMarginTop(String margin) {
        return copyWith("pdf_margin_top", margin);
    }

    /**
     * Sets the PDF right margin.
     *
     * @param margin margin with units
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfMarginRight(String margin) {
        return copyWith("pdf_margin_right", margin);
    }

    /**
     * Sets the PDF bottom margin.
     *
     * @param margin margin with units
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfMarginBottom(String margin) {
        return copyWith("pdf_margin_bottom", margin);
    }

    /**
     * Sets the PDF left margin.
     *
     * @param margin margin with units
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfMarginLeft(String margin) {
        return copyWith("pdf_margin_left", margin);
    }

    /**
     * Sets the PDF scale.
     *
     * @param scale scale factor (0.1 - 2.0)
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfScale(double scale) {
        return copyWith("pdf_scale", scale);
    }

    /**
     * Prints the background graphics.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfPrintBackground() {
        return copyWith("pdf_print_background", true);
    }

    /**
     * Sets the page ranges to print.
     *
     * @param ranges page ranges (e.g., "1-5, 8, 11-13")
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfPageRanges(String ranges) {
        return copyWith("pdf_page_ranges", ranges);
    }

    /**
     * Sets the PDF header HTML template.
     *
     * @param html header HTML
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfHeader(String html) {
        return copyWith("pdf_header", html);
    }

    /**
     * Sets the PDF footer HTML template.
     *
     * @param html footer HTML
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfFooter(String html) {
        return copyWith("pdf_footer", html);
    }

    /**
     * Fits the content to one page.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfFitOnePage() {
        return copyWith("pdf_fit_one_page", true);
    }

    /**
     * Uses the CSS page size if defined.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions pdfPreferCssPageSize() {
        return copyWith("pdf_prefer_css_page_size", true);
    }

    // ========== Storage Options ==========

    /**
     * Enables storage to R2.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions storageEnabled() {
        return copyWith("storage_enabled", true);
    }

    /**
     * Sets the storage path prefix.
     *
     * @param path path prefix
     * @return a new TakeOptions instance
     */
    public TakeOptions storagePath(String path) {
        return copyWith("storage_path", path);
    }

    /**
     * Sets the storage ACL.
     *
     * @param acl ACL setting ("private" or "public-read")
     * @return a new TakeOptions instance
     */
    public TakeOptions storageAcl(String acl) {
        return copyWith("storage_acl", acl);
    }

    // ========== Response Options ==========

    /**
     * Sets the response type.
     *
     * @param type "binary" or "json"
     * @return a new TakeOptions instance
     */
    public TakeOptions responseType(String type) {
        return copyWith("response_type", type);
    }

    // ========== Serialization ==========

    /**
     * Returns the raw configuration map.
     *
     * @return a copy of the configuration
     */
    public Map<String, Object> toConfig() {
        return new HashMap<>(config);
    }

    /**
     * Converts options to API request parameters with nested structure.
     *
     * <p>This method organizes options into the nested structure expected by the API:
     * viewport.width, viewport.height, pdf.paper_size, storage.enabled, etc.</p>
     *
     * @return parameters map for POST body
     */
    public Map<String, Object> toParams() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> viewport = new HashMap<>();
        Map<String, Object> pdf = new HashMap<>();
        Map<String, Object> storage = new HashMap<>();

        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Viewport options
            if (key.equals("width") || key.equals("height") || key.equals("scale") || key.equals("mobile")) {
                viewport.put(key, value);
            }
            // PDF options
            else if (key.startsWith("pdf_")) {
                String pdfKey = key.substring(4); // Remove "pdf_" prefix
                pdf.put(pdfKey, value);
            }
            // Storage options
            else if (key.startsWith("storage_")) {
                String storageKey = key.substring(8); // Remove "storage_" prefix
                storage.put(storageKey, value);
            }
            // Top-level options
            else {
                params.put(key, value);
            }
        }

        // Add nested objects if not empty
        if (!viewport.isEmpty()) {
            params.put("viewport", viewport);
        }
        if (!pdf.isEmpty()) {
            params.put("pdf", pdf);
        }
        if (!storage.isEmpty()) {
            params.put("storage", storage);
        }

        return params;
    }

    /**
     * Converts options to a URL query string.
     *
     * <p>This method flattens options for GET requests, converting nested
     * structures to flat parameter names.</p>
     *
     * @return query string (without leading "?")
     */
    public String toQueryString() {
        StringJoiner joiner = new StringJoiner("&");

        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) value;
                for (Map.Entry<String, Object> nestedEntry : nested.entrySet()) {
                    String encodedKey = encodeUrl(key + "_" + nestedEntry.getKey());
                    String encodedValue = encodeUrl(String.valueOf(nestedEntry.getValue()));
                    joiner.add(encodedKey + "=" + encodedValue);
                }
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) value;
                for (Object item : list) {
                    String encodedKey = encodeUrl(key + "[]");
                    String encodedValue = encodeUrl(String.valueOf(item));
                    joiner.add(encodedKey + "=" + encodedValue);
                }
            } else if (value instanceof Boolean) {
                if ((Boolean) value) {
                    joiner.add(encodeUrl(key) + "=true");
                }
            } else {
                String encodedKey = encodeUrl(key);
                String encodedValue = encodeUrl(String.valueOf(value));
                joiner.add(encodedKey + "=" + encodedValue);
            }
        }

        return joiner.toString();
    }

    private String encodeUrl(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is always supported, this should never happen
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }

    @Override
    public String toString() {
        return "TakeOptions" + config;
    }
}
