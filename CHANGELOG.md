# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-01-30

### Added

- Initial release of the RenderScreenshot Java SDK
- `Client` class for API interactions
  - `take()` - Capture screenshot as binary data
  - `takeJson()` - Capture screenshot with metadata response
  - `generateUrl()` - Generate signed URLs
  - `batch()` - Batch capture multiple URLs
  - `batchAdvanced()` - Batch capture with individual options
  - `getBatch()` - Get batch status
  - `presets()` - List available presets
  - `preset()` - Get preset details
  - `devices()` - List available devices
- `TakeOptions` fluent builder with 60+ configuration options
  - Viewport settings (width, height, scale, mobile)
  - Capture options (fullPage, element, format, quality)
  - Wait strategies (waitFor, delay, waitForSelector)
  - Presets and device emulation
  - Content blocking (ads, trackers, cookie banners, chat widgets)
  - Page manipulation (inject script/style, click, hide, remove)
  - Browser emulation (dark mode, reduced motion, timezone, locale, geolocation)
  - Network options (headers, cookies, auth)
  - Cache control
  - PDF generation options
  - Storage options
- `CacheManager` for cache operations
  - `get()` - Retrieve cached screenshot
  - `delete()` - Delete cache entry
  - `purge()` - Purge multiple entries
  - `purgeUrl()` - Purge by URL pattern
  - `purgeBefore()` - Purge entries before date
- `Webhook` utilities
  - `verify()` - Verify webhook signatures
  - `parse()` - Parse webhook payloads
  - `extractHeaders()` - Extract webhook headers
- `RenderScreenshotException` with detailed error information
  - HTTP status codes
  - API error codes
  - Retry information for rate limits
- Response models
  - `ScreenshotResponse`
  - `BatchResponse`
  - `BatchResult`
  - `Preset`
  - `Device`
- Comprehensive test suite with >90% coverage
- GitHub Actions CI with Java 11, 17, 21 matrix
- Checkstyle code style enforcement
- JaCoCo code coverage reporting

[1.0.0]: https://github.com/render-screenshot/rs-java/releases/tag/v1.0.0
