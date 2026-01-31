# RenderScreenshot Java SDK

Official Java SDK for the [RenderScreenshot](https://renderscreenshot.com) API — capture web page screenshots programmatically.

[![CI](https://github.com/render-screenshot/rs-java/actions/workflows/ci.yml/badge.svg)](https://github.com/render-screenshot/rs-java/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-11%2B-blue)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Installation

### Maven

```xml
<dependency>
    <groupId>com.renderscreenshot</groupId>
    <artifactId>sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.renderscreenshot:sdk:1.0.0'
```

## Requirements

- Java 11 or higher
- A RenderScreenshot API key ([get one here](https://renderscreenshot.com))

## Quick Start

```java
import com.renderscreenshot.sdk.Client;
import com.renderscreenshot.sdk.TakeOptions;

import java.nio.file.Files;
import java.nio.file.Path;

public class Example {
    public static void main(String[] args) throws Exception {
        Client client = new Client("your_api_key");

        // Capture a screenshot
        byte[] screenshot = client.take(TakeOptions.url("https://example.com")
            .width(1200)
            .height(630)
            .format("png"));

        // Save to file
        Files.write(Path.of("screenshot.png"), screenshot);
    }
}
```

## Features

- **Simple API** — Fluent builder pattern for screenshot options
- **Multiple formats** — PNG, JPEG, WebP, and PDF
- **Device emulation** — Mobile devices, tablets, and custom viewports
- **Content blocking** — Block ads, trackers, cookie banners, and chat widgets
- **Browser emulation** — Dark mode, reduced motion, custom user agents
- **Batch processing** — Capture multiple URLs efficiently
- **Signed URLs** — Generate secure URLs for client-side use
- **Cache management** — Control caching and purge cached screenshots
- **Webhook support** — Verify webhook signatures

## Usage

### Basic Screenshot

```java
Client client = new Client("your_api_key");

byte[] screenshot = client.take(TakeOptions.url("https://example.com"));
Files.write(Path.of("screenshot.png"), screenshot);
```

### Custom Dimensions

```java
byte[] screenshot = client.take(TakeOptions.url("https://example.com")
    .width(1200)
    .height(630)
    .format("png")
    .quality(90));
```

### Using Presets

```java
// Open Graph card (1200x630)
byte[] ogCard = client.take(TakeOptions.url("https://example.com")
    .preset("og_card"));

// Twitter card (800x418)
byte[] twitterCard = client.take(TakeOptions.url("https://example.com")
    .preset("twitter_card"));

// Full page capture
byte[] fullPage = client.take(TakeOptions.url("https://example.com")
    .fullPage());
```

### Device Emulation

```java
// iPhone 14
byte[] mobile = client.take(TakeOptions.url("https://example.com")
    .device("iphone_14"));

// Custom mobile viewport
byte[] custom = client.take(TakeOptions.url("https://example.com")
    .width(375)
    .height(667)
    .scale(2.0)
    .mobile());
```

### Content Blocking

```java
byte[] clean = client.take(TakeOptions.url("https://example.com")
    .blockAds()
    .blockTrackers()
    .blockCookieBanners()
    .blockChatWidgets());
```

### Dark Mode

```java
byte[] dark = client.take(TakeOptions.url("https://example.com")
    .darkMode()
    .width(1200)
    .height(630));
```

### JSON Response (Metadata)

```java
ScreenshotResponse response = client.takeJson(TakeOptions.url("https://example.com")
    .width(1200)
    .height(630));

System.out.println("URL: " + response.getUrl());
System.out.println("Size: " + response.getSize() + " bytes");
System.out.println("Cached: " + response.isCached());
```

### Signed URLs

Generate signed URLs for use in `<img>` tags without exposing your API key:

```java
import java.time.Duration;
import java.time.Instant;

TakeOptions options = TakeOptions.url("https://example.com")
    .width(1200)
    .height(630);

// URL expires in 1 hour
Instant expiresAt = Instant.now().plus(Duration.ofHours(1));
String signedUrl = client.generateUrl(options, expiresAt);

// Use in HTML: <img src="{signedUrl}" />
```

### Batch Processing

Capture multiple URLs efficiently:

```java
import java.util.Arrays;
import java.util.List;

// Simple batch with same options
List<String> urls = Arrays.asList(
    "https://example.com",
    "https://example.org",
    "https://example.net"
);

TakeOptions options = TakeOptions.url("dummy")
    .width(1200)
    .height(630);

BatchResponse batch = client.batch(urls, options);

// Poll for completion
while (!batch.isComplete()) {
    Thread.sleep(1000);
    batch = client.getBatch(batch.getId());
}

// Process results
for (BatchResult result : batch.getResults()) {
    if (result.isSuccess()) {
        System.out.println(result.getUrl() + " -> " + result.getResponse().getUrl());
    } else {
        System.out.println(result.getUrl() + " failed: " + result.getError());
    }
}
```

### Advanced Batch (Individual Options)

```java
import com.renderscreenshot.sdk.Client.BatchRequest;

List<BatchRequest> requests = Arrays.asList(
    new BatchRequest("https://example.com",
        TakeOptions.url("https://example.com").width(1200).height(630)),
    new BatchRequest("https://example.org",
        TakeOptions.url("https://example.org").device("iphone_14").darkMode())
);

BatchResponse batch = client.batchAdvanced(requests);
```

### PDF Generation

```java
byte[] pdf = client.take(TakeOptions.url("https://example.com")
    .format("pdf")
    .pdfPaperSize("a4")
    .pdfMargin("1in")
    .pdfPrintBackground());

Files.write(Path.of("document.pdf"), pdf);
```

### HTML to Screenshot

```java
String html = """
    <!DOCTYPE html>
    <html>
    <head><title>Hello</title></head>
    <body><h1>Hello World</h1></body>
    </html>
    """;

byte[] screenshot = client.take(TakeOptions.html(html)
    .width(800)
    .height(600));
```

### Cache Management

```java
// Get cached screenshot
byte[] cached = client.cache.get("cache_key_here");

// Delete specific cache entry
client.cache.delete("cache_key_here");

// Purge multiple keys
CacheManager.PurgeResponse response = client.cache.purge(
    Arrays.asList("key1", "key2", "key3"));
System.out.println("Purged: " + response.getPurged());

// Purge by URL pattern
client.cache.purgeUrl("https://example.com/*");

// Purge by storage path pattern
client.cache.purgePattern("screenshots/2024/*");

// Purge entries older than a date
client.cache.purgeBefore(Instant.parse("2024-01-01T00:00:00Z"));
```

### Webhook Verification

```java
import com.renderscreenshot.sdk.Webhook;
import com.renderscreenshot.sdk.Webhook.WebhookEvent;

// In your webhook handler
String payload = request.getBody();
String signature = request.getHeader("X-RenderScreenshot-Signature");
String timestamp = request.getHeader("X-RenderScreenshot-Timestamp");

if (Webhook.verify(payload, signature, timestamp, webhookSecret)) {
    WebhookEvent event = Webhook.parse(payload);
    System.out.println("Event type: " + event.getType());
    System.out.println("Event data: " + event.getData());
} else {
    // Invalid signature - reject request
}
```

## TakeOptions Reference

### Target

| Method | Description |
|--------|-------------|
| `url(String)` | URL to capture |
| `html(String)` | HTML content to render |

### Viewport

| Method | Description |
|--------|-------------|
| `width(int)` | Viewport width in pixels |
| `height(int)` | Viewport height in pixels |
| `scale(double)` | Device scale factor (e.g., 2.0 for retina) |
| `mobile()` | Enable mobile emulation |

### Capture

| Method | Description |
|--------|-------------|
| `fullPage()` | Capture full scrollable page |
| `element(String)` | Capture specific CSS selector |
| `format(String)` | Output format: "png", "jpeg", "webp", "pdf" |
| `quality(int)` | Image quality 0-100 (for jpeg/webp) |

### Wait

| Method | Description |
|--------|-------------|
| `waitFor(String)` | Wait strategy: "load", "domcontentloaded", "networkidle" |
| `delay(int)` | Additional delay in milliseconds |
| `waitForSelector(String)` | Wait for CSS selector to appear |
| `waitForTimeout(int)` | Maximum wait time in milliseconds |

### Presets

| Method | Description |
|--------|-------------|
| `preset(String)` | Use preset: "og_card", "twitter_card", etc. |
| `device(String)` | Emulate device: "iphone_14", "pixel_7", etc. |

### Blocking

| Method | Description |
|--------|-------------|
| `blockAds()` | Block advertisements |
| `blockTrackers()` | Block tracking scripts |
| `blockCookieBanners()` | Block cookie consent banners |
| `blockChatWidgets()` | Block chat widgets |
| `blockUrls(List<String>)` | Block specific URL patterns |
| `blockResources(List<String>)` | Block resource types |

### Page Manipulation

| Method | Description |
|--------|-------------|
| `injectScript(String)` | Inject JavaScript before capture |
| `injectStyle(String)` | Inject CSS before capture |
| `click(String)` | Click element before capture |
| `hide(List<String>)` | Hide elements matching selectors |
| `remove(List<String>)` | Remove elements matching selectors |

### Browser Emulation

| Method | Description |
|--------|-------------|
| `darkMode()` | Enable dark color scheme |
| `reducedMotion()` | Enable reduced motion |
| `mediaType(String)` | CSS media type: "screen", "print" |
| `userAgent(String)` | Custom user agent string |
| `timezone(String)` | Timezone (e.g., "America/New_York") |
| `locale(String)` | Locale (e.g., "en-US") |
| `geolocation(lat, lon)` | Set geolocation |

### Network

| Method | Description |
|--------|-------------|
| `headers(Map<String, String>)` | Custom request headers |
| `cookies(List<Map<String, Object>>)` | Set cookies |
| `authBasic(user, pass)` | HTTP Basic auth |
| `authBearer(token)` | Bearer token auth |
| `bypassCsp()` | Bypass Content Security Policy |

### Cache

| Method | Description |
|--------|-------------|
| `cacheTtl(int)` | Cache TTL in seconds |
| `cacheRefresh()` | Bypass cache for this request |

### PDF Options

| Method | Description |
|--------|-------------|
| `pdfPaperSize(String)` | Paper size: "letter", "a4", etc. |
| `pdfWidth(String)` | Custom width (e.g., "8.5in") |
| `pdfHeight(String)` | Custom height (e.g., "11in") |
| `pdfLandscape()` | Landscape orientation |
| `pdfMargin(String)` | All margins (e.g., "1in") |
| `pdfMarginTop/Right/Bottom/Left(String)` | Individual margins |
| `pdfScale(double)` | Scale factor (0.1-2.0) |
| `pdfPrintBackground()` | Print background graphics |
| `pdfPageRanges(String)` | Page ranges (e.g., "1-5, 8") |
| `pdfHeader(String)` | Header HTML template |
| `pdfFooter(String)` | Footer HTML template |
| `pdfFitOnePage()` | Fit content to one page |
| `pdfPreferCssPageSize()` | Use CSS @page size |

### Storage

| Method | Description |
|--------|-------------|
| `storageEnabled()` | Enable R2 storage |
| `storagePath(String)` | Storage path prefix |
| `storageAcl(String)` | ACL: "private", "public-read" |

## Error Handling

```java
import com.renderscreenshot.sdk.RenderScreenshotException;

try {
    byte[] screenshot = client.take(options);
} catch (RenderScreenshotException e) {
    System.out.println("Error: " + e.getMessage());
    System.out.println("HTTP Status: " + e.getHttpStatus());
    System.out.println("Error Code: " + e.getCode());

    if (e.isRetryable()) {
        Integer retryAfter = e.getRetryAfter();
        if (retryAfter != null) {
            System.out.println("Retry after: " + retryAfter + " seconds");
            Thread.sleep(retryAfter * 1000L);
            // Retry the request...
        }
    }
}
```

## Development

```bash
# Run tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Check code style
mvn checkstyle:check

# Build package
mvn package
```

## Links

- [Documentation](https://renderscreenshot.com/docs)
- [API Reference](https://renderscreenshot.com/docs/api)
- [GitHub](https://github.com/render-screenshot/rs-java)

## License

MIT License - see [LICENSE](LICENSE) for details.
