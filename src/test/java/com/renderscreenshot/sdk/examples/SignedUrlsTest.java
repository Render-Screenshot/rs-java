package com.renderscreenshot.sdk.examples;

import com.renderscreenshot.sdk.Client;
import com.renderscreenshot.sdk.TakeOptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that demonstrate signed URL generation examples.
 */
class SignedUrlsTest {

    /**
     * Example: Generate a signed URL for embedding in an img tag.
     */
    @Test
    void basicSignedUrl() {
        Client client = new Client("test_secret_key", "https://api.renderscreenshot.com", Duration.ofSeconds(30));

        // Example code from README
        TakeOptions options = TakeOptions.url("https://example.com")
                .width(1200)
                .height(630);

        // URL expires in 1 hour
        Instant expiresAt = Instant.now().plus(Duration.ofHours(1));

        String signedUrl = client.generateUrl(options, expiresAt);

        // Can be used in HTML: <img src="{signedUrl}" />
        assertTrue(signedUrl.startsWith("https://api.renderscreenshot.com/v1/screenshot?"));
        assertTrue(signedUrl.contains("url="));
        assertTrue(signedUrl.contains("width=1200"));
        assertTrue(signedUrl.contains("height=630"));
        assertTrue(signedUrl.contains("expires="));
        assertTrue(signedUrl.contains("signature="));
    }

    /**
     * Example: Signed URL with OG card preset.
     */
    @Test
    void signedUrlWithPreset() {
        Client client = new Client("test_secret_key", "https://api.renderscreenshot.com", Duration.ofSeconds(30));

        // Example code from README
        TakeOptions options = TakeOptions.url("https://example.com/blog/my-post")
                .preset("og_card")
                .blockAds()
                .blockCookieBanners();

        // URL expires in 24 hours
        Instant expiresAt = Instant.now().plus(Duration.ofDays(1));

        String signedUrl = client.generateUrl(options, expiresAt);

        assertTrue(signedUrl.contains("preset=og_card"));
        assertTrue(signedUrl.contains("block_ads=true"));
        assertTrue(signedUrl.contains("block_cookie_banners=true"));
    }

    /**
     * Example: Signed URL with long expiration for CDN caching.
     */
    @Test
    void signedUrlForCdn() {
        Client client = new Client("test_secret_key", "https://api.renderscreenshot.com", Duration.ofSeconds(30));

        // Example code from README
        TakeOptions options = TakeOptions.url("https://example.com")
                .width(800)
                .height(600)
                .cacheTtl(86400); // Cache for 1 day

        // Max expiration: 30 days
        Instant expiresAt = Instant.now().plus(Duration.ofDays(30));

        String signedUrl = client.generateUrl(options, expiresAt);

        assertTrue(signedUrl.contains("cache_ttl=86400"));
    }

    /**
     * Example: Signed URL with dark mode and mobile emulation.
     */
    @Test
    void signedUrlWithEmulation() {
        Client client = new Client("test_secret_key", "https://api.renderscreenshot.com", Duration.ofSeconds(30));

        // Example code from README
        TakeOptions options = TakeOptions.url("https://example.com")
                .device("iphone_14")
                .darkMode();

        Instant expiresAt = Instant.now().plus(Duration.ofHours(2));

        String signedUrl = client.generateUrl(options, expiresAt);

        assertTrue(signedUrl.contains("device=iphone_14"));
        assertTrue(signedUrl.contains("dark_mode=true"));
    }
}
