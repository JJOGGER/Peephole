package cn.jcyh.peephole.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.constant.ExtendFunction;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.control.DoorbellAudioManager;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.service.AudioValiService;
import cn.jcyh.peephole.ui.activity.CameraActivity;
import cn.jcyh.peephole.ui.activity.ObjectDetectingActivity;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.ServiceUtil;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.video.AVChatProfile;

public class BcReceiver extends BroadcastReceiver {
    private static final String LOCK_DETECT = "kphone.intent.action.LOCK_DETECT";
    private static final String PIR = "kphone.intent.action.PIR";
    private static final String RING = "kphone.intent.action.RING";
    private static final String MAGEINT = "kphone.intent.action.MAGEINT";
    private static final String NORMAL = "normal";
    private static final String PEOPLE_IN = "PeopleIn";
    private static final String PEOPLE_OUT = "PeopleOut";
    private static final String PRESSED = "pressed";
    private static final String RELEASED = "released";
    private static final String CLOSE = "close";
    private static final String OPEN = "open";
    private int mSensorTime;
    private boolean mIsPlaying;
    private ExecutorService mExecutorService;
    @SuppressLint("StaticFieldLeak")
    private static PIRRunnable sPirRunnable;

    @Override
    public void onReceive(Context context, Intent intent) {
        mSensorTime = ControlCenter.getDoorbellManager().getDoorbellConfig()
                .getAutoSensorTime();
        String act = intent.getAction();
        if (act == null) return;
        String extAct = intent.getStringExtra(Constant.VALUE);
        L.e("------------act："+act);
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
                ringAction(context, extAct);
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

    /**
     * 门铃
     */
    private void ringAction(Context context, String extAct) {
        if (AVChatProfile.getInstance().isAVChatting()) return;
        if (extAct.equals(PRESSED)) {
            /*-------------------------------人脸声纹拓展start---------------------------*/
            //检测人脸识别
            Intent intent;
            boolean faceVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_FACE_VALI);
            if (faceVali) {
                intent = new Intent(context, ObjectDetectingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            } else {
                //检测声纹识别
                boolean audioVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_AUDIO_VALI);
                if (audioVali) {
                    if (ServiceUtil.isServiceRunning(AudioValiService.class))
                        ServiceUtil.stopService(AudioValiService.class);
                    ServiceUtil.startService(AudioValiService.class);
                    return;
                }
            }
            /*-------------------------------人脸声纹拓展end---------------------------*/
            play(ControlCenter.DOORBELL_TYPE_RING);
            //启动播放服务后且服务未结束、抓拍界面未关闭时，不再重复抓拍
            if (!ControlCenter.sIsVideo) {
                ControlCenter.sIsVideo = true;
                intent = new Intent(context, CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING);
                context.startActivity(intent);
            }
            //发送
            DoorbellSystemAction systemAction = new DoorbellSystemAction(DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING);
            EventBus.getDefault().post(systemAction);
        } else if (extAct.equals(RELEASED)) {
            return;
        }
    }

    /**
     * 门磁
     */
    private void mageintAction(String extAct) {
        L.e("------------extAct："+extAct);
        if (CLOSE.equals(extAct)) {
            T.show("门磁关闭");
        } else if (OPEN.equals(extAct)) {
            T.show("门磁打开");
        }
    }

