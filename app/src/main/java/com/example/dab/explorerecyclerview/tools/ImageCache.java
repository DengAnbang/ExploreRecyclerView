package com.example.dab.explorerecyclerview.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import master.flame.danmaku.danmaku.util.IOUtils;

/**
 * Created by dab on 2017/4/25.
 */

public class ImageCache {
    public static final boolean DEBUG = false;
    private static final String TAG = "ImageCache";
    private static ImageCache instance;
    private static String mRootCacheDir;
    private LruCache<String, Bitmap> mCache;

    public static ImageCache get() {
        if (instance == null) {
            synchronized (ImageCache.class) {
                if (instance == null) {
                    instance = new ImageCache();
                }
            }
        }
        return instance;
    }


    private ImageCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
    }


    public static String init(Context context) {
        String rootCacheDir;
        if (context.getExternalCacheDir() != null) {
            rootCacheDir = context.getExternalCacheDir().getPath() + "/cache/";
        } else {
            rootCacheDir = Environment.getExternalStorageDirectory() + "/niuniu/cache/";
        }
        mRootCacheDir = rootCacheDir;
        return rootCacheDir;
    }

    public static String init(String cacheDir) {
        mRootCacheDir = cacheDir;
        return cacheDir;
    }


    @Nullable
    private void findBitmap(@NonNull String url, Bitmap.Config config, @Nullable final onBitmapCallback onBitmapCallback) {
        String md5 = getMD5(url);
        Bitmap bitmap = getBitmapFromMemoryCache(md5);
        if (bitmap != null) {
            if (DEBUG) Log.e(TAG, "getBitmap: 内存" + Thread.currentThread().getName());
            runUiThread(() -> callback(onBitmapCallback,bitmap));

            return;
        }
        if (!isUiThread()) {
            findBitmapOnIO(url, config, onBitmapCallback, md5);
        } else {
            new Thread(() -> {
                findBitmapOnIO(url, config, onBitmapCallback, md5);
            }).start();
        }

    }

    private static void callback(@Nullable final onBitmapCallback onBitmapCallback, @Nullable Bitmap bitmap) {
        if (onBitmapCallback != null) {
            onBitmapCallback.onCallback(bitmap);
        }
    }

    @WorkerThread
    private void findBitmapOnIO(@NonNull String url, Bitmap.Config config, @Nullable onBitmapCallback onBitmapCallback, String md5) {
        Bitmap bitmap;
        bitmap = getBitmapFromDisk(md5, config);
        if (bitmap != null) {
            if (DEBUG) Log.e(TAG, "getBitmap: 磁盘" + Thread.currentThread().getName());
            Bitmap finalBitmap = bitmap;
            runUiThread(() -> callback(onBitmapCallback,finalBitmap));
            return;
        }
        if (DEBUG) Log.e(TAG, "没读取到图片");
        bitmap = netPicToBmp(url, config);
        if (bitmap != null) {
            putBitmap(url, bitmap);
        }
        Bitmap finalBitmap1 = bitmap;
        runUiThread(() -> callback(onBitmapCallback,finalBitmap1));
        return;
    }

    @Nullable
    public void getBitmap(@NonNull String url, Bitmap.Config config, @Nullable final onBitmapCallback onBitmapCallback) {
        if (TextUtils.isEmpty(url)) {
            throw new NullPointerException("url");
        }
        if (config == null) {
            throw new NullPointerException("config");
        }
        findBitmap(url, config, onBitmapCallback);
    }


    public void putBitmap(@NonNull String url, @NonNull final Bitmap bitmap) {
        final String md5 = getMD5(url);
        addBitmapToMemoryCache(md5, bitmap);
        if (isUiThread()) {
            new Thread(() -> {
                addBitmapToDiskCache(md5, bitmap);
            }).start();
        } else {
            addBitmapToDiskCache(md5, bitmap);
        }
    }


    /**
     * 添加内存
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(@NonNull String key, @NonNull Bitmap bitmap) {
        mCache.put(key, bitmap);
    }

    /**
     * 读取内存
     *
     * @param key
     * @return
     */
    @Nullable
    private Bitmap getBitmapFromMemoryCache(@NonNull String key) {
        return mCache.get(key);
    }


    private void addBitmapToDiskCache(@NonNull String url, @NonNull Bitmap bitmap) {

        if (TextUtils.isEmpty(mRootCacheDir)) {
            if (DEBUG) Log.e(TAG, "保存磁盘路径为空:需要調用init()方法設置路徑");
            return;
        }
        String path = mRootCacheDir;
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            boolean mkdir = dirFile.mkdir();
        }
        File myCaptureFile = new File(dirFile, url);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            if (DEBUG) Log.e(TAG, "保存磁盘成功: " + Thread.currentThread().getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (bos != null)
                bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (bos != null)
                bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Nullable
    private Bitmap getBitmapFromDisk(String pathUrl, Bitmap.Config config) {
        if (TextUtils.isEmpty(mRootCacheDir)) {
            if (DEBUG) Log.e(TAG, "读取磁盘路径为空 ");
            return null;
        }
        String path = mRootCacheDir;
        try {
            File file = new File(path);
            File myCaptureFile = new File(file, pathUrl);
            FileInputStream fis = new FileInputStream(myCaptureFile);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = config;
            Bitmap bitmap = BitmapFactory.decodeStream(fis, null, opts);
            addBitmapToMemoryCache(pathUrl, bitmap);
            return bitmap;

        } catch (Exception e) {
//            e.printStackTrace();
        }
        return null;
    }

    private static String getMD5(String val) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(val.getBytes());
            byte[] m = md5.digest();//加密
            StringBuilder sb = new StringBuilder();
            for (byte aM : m) {
                sb.append(aM);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return val;
    }


    /**
     * 获取网络图片
     *
     * @param src
     * @return
     */
    private static Bitmap netPicToBmp(String src, Bitmap.Config config) {
        InputStream inputStream = null;
        try {
            // 从网络获取图片并且保存到一个bitmap里
            URLConnection urlConnection = new URL(src).openConnection();
            inputStream = urlConnection.getInputStream();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = config;
            return BitmapFactory.decodeStream(inputStream, null, opts);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return null;
    }

    private static boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private static void runUiThread(Runnable runnable) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }

    public interface onBitmapCallback {
        void onCallback(@Nullable Bitmap bitmap);
    }

}
