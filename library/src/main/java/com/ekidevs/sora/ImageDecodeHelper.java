package com.ekidevs.sora;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

class ImageDecodeHelper {

    static Drawable decode(byte[] data, ImageRequest request, Context context) {
        if (data == null || data.length == 0) return null;
        String lowerUrl = request.getUrl().toLowerCase(Locale.US);
        if (request.isAllowSvg() && isSvg(data, lowerUrl)) {
            return decodeSvg(data);
        }
        int w = request.getTargetWidth() > 0 ? request.getTargetWidth() : 128;
        int h = request.getTargetHeight() > 0 ? request.getTargetHeight() : 128;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                android.graphics.ImageDecoder.Source source = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ? android.graphics.ImageDecoder.createSource(data)
                        : android.graphics.ImageDecoder.createSource(ByteBuffer.wrap(data));
                android.graphics.ImageDecoder.OnHeaderDecodedListener listener = (decoder, info, src) -> {
                    decoder.setTargetSampleSize(calculateSampleSize(info.getSize().getWidth(), info.getSize().getHeight(), w, h));
                    if (!request.isAllowAnimated()) decoder.setMemorySizePolicy(android.graphics.ImageDecoder.MEMORY_POLICY_LOW_RAM);
                };
                Drawable d = android.graphics.ImageDecoder.decodeDrawable(source, listener);
                if (d instanceof AnimatedImageDrawable) {
                    if (request.isAllowAnimated()) ((AnimatedImageDrawable) d).start();
                    else d = ((AnimatedImageDrawable) d).getConstantState().newDrawable();
                }
                return d;
            } catch (IOException e) {
                return decodeBitmap(data, w, h, context);
            }
        } else {
            return decodeBitmap(data, w, h, context);
        }
    }

    private static Drawable decodeBitmap(byte[] data, int w, int h, Context context) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, w, h);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) opts.inPreferredConfig = Bitmap.Config.HARDWARE;
        else opts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        return bitmap != null ? new BitmapDrawable(context.getResources(), bitmap) : null;
    }

    private static Drawable decodeSvg(byte[] data) {
        try {
            SVG svg = SVG.getFromString(new String(data, StandardCharsets.UTF_8));
            return new PictureDrawable(svg.renderToPicture());
        } catch (SVGParseException e) {
            return null;
        }
    }

    private static boolean isSvg(byte[] data, String urlHint) {
        if (urlHint.endsWith(".svg")) return true;
        String header = new String(data, 0, Math.min(data.length, 200), StandardCharsets.UTF_8).trim();
        return header.contains("<svg") || header.contains("<?xml");
    }

    private static int calculateSampleSize(int width, int height, int reqW, int reqH) {
        int inSampleSize = 1;
        if (height > reqH || width > reqW) {
            int halfH = height / 2;
            int halfW = width / 2;
            while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
                inSampleSize *= 2;
            }
        }
        return Math.max(inSampleSize, 1);
    }
}