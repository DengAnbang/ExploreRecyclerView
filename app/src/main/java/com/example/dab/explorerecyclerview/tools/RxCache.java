package com.example.dab.explorerecyclerview.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dab on 2017/4/25.
 */

public class RxCache {
    public static final boolean DEBUG = true;
    private static final String TAG = "RxCache";
    private static RxCache instance;
    private static String mRootCacheDir;
    private LruCache<String, Bitmap> mCache;

    //    Bitmap bitmap = RxCache.get().getBitmap(UserSettings.getAvatarUrlPrefix() + result.getAvatar());
//    if (bitmap == null) {
//        Glide.with(this).load(UserSettings.getAvatarUrlPrefix() + result.getAvatar())
//                .asBitmap()
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                        RxCache.get().putBitmap(UserSettings.getAvatarUrlPrefix() + result.getAvatar(), resource);
//                        userImage.setImageBitmap(resource);
//                    }
//                });
//    } else {
//        userImage.setImageBitmap(bitmap);
//    }

//    RxCache.get().getBitmap(avator, new RxCache.onBitmapCallback() {
//        @Override
//        public void onCallback(@Nullable Bitmap bitmap) {
//            if (bitmap == null) {
//                Glide.with(MainActivity.this).load(avator)
//                        .asBitmap()
//                        .into(new SimpleTarget<Bitmap>() {
//                            @Override
//                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                                RxCache.get().putBitmap(avator, resource);
//                                mImageView.setImageBitmap(resource);
//                            }
//                        });
//            } else {
//                mImageView.setImageBitmap(bitmap);
//            }
//        }
//    });





    private RxCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
    }


    public static String init(Activity context) {
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

    public static RxCache get() {
        if (instance == null) {
            synchronized (RxCache.class) {
                if (instance == null) {
                    instance = new RxCache();
                }
            }
        }
        return instance;
    }


    @Nullable
    public Bitmap getBitmap(@NonNull String url) {
        if (DEBUG)
            Log.e(TAG, "开始读取图片: ");
        if (isUiThread()) {
            Log.e(TAG, "getBitmap: 读取图片在主线程中");
        }

        String md5 = getMD5(url);
        Bitmap bitmap = getBitmapFromMemoryCache(md5);
        if (bitmap != null) {
            if (DEBUG) Log.e(TAG, "getBitmap: 内存");
            return bitmap;
        }

        bitmap = getBitmapFromDisk(md5);
        if (bitmap != null) {
            if (DEBUG) Log.e(TAG, "getBitmap: 磁盘"+ Thread.currentThread().getName());
            return bitmap;
        }
        if (DEBUG) Log.e(TAG, "没读取到图片");
        return null;
    }

    @Nullable
    public void getBitmap(@NonNull String url, @NonNull final onBitmapCallback onBitmapCallback) {
        if (DEBUG)
            Log.e(TAG, "开始读取图片: ");
        final String md5 = getMD5(url);
        Bitmap bitmap = getBitmapFromMemoryCache(md5);
        if (bitmap != null) {
            if (DEBUG) Log.e(TAG, "getBitmap: 内存");
            onCallback(bitmap, onBitmapCallback);
        }
        if (isUiThread()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmapFromDisk = getBitmapFromDisk(md5);
                    if (bitmapFromDisk != null) {
                        if (DEBUG) Log.e(TAG, "getBitmap: 磁盘"+ Thread.currentThread().getName());
                        onCallback(bitmapFromDisk, onBitmapCallback);
                    } else {
                        if (DEBUG) Log.e(TAG, "没读取到图片");
                        onCallback(null, onBitmapCallback);
                    }
                }
            }).start();
        } else {
            bitmap = getBitmapFromDisk(md5);
            if (bitmap != null) {
                if (DEBUG) Log.e(TAG, "getBitmap: 磁盘"+ Thread.currentThread().getName());
                onCallback(bitmap, onBitmapCallback);
            } else {
                if (DEBUG) Log.e(TAG, "没读取到图片");
                onCallback(null, onBitmapCallback);

            }
        }

    }

    public void putBitmap(@NonNull String url, @NonNull final Bitmap bitmap) {
        final String md5 = getMD5(url);
        addBitmapToMemoryCache(md5, bitmap);
        if (isUiThread()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    addBitmapToDiskCache(md5, bitmap);
                }
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
            if (DEBUG) Log.e(TAG, "保存磁盘路径为空 ");
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
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
    private Bitmap getBitmapFromDisk(String pathUrl) {
        if (TextUtils.isEmpty(mRootCacheDir)) {
            if (DEBUG) Log.e(TAG, "读取磁盘路径为空 ");
            return null;
        }
        String path = mRootCacheDir;
        try {
            File file = new File(path);
            File myCaptureFile = new File(file, pathUrl);
            FileInputStream fis = new FileInputStream(myCaptureFile);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            addBitmapToMemoryCache(pathUrl, bitmap);
            return bitmap;

        } catch (FileNotFoundException e) {
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

    private void onCallback(final Bitmap bitmap, @NonNull final onBitmapCallback onBitmapCallback) {
        if (isUiThread()) {
            onBitmapCallback.onCallback(bitmap);
        } else {
            runUiThread(new Runnable() {
                @Override
                public void run() {
                    onBitmapCallback.onCallback(bitmap);
                }
            });
        }
    }

    public boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public interface onBitmapCallback {
        void onCallback(@Nullable Bitmap bitmap);
    }

    public void runUiThread(Runnable runnable) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }
}
