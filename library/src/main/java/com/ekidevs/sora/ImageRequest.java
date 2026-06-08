package com.ekidevs.sora;

import androidx.annotation.DrawableRes;

public final class ImageRequest {
    private final String url;
    private final int placeholderResId;
    private final int errorResId;
    private final int targetWidth;
    private final int targetHeight;
    private final boolean allowSvg;
    private final boolean allowAnimated;
    private final boolean memoryCacheEnabled;
    private final boolean diskCacheEnabled;
    private final Object tag;
    private final String verificationKey;

    private ImageRequest(Builder builder) {
        this.url = builder.url;
        this.placeholderResId = builder.placeholderResId;
        this.errorResId = builder.errorResId;
        this.targetWidth = builder.targetWidth;
        this.targetHeight = builder.targetHeight;
        this.allowSvg = builder.allowSvg;
        this.allowAnimated = builder.allowAnimated;
        this.memoryCacheEnabled = builder.memoryCacheEnabled;
        this.diskCacheEnabled = builder.diskCacheEnabled;
        this.tag = builder.tag;
        this.verificationKey = builder.verificationKey;
    }

    public String getUrl() { return url; }
    public int getPlaceholderResId() { return placeholderResId; }
    public int getErrorResId() { return errorResId; }
    public int getTargetWidth() { return targetWidth; }
    public int getTargetHeight() { return targetHeight; }
    public boolean isAllowSvg() { return allowSvg; }
    public boolean isAllowAnimated() { return allowAnimated; }
    public boolean isMemoryCacheEnabled() { return memoryCacheEnabled; }
    public boolean isDiskCacheEnabled() { return diskCacheEnabled; }
    public Object getTag() { return tag; }
    public String getVerificationKey() { return verificationKey; }

    public String getCacheKey() {
        return url + "_" + targetWidth + "x" + targetHeight + "_" + allowSvg + "_" + allowAnimated;
    }

    public static class Builder {
        private final String url;
        private int placeholderResId = 0;
        private int errorResId = 0;
        private int targetWidth = 0;
        private int targetHeight = 0;
        private boolean allowSvg = true;
        private boolean allowAnimated = false;
        private boolean memoryCacheEnabled = true;
        private boolean diskCacheEnabled = false;
        private Object tag;
        private String verificationKey;

        public Builder(String url) {
            this.url = url;
        }

        public Builder placeholder(@DrawableRes int resId) { this.placeholderResId = resId; return this; }
        public Builder error(@DrawableRes int resId) { this.errorResId = resId; return this; }
        public Builder resize(int w, int h) { this.targetWidth = w; this.targetHeight = h; return this; }
        public Builder allowSvg(boolean allow) { this.allowSvg = allow; return this; }
        public Builder allowAnimated(boolean allow) { this.allowAnimated = allow; return this; }
        public Builder memoryCache(boolean enable) { this.memoryCacheEnabled = enable; return this; }
        public Builder diskCache(boolean enable) { this.diskCacheEnabled = enable; return this; }
        public Builder tag(Object tag) { this.tag = tag; return this; }
        public Builder verificationKey(String key) { this.verificationKey = key; return this; }
        public ImageRequest build() { return new ImageRequest(this); }
    }
}