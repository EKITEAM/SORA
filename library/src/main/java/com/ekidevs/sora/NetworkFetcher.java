package com.ekidevs.sora;

import com.ekidevs.sora.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

class NetworkFetcher {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    private static final int MAX_RETRIES = 2;

    private static final String USER_AGENT = "Sora/" + BuildConfig.SORA_VERSION;

    byte[] downloadWithRetry(String urlString, AtomicBoolean cancelled) throws LoadError {
        Exception lastCause = null;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            if (cancelled != null && cancelled.get())
                throw new LoadError(LoadError.ErrorType.CANCELED, null);
            try {
                return download(urlString, cancelled);
            } catch (IOException e) {
                lastCause = e;
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(200 * (attempt + 1)); } catch (InterruptedException ignored) {}
                }
            }
        }
        if (lastCause instanceof SocketTimeoutException) {
            throw new LoadError(LoadError.ErrorType.NETWORK, lastCause);
        }
        throw new LoadError(LoadError.ErrorType.NETWORK,
                lastCause != null ? lastCause : new IOException("Download failed"));
    }

    private byte[] download(String urlString, AtomicBoolean cancelled) throws IOException {
        HttpURLConnection conn = null;
        InputStream input = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.connect();

            if (cancelled != null && cancelled.get())
                throw new IOException("Cancelled");

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode);
            }

            input = conn.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = input.read(chunk)) != -1) {
                if (cancelled != null && cancelled.get())
                    throw new IOException("Cancelled");
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        } finally {
            if (input != null) try { input.close(); } catch (IOException ignored) {}
            if (conn != null) conn.disconnect();
        }
    }
}