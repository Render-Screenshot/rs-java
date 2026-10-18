package com.renderscreenshot.sdk;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

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
     * Maximum time to wait, in seconds.
     *
     * @param seconds timeout in seconds (the API clamps it to 1-60)
     * @return a new TakeOptions instance
     */
    public TakeOptions waitForTimeout(int seconds) {
        return copyWith("wait_for_timeout", seconds);
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

    /**
     * Serves a previous screenshot with the same options when one exists
     * ({@code cache.available}). The API reuses a cached render only when this is set.
     *
     * @return a new TakeOptions instance
     */
    public TakeOptions cacheAvailable() {
        return copyWith("cache_available", true);
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
     * <p>This value is not sent to the API. Use {@link Client#takeJson(TakeOptions)}
     * for a JSON response (it sends {@code Accept: application/json}) or
     * {@link Client#take(TakeOptions)} for binary data.</p>
     *
     * @param type "binary" or "json"
     * @return a new TakeOptions instance
     * @deprecated the API selects the response type with the Accept header
     */
    @Deprecated
    public TakeOptions responseType(String type) {
        return copyWith("response_type", type);
    }

    // ========== Serialization ==========

    /**
     * Flat GET query parameter names, keyed by the option they come from.
     * Only parameters that the GET /v1/screenshot endpoint reads are listed.
     */
    private static final Map<String, String> QUERY_PARAM_NAMES = new LinkedHashMap<>();

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    static {
        QUERY_PARAM_NAMES.put("url", "url");
        QUERY_PARAM_NAMES.put("html", "html");
        QUERY_PARAM_NAMES.put("preset", "preset");
        QUERY_PARAM_NAMES.put("device", "device");
        QUERY_PARAM_NAMES.put("width", "width");
        QUERY_PARAM_NAMES.put("height", "height");
        QUERY_PARAM_NAMES.put("scale", "scale");
        QUERY_PARAM_NAMES.put("full_page", "full_page");
        QUERY_PARAM_NAMES.put("element", "selector");
        QUERY_PARAM_NAMES.put("format", "format");
        QUERY_PARAM_NAMES.put("quality", "quality");
        QUERY_PARAM_NAMES.put("delay", "delay");
        QUERY_PARAM_NAMES.put("wait_for_timeout", "timeout");
        QUERY_PARAM_NAMES.put("click", "click");
        QUERY_PARAM_NAMES.put("hide", "hide");
        QUERY_PARAM_NAMES.put("remove", "remove");
        QUERY_PARAM_NAMES.put("block_ads", "block_ads");
        QUERY_PARAM_NAMES.put("block_cookie_banners", "block_cookies");
        QUERY_PARAM_NAMES.put("dark_mode", "dark_mode");
        QUERY_PARAM_NAMES.put("user_agent", "user_agent");
        QUERY_PARAM_NAMES.put("locale", "locale");
        QUERY_PARAM_NAMES.put("timezone", "timezone");
        QUERY_PARAM_NAMES.put("cache_ttl", "cache_ttl");
        QUERY_PARAM_NAMES.put("cache_refresh", "cache_refresh");
        QUERY_PARAM_NAMES.put("cache_available", "cache_available");
        QUERY_PARAM_NAMES.put("pdf_paper_size", "pdf_paper");
        QUERY_PARAM_NAMES.put("pdf_width", "pdf_width");
        QUERY_PARAM_NAMES.put("pdf_height", "pdf_height");
        QUERY_PARAM_NAMES.put("pdf_landscape", "pdf_landscape");
        QUERY_PARAM_NAMES.put("pdf_scale", "pdf_scale");
        QUERY_PARAM_NAMES.put("pdf_fit_one_page", "pdf_fit_one_page");
        QUERY_PARAM_NAMES.put("pdf_margin", "pdf_margin");
        QUERY_PARAM_NAMES.put("pdf_margin_top", "pdf_margin_top");
        QUERY_PARAM_NAMES.put("pdf_margin_right", "pdf_margin_right");
        QUERY_PARAM_NAMES.put("pdf_margin_bottom", "pdf_margin_bottom");
        QUERY_PARAM_NAMES.put("pdf_margin_left", "pdf_margin_left");
        QUERY_PARAM_NAMES.put("pdf_print_background", "pdf_print_background");
        QUERY_PARAM_NAMES.put("pdf_prefer_css_page_size", "pdf_prefer_css_page_size");
        QUERY_PARAM_NAMES.put("pdf_page_ranges", "pdf_page_ranges");
        QUERY_PARAM_NAMES.put("pdf_header", "pdf_header");
        QUERY_PARAM_NAMES.put("pdf_footer", "pdf_footer");
    }

    /**
     * Returns the raw configuration map.
     *
     * @return a copy of the configuration
     */
    public Map<String, Object> toConfig() {
        return new HashMap<>(config);
    }

    /**
     * Converts options to the nested JSON body for POST /v1/screenshot.
     *
     * <p>Options are grouped as the API expects: {@code viewport.width},
     * {@code capture.mode}, {@code output.format}, {@code wait.for_selector},
     * {@code block.ads}, {@code browser.dark_mode}, {@code cache.ttl},
     * {@code pdf.paper}, {@code storage.enabled}, and so on.</p>
     *
     * @return parameters map for the POST body
     */
    public Map<String, Object> toParams() {
        Map<String, Object> params = new LinkedHashMap<>();

        // Top-level
        putIfPresent(params, "url", "url");
        putIfPresent(params, "html", "html");
        putIfPresent(params, "preset", "preset");
        putIfPresent(params, "device", "device");

        // Viewport
        Map<String, Object> viewport = new LinkedHashMap<>();
        putIfPresent(viewport, "width", "width");
        putIfPresent(viewport, "height", "height");
        putIfPresent(viewport, "scale", "scale");
        putIfPresent(viewport, "mobile", "mobile");
        putGroup(params, "viewport", viewport);

        // Capture
        Map<String, Object> capture = new LinkedHashMap<>();
        if (Boolean.TRUE.equals(config.get("full_page"))) {
            capture.put("mode", "full_page");
        }
        putIfPresent(capture, "element", "selector");
        putGroup(params, "capture", capture);

        // Output
        Map<String, Object> output = new LinkedHashMap<>();
        putIfPresent(output, "format", "format");
        putIfPresent(output, "quality", "quality");
        putGroup(params, "output", output);

        // Wait
        Map<String, Object> wait = new LinkedHashMap<>();
        putIfPresent(wait, "wait_for", "until");
        putIfPresent(wait, "delay", "delay");
        putIfPresent(wait, "wait_for_selector", "for_selector");
        putIfPresent(wait, "wait_for_timeout", "timeout");
        putGroup(params, "wait", wait);

        // Block
        Map<String, Object> block = new LinkedHashMap<>();
        putIfPresent(block, "block_ads", "ads");
        putIfPresent(block, "block_trackers", "trackers");
        putIfPresent(block, "block_cookie_banners", "cookie_banners");
        putIfPresent(block, "block_chat_widgets", "chat_widgets");
        putIfPresent(block, "block_urls", "requests");
        putIfPresent(block, "block_resources", "resources");
        putGroup(params, "block", block);

        // Page
        Map<String, Object> page = new LinkedHashMap<>();
        if (config.containsKey("inject_script")) {
            page.put("scripts", Collections.singletonList(config.get("inject_script")));
        }
        if (config.containsKey("inject_style")) {
            page.put("styles", Collections.singletonList(config.get("inject_style")));
        }
        putIfPresent(page, "click", "click");
        putIfPresent(page, "hide", "hide");
        putIfPresent(page, "remove", "remove");
        putGroup(params, "page", page);

        // Browser
        Map<String, Object> browser = new LinkedHashMap<>();
        putIfPresent(browser, "dark_mode", "dark_mode");
        putIfPresent(browser, "reduced_motion", "reduced_motion");
        putIfPresent(browser, "media_type", "media");
        putIfPresent(browser, "user_agent", "user_agent");
        putIfPresent(browser, "timezone", "timezone");
        putIfPresent(browser, "locale", "locale");
        putIfPresent(browser, "geolocation", "geolocation");
        putGroup(params, "browser", browser);

        // Network
        Map<String, Object> network = new LinkedHashMap<>();
        putIfPresent(network, "headers", "headers");
        putIfPresent(network, "cookies", "cookies");
        putIfPresent(network, "bypass_csp", "bypass_csp");
        if (config.containsKey("auth_basic")) {
            Map<String, Object> auth = new LinkedHashMap<>();
            auth.put("type", "basic");
            @SuppressWarnings("unchecked")
            Map<String, Object> basic = (Map<String, Object>) config.get("auth_basic");
            auth.putAll(basic);
            network.put("auth", auth);
        } else if (config.containsKey("auth_bearer")) {
            Map<String, Object> auth = new LinkedHashMap<>();
            auth.put("type", "bearer");
            auth.put("token", config.get("auth_bearer"));
            network.put("auth", auth);
        }
        putGroup(params, "network", network);

        // Cache
        Map<String, Object> cache = new LinkedHashMap<>();
        putIfPresent(cache, "cache_ttl", "ttl");
        putIfPresent(cache, "cache_refresh", "refresh");
        putIfPresent(cache, "cache_available", "available");
        putGroup(params, "cache", cache);

        // PDF
        Map<String, Object> pdf = new LinkedHashMap<>();
        putIfPresent(pdf, "pdf_paper_size", "paper");
        putIfPresent(pdf, "pdf_width", "width");
        putIfPresent(pdf, "pdf_height", "height");
        putIfPresent(pdf, "pdf_landscape", "landscape");
        putIfPresent(pdf, "pdf_scale", "scale");
        putIfPresent(pdf, "pdf_print_background", "print_background");
        putIfPresent(pdf, "pdf_page_ranges", "page_ranges");
        putIfPresent(pdf, "pdf_header", "header");
        putIfPresent(pdf, "pdf_footer", "footer");
        putIfPresent(pdf, "pdf_fit_one_page", "fit_one_page");
        putIfPresent(pdf, "pdf_prefer_css_page_size", "prefer_css_page_size");
        putIfPresent(pdf, "pdf_margin", "margin");
        putIfPresent(pdf, "pdf_margin_top", "margin_top");
        putIfPresent(pdf, "pdf_margin_right", "margin_right");
        putIfPresent(pdf, "pdf_margin_bottom", "margin_bottom");
        putIfPresent(pdf, "pdf_margin_left", "margin_left");
        putGroup(params, "pdf", pdf);

        // Storage
        Map<String, Object> storage = new LinkedHashMap<>();
        putIfPresent(storage, "storage_enabled", "enabled");
        putIfPresent(storage, "storage_path", "path");
        putIfPresent(storage, "storage_acl", "acl");
        putGroup(params, "storage", storage);

        return params;
    }

    /**
     * Converts options to flat query parameters for GET /v1/screenshot.
     *
     * <p>Only options that the GET endpoint reads are included (for example
     * {@code width}, {@code full_page}, {@code selector}, {@code block_cookies},
     * {@code pdf_paper}). Options that only the POST endpoint supports are left out.
     * List values (hide, remove) are joined with commas. Keys are sorted.</p>
     *
     * @return sorted map of query parameter names to string values
     */
    public Map<String, String> toQueryParams() {
        Map<String, String> result = new TreeMap<>();
        for (Map.Entry<String, String> mapping : QUERY_PARAM_NAMES.entrySet()) {
            Object value = config.get(mapping.getKey());
            if (value == null) {
                continue;
            }
            if (value instanceof List) {
                StringJoiner joiner = new StringJoiner(",");
                for (Object item : (List<?>) value) {
                    joiner.add(String.valueOf(item));
                }
                result.put(mapping.getValue(), joiner.toString());
            } else {
                result.put(mapping.getValue(), String.valueOf(value));
            }
        }
        return result;
    }

    /**
     * Converts options to a URL query string for GET /v1/screenshot.
     *
     * <p>Uses the flat parameters from {@link #toQueryParams()}. Values are encoded
     * the same way as Ruby's {@code CGI.escape} and pairs are sorted, which matches
     * the string the API uses to verify signed URLs.</p>
     *
     * @return query string (without leading "?")
     */
    public String toQueryString() {
        return buildQueryString(toQueryParams());
    }

    /**
     * Builds a sorted, CGI-escaped query string from flat parameters.
     *
     * @param params flat parameters
     * @return query string (without leading "?")
     */
    static String buildQueryString(Map<String, String> params) {
        List<String> pairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            pairs.add(cgiEscape(entry.getKey()) + "=" + cgiEscape(entry.getValue()));
        }
        Collections.sort(pairs);
        return String.join("&", pairs);
    }

    /**
     * Encodes a value like Ruby's {@code CGI.escape}: A-Z a-z 0-9 - . _ ~ are kept,
     * space becomes '+', and every other UTF-8 byte becomes %XX (uppercase hex).
     *
     * @param value the value to encode
     * @return the encoded value
     */
    static String cgiEscape(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            int c = b & 0xFF;
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '.' || c == '_' || c == '~') {
                sb.append((char) c);
            } else if (c == ' ') {
                sb.append('+');
            } else {
                sb.append('%');
                sb.append(HEX[c >> 4]);
                sb.append(HEX[c & 0x0F]);
            }
        }
        return sb.toString();
    }

    private void putIfPresent(Map<String, Object> target, String configKey, String apiKey) {
        Object value = config.get(configKey);
        if (value != null) {
            target.put(apiKey, value);
        }
    }

    private static void putGroup(Map<String, Object> params, String name, Map<String, Object> group) {
        if (!group.isEmpty()) {
            params.put(name, group);
        }
    }

    @Override
    public String toString() {
        return "TakeOptions" + config;
    }
}
