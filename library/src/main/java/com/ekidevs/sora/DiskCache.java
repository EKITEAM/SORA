package com.ekidevs.sora;

import android.content.Context;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

class DiskCache {

    private final File cacheDir;
    private final long maxSizeBytes;
    private final LinkedHashMap<String, DiskEntry> lru = new LinkedHashMap<>(16, 0.75f, true);
    private long totalSize = 0;
    private boolean closed = false;

    DiskCache(Context context, long maxSizeBytes) {
        this.cacheDir = new File(context.getCacheDir(), "image_disk_cache");
        this.maxSizeBytes = maxSizeBytes;
        if (!cacheDir.exists()) cacheDir.mkdirs();
        loadFromDisk();
    }

    private void loadFromDisk() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".cache")) {
                    String key = decode(f.getName().replace(".cache", ""));
                    long size = f.length();
                    long time = f.lastModified();
                    lru.put(key, new DiskEntry(size, time));
                    totalSize += size;
                }
            }
            evictIfNeeded();
        }
    }

    private String encode(String key) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(key.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String encoded) {
        try {
            return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) { return encoded; }
    }

    synchronized byte[] getBytes(String key) {
        if (closed) return null;
        File file = new File(cacheDir, encode(key) + ".cache");
        if (!file.exists()) {
            DiskEntry removed = lru.remove(key);
            if (removed != null) totalSize -= removed.size;
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) bos.write(buffer, 0, len);
            DiskEntry entry = lru.get(key);
            if (entry != null) {
                entry.lastAccess = System.currentTimeMillis();
                file.setLastModified(entry.lastAccess);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            DiskEntry removed = lru.remove(key);
            if (removed != null) totalSize -= removed.size;
            file.delete();
        }
        return null;
    }

    synchronized void putBytes(String key, byte[] data) {
        if (closed || data == null) return;
        File file = new File(cacheDir, encode(key) + ".cache");
        File tmp = new File(cacheDir, encode(key) + ".tmp");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tmp);
            fos.write(data);
            fos.flush();
            fos.close();
            if (file.exists()) file.delete();
            if (!tmp.renameTo(file)) {
                copyFile(tmp, file);
                tmp.delete();
            }
            long size = file.length();
            long now = System.currentTimeMillis();
            DiskEntry old = lru.put(key, new DiskEntry(size, now));
            if (old != null) totalSize -= old.size;
            totalSize += size;
            file.setLastModified(now);
            evictIfNeeded();
        } catch (IOException ignored) {
            tmp.delete();
        } finally {
            if (fos != null) try { fos.close(); } catch (IOException ignored) {}
        }
    }

    synchronized void clear() {
        lru.clear();
        totalSize = 0;
        File[] files = cacheDir.listFiles();
        if (files != null) for (File f : files) f.delete();
    }

    synchronized void close() {
        closed = true;
        lru.clear();
    }

    private void evictIfNeeded() {
        while (totalSize > maxSizeBytes && !lru.isEmpty()) {
            Map.Entry<String, DiskEntry> eldest = lru.entrySet().iterator().next();
            String key = eldest.getKey();
            DiskEntry entry = eldest.getValue();
            File f = new File(cacheDir, encode(key) + ".cache");
            if (f.exists()) f.delete();
            lru.remove(key);
            totalSize -= entry.size;
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) fos.write(buffer, 0, length);
        }
    }

    private static class DiskEntry {
        long size;
        long lastAccess;
        DiskEntry(long size, long lastAccess) { this.size = size; this.lastAccess = lastAccess; }
    }
}