package com.example.dab.explorerecyclerview.tools;

import android.app.Activity;
import android.content.CursorLoader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.lang.ref.SoftReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_SMS;

/**
 * Created by dab on 2017/5/10.
 * 自动填写验证码
 *  this.getContentResolver().unregisterContentObserver(mContent);
 *  this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mContent);
 */

public class SmsCaptchaObserver extends ContentObserver {
    public static final boolean debug = true;
    private static final String TAG = "SmsCaptchaObserver";
    private Cursor cursor = null;
    private SoftReference<Activity> mActivitySoftReference;
    private EditText mEditText;
    private int captchaLength = 4;
    private String matches;

    public SmsCaptchaObserver setMatches(String matches) {
        this.matches = matches;
        return this;
    }

    public SmsCaptchaObserver setCaptchaLength(int captchaLength) {
        this.captchaLength = captchaLength;
        return this;
    }

    public SmsCaptchaObserver(Handler handler, Activity activity, @NonNull EditText mEditText) {
        this(handler, activity, mEditText, null);
    }

    public SmsCaptchaObserver(Handler handler, Activity activity, @NonNull EditText mEditText, String matches) {
        super(handler);
        mActivitySoftReference = new SoftReference<>(activity);
        this.mEditText = mEditText;
        this.matches = matches;
    }

    private String func(String str) {
        Pattern p = Pattern.compile("\\d{" + captchaLength + "}");
        Matcher m = p.matcher(str);
        return m.find() ? m.group() : null;
    }

    private boolean isMatches(String smsBody) {
        if (TextUtils.isEmpty(matches)) {
            return true;
        }
        if (!TextUtils.isEmpty(smsBody) && smsBody.contains(matches)) {
            return true;
        }
        return false;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (selfChange) return;
        Activity activity = mActivitySoftReference.get();
        if (activity == null) return;
        if (mEditText != null) {
            int length = mEditText.getText().toString().replace(" ", "").length();
            if (length == captchaLength) return;
        }
        if (debug) Log.e(TAG, "onChange: ");
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(READ_SMS)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        //读取收件箱中指定号码的短信
                        CursorLoader cursorLoader = new CursorLoader(activity, Uri.parse("content://sms/inbox"), new String[]{"_id", "address", "read", "body"},
                                "read=?", new String[]{"0"}, "_id desc");
//                            CursorLoader cursorLoader = new CursorLoader(activity, Uri.parse("content://sms/inbox"), new String[]{"_id", "address", "read", "body"},
//                                    " address=? and read=?", new String[]{sendPhoneNumber, "0"}, "_id desc");
                        cursor = cursorLoader.loadInBackground();
                        if (debug) Log.e(TAG, "onChange: " + cursor.getColumnCount());
                        while (cursor != null && cursor.moveToNext()) {
//                                ContentValues values = new ContentValues();
//                                values.put("read", "1");  //修改短信为已读模式
                            int smsbodyColumn = cursor.getColumnIndex("body");
                            String smsBody = cursor.getString(smsbodyColumn);
                            if (!isMatches(smsBody)) continue;
                            if (mEditText != null) {
                                mEditText.setText(func(smsBody));
                                break;
                            }
                        }
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}