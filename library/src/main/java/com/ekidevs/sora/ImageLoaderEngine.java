package com.ekidevs.sora;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import com.ekidevs.sora.R;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

class ImageLoaderEngine {

    private static final int IMAGE_THREAD_COUNT = 4;
    private final Context appContext;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final MemoryCache memoryCache;
    private final DiskCache diskCache;
    private final NetworkFetcher networkFetcher;
    private final ConcurrentHashMap<Object, AtomicBoolean> tagCancellation = new ConcurrentHashMap<>();
    private volatile boolean isShutdown = false;
    private volatile boolean isPaused = false;

    ImageLoaderEngine(Context appContext) {
        this.appContext = appContext;
        this.executor = Executors.newFixedThreadPool(IMAGE_THREAD_COUNT);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.memoryCache = new MemoryCache();
        this.diskCache = new DiskCache(appContext, 20 * 1024 * 1024);
        this.networkFetcher = new NetworkFetcher();
    }

    void pause() { isPaused = true; }
    void resume() { isPaused = false; }
    void clearMemoryCache() { memoryCache.clear(); }
    void clearDiskCache() { diskCache.clear(); }
    void clearAllCaches() { memoryCache.clear(); diskCache.clear(); }
    void cancelRequests(Object tag) {
        if (tag == null) return;
        AtomicBoolean flag = tagCancellation.get(tag);
        if (flag != null) flag.set(true);
    }
    void cancel(ImageView target) {}
    void cancelAll() {
        for (AtomicBoolean flag : tagCancellation.values()) flag.set(true);
        tagCancellation.clear();
    }
    void trimMemory(int level) {
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            memoryCache.clear();
        }
    }
    void shutdown() {
        isShutdown = true;
        cancelAll();
        executor.shutdownNow();
        memoryCache.clear();
        diskCache.close();
    }

    void enqueue(ImageRequest request, ImageTarget target) {
        if (isShutdown || request == null || target == null) return;
        String cacheKey = request.getCacheKey();
        String url = request.getUrl();
        if (url == null || url.isEmpty()) {
            applyPlaceholder(request, target);
            return;
        }

        ImageView imageView = target.getImageView();
        if (imageView != null) {
            imageView.setTag(R.id.tag_image_url, cacheKey);
            String vKey = request.getVerificationKey();
            if (vKey != null) {
                imageView.setTag(R.id.tag_image_verification_key, vKey);
            }
        }

        if (request.isMemoryCacheEnabled()) {
            Drawable cached = memoryCache.get(cacheKey);
            if (cached != null) {
                applyDrawable(cached, request, target);
                return;
            }
        }

        if (imageView != null) {
            applyPlaceholder(request, target);
        }

        final Object requestTag = request.getTag();
        AtomicBoolean cancelFlag = null;
        if (requestTag != null) {
            cancelFlag = tagCancellation.computeIfAbsent(requestTag, k -> new AtomicBoolean(false));
        }
        final AtomicBoolean finalCancelFlag = cancelFlag;

        executor.execute(() -> {
            if (isPaused || isShutdown) return;
            if (finalCancelFlag != null && finalCancelFlag.get()) return;

            byte[] rawData = null;
            try {
                if (request.isDiskCacheEnabled()) {
                    rawData = diskCache.getBytes(cacheKey);
                }
                if (rawData == null) {
                    rawData = networkFetcher.downloadWithRetry(request.getUrl(), finalCancelFlag);
                    if (rawData != null && request.isDiskCacheEnabled()) {
                        diskCache.putBytes(cacheKey, rawData);
                    }
                }
                if (rawData == null) {
                    postError(request, target, new LoadError(LoadError.ErrorType.NETWORK, new Exception("No data")));
                    return;
                }
                Drawable drawable = ImageDecodeHelper.decode(rawData, request, appContext);
                if (drawable == null) {
                    postError(request, target, new LoadError(LoadError.ErrorType.DECODE, new Exception("Decode failed")));
                    return;
                }
                if (request.isMemoryCacheEnabled()) {
                    memoryCache.put(cacheKey, drawable);
                }
                mainHandler.post(() -> applyDrawable(drawable, request, target));
            } catch (Exception e) {
                postError(request, target, new LoadError(LoadError.ErrorType.UNKNOWN, e));
            }
        });
    }

    private void postError(ImageRequest request, ImageTarget target, LoadError error) {
        mainHandler.post(() -> {
            ImageView iv = target.getImageView();
            if (iv != null) {
                String vKey = request.getVerificationKey();
                if (vKey != null) {
                    Object currentKey = iv.getTag(R.id.tag_image_verification_key);
                    if (!vKey.equals(currentKey)) return;
                }
                if (request.getErrorResId() != 0) iv.setImageResource(request.getErrorResId());
                else iv.setImageResource(R.drawable.ic_placeholder);
            }
            ImageLoadObserver observer = target.getObserver();
            if (observer != null) observer.onStateChanged(ImageLoadState.error(request.getUrl(), null, error));
        });
    }

    private void applyPlaceholder(ImageRequest request, ImageTarget target) {
        ImageView iv = target.getImageView();
        if (iv != null && request.getPlaceholderResId() != 0) {
            mainHandler.post(() -> iv.setImageResource(request.getPlaceholderResId()));
        }
    }

    private void applyDrawable(Drawable drawable, ImageRequest request, ImageTarget target) {
        ImageView iv = target.getImageView();
        if (iv != null) {
            String vKey = request.getVerificationKey();
            if (vKey != null) {
                Object currentKey = iv.getTag(R.id.tag_image_verification_key);
                if (!vKey.equals(currentKey)) return;
            }
            iv.setImageDrawable(drawable);
        }
        ImageLoadObserver observer = target.getObserver();
        if (observer != null) observer.onStateChanged(ImageLoadState.success(request.getUrl(), drawable, null));
    }
}