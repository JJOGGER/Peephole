package cn.jcyh.peephole.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;

/**
 * Created by jogger on 2018/1/16.
 */

public class SystemUtil {

    public static int getTerminalSize() {
        int size = 10;
        DisplayMetrics displayMetrics = Util.getApp().getResources().getDisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        if (widthPixels == 854 && heightPixels == 480)
            size = 5;
        else if (widthPixels == 1024 && heightPixels == 600)
            size = 7;
        L.e("-------" + widthPixels + ":" + heightPixels);
        return size;
    }

    public static String getProcessName(Context context) {
        String processName = null;
        // ActivityManager
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));

        while (true) {
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == android.os.Process.myPid()) {
                    processName = info.processName;
                    break;
                }
            }

            // go home
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }

            // take a rest and again
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }


    public static String getVersionName() {
        String versionName = null;
        try {
            PackageInfo packageInfo = Util.getApp().getPackageManager().getPackageInfo(Util.getApp().getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static int getVersionCode() {
        int versionCode = 1;
        try {
            PackageInfo packageInfo = Util.getApp().getPackageManager().getPackageInfo(Util.getApp().getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getPackageName() {
        String packageName = "";
        try {
            PackageInfo packageInfo = Util.getApp().getPackageManager().getPackageInfo(Util.getApp().getPackageName(), 0);
            packageName = packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageName;
    }

    /**
     * 获取当前手机系统版本号     *     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return android.os.SystemProperties.get("ro.build.custswversion", "");
    }

    public static String getANDROID_ID() {
        return Settings.System.getString(Util.getApp().getContentResolver(), Settings.System.ANDROID_ID);

    }

    public static void wakeLock(int levelAndFlags) {
        //唤醒
        //获取电源管理器对象
        PowerManager pm = (PowerManager) Util.getApp().getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        assert pm != null;
        PowerManager.WakeLock wl = pm.newWakeLock(levelAndFlags, "bright");
        //点亮屏幕
        wl.acquire(2000);
        wl.release();
    }
}
