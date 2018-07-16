package cn.jcyh.peephole.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import cn.jcyh.eaglelock.api.MyLockAPI;
import cn.jcyh.eaglelock.api.MyLockCallback;
import cn.jcyh.peephole.BuildConfig;
import cn.jcyh.peephole.config.Config;


/**
 * Created by jogger on 2018/5/25.控制中心
 */

public class Util {
    @SuppressLint("StaticFieldLeak")
    private static Application sApplication;
    private static RefWatcher sRefWatcher;

    public static void init(@NonNull final Context context) {
        init((Application) context.getApplicationContext());
    }

    public static void init(@NonNull final Application app) {
        if (sApplication == null) {
            sApplication = app;
        }
        if (BuildConfig.DEBUG)
            L.plant(new L.DebugTree());
        sRefWatcher = LeakCanary.install(app);
        if (LeakCanary.isInAnalyzerProcess(app)) {
            return;
        }
        //初始化蓝牙锁
        MyLockAPI.init(app, new MyLockCallback(app));
        //初始化语音
        SpeechUtility.createUtility(app, SpeechConstant.APPID + "=" + Config.AUDIO_APP_ID);
//        JPushInterface.init(app);
//        UMConfigure.init(app, Config.UMENG_APP_KEY, null, UMConfigure.DEVICE_TYPE_PHONE, null);
//        //初始化数据库
//        DBManager.initDB(app);
//        PushManager.initPush();
    }

    public static void watch(Object watchedReference) {
        sRefWatcher.watch(watchedReference);
    }

    public static Application getApp() {
        if (sApplication != null) return sApplication;
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object at = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(at);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }
            init((Application) app);
            return sApplication;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NullPointerException("u should init first");
    }

}
