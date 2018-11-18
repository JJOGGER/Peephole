package cn.jcyh.peephole.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.control.DoorbellAudioManager;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.ui.activity.CameraActivity;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.SystemUtil;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.utils.Util;
import cn.jcyh.peephole.video.AVChatProfile;

public class BcReceiver extends BroadcastReceiver {
    private static final String LOCK_DETECT = "kphone.intent.action.LOCK_DETECT";
    private static final String PIR = "kphone.intent.action.PIR";
    private static final String RING = "kphone.intent.action.RING";
    private static final String MAGEINT = "kphone.intent.action.MAGEINT";
    private static final String NORMAL = "normal";
    private static final String SEPARATE = "separate";
    private static final String PEOPLE_IN = "PeopleIn";
    private static final String PEOPLE_OUT = "PeopleOut";
    private static final String PRESSED = "pressed";
    private static final String RELEASED = "released";
    private static final String CLOSE = "close";
    private static final String OPEN = "open";
    private int mSensorTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        mSensorTime = ControlCenter.getDoorbellManager().getDoorbellConfig()
                .getAutoSensorTime();
        String act = intent.getAction();
        if (act == null) return;
        String extAct = intent.getStringExtra(Constant.VALUE);
        switch (act) {
            case LOCK_DETECT: { // TAMPER
                tamperAction(extAct);
                break;
            }
            case PIR: {
                pirAction(context, extAct);
                break;
            }
            case RING: {
                //当前查看猫眼界面时不抓拍
                if (SystemUtil.getVersionCode() == 10086) {
                    debugRingAction(context, extAct);
                } else {
                    ringAction(context, extAct);
                }
//		}else if (act.equals("kphone.intent.action.HOME_PRESS")) { // INDOOR_PRESS
//			String extAct = intent.getStringExtra("value");
//			if (extAct.equals("pressed")) {
//				L( "HOME ---- pressed!");
//				show(context,"HOME中断:按下HOME键");
//			} else if (extAct.equals("released")) {
//				L( "HOME ---- released!");
//				show(context,"HOME中断:放开HOME键");
//			}
                break;
            }
            case MAGEINT:
                mageintAction(extAct);
                break;
        }
    }

    private void debugRingAction(final Context context, String extAct) {
        if (extAct.equals(PRESSED)) {
            AVChatManager.getInstance().createRoom(ControlCenter.getSN(), null, new
                    AVChatCallback<AVChatChannelInfo>() {
                        @Override
                        public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                            L.e("----------创建房间成功");
                            //启动播放服务后且服务未结束、抓拍界面未关闭时，不再重复抓拍
                            if (!ControlCenter.sIsVideo) {
                                ControlCenter.sIsVideo = true;
                                Intent intent = new Intent(context, CameraActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(Constant.TYPE, DoorbellSystemAction
                                        .TYPE_DOORBELL_SYSTEM_RING);

                                context.startActivity(intent);
                            }
                            //发送
                            DoorbellSystemAction systemAction = new DoorbellSystemAction
                                    (DoorbellSystemAction
                                            .TYPE_DOORBELL_SYSTEM_RING);
                            EventBus.getDefault().post(systemAction);
                        }

                        @Override
                        public void onFailed(int i) {
                            L.e("----------创建房间失败" + i);
                            if (i != 417) return;
                            //启动播放服务后且服务未结束、抓拍界面未关闭时，不再重复抓拍
                            if (!ControlCenter.sIsVideo) {
                                ControlCenter.sIsVideo = true;
                                Intent intent = new Intent(context, CameraActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(Constant.TYPE, DoorbellSystemAction
                                        .TYPE_DOORBELL_SYSTEM_RING);
                                context.startActivity(intent);
                            }
                            //发送
                            DoorbellSystemAction systemAction = new DoorbellSystemAction
                                    (DoorbellSystemAction
                                    .TYPE_DOORBELL_SYSTEM_RING);
                            EventBus.getDefault().post(systemAction);
                        }

                        @Override
                        public void onException(Throwable throwable) {
                            L.e("----------创建房间失败" + throwable.getMessage());
                        }
                    });
        }
    }

    /**
     * 门铃
     */
    private void ringAction(Context context, String extAct) {
        if (AVChatProfile.getInstance().isAVChatting()) return;
        if (extAct.equals(PRESSED)) {
            play(ControlCenter.DOORBELL_TYPE_RING);
            //启动播放服务后且服务未结束、抓拍界面未关闭时，不再重复抓拍
            if (!ControlCenter.sIsVideo) {
                ControlCenter.sIsVideo = true;
                Intent intent = new Intent(context, CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING);
                context.startActivity(intent);
            }
            //发送
            DoorbellSystemAction systemAction = new DoorbellSystemAction(DoorbellSystemAction
                    .TYPE_DOORBELL_SYSTEM_RING);
            EventBus.getDefault().post(systemAction);
        }


    }

    /**
     * 门磁
     */
    private void mageintAction(String extAct) {
        L.e("------------extAct：" + extAct);
        boolean isOpen = false;
        if (CLOSE.equals(extAct)) {
            T.show("门磁关闭");
        } else if (OPEN.equals(extAct)) {
            T.show("门磁打开");
            isOpen = true;
        }
        HttpAction.getHttpAction().doorbellMagneticNotice(isOpen, null);
    }

//    private PrintWriter mPrintWriter;

//    {
//        try {
//            FileOutputStream fileOutputStream = new FileOutputStream(new File(FileUtil
// .getSDCardPath() + "peephole_alarm_state.txt"), true);
//            mPrintWriter = new PrintWriter(fileOutputStream);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 人体感应
     */
    private void pirAction(Context context, String extAct) {
        //通话过程、猫眼查看界面、播放铃声过程不报警
        int monitorSwitch = ControlCenter.getDoorbellManager().getDoorbellConfig()
                .getDoorbellSensorParam().getMonitor();
        if (monitorSwitch != 1) return;
        if (AVChatProfile.getInstance().isAVChatting()) return;
        if (extAct.equals(PEOPLE_IN)) {
            String time = SimpleDateFormat.getDateTimeInstance().format(new Date(System
                    .currentTimeMillis()));
//            mPrintWriter.write("---" + time + ": PIR中断有人来了" + "\n");
//            mPrintWriter.flush();
            L.e("---------PIR中断:有人来了");
            if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying()) return;
            if (!ControlCenter.sPIRRunning) {
//                mPrintWriter.write("---" + time + ": 唤醒和创建计时线程操作" + "\n");
//                mPrintWriter.flush();
                AlarmManager alarmManager = (AlarmManager) Util.getApp().getSystemService(Context
                        .ALARM_SERVICE);
                Intent intentAlarm = new Intent(context, AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, intentAlarm, 0);
                assert alarmManager != null;
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock
                        .elapsedRealtime() + 1000 * mSensorTime, pi);
                ControlCenter.sPIRRunning = true;
            }
//            else {
//                mPrintWriter.write("---" + time + ": 已经设置了闹钟，不再设置" + "\n");
//                mPrintWriter.flush();
//            }
        }
//        else if (extAct.equals(PEOPLE_OUT)) {
//            L.e("----- PIR中断:人走了");
//            String time = SimpleDateFormat.getDateTimeInstance().format(new Date(System
// .currentTimeMillis()));
//            mPrintWriter.write("---" + time + " PIR中断:人走了" + "\n");
//            mPrintWriter.flush();
//        }
    }

    /**
     * 防拆
     */
    private void tamperAction(String extAct) {
        boolean antiBreak = false;
        if (extAct.equals(NORMAL)) {
            T.show("防拆中断:连接正常");
        } else if (extAct.equals(SEPARATE)) {
            T.show("防拆中断:设备拆开了");
            antiBreak = true;
        }
        HttpAction.getHttpAction().antiBreakAlarm(antiBreak, null);
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

