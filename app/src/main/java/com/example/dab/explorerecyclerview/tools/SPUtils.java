package com.example.dab.explorerecyclerview.tools;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Created by dab on 2017/5/12.
 */

public class SPUtils {
    private static final String TAG = "SPUtils";
    private static boolean debug = true;
    private static String FILE_NAME = "SPUtils";
    private static Application applicationContext;
    private static OnJsonHelper sOnJsonHelper;

    public static String getFileName() {
        return FILE_NAME;
    }

    public static void setOnJsonHelper(OnJsonHelper onJsonHelper) {
        sOnJsonHelper = onJsonHelper;
    }

    public static void init(@NonNull Application context, @NonNull String file_name) {
        if (TextUtils.isEmpty(file_name)) {
            throw new IllegalArgumentException("file_name为空或者null");
        }
        applicationContext = context;
        FILE_NAME = file_name;
    }

    public static void init(@NonNull Application context, @NonNull String file_name, @NonNull OnJsonHelper onJsonHelper) {
        if (TextUtils.isEmpty(file_name)) {
            throw new IllegalArgumentException("file_name为空或者null");
        }
        applicationContext = context;
        FILE_NAME = file_name;
        sOnJsonHelper = onJsonHelper;
    }

    public static void put(@NonNull String key, @NonNull Object o) {
        put(applicationContext, key, o);
    }

    public static void put(Context context, String key, Object object) {
        if (object == null) {
            if (debug) Log.e(TAG, "object== null");
            return;
        }
        if (context == null) {
            throw new NullPointerException("context is null,可能没有初始化init()");
        }
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        do {
            if (object instanceof String) {
                editor.putString(key, (String) object);
                break;
            }
            if (object instanceof Integer) {
                editor.putInt(key, (Integer) object);
                break;
            }
            if (object instanceof Boolean) {
                editor.putBoolean(key, (Boolean) object);
                break;
            }
            if (object instanceof Float) {
                editor.putFloat(key, (Float) object);
                break;
            }
            if (object instanceof Long) {
                editor.putLong(key, (Long) object);
                break;
            }
            if (sOnJsonHelper != null) {
                String json = sOnJsonHelper.toJson(object);
                if (TextUtils.isEmpty(json)) {
                    if (debug) Log.e(TAG, "获取到的json为null");
                    break;
                }
                editor.putString(key, json);
                break;
            }

            if (debug) Log.e(TAG, "保存失败:不是基础的类型且未注册OnJsonHelper接口" + object.toString());

        } while (false);
        editor.apply();
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    public static void remove(@NonNull String key) {
        remove(applicationContext, key);
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, @NonNull String key) {
        if (context == null) {
            throw new NullPointerException("context is null,可能没有初始化init()");
        }
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * 清除所有数据
     */
    public static void clear() {
        clear(applicationContext);
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        if (context == null) {
            throw new NullPointerException("context is null,可能没有初始化init()");
        }
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public static <T> T get(@NonNull String key, @NonNull T t) {
        return get(applicationContext, key, t);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Context context, @NonNull String key, T t) {
        if (debug) {
            try {
                ParameterizedType type = (ParameterizedType) t.getClass().getGenericSuperclass();
                Type[] actualTypeArguments = type.getActualTypeArguments();
                Log.e(TAG, "dispose: " + " type:" + Arrays.toString(actualTypeArguments));
            } catch (Exception ignored) {

            }
        }

        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        if (t instanceof String) {
            return (T) sp.getString(key, (String) t);
        }
        if (t instanceof Integer) {
            return (T) Integer.valueOf(sp.getInt(key, (Integer) t));
        }
        if (t instanceof Boolean) {
            return (T) Boolean.valueOf(sp.getBoolean(key, (Boolean) t));
        }
        if (t instanceof Float) {
            return (T) Float.valueOf(sp.getFloat(key, (Float) t));
        }
        if (t instanceof Long) {
            return (T) Long.valueOf(sp.getLong(key, (Long) t));
        }
        String json = sp.getString(key, null);
        if (TextUtils.isEmpty(json)) {
            return t;
        }
        if (sOnJsonHelper != null) {
            return (T) sOnJsonHelper.formJson(json, t.getClass());
        }
        if (debug) Log.e(TAG, "保存失败:不是基础的类型且未注册OnJsonHelper接口" + t.toString());
        return null;
    }


    interface OnJsonHelper {
        String toJson(Object object);

        Object formJson(String json, Class<?> aClass);
    }
}

