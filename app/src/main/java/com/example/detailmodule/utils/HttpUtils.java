package com.example.detailmodule.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.util.ArrayMap;

import androidx.collection.LruCache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class HttpUtils {
    private static final String TAG = HttpUtils.class.getSimpleName();
    public static final int BITMAP_LOAD_SUCCESS = 1;
    public static final int BITMAP_LOAD_FAILED = 2;
    private static LruCache<String, Bitmap> mBitmapCache = null;
    private static Map<String, File> mUrlLocalFileMap = new ArrayMap<>();
    private static final int WAIT_FOR_DOWNLOAD_POLL_TIME = 1 * 1000;  // 1 second
    private static final int MAX_WAIT_FOR_DOWNLOAD_TIME = 6 * 60 * 1000; // 6 minute
    private boolean mDownLoadSuccess;

    public static interface LoadSuccess {
        void onLoadSuccess(String result);
    }

    public static String sendPostMsg(String path, String encode) {
        String result = "";
        return result;
    }

    public static void clearCacheBitmap(String url) {
        if (null != url) {
            mBitmapCache.remove(url);
        }
    }
    public static class DetailBitmapLoadTask extends AsyncTask<String, Void, Bitmap> {
        private final Handler mHandler;
        private List<Bitmap> mList;
        private HashMap<String ,Bitmap> mBitmapMap = null;

        public DetailBitmapLoadTask(Handler handler, List<Bitmap> list, HashMap<String, Bitmap> bitmapHashMap) {
            mHandler = handler;
            mList = list;
            mBitmapMap = bitmapHashMap;
            if (null == mBitmapCache) {
                mBitmapCache = new LruCache<String, Bitmap>(1024*1024*6);
            }
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = mBitmapCache.get(urls[0]);
            if ((null != bitmap) && (mBitmapMap.get(urls[0]) == null)) {
                mList.add(bitmap);
                mBitmapMap.put(urls[0], bitmap);
                return bitmap;
            }
            InputStream  inputStream = null;
            ByteArrayOutputStream outputStream = null;
            HttpURLConnection httpURLConnection = null;
            ByteArrayInputStream byteArrayInputStream = null;
            try {
                URL url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                int fileLength = httpURLConnection.getContentLength();
                int statusCode = httpURLConnection.getResponseCode();
                if (statusCode == 200) {
                    inputStream = httpURLConnection.getInputStream();
                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    outputStream = new ByteArrayOutputStream();
                    while ((count = inputStream.read(data)) != -1) {
                        total += count;
                        outputStream.write(data, 0, count);
                    }
                    byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
                    bitmap = BitmapFactory.decodeStream(byteArrayInputStream);
                    mList.add(bitmap);
                    mBitmapMap.put(urls[0], bitmap);
                    return bitmap;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null != byteArrayInputStream) {
                    try {
                        byteArrayInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null != outputStream) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (null != httpURLConnection) {
                    httpURLConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mHandler.sendEmptyMessage(BITMAP_LOAD_SUCCESS);
        }
    }

    public static void downLoadFileFromUri(Context context, String uri, String folder, LoadSuccess callBack) {
        boolean downLoadSuccess = false;
        String existFile = getFileFromUri(uri);
        if ((uri != null && !uri.equals("")) && (null == existFile)) {
            String fileName = uri.substring(uri.lastIndexOf("/") + 1);
            String localDir = context.getExternalFilesDir(folder).getPath();
            File localFile = new File(localDir, fileName);
            if (localFile.exists()) {
                mUrlLocalFileMap.put(uri, localFile);
                return;
            }
            Uri serverUri = Uri.parse(uri);
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(serverUri);
            Uri localUri = Uri.fromFile(localFile);
            request.setDestinationUri(localUri);
            long dlRequest = downloadManager.enqueue(request);
            downLoadSuccess = false;
            downLoadSuccess = doWaitForDownloadOrTimeout(downloadManager, new DownloadManager.Query().setFilterById(dlRequest),
                    WAIT_FOR_DOWNLOAD_POLL_TIME, MAX_WAIT_FOR_DOWNLOAD_TIME);
            if (downLoadSuccess) {
                mUrlLocalFileMap.put(uri, localFile);
                if (null != callBack) {
                    callBack.onLoadSuccess(localFile.getAbsolutePath());
                }
            } else {
                localFile.delete();
            }
        }
    }

    public static String getFileFromUri(String uri) {
        File localFile = mUrlLocalFileMap.get(uri);
        if ((null != localFile) && localFile.exists() && localFile.canRead()) {
            return localFile.getAbsolutePath();
        }
        return null;
    }

    private static boolean doWaitForDownloadOrTimeout(DownloadManager downloadManager,
                                       DownloadManager.Query query, long poll, long timeoutMillis) {
        int currentWaitTime = 0;
        while (true) {
            query.setFilterByStatus(DownloadManager.STATUS_PENDING |
                    DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_RUNNING);
            Cursor cursor = downloadManager.query(query);
            try {
                if (cursor.getCount() ==0) {
                    return true;
                }
                currentWaitTime = timeoutWait(currentWaitTime, poll, timeoutMillis,
                        "Timed out waiting for download to finish.");
            } catch (TimeoutException e){
                e.printStackTrace();
                return false;
            } finally {
                cursor.close();
            }
        }
    }

    private static int timeoutWait(int currentTotalWaitTime, long poll, long maxTimeoutMillis,
                            String timeOutMsg) throws TimeoutException{
        long now = SystemClock.elapsedRealtime();
        long end = now + poll;
        while (now < end) {
            try {
                Thread.sleep(end - now);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            now = SystemClock.elapsedRealtime();
        }
        currentTotalWaitTime += poll;
        if (currentTotalWaitTime > maxTimeoutMillis) {
            throw new TimeoutException(timeOutMsg);
        }
        return currentTotalWaitTime;
    }
}
