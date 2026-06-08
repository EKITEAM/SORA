package com.ekidevs.sora;

import android.content.Context;
import android.widget.ImageView;

public class ImageLoader {

    private final ImageLoaderEngine engine;

    public ImageLoader(Context context) {
        this.engine = new ImageLoaderEngine(context.getApplicationContext());
    }

    public static RequestBuilder with(Context context) {
        return new RequestBuilder(ImageLoaderProvider.get(context));
    }

    public static RequestBuilder with(ImageLoader loader) {
        return new RequestBuilder(loader);
    }

    public ImageLoaderEngine getEngine() { return engine; }

    public void pause() { engine.pause(); }
    public void resume() { engine.resume(); }
    public void clearMemoryCache() { engine.clearMemoryCache(); }
    public void clearDiskCache() { engine.clearDiskCache(); }
    public void cancelRequests(Object tag) { engine.cancelRequests(tag); }
    public void cancel(ImageView target) { engine.cancel(target); }
    public void cancelAll() { engine.cancelAll(); }
    public void trimMemory(int level) { engine.trimMemory(level); }
    public void shutdown() { engine.shutdown(); }

    public static final class RequestBuilder {
        private final ImageLoader loader;
        private String url;
        private int placeholderResId;
        private int errorResId;
        private int targetWidth;
        private int targetHeight;
        private boolean allowSvg = true;
        private boolean allowAnimated = false;
        private boolean memoryCache = true;
        private boolean diskCache = true;
        private Object tag;
        private String verificationKey;

        private RequestBuilder(ImageLoader loader) { this.loader = loader; }

        public RequestBuilder load(String url) { this.url = url; return this; }
        public RequestBuilder placeholder(int resId) { this.placeholderResId = resId; return this; }
        public RequestBuilder error(int resId) { this.errorResId = resId; return this; }
        public RequestBuilder resize(int w, int h) { this.targetWidth = w; this.targetHeight = h; return this; }
        public RequestBuilder allowSvg(boolean allow) { this.allowSvg = allow; return this; }
        public RequestBuilder allowAnimated(boolean allow) { this.allowAnimated = allow; return this; }
        public RequestBuilder memoryCache(boolean enable) { this.memoryCache = enable; return this; }
        public RequestBuilder diskCache(boolean enable) { this.diskCache = enable; return this; }
        public RequestBuilder tag(Object tag) { this.tag = tag; return this; }
        public RequestBuilder verificationKey(String key) { this.verificationKey = key; return this; }

        public void into(ImageView imageView) {
            ImageRequest request = buildRequest();
            loader.getEngine().enqueue(request, ImageTarget.into(imageView));
        }
        public void observe(ImageLoadObserver observer) {
            ImageRequest request = buildRequest();
            loader.getEngine().enqueue(request, ImageTarget.observe(observer));
        }
        public void into(ImageView imageView, ImageLoadObserver observer) {
            ImageRequest request = buildRequest();
            loader.getEngine().enqueue(request, ImageTarget.both(imageView, observer));
        }

        private ImageRequest buildRequest() {
            return new ImageRequest.Builder(url)
                    .placeholder(placeholderResId)
                    .error(errorResId)
                    .resize(targetWidth, targetHeight)
                    .allowSvg(allowSvg)
                    .allowAnimated(allowAnimated)
                    .memoryCache(memoryCache)
                    .diskCache(diskCache)
                    .tag(tag)
                    .verificationKey(verificationKey)
                    .build();
        }
    }

    static class ImageLoaderProvider {
        private static volatile ImageLoader instance;
        static ImageLoader get(Context context) {
            if (instance == null) {
                synchronized (ImageLoaderProvider.class) {
                    if (instance == null) {
                        instance = new ImageLoader(context.getApplicationContext());
                    }
                }
            }
            return instance;
        }
    }
}