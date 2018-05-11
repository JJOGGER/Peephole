package cn.jcyh.peephole.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;

import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.service.KeepBackLocalService;

/**
 * Created by jogger on 2018/5/2.
 * 循环检测在线
 */

public class AlarmReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        //再次注册闹钟广播
        if (!DoorBellControlCenter.sIsAnychatLogin) {
            //唤醒
//            Timber.e("-----------10分钟重连：" + DoorBellControlCenter.sIsAnychatLogin);
            //获取电源管理器对象
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            assert pm != null;
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.PARTIAL_WAKE_LOCK, "bright");
            //点亮屏幕
            wl.acquire(20000);
            context.startService(new Intent(context, KeepBackLocalService.class));
            wl.release();
        } else {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context
                    .ALARM_SERVICE);
            Intent intentAlarm = new Intent(context, AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intentAlarm, 0);
            assert alarmManager != null;
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
                    + 1000 * 60, pi);
        }
    }
}
