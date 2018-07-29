package com.rx.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class ProxyRequestBody extends RequestBody {

    private RequestBody mRequestBody;
    private UploadListener mUploadListener;

    public ProxyRequestBody(RequestBody mRequestBody, UploadListener mUploadListener) {

        this.mRequestBody = mRequestBody;
        this.mUploadListener = mUploadListener;

    }

    @Override
    public long contentLength() throws IOException {

        return mRequestBody.contentLength();

    }

    @Nullable
    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {

        CountBufferedSink countBufferedSink = new CountBufferedSink(sink);
        BufferedSink sink1 = Okio.buffer(countBufferedSink);
        mRequestBody.writeTo(sink1);
        sink1.flush();

    }
    public interface UploadListener {
        void onUpload(double progress);
    }

    class CountBufferedSink extends ForwardingSink {

        private double writeCount = 0;
        public CountBufferedSink(Sink delegate) {
            super(delegate);
        }


        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            writeCount += byteCount;
            mUploadListener.onUpload(writeCount * 100 / contentLength());
        }

    }

}
