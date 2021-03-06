package cn.jcyh.peephole.receiver;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;

import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.service.MainService;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.NetworkUtil;
import cn.jcyh.peephole.utils.SystemUtil;

/**
 * Created by jogger on 2018/5/2.
 * 循环检测在线
 */

public class AwakenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //再次注册闹钟广播
        StatusCode status = NIMClient.getStatus();
        L.i("-----------------AwakenReceiver" + status);
        registAlarm(context);
        if (status != StatusCode.LOGINED) {
            SystemUtil.wakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.PARTIAL_WAKE_LOCK);
            if (!NetworkUtil.isConnected()) {
                return;
            }
            context.startService(new Intent(context, MainService.class));
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void registAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context
                .ALARM_SERVICE);
        Intent intentAlarm = new Intent(context, AwakenReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intentAlarm, 0);
        assert alarmManager != null;
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
                + 4000 * 60, pi);
        L.i("-----------------coming in request HeartBeat");
        if (ControlCenter.getSN().startsWith(Constant.ZHONGKE_SN)) {

            StatusCode status = NIMClient.getStatus();
            L.i("-----------------http start request HeartBeat" + status.getValue());
            HttpAction.getHttpAction().sendHeartBeat(status.getValue(),null);
        }
//        SystemUtil.wakeLPowerManager.ACQUIRE_CAUSES_WAKEUP |
//                PowerManager.PARTIAL_WAKE_LOCK);
    }
}
