# SORA – Image Loader for Android

[![Android](https://img.shields.io/badge/Android-21%2B-brightgreen)](https://developer.android.com)
[![JitPack](https://img.shields.io/jitpack/v/github/EKITEAM/SORA?label=JitPack)](https://jitpack.io/#EKITEAM/SORA)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**SORA** is a powerful, efficient, and easy‑to‑use image loading library for Android (API 21+).  
It handles image fetching from the network, memory & disk caching, SVG rendering, animated images, and automatic retries – all while being lightweight and fully customisable.

---

## ✨ Features

- 🚀 **Multi‑level caching** – Memory (LRU) + Disk (LRU with size limit)
- 🌐 **Network support** – HTTP/HTTPS with automatic retries and timeouts
- 🖼️ **SVG support** – Powered by `androidsvg` (optional)
- 🎞️ **Animated images** – Support for GIF & WebP animations (opt‑in)
- 📐 **Smart resizing** – Downsample images to requested dimensions to save memory
- ⚡ **Background loading** – Thread‑pooled executor (4 threads by default)
- 🔁 **Request cancellation** – Per‑tag or per‑target cancellation
- ✅ **Verification keys** – Prevent image mismatches in recycled views
- 🧩 **Pluggable observers** – Receive load states (loading, success, error) via `ImageLoadObserver`
- 🧹 **Memory trimming** – Responds to `onTrimMemory()` events
- 🛡️ **Graceful fallbacks** – Placeholder & error drawables

---

## 📱 Requirements

- **Minimum Android SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Java 8+** (Java 11 recommended)

---

## 📦 Installation

SORA is available via **JitPack**. Add the repository and dependency to your `build.gradle`:

### Step 1 – Add JitPack repository

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Or, for newer Gradle versions (catalog):

```gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2 – Add the dependency

```gradle
dependencies {
    implementation 'com.github.EKITEAM:SORA:v1.0.0'
}
```

⚠️ Replace your-username with your actual GitHub username.

### Step 3 – Required permission

Add Internet permission to your AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🚀 Basic Usage

### 1. Create an instance

```java
ImageLoader imageLoader = new ImageLoader(context);
```

Or use the global provider (singleton per application context):

```java
ImageLoader imageLoader = ImageLoader.with(context);
```

### 2. Load an image into an ImageView

```java
ImageLoader.with(context)
    .load("https://example.com/image.jpg")
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error)
    .resize(300, 300)
    .into(imageView);
```

### 3. Observe loading state

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

## ⚙️ Advanced Configuration

### Request Builder Options

| Method | Description |
|---|---|
| `load(String url)` | Set the image URL (HTTP/HTTPS) |
| `placeholder(int resId)` | Drawable shown while loading |
| `error(int resId)` | Drawable shown on failure |
| `resize(int width, int height)` | Target dimensions (downsampling) |
| `allowSvg(boolean)` | Enable/disable SVG decoding (default true) |
| `allowAnimated(boolean)` | Enable/disable animated drawables (default false) |
| `memoryCache(boolean)` | Store in memory cache (default true) |
| `diskCache(boolean)` | Store in disk cache (default true) |
| `tag(Object tag)` | Group requests for cancellation |
| `verificationKey(String key)` | Prevent view recycling mismatches |

### Example with all options

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

### Cancelling requests

```java
// Cancel by tag
imageLoader.cancelRequests("profile_screen");

// Cancel a specific target
imageLoader.cancel(imageView);

// Cancel all
imageLoader.cancelAll();
```

### Cache management

```java
// Clear memory cache (e.g., on low memory)
imageLoader.clearMemoryCache();

// Clear disk cache
imageLoader.clearDiskCache();

// Trim memory (called from Activity/Application)
imageLoader.trimMemory(level);
```

---

## 📚 API Reference

### ImageLoader

| Method | Description |
|---|---|
| `pause()` | Pause all new requests |
| `resume()` | Resume processing |
| `shutdown()` | Shutdown executor and clear caches |
| `trimMemory(int level)` | Forward Android trim memory events |

### ImageLoadState

| Field | Type |
|---|---|
| `Status` | IDLE, LOADING, SUCCESS, ERROR, CANCELED |
| `getUrl()` | Original URL |
| `getDrawable()` | Resulting drawable (if success) |
| `getError()` | LoadError with type and cause |

### LoadError.ErrorType

- `NETWORK` – Connection / timeout issues
- `DECODE` – Failed to decode image data
- `SVG` – SVG parsing failure
- `HTTP` – Non‑200 response code
- `CANCELED` – Explicit cancellation
- `UNKNOWN` – Any other exception

---

## 🧪 ProGuard / R8 Rules

If you enable obfuscation, add these rules to your `proguard-rules.pro`:

```proguard
# SORA library
-keep class com.ekidevs.sora.** { *; }
-keepclassmembers class com.ekidevs.sora.** { *; }

# SVG library
-keep class com.caverock.androidsvg.** { *; }
```

---

## 🔧 Customising the User‑Agent

By default, SORA sends User-Agent: Sora/1.0.0 (where the version matches the library’s versionName).  
You can override this by modifying NetworkFetcher.java before building your own version – the library is designed to be transparent.

---

## 📄 License

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

## 🤝 Contributing

Contributions are welcome!  
Please open an issue or submit a pull request on GitHub.

---

## 📧 Contact

For questions or support, please open an issue on the GitHub repository.

SORA – Light up your images.
