package cn.jcyh.peephole.receiver;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.control.DoorbellAudioManager;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.ui.activity.CameraActivity;
import cn.jcyh.peephole.utils.L;

/**
 * Created by jogger on 2018/5/2.
 * 停留报警计时
 */

public class AlarmReceiver extends BroadcastReceiver {
//    private PrintWriter mPrintWriter;
//
//    {
//        try {
//            FileOutputStream fileOutputStream = new FileOutputStream(new File(FileUtil
// .getSDCardPath() + "peephole_alarm_state.txt"), true);
//            mPrintWriter = new PrintWriter(fileOutputStream);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!ControlCenter.sPIRRunning) return;
        String time = SimpleDateFormat.getDateTimeInstance().format(new Date(System
                .currentTimeMillis()));
//        mPrintWriter.write("---" + time + " 达到感应时间:");
        boolean pirStatus = ControlCenter.getBCManager().getPIRStatus();
//        mPrintWriter.write("--- 当前pir状态:" + pirStatus + "\n");
//        mPrintWriter.flush();
        if (pirStatus) {
            final DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager()
                    .getDoorbellConfig();
            boolean multiVideo =doorbellConfig.isMultiVideo();
            if (multiVideo) {
                AVChatManager.getInstance().createRoom(ControlCenter.getSN(), null, new
                        AVChatCallback<AVChatChannelInfo>() {
                            @Override
                            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                                L.i("----------创建房间成功");
                                //启动播放服务后且服务未结束、抓拍界面未关闭时，不再重复抓拍
                                toTakePicture(context, doorbellConfig);
                            }

                            @Override
                            public void onFailed(int i) {
                                L.e("----------创建房间失败" + i);
//                                if (i != 417) {
//                                    return;
//                                }
                                //启动播放服务后且服务未结束、抓拍界面未关闭时，不再重复抓拍
                                toTakePicture(context, doorbellConfig);
                            }

                            @Override
                            public void onException(Throwable throwable) {
                                L.e("----------创建房间失败" + throwable.getMessage());
                                toTakePicture(context,doorbellConfig);
                            }
                        });
            } else {
                toTakePicture(context,doorbellConfig);
            }

        }
        ControlCenter.sPIRRunning = false;
    }

    private void toTakePicture(Context context, DoorbellConfig doorbellConfig) {
        if (doorbellConfig.getFaceRecognize() != 1) {
            //开启人脸识别，则停留报警不在此处理
            if (doorbellConfig.getDoorbellSensorParam().getRingAlarm() == 1) {
                //开启了停留报警
                play(ControlCenter.DOORBELL_TYPE_ALARM);
            }
        }
        ControlCenter.sPIRRunning = false;
        if (ControlCenter.sIsVideo) return;
        ControlCenter.sIsVideo = true;
//            mPrintWriter.write("--------------发起报警 \n");
//            mPrintWriter.flush();
        Intent intent = new Intent(context, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM);
        context.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void registAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context
                .ALARM_SERVICE);
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intentAlarm, 0);
        assert alarmManager != null;
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
                + 4000 * 60, pi);
//        SystemUtil.wakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
//                PowerManager.PARTIAL_WAKE_LOCK);
    }

    private void play(final int type) {
        //如果正在留言中，则不再响铃
        if (ControlCenter.sIsLeaveMsgRecording) {
            return;
        }
        if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying(DoorbellAudioManager
                .RingerTypeEnum.DOORBELL_RING)) {
            //正在播放门铃，不处理
            return;
        }
        if (type == ControlCenter.DOORBELL_TYPE_RING) {
            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager
                    .RingerTypeEnum.DOORBELL_RING, null);
        } else {
            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager
                    .RingerTypeEnum.DOORBELL_ALARM, null);
        }
    }
}
