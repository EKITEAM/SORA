# SORA

<p align="center">
  <img src="assets/logo.png" width="140" alt="SORA Logo" />
</p>

<h1 align="center">SORA</h1>

<p align="center">
  <b>Image Loader for Android</b><br />
  Lightweight • Fast • Reliable • Easy to use
</p>

<p align="center">
  <a href="https://developer.android.com/">
    <img alt="Android" src="https://img.shields.io/badge/Android-21%2B-brightgreen" />
  </a>
  <a href="https://jitpack.io/#EKITEAM/SORA">
    <img alt="JitPack" src="https://img.shields.io/jitpack/v/github/EKITEAM/SORA?label=JitPack" />
  </a>
  <a href="https://github.com/EKITEAM/SORA/releases/tag/v1.0.5">
    <img alt="Release" src="https://img.shields.io/badge/Release-v1.0.5-blue" />
  </a>
  <a href="LICENSE">
    <img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-orange.svg" />
  </a>
</p>

**SORA** is a lightweight Android image loading library built for API 21+.
It is designed to load and render images efficiently with memory and disk caching, SVG support, smart resizing, request cancellation, and graceful fallback handling.

<p align="center">
  <img src="assets/banner.png" alt="SORA Banner" />
</p>

## Current Release

- Version: `v1.0.5`
- Distribution: [JitPack](https://jitpack.io/#EKITEAM/SORA)
- Artifact: `com.github.EKITEAM:SORA:v1.0.5`

---

## Features

- Multi-level caching with memory and disk support
- Network image loading over HTTP/HTTPS
- SVG rendering support
- Optional animated image support
- Smart resizing and downsampling
- Background loading on worker threads
- Request cancellation by tag or target
- Verification keys for recycled views
- Loading state observation via `ImageLoadObserver`
- Memory trimming support
- Placeholder and error drawables

---

## Requirements

- Minimum Android SDK: 21
- Target SDK: 34
- Java 8 or later
- Java 11 recommended

---

## Installation

SORA is published through **JitPack**.

### 1. Add the JitPack repository

In `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

If your project still uses the older Gradle setup, add JitPack in the root `build.gradle` repositories block instead.

### 2. Add the dependency

```gradle
dependencies {
    implementation 'com.github.EKITEAM:SORA:1.0.5'
}
```

### 3. Add internet permission

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Quick Start

### Load an image into an `ImageView`

```java
ImageLoader.with(context)
    .load("https://example.com/image.jpg")
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error)
    .resize(300, 300)
    .into(imageView);
```

### Observe loading state

```java
ImageLoader.with(context)
    .load(url)
    .into(imageView, new ImageLoadObserver() {
        @Override
        public void onStateChanged(ImageLoadState state) {
            if (state.getStatus() == ImageLoadState.Status.SUCCESS) {
                // Image loaded successfully
            } else if (state.getStatus() == ImageLoadState.Status.ERROR) {
                LoadError error = state.getError();
                // Handle error
            }
        }
    });
```

---

## Request Builder

| Method | Description |
|---|---|
| `load(String url)` | Set the image URL (HTTP/HTTPS) |
| `placeholder(int resId)` | Drawable shown while loading |
| `error(int resId)` | Drawable shown on failure |
| `resize(int width, int height)` | Target dimensions for downsampling |
| `allowSvg(boolean)` | Enable or disable SVG decoding |
| `allowAnimated(boolean)` | Enable or disable animated drawable support |
| `memoryCache(boolean)` | Store in memory cache |
| `diskCache(boolean)` | Store in disk cache |
| `tag(Object tag)` | Group requests for cancellation |
| `verificationKey(String key)` | Help prevent recycled-view mismatches |

### Example

```java
ImageLoader.with(context)
    .load("https://example.com/avatar.svg")
    .placeholder(R.drawable.ic_placeholder)
    .error(R.drawable.ic_broken_image)
    .resize(256, 256)
    .allowSvg(true)
    .allowAnimated(true)
    .memoryCache(true)
    .diskCache(true)
    .tag("profile_screen")
    .verificationKey("user_123_" + imageUrl)
    .into(imageView);
```

---

## Request Cancellation

```java
// Cancel by tag
imageLoader.cancelRequests("profile_screen");

// Cancel a specific target
imageLoader.cancel(imageView);

// Cancel all
imageLoader.cancelAll();
```

---

## Cache Management

```java
// Clear memory cache
imageLoader.clearMemoryCache();

// Clear disk cache
imageLoader.clearDiskCache();

// Trim memory
imageLoader.trimMemory(level);
```

---

## API Reference

### `ImageLoader`

| Method | Description |
|---|---|
| `pause()` | Pause all new requests |
| `resume()` | Resume processing |
| `shutdown()` | Shutdown the executor and clear caches |
| `trimMemory(int level)` | Forward Android trim-memory events |

### `ImageLoadState`

| Status | Meaning |
|---|---|
| `IDLE` | No active request |
| `LOADING` | Request is in progress |
| `SUCCESS` | Image loaded successfully |
| `ERROR` | Loading failed |
| `CANCELED` | Request was canceled |

Additional fields and methods:

- `getUrl()` — Original URL
- `getDrawable()` — Resulting drawable on success
- `getError()` — `LoadError` with type and cause

### `LoadError.ErrorType`

- `NETWORK` — Connection or timeout issues
- `DECODE` — Failed to decode image data
- `SVG` — SVG parsing failure
- `HTTP` — Non-200 response code
- `CANCELED` — Explicit cancellation
- `UNKNOWN` — Any other exception

---

## ProGuard / R8

If you enable obfuscation, add these rules to `proguard-rules.pro`:

```proguard
# SORA library
-keep class com.ekidevs.sora.** { *; }
-keepclassmembers class com.ekidevs.sora.** { *; }

# SVG library
-keep class com.caverock.androidsvg.** { *; }
```

---

## User-Agent

By default, SORA sends:

```text
Sora/1.0.5
```

The version matches the library `versionName`.

---

## Release Notes

### v1.0.5

- First successful JitPack release
- AndroidX compatibility fixes
- Gradle wrapper and publishing setup
- Resource and manifest fixes
- Stable `.aar` artifact generation

JitPack dependency:

```gradle
implementation 'com.github.EKITEAM:SORA:v1.0.5'
```

---

## Contributing

Contributions are welcome.

Please open an issue or submit a pull request:
https://github.com/EKITEAM/SORA

---

## Support

For questions, bug reports, or feature requests:
https://github.com/EKITEAM/SORA/issues

---

## License

SORA is licensed under the **Apache License 2.0**.

See the [LICENSE](LICENSE) file for full details.

---

<p align="center">
  <b>SORA — Light up your images.</b>
</p>
