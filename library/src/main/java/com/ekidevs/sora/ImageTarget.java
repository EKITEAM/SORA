package com.ekidevs.sora;

import android.widget.ImageView;
import androidx.annotation.Nullable;

public final class ImageTarget {
    private final ImageView imageView;
    private final ImageLoadObserver observer;

    private ImageTarget(ImageView imageView, ImageLoadObserver observer) {
        this.imageView = imageView;
        this.observer = observer;
    }

    @Nullable
    public ImageView getImageView() { return imageView; }

    @Nullable
    public ImageLoadObserver getObserver() { return observer; }

    public static ImageTarget into(ImageView imageView) {
        return new ImageTarget(imageView, null);
    }

    public static ImageTarget observe(ImageLoadObserver observer) {
        return new ImageTarget(null, observer);
    }

    public static ImageTarget both(ImageView imageView, ImageLoadObserver observer) {
        return new ImageTarget(imageView, observer);
    }
}