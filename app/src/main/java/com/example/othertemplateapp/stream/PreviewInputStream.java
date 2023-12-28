package com.example.othertemplateapp.stream;

import com.sunyard.commonlib.utils.Util;

import java.io.IOException;
import java.io.InputStream;

public class PreviewInputStream extends InputStream {
    private final InputStream mProxy;
    private final ByteInputStream mCache;

    public PreviewInputStream(InputStream proxy) {
        mProxy = proxy;
        mCache = new ByteInputStream();
    }

    public int previewAvailable() throws IOException {
        return mProxy.available();
    }

    @Override
    public int available() throws IOException {
        return mCache.available() + mProxy.available();
    }

    public int head(byte[] b) throws IOException {
        int available = mCache.available();
        if (available != 0) {
            available = mCache.read(b);
        }
        int remainLen = b.length - available;
        if (remainLen != 0) {
            return available + preview(b, available, remainLen);
        }
        preview(new byte[1]);
        return available;
    }

    public int preview(byte[] b) throws IOException {
        return preview(b, 0, b.length);
    }

    public int preview(byte[] b, int off, int len) throws IOException {
        int readLen = mProxy.read(b, off, len);
        if (readLen != 0) {
            mCache.addData(Util.arrayCopy(b, off, readLen));
        }
        return readLen;
    }

    @Override
    public int read() throws IOException {
        if (mCache.available() != 0) {
            return mCache.read();
        }
        return mProxy.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int offset = 0;
        int available = mCache.available();
        if (mCache.available() != 0) {
            offset = mCache.read(b, off, Math.min(available, len));
        }
        return offset + mProxy.read(b, off + offset, len - offset);
    }
}
