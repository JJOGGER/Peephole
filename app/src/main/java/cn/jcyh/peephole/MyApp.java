package cn.jcyh.peephole;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import cn.jcyh.peephole.utils.SystemUtil;
import timber.log.Timber;


public class MyApp extends Application {
    public static String sImei;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        sImei = getAndroidIMEI();
        Timber.e("---------imei:"+sImei);
//        HttpAction.getHttpAction().initDoorbell(sImei, null);
    }

    private String getAndroidIMEI() {
        String androidIMEI = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.ANDROID_ID);
        Log.d("ANDROID_ID", androidIMEI + " ");
        return androidIMEI;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static boolean inMainProcess(Context context) {
        String packageName = context.getPackageName();
        String processName = SystemUtil.getProcessName(context);
        return packageName.equals(processName);
    }

}
