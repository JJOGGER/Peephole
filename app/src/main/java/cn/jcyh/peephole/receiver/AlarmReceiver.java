package cn.jcyh.peephole.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.service.KeepBackLocalService;
import timber.log.Timber;

/**
 * Created by jogger on 2018/5/2.
 * 循环检测在线
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //再次注册闹钟广播
        Timber.e("-----onReceive:" + DoorBellControlCenter.sIsAnychatLogin);
        if (!DoorBellControlCenter.sIsAnychatLogin) {
            context.startService(new Intent(context, KeepBackLocalService.class));
        } else {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intentAlarm = new Intent(context, AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intentAlarm, 0);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * 60, pi);
        }
    }
}
