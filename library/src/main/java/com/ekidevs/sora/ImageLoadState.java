package com.ekidevs.sora;

import android.graphics.drawable.Drawable;

public final class ImageLoadState {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR, CANCELED }

    private final Status status;
    private final String url;
    private final Drawable drawable;
    private final LoadError error;

    private ImageLoadState(Status status, String url, Drawable drawable, LoadError error) {
        this.status = status;
        this.url = url;
        this.drawable = drawable;
        this.error = error;
    }

    public static ImageLoadState idle() {
        return new ImageLoadState(Status.IDLE, null, null, null);
    }

    public static ImageLoadState loading(String url) {
        return new ImageLoadState(Status.LOADING, url, null, null);
    }

    public static ImageLoadState success(String url, Drawable drawable, LoadError error) {
        return new ImageLoadState(Status.SUCCESS, url, drawable, error);
    }

    public static ImageLoadState error(String url, Drawable drawable, LoadError error) {
        return new ImageLoadState(Status.ERROR, url, drawable, error);
    }

    public static ImageLoadState canceled(String url) {
        return new ImageLoadState(Status.CANCELED, url, null, null);
    }

    public Status getStatus() { return status; }
    public String getUrl() { return url; }
    public Drawable getDrawable() { return drawable; }
    public LoadError getError() { return error; }
}