    /**
     * 人体感应
     */
    private void pirAction(Context context, String extAct) {
        //通话过程、猫眼查看界面、播放铃声过程不报警
        //视频通话过程不处理
        if (AVChatProfile.getInstance().isAVChatting()) return;
        if (extAct.equals(PEOPLE_IN)) {
            L.e("---------PIR中断:有人来了");
            if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying()) return;
            if (mExecutorService == null) {
                mExecutorService = Executors.newSingleThreadExecutor();
            }
            if (sPirRunnable == null) {
                sPirRunnable = new PIRRunnable(context);
            } else {
                return;
            }
            mExecutorService.execute(sPirRunnable);
        } else if (extAct.equals(PEOPLE_OUT)) {
            L.e("----- PIR中断:人走了");
            boolean pirSensorOn = ControlCenter.getBCManager().getPIRSensorOn();//人体监控是否打开
            if (!pirSensorOn) {
                if (mExecutorService != null && !mExecutorService.isShutdown()) {
                    mExecutorService.shutdown();
                }
                sPirRunnable = null;
            }
        }
    }

    /**
     * 防拆
     */
    private void tamperAction(String extAct) {
        if (extAct.equals(NORMAL)) {
            T.show("防拆中断:连接正常");
        } else if (extAct.equals("separate")) {
            T.show("防拆中断:设备拆开了");
        }
    }


    private void play(final int type) {
        mIsPlaying = false;
        //如果正在留言中，则不再响铃
        if (ControlCenter.sIsLeaveMsgRecording) {
            return;
        }
        if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying(DoorbellAudioManager.RingerTypeEnum.DOORBELL_RING)) {
            //正在播放门铃，不处理
            return;
        }
        if (type == ControlCenter.DOORBELL_TYPE_RING) {
            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager.RingerTypeEnum.DOORBELL_RING, null);
        } else {
            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager.RingerTypeEnum.DOORBELL_ALARM, null);
        }
    }

//    private class MyTimeTask extends TimerTask {
//        private Context mContext;
//        private int mCount;
//
//        MyTimeTask(Context context) {
//            mContext = context;
//            mCount = 0;
//        }
//
//        @Override
//        public synchronized void run() {
//            boolean pirSensorOn = ControlCenter.getBCManager().getPIRSensorOn();//人体监控是否打开
//            L.e("---------->mCount:" + mCount);
//            if (!pirSensorOn) {
//                mCount = 0;
//                cancelTimer();
//                return;
//            }
//            mCount++;
//            if (mCount >= mSensorTime) {//达到感应时间
//                mCount = 0;
//                cancelTimer();
//                boolean pirStatus = ControlCenter.getBCManager().getPIRStatus();
//                if (pirStatus) {
//                    //表示有人
//                    L.e("---------->仍然有人:");
//                    DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
//                    if (doorbellConfig.getSensorRingAlarm() == 1) {
//                        //开启了停留报警
//                        play(ControlCenter.DOORBELL_TYPE_ALARM);
//                    }
//                    if (ControlCenter.sIsVideo || mIsPlaying) return;
//                    ControlCenter.sIsVideo = true;
//                    Intent intent = new Intent(mContext, CameraActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM);
//                    mContext.startActivity(intent);
//                }
//            }
//        }
//    }

    private class PIRRunnable implements Runnable {
        private Context mContext;

        PIRRunnable(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        public void run() {
            boolean pirSensorOn = ControlCenter.getBCManager().getPIRSensorOn();//人体监控是否打开
            if (!pirSensorOn) {
                if (!mExecutorService.isShutdown()) {
                    mExecutorService.shutdown();
                }
                sPirRunnable = null;
                return;
            }
            try {
                Thread.sleep(mSensorTime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //达到感应时间
            if (sPirRunnable == null) return;
            if (!mExecutorService.isShutdown()) {
                mExecutorService.shutdown();
            }
            boolean pirStatus = ControlCenter.getBCManager().getPIRStatus();
            if (pirStatus) {
                //表示有人
                /*-------------------------------人脸声纹拓展start---------------------------*/
                //检测人脸识别
                if (ControlCenter.sIsFaceValing) return;
                Intent intent;
                boolean faceVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_FACE_VALI);
                if (faceVali) {
                    intent = new Intent(mContext, ObjectDetectingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    sPirRunnable = null;
                    return;
                } else {
                    //检测声纹识别
                    boolean audioVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_AUDIO_VALI);
                    if (audioVali) {
                        if (ServiceUtil.isServiceRunning(AudioValiService.class))
                            ServiceUtil.stopService(AudioValiService.class);
                        ServiceUtil.startService(AudioValiService.class);
                        sPirRunnable = null;
                        return;
                    }
                }
                /*-------------------------------人脸声纹拓展end---------------------------*/

                DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
                if (doorbellConfig.getSensorRingAlarm() == 1) {
                    //开启了停留报警
                    play(ControlCenter.DOORBELL_TYPE_ALARM);
                }
                if (ControlCenter.sIsVideo || mIsPlaying) return;
                ControlCenter.sIsVideo = true;
                intent = new Intent(mContext, CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM);
                mContext.startActivity(intent);
            }
            sPirRunnable = null;
        }
    }
}
