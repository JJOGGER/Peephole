package cn.jcyh.peephole.control;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.lang.reflect.InvocationTargetException;

import cn.jcyh.peephole.BuildConfig;
import timber.log.Timber;

/**
 * Created by Jogger on 2017/4/25.
 * 控制类
 */

public class ControlCenter {
    protected Gson mGson;
    @SuppressLint("StaticFieldLeak")
    private static Application sApplication;
    private static RefWatcher sRefWatcher;

    public static void init(@NonNull final Context context) {
        init((Application) context.getApplicationContext());
    }

    ControlCenter() {
        mGson = new Gson();
    }

    public static void init(@NonNull final Application app) {
        if (sApplication == null) {
            sApplication = app;
        }
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        if (LeakCanary.isInAnalyzerProcess(app)) {
            return;
        }
        sRefWatcher = LeakCanary.install(app);
    }

    public static void watch(Object watchedReference) {
        sRefWatcher.watch(watchedReference);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
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
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("u should init first");
    }
}
