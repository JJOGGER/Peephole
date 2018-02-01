package cn.jcyh.peephole.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jogger on 2017/4/12.
 *
 */

public class SharePreUtil {
    private static SharePreUtil mSharePreUtil;
    private static Context mContext;
    private static SharedPreferences sp;
    private static String mCustomFileName;

    private SharePreUtil() {
    }

    public static SharePreUtil getInstance(Context context) {
        mContext = context.getApplicationContext();
        if (mSharePreUtil == null) {
            synchronized (SharePreUtil.class) {
                if (mSharePreUtil == null) {
                    mSharePreUtil = new SharePreUtil();
                }
            }
        }
        sp = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return mSharePreUtil;
    }

    /**
     * 保存在手机里的文件名
     */
    private static final String FILE_NAME = "jcyh_data";

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void setBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String getString(String key, String value) {
        return sp.getString(key, value);
    }

    public int getInt(String key, int value) {
        return sp.getInt(key, value);
    }

    public boolean getBoolean(String key, boolean value) {
        return sp.getBoolean(key, value);
    }


}