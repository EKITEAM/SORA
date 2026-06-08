package com.ekidevs.sora;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

class MemoryCache {
    private final LruCache<String, Drawable> cache;

    MemoryCache() {
        int maxMemoryKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSizeKb = maxMemoryKb / 8;
        cache = new LruCache<String, Drawable>(cacheSizeKb) {
            @Override
            protected int sizeOf(String key, Drawable value) {
                if (value instanceof BitmapDrawable) {
                    BitmapDrawable bd = (BitmapDrawable) value;
                    if (bd.getBitmap() != null) return bd.getBitmap().getByteCount() / 1024;
                }
                return 50;
            }
        };
    }

    Drawable get(String key) { return cache.get(key); }
    void put(String key, Drawable drawable) { cache.put(key, drawable); }
    void clear() { cache.evictAll(); }
}