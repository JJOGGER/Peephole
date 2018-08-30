package cn.jcyh.peephole.thread;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import cn.jcyh.peephole.MainActivity;
import cn.jcyh.peephole.utils.Util;

/**
 * Created by jogger on 2018/7/19.崩溃重启
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler mAppCrashHandler;

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public void initCrashHandler() {
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static CrashHandler getInstance() {
        if (mAppCrashHandler == null) {
            mAppCrashHandler = new CrashHandler();
        }
        return mAppCrashHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            AlarmManager mgr = (AlarmManager) Util.getApp().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(Util.getApp(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("crash", true);
            PendingIntent restartIntent = PendingIntent.getActivity(Util.getApp(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            assert mgr != null;
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            System.gc();
        }
    }

    /**
     * 错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 异常
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex != null) {
        }
        return ex != null;
    }
}
