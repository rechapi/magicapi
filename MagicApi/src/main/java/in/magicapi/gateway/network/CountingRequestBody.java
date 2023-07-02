package in.magicapi.gateway.network;


import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class CountingRequestBody extends RequestBody {

    private File mFile;
    private UploadCallbacks mListener;
    private String mediaType;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);

        void onError();

        void onFinish();

        void uploadStart();
    }

    public CountingRequestBody(final File file, final UploadCallbacks listener, String mediaTypes) {
        mFile = file;
        mListener = listener;
        mediaType = mediaTypes;
        mListener.uploadStart();
    }

    @Override
    public MediaType contentType() {
        // i want to upload only video
        return MediaType.parse(mediaType);
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {
                uploaded += read;
                sink.write(buffer, 0, read);
                handler.post(new ProgressUpdater(uploaded, fileLength));
            }
        } finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;

        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            try {

                int progress = (int) (100 * mUploaded / mTotal);

                if (progress == 100)
                    mListener.onFinish();

                else
                    mListener.onProgressUpdate(progress);
            } catch (ArithmeticException e) {
                mListener.onError();
                e.printStackTrace();
            }
        }
    }
}