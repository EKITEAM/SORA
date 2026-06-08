# SORA

<p align="center">
  <img src="assets/logo.png" width="140" alt="SORA Logo" />
</p>

<h1 align="center">SORA</h1>

<p align="center">
  <b>Image Loader for Android</b><br />
  Lightweight • Efficient • Easy to use
</p>

<p align="center">
  <a href="https://developer.android.com">
    <img alt="Android" src="https://img.shields.io/badge/Android-21%2B-brightgreen" />
  </a>
  <a href="https://jitpack.io/#EKITEAM/SORA">
    <img alt="JitPack" src="https://img.shields.io/jitpack/v/github/EKITEAM/SORA?label=JitPack" />
  </a>
  <a href="LICENSE">
    <img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" />
  </a>
</p>

**SORA** is an Android image loading library for API 21+ designed to load images from the network with memory and disk caching, SVG rendering, animated image support, smart resizing, request cancellation, and graceful fallbacks.

---

## Features

- Multi-level caching: memory (LRU) + disk (LRU with size limit)
- Network support with automatic retries and timeouts
- SVG support powered by `androidsvg` (optional)
- Animated image support for GIF and animated WebP (opt-in)
- Smart resizing with downsampling to requested dimensions
- Background loading with a thread-pooled executor
- Request cancellation by tag or by target
- Verification keys to help prevent image mismatches in recycled views
- Pluggable observers via `ImageLoadObserver`
- Memory trimming support via `onTrimMemory()`
- Placeholder and error drawables for graceful fallbacks

---

## Requirements

- Minimum Android SDK: 21 (Android 5.0)
- Target SDK: 34 (Android 14)
- Java 8+ (Java 11 recommended)

---

## Installation

SORA is available through **JitPack**.

### 1) Add the JitPack repository

In your `settings.gradle` or `settings.gradle.kts`:

```gradle
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

If your project still uses the older repository setup, add JitPack in the root `build.gradle` instead.

### 2) Add the dependency

```gradle
dependencies {
    implementation 'com.github.EKITEAM:SORA:v1.0.5'
}
```

### 3) Add internet permission

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
                // Image loaded
            } else if (state.getStatus() == ImageLoadState.Status.ERROR) {
                LoadError error = state.getError();
                // Handle error
            }
        }
    });
```

---

## Request Builder Options

| Method | Description |
|---|---|
| `load(String url)` | Set the image URL (HTTP/HTTPS) |
| `placeholder(int resId)` | Drawable shown while loading |
| `error(int resId)` | Drawable shown on failure |
| `resize(int width, int height)` | Target dimensions (downsampling) |
| `allowSvg(boolean)` | Enable/disable SVG decoding (default `true`) |
| `allowAnimated(boolean)` | Enable/disable animated drawables (default `false`) |
| `memoryCache(boolean)` | Store in memory cache (default `true`) |
| `diskCache(boolean)` | Store in disk cache (default `true`) |
| `tag(Object tag)` | Group requests for cancellation |
| `verificationKey(String key)` | Prevent view recycling mismatches |

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

## Cancelling Requests

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
| `shutdown()` | Shutdown executor and clear caches |
| `trimMemory(int level)` | Forward Android trim memory events |

### `ImageLoadState`

| Field | Description |
|---|---|
| `Status.IDLE` | No active request |
| `Status.LOADING` | Request is in progress |
| `Status.SUCCESS` | Image loaded successfully |
| `Status.ERROR` | Loading failed |
| `Status.CANCELED` | Request was canceled |

Additional fields and methods:

- `getUrl()` — Original URL
- `getDrawable()` — Resulting drawable on success
- `getError()` — `LoadError` containing type and cause

### `LoadError.ErrorType`

- `NETWORK` — Connection / timeout issues
- `DECODE` — Failed to decode image data
- `SVG` — SVG parsing failure
- `HTTP` — Non-200 response code
- `CANCELED` — Explicit cancellation
- `UNKNOWN` — Any other exception

---

## ProGuard / R8 Rules

If you enable obfuscation, add these rules to `proguard-rules.pro`:

```proguard
# SORA library
-keep class com.ekidevs.sora.** { *; }
-keepclassmembers class com.ekidevs.sora.** { *; }

# SVG library
-keep class com.caverock.androidsvg.** { *; }
```

---

## Customising the User-Agent

By default, SORA sends:

```text
Sora/1.0.5
```

The version matches the library `versionName`. You can override this by modifying `NetworkFetcher.java` before building your own version.

---

## Release

Current release:

- Version: `v1.0.5`
- JitPack dependency: `com.github.EKITEAM:SORA:v1.0.5`

---

## Contributing

Contributions are welcome.

Please open an issue or submit a pull request on GitHub:
https://github.com/EKITEAM/SORA

---

## Support

For questions, bug reports, or feature requests, please open an issue:
https://github.com/EKITEAM/SORA/issues

---

## License

```text
Copyright 2025 Ekidevs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

<p align="center">
  <b>SORA — Light up your images.</b>
</p>
