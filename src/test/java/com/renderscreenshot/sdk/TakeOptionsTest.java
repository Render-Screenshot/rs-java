package com.renderscreenshot.sdk;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TakeOptionsTest {

    @Test
    void url_createsOptionsWithUrl() {
        TakeOptions options = TakeOptions.url("https://example.com");
        Map<String, Object> config = options.toConfig();

        assertEquals("https://example.com", config.get("url"));
    }

    @Test
    void html_createsOptionsWithHtml() {
        TakeOptions options = TakeOptions.html("<html><body>Hello</body></html>");
        Map<String, Object> config = options.toConfig();

        assertEquals("<html><body>Hello</body></html>", config.get("html"));
    }

    @Test
    void methodChaining_returnsNewInstance() {
        TakeOptions original = TakeOptions.url("https://example.com");
        TakeOptions modified = original.width(1200);

        assertNotSame(original, modified);
        assertEquals(null, original.toConfig().get("width"));
        assertEquals(1200, modified.toConfig().get("width"));
    }

    // ========== Viewport Options ==========

    @Test
    void viewportOptions_setCorrectly() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .width(1200)
                .height(630)
                .scale(2.0)
                .mobile();

        Map<String, Object> config = options.toConfig();
        assertEquals(1200, config.get("width"));
        assertEquals(630, config.get("height"));
        assertEquals(2.0, config.get("scale"));
        assertEquals(true, config.get("mobile"));
    }

    // ========== Capture Options ==========

    @Test
    void captureOptions_setCorrectly() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .fullPage()
                .element("#main")
                .format("png")
                .quality(90);

        Map<String, Object> config = options.toConfig();
        assertEquals(true, config.get("full_page"));
        assertEquals("#main", config.get("element"));
        assertEquals("png", config.get("format"));
        assertEquals(90, config.get("quality"));
    }

    // ========== Wait Options ==========

    @Test
    void waitOptions_setCorrectly() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .waitFor("networkidle")
                .delay(1000)
                .waitForSelector(".loaded")
                .waitForTimeout(5000);

        Map<String, Object> config = options.toConfig();
        assertEquals("networkidle", config.get("wait_for"));
        assertEquals(1000, config.get("delay"));
        assertEquals(".loaded", config.get("wait_for_selector"));
        assertEquals(5000, config.get("wait_for_timeout"));
    }

    // ========== Preset Options ==========

    @Test
    void presetOptions_setCorrectly() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .preset("og_card")
                .device("iphone_14");

        Map<String, Object> config = options.toConfig();
        assertEquals("og_card", config.get("preset"));
        assertEquals("iphone_14", config.get("device"));
    }

    // ========== Blocking Options ==========

    @Test
    void blockingOptions_setCorrectly() {
        List<String> blockedUrls = Arrays.asList("https://ads.com/*", "https://tracking.com/*");
        List<String> blockedResources = Arrays.asList("image", "stylesheet");

        TakeOptions options = TakeOptions.url("https://example.com")
                .blockAds()
                .blockTrackers()
                .blockCookieBanners()
                .blockChatWidgets()
                .blockUrls(blockedUrls)
                .blockResources(blockedResources);

        Map<String, Object> config = options.toConfig();
        assertEquals(true, config.get("block_ads"));
        assertEquals(true, config.get("block_trackers"));
        assertEquals(true, config.get("block_cookie_banners"));
        assertEquals(true, config.get("block_chat_widgets"));
        assertEquals(blockedUrls, config.get("block_urls"));
        assertEquals(blockedResources, config.get("block_resources"));
    }

    // ========== Page Manipulation ==========

    @Test
    void pageManipulation_setCorrectly() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .injectScript("document.body.style.color = 'red';")
                .injectStyle("body { background: blue; }")
                .click("#button")
                .hide(Arrays.asList(".banner", ".ad"))
                .remove(Arrays.asList("#popup", "#modal"));

        Map<String, Object> config = options.toConfig();
        assertEquals("document.body.style.color = 'red';", config.get("inject_script"));
        assertEquals("body { background: blue; }", config.get("inject_style"));
        assertEquals("#button", config.get("click"));
        assertEquals(Arrays.asList(".banner", ".ad"), config.get("hide"));
        assertEquals(Arrays.asList("#popup", "#modal"), config.get("remove"));
    }

    // ========== Browser Emulation ==========

    @Test
    void browserEmulation_setCorrectly() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .darkMode()
                .reducedMotion()
                .mediaType("print")
                .userAgent("Custom Agent")
                .timezone("America/New_York")
                .locale("en-US")
                .geolocation(40.7128, -74.0060);

        Map<String, Object> config = options.toConfig();
        assertEquals(true, config.get("dark_mode"));
        assertEquals(true, config.get("reduced_motion"));
        assertEquals("print", config.get("media_type"));
        assertEquals("Custom Agent", config.get("user_agent"));
        assertEquals("America/New_York", config.get("timezone"));
        assertEquals("en-US", config.get("locale"));

        @SuppressWarnings("unchecked")
        Map<String, Object> geo = (Map<String, Object>) config.get("geolocation");
        assertEquals(40.7128, geo.get("latitude"));
        assertEquals(-74.0060, geo.get("longitude"));
    }

    @Test
    void geolocationWithAccuracy_setsAllFields() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .geolocation(40.7128, -74.0060, 100.0);

        @SuppressWarnings("unchecked")
        Map<String, Object> geo = (Map<String, Object>) options.toConfig().get("geolocation");
        assertEquals(40.7128, geo.get("latitude"));
        assertEquals(-74.0060, geo.get("longitude"));
        assertEquals(100.0, geo.get("accuracy"));
    }

    // ========== Network Options ==========

    @Test
    void networkOptions_setCorrectly() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "value");

        TakeOptions options = TakeOptions.url("https://example.com")
                .headers(headers)
                .authBasic("user", "pass")
                .authBearer("token123")
                .bypassCsp();

        Map<String, Object> config = options.toConfig();
        assertEquals(headers, config.get("headers"));
        assertEquals("token123", config.get("auth_bearer"));
        assertEquals(true, config.get("bypass_csp"));

        @SuppressWarnings("unchecked")
        Map<String, String> auth = (Map<String, String>) config.get("auth_basic");
        assertEquals("user", auth.get("username"));
        assertEquals("pass", auth.get("password"));
    }

    // ========== Cache Options ==========

    @Test
    void cacheOptions_setCorrectly() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .cacheTtl(3600)
                .cacheRefresh();

        Map<String, Object> config = options.toConfig();
        assertEquals(3600, config.get("cache_ttl"));
        assertEquals(true, config.get("cache_refresh"));
    }

    // ========== PDF Options ==========

    @Test
    void pdfOptions_setCorrectly() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .format("pdf")
                .pdfPaperSize("letter")
                .pdfWidth("8.5in")
                .pdfHeight("11in")
                .pdfLandscape()
                .pdfMargin("1in")
                .pdfMarginTop("0.5in")
                .pdfMarginRight("0.75in")
                .pdfMarginBottom("0.5in")
                .pdfMarginLeft("0.75in")
                .pdfScale(0.8)
                .pdfPrintBackground()
                .pdfPageRanges("1-5")
                .pdfHeader("<div>Header</div>")
                .pdfFooter("<div>Footer</div>")
                .pdfFitOnePage()
                .pdfPreferCssPageSize();

        Map<String, Object> config = options.toConfig();
        assertEquals("pdf", config.get("format"));
        assertEquals("letter", config.get("pdf_paper_size"));
        assertEquals("8.5in", config.get("pdf_width"));
        assertEquals("11in", config.get("pdf_height"));
        assertEquals(true, config.get("pdf_landscape"));
        assertEquals("1in", config.get("pdf_margin"));
        assertEquals("0.5in", config.get("pdf_margin_top"));
        assertEquals("0.75in", config.get("pdf_margin_right"));
        assertEquals("0.5in", config.get("pdf_margin_bottom"));
        assertEquals("0.75in", config.get("pdf_margin_left"));
        assertEquals(0.8, config.get("pdf_scale"));
        assertEquals(true, config.get("pdf_print_background"));
        assertEquals("1-5", config.get("pdf_page_ranges"));
        assertEquals("<div>Header</div>", config.get("pdf_header"));
        assertEquals("<div>Footer</div>", config.get("pdf_footer"));
        assertEquals(true, config.get("pdf_fit_one_page"));
        assertEquals(true, config.get("pdf_prefer_css_page_size"));
    }

    // ========== Storage Options ==========

    @Test
    void storageOptions_setCorrectly() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .storageEnabled()
                .storagePath("screenshots/2024/")
                .storageAcl("public-read");

        Map<String, Object> config = options.toConfig();
        assertEquals(true, config.get("storage_enabled"));
        assertEquals("screenshots/2024/", config.get("storage_path"));
        assertEquals("public-read", config.get("storage_acl"));
    }

    // ========== toParams ==========

    @Test
    void toParams_nests_viewportOptions() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .width(1200)
                .height(630)
                .scale(2.0);

        Map<String, Object> params = options.toParams();
        assertEquals("https://example.com", params.get("url"));

        @SuppressWarnings("unchecked")
        Map<String, Object> viewport = (Map<String, Object>) params.get("viewport");
        assertEquals(1200, viewport.get("width"));
        assertEquals(630, viewport.get("height"));
        assertEquals(2.0, viewport.get("scale"));
    }

    @Test
    void toParams_nests_pdfOptions() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .format("pdf")
                .pdfPaperSize("a4")
                .pdfLandscape();

        Map<String, Object> params = options.toParams();

        @SuppressWarnings("unchecked")
        Map<String, Object> pdf = (Map<String, Object>) params.get("pdf");
        assertEquals("a4", pdf.get("paper_size"));
        assertEquals(true, pdf.get("landscape"));
    }

    @Test
    void toParams_nests_storageOptions() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .storageEnabled()
                .storagePath("path/");

        Map<String, Object> params = options.toParams();

        @SuppressWarnings("unchecked")
        Map<String, Object> storage = (Map<String, Object>) params.get("storage");
        assertEquals(true, storage.get("enabled"));
        assertEquals("path/", storage.get("path"));
    }

    // ========== toQueryString ==========

    @Test
    void toQueryString_encodesParameters() {
        TakeOptions options = TakeOptions.url("https://example.com/path?query=value")
                .width(1200);

        String qs = options.toQueryString();
        assertTrue(qs.contains("url=https%3A%2F%2Fexample.com%2Fpath%3Fquery%3Dvalue"));
        assertTrue(qs.contains("width=1200"));
    }

    @Test
    void toQueryString_handlesBooleans() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .blockAds()
                .darkMode();

        String qs = options.toQueryString();
        assertTrue(qs.contains("block_ads=true"));
        assertTrue(qs.contains("dark_mode=true"));
    }

    @Test
    void toQueryString_handlesList() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .blockUrls(Arrays.asList("https://a.com", "https://b.com"));

        String qs = options.toQueryString();
        assertTrue(qs.contains("block_urls%5B%5D=https%3A%2F%2Fa.com"));
        assertTrue(qs.contains("block_urls%5B%5D=https%3A%2F%2Fb.com"));
    }

    // ========== Immutability ==========

    @Test
    void options_areImmutable() {
        TakeOptions original = TakeOptions.url("https://example.com");
        TakeOptions modified = original.width(1200).height(630);

        // Original should be unchanged
        Map<String, Object> origConfig = original.toConfig();
        assertEquals(1, origConfig.size());
        assertEquals("https://example.com", origConfig.get("url"));

        // Modified should have all values
        Map<String, Object> modConfig = modified.toConfig();
        assertEquals(3, modConfig.size());
        assertEquals("https://example.com", modConfig.get("url"));
        assertEquals(1200, modConfig.get("width"));
        assertEquals(630, modConfig.get("height"));
    }

    // ========== Full Example ==========

    @Test
    void fullExample_ogCard() {
        TakeOptions options = TakeOptions.url("https://example.com")
                .width(1200)
                .height(630)
                .format("png")
                .blockAds()
                .blockTrackers()
                .blockCookieBanners()
                .cacheTtl(86400);

        Map<String, Object> params = options.toParams();
        assertEquals("https://example.com", params.get("url"));
        assertEquals("png", params.get("format"));
        assertEquals(true, params.get("block_ads"));
        assertEquals(true, params.get("block_trackers"));
        assertEquals(true, params.get("block_cookie_banners"));
        assertEquals(86400, params.get("cache_ttl"));

        @SuppressWarnings("unchecked")
        Map<String, Object> viewport = (Map<String, Object>) params.get("viewport");
        assertEquals(1200, viewport.get("width"));
        assertEquals(630, viewport.get("height"));
    }
}
