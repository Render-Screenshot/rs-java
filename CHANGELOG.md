# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.2] - 2026-10-18

### Fixed

- `take()`, `takeJson()`, `batch()` and `batchAdvanced()` now send the nested
  JSON body that the API expects (`viewport.*`, `capture.mode`/`capture.selector`,
  `output.*`, `wait.*` including `wait.for_selector`, `block.*`, `page.*`,
  `browser.*`, `network.*`, `cache.*`, `pdf.*`, `storage.*`). Before, most
  options were sent as flat top-level keys and the API ignored them.
- PDF options use the API names: `pdf.paper` (was `paper_size`),
  `pdf.print_background`, `pdf.margin_top` etc.
- `takeJson()` sends `Accept: application/json` instead of the ignored
  `response_type` body field. `TakeOptions.responseType()` is deprecated and
  is no longer sent.
- `generateUrl()` now creates signed URLs that the API accepts: the query has
  `key_id` (`rs_pub_...`) and `expires`, uses the GET parameter names
  (`selector`, `timeout`, `block_cookies`, `pdf_paper`, comma-joined `hide`/`remove`),
  is sorted and encoded like Ruby `CGI.escape` (`~` kept, `*` encoded), and is
  signed with HMAC-SHA256 using the secret key (`rs_secret_...`), not the API key.
- `ScreenshotResponse` (from `takeJson()`) now maps the real API response:
  `getId()`, `getStatus()`, `getImage()` (`url`, `width`, `height`, `size`,
  `format`), `getMetadata()`, `getCache()` (`hit`, `key`, `url`, `expires_at`)
  and `getUsage()` (`credits`, `remaining`). Before, `getUrl()`, `getWidth()`,
  `isCached()` etc. read flat fields that the API never sends, so they were
  always null, 0 or false. These getters now read the nested objects.
  `getTtl()` and `getStoragePath()` are deprecated (the API has no such fields).
- `WebhookEvent` now reads the `event` field. `getType()` was always null; it now
  returns the same value as the new `getEvent()`. Added `getTimestamp()` and
  `getErrorCode()` (`data.error_code` on failed events).
- `Webhook.verify()` now accepts the `X-Webhook-Signature` header as the API
  sends it (`sha256=<hex>`). Before, real webhooks always failed verification.
  Bare hex is still accepted.
- `batchAdvanced()`: the `BatchRequest` URL now takes precedence over a URL in
  its options; `batch()` no longer sends `url`/`html` inside shared options.

### Added

- `Client(String apiKey, String baseUrl, Duration timeout, String signingKey, String publicKeyId)`.
- `generateUrl(TakeOptions, Instant, String signingKey, String publicKeyId)`.
  `generateUrl` throws `RenderScreenshotException` (`invalid_request`) when
  signing credentials are missing.
- `TakeOptions.toQueryParams()` returns the flat GET parameters.

### Changed

- Removed the `kotlin-stdlib` 2.4.20 pin added in 2.0.0. CVE-2026-53914
  (GHSA-r937-wjx7-w2jp) affects `kotlin-gradle-plugin` only, not
  `kotlin-stdlib`; the earlier scanner flagged it by product name.

## [2.0.1] - 2026-10-05

### Fixed

- JitPack build: add `jitpack.yml` (JitPack's image no longer ships Maven).
  The v2.0.0 tag could not be built on JitPack; use 2.0.1. The code is the
  same as 2.0.0.

## [2.0.0] - 2026-10-05

### Changed (breaking)

- `BatchResult` now matches the batch API response: `getStatus()`,
  `getImage()` (a new `BatchImage` with `getImageUrl()`, `getWidth()`,
  `getHeight()`, `getSize()`, `getFormat()`), `getPosition()` and
  `getResponseTimeMs()`. `getError()` now returns the error message `String`.
- Removed `BatchResult.getResponse()`/`setResponse()`, `setSuccess()` and the
  `BatchError` class. The API never returned those fields, so they were
  always empty. `isSuccess()` still works and is derived from `status`.

### Security

- OkHttp upgraded to 5.5.0 and `kotlin-stdlib` pinned to 2.4.20
  (CVE-2026-53914)
- Jackson Databind upgraded to 2.22.3 (CVE-2026-54512 to CVE-2026-54518)

## [1.1.0] - 2026-02-24

### Changed

- **OkHttp** upgraded from 4.12.0 to 5.3.2
  - Migrated Maven artifact from `okhttp` to `okhttp-jvm` (required for OkHttp 5 on Maven)
  - No API changes required (existing parameter order already compatible)
- **Jackson Databind** upgraded from 2.16.1 to 2.21.1 (5 minor versions of bug fixes and improvements)
- **JUnit Jupiter** upgraded from 5.10.1 to 5.11.0
- **Maven plugins** upgraded to latest versions:
  - maven-compiler-plugin 3.12.1 → 3.15.0
  - maven-surefire-plugin 3.2.3 → 3.5.5
  - maven-source-plugin 3.3.0 → 3.4.0
  - maven-javadoc-plugin 3.6.3 → 3.12.0
  - maven-checkstyle-plugin 3.3.1 → 3.6.0
  - jacoco-maven-plugin 0.8.11 → 0.8.14

### Added

- Java 25 (LTS) added to CI test matrix
- Dependabot configuration for automated dependency updates

### Fixed

- OWASP dependency check now fails the build on high-severity CVEs (was silently passing)
- Codecov action upgraded from v3 to v5
- Pre-commit hooks updated (gitleaks v8.30.0, pre-commit-hooks v6.0.0)

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

[1.1.0]: https://github.com/render-screenshot/rs-java/releases/tag/v1.1.0
[1.0.0]: https://github.com/render-screenshot/rs-java/releases/tag/v1.0.0
