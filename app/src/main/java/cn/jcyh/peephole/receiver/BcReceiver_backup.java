//package cn.jcyh.peephole.receiver;
//
//import android.annotation.SuppressLint;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.PowerManager;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.PrintWriter;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import cn.jcyh.peephole.constant.Constant;
//import cn.jcyh.peephole.control.ControlCenter;
//import cn.jcyh.peephole.control.DoorbellAudioManager;
//import cn.jcyh.peephole.entity.DoorbellConfig;
//import cn.jcyh.peephole.event.DoorbellSystemAction;
//import cn.jcyh.peephole.http.HttpAction;
//import cn.jcyh.peephole.ui.activity.CameraActivity;
//import cn.jcyh.peephole.utils.FileUtil;
//import cn.jcyh.peephole.utils.L;
//import cn.jcyh.peephole.utils.T;
//import cn.jcyh.peephole.utils.Util;
//import cn.jcyh.peephole.video.AVChatProfile;
//
//public class BcReceiver extends BroadcastReceiver {
//    private static final String LOCK_DETECT = "kphone.intent.action.LOCK_DETECT";
//    private static final String PIR = "kphone.intent.action.PIR";
//    private static final String RING = "kphone.intent.action.RING";
//    private static final String MAGEINT = "kphone.intent.action.MAGEINT";
//    private static final String NORMAL = "normal";
//    private static final String SEPARATE = "separate";
//    private static final String PEOPLE_IN = "PeopleIn";
//    private static final String PEOPLE_OUT = "PeopleOut";
//    private static final String PRESSED = "pressed";
//    private static final String RELEASED = "released";
//    private static final String CLOSE = "close";
//    private static final String OPEN = "open";
//    private int mSensorTime;
//    private ExecutorService mExecutorService;
//    @SuppressLint("StaticFieldLeak")
//    private static PIRRunnable sPirRunnable;
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        mSensorTime = ControlCenter.getDoorbellManager().getDoorbellConfig()
//                .getAutoSensorTime();
//        String act = intent.getAction();
//        if (act == null) return;
//        String extAct = intent.getStringExtra(Constant.VALUE);
//        switch (act) {
//            case LOCK_DETECT: { // TAMPER
//                tamperAction(extAct);
//                break;
//            }
//            case PIR: {
//                pirAction(context, extAct);
//                break;
//            }
//            case RING: {
//                //当前查看猫眼界面时不抓拍
//                ringAction(context, extAct);
////		}else if (act.equals("kphone.intent.action.HOME_PRESS")) { // INDOOR_PRESS
////			String extAct = intent.getStringExtra("value");
////			if (extAct.equals("pressed")) {
////				L( "HOME ---- pressed!");
////				show(context,"HOME中断:按下HOME键");
////			} else if (extAct.equals("released")) {
////				L( "HOME ---- released!");
////				show(context,"HOME中断:放开HOME键");
////			}
//                break;
//            }
//            case MAGEINT:
//                mageintAction(extAct);
//                break;
//        }
//    }
//
//    /**
//     * 门铃
//     */
//    private void ringAction(Context context, String extAct) {
//        if (AVChatProfile.getInstance().isAVChatting()) return;
//        if (extAct.equals(PRESSED)) {
//            /*-------------------------------人脸声纹拓展start---------------------------*/
//            //检测人脸识别
////            Intent intent;
////            boolean faceVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_FACE_VALI);
////            if (faceVali) {
////                intent = new Intent(context, ObjectDetectingActivity.class);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                context.startActivity(intent);
////                return;
////            } else {
////                //检测声纹识别
////                boolean audioVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_AUDIO_VALI);
////                if (audioVali) {
////                    if (ServiceUtil.isServiceRunning(AudioValiService.class))
////                        ServiceUtil.stopService(AudioValiService.class);
////                    ServiceUtil.startService(AudioValiService.class);
////                    return;
////                }
////            }
//            /*-------------------------------人脸声纹拓展end---------------------------*/
//            play(ControlCenter.DOORBELL_TYPE_RING);
//            //启动播放服务后且服务未结束、抓拍界面未关闭时，不再重复抓拍
//            if (!ControlCenter.sIsVideo) {
//                ControlCenter.sIsVideo = true;
//                Intent intent = new Intent(context, CameraActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING);
//                context.startActivity(intent);
//            }
//            //发送
//            DoorbellSystemAction systemAction = new DoorbellSystemAction(DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING);
//            EventBus.getDefault().post(systemAction);
//        } else if (extAct.equals(RELEASED)) {
//            return;
//        }
//    }
//
//    /**
//     * 门磁
//     */
//    private void mageintAction(String extAct) {
//        L.e("------------extAct：" + extAct);
//        boolean isOpen = false;
//        if (CLOSE.equals(extAct)) {
//            T.show("门磁关闭");
//        } else if (OPEN.equals(extAct)) {
//            T.show("门磁打开");
//            isOpen = true;
//        }
//        HttpAction.getHttpAction().doorbellMagneticNotice(ControlCenter.getSN(), isOpen, null);
//    }
//
//    private PrintWriter mPrintWriter;
//
//    {
//        try {
//            FileOutputStream fileOutputStream = new FileOutputStream(new File(FileUtil.getSDCardPath() + "peephole_alarm_state.txt"), true);
//            mPrintWriter = new PrintWriter(fileOutputStream);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 人体感应
//     */
//    private void pirAction(Context context, String extAct) {
//        //通话过程、猫眼查看界面、播放铃声过程不报警
//        //视频通话过程不处理
//        int monitorSwitch = ControlCenter.getDoorbellManager().getDoorbellConfig().getDoorbellSensorParam().getMonitor();
//        if (monitorSwitch != 1) return;
//        if (AVChatProfile.getInstance().isAVChatting()) return;
//        if (extAct.equals(PEOPLE_IN)) {
//            String time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
//            mPrintWriter.write("---" + time + ": PIR中断有人来了" + "\n");
//            mPrintWriter.flush();
//            L.e("---------PIR中断:有人来了");
//            if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying()) return;
//            if (mExecutorService == null) {
//                mExecutorService = Executors.newSingleThreadExecutor();
//            }
//            if (sPirRunnable == null) {
//                sPirRunnable = new PIRRunnable(context);
////                SystemUtil.wakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
////                        PowerManager.PARTIAL_WAKE_LOCK);
//            } else {
//                return;
//            }
//            mExecutorService.execute(sPirRunnable);
//        } else if (extAct.equals(PEOPLE_OUT)) {
//            L.e("----- PIR中断:人走了");
//            String time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
//            mPrintWriter.write("---" + time + " PIR中断:人走了" + "\n");
//            mPrintWriter.flush();
//            boolean pirSensorOn = ControlCenter.getBCManager().getPIRSensorOn();//人体监控是否打开
//            if (!pirSensorOn) {
//                if (mExecutorService != null && !mExecutorService.isShutdown()) {
//                    mExecutorService.shutdown();
//                }
//                sPirRunnable = null;
//            }
//        }
//    }
//
//    /**
//     * 防拆
//     */
//    private void tamperAction(String extAct) {
//        boolean antiBreak = false;
//        if (extAct.equals(NORMAL)) {
//            T.show("防拆中断:连接正常");
//        } else if (extAct.equals(SEPARATE)) {
//            T.show("防拆中断:设备拆开了");
//            antiBreak = true;
//        }
//        HttpAction.getHttpAction().antiBreakAlarm(ControlCenter.getSN(), antiBreak, null);
//    }
//
//
//    private void play(final int type) {
//        //如果正在留言中，则不再响铃
//        if (ControlCenter.sIsLeaveMsgRecording) {
//            return;
//        }
//        if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying(DoorbellAudioManager.RingerTypeEnum.DOORBELL_RING)) {
//            //正在播放门铃，不处理
//            return;
//        }
//        if (type == ControlCenter.DOORBELL_TYPE_RING) {
//            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager.RingerTypeEnum.DOORBELL_RING, null);
//        } else {
//            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager.RingerTypeEnum.DOORBELL_ALARM, null);
//        }
//    }
//
//    private class PIRRunnable implements Runnable {
//        private Context mContext;
//
//        PIRRunnable(Context context) {
//            mContext = context.getApplicationContext();
//        }
//
//        @SuppressLint("WakelockTimeout")
//        @Override
//        public void run() {
//            boolean pirSensorOn = ControlCenter.getBCManager().getPIRSensorOn();//人体监控是否打开
//            if (!pirSensorOn) {
//                if (!mExecutorService.isShutdown()) {
//                    mExecutorService.shutdown();
//                }
//                sPirRunnable = null;
//                return;
//            }
//            String time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
//            mPrintWriter.write("---" + time + ": 唤醒和创建计时线程操作" + "\n");
//            mPrintWriter.flush();
//            PowerManager pm = (PowerManager) Util.getApp().getSystemService(Context.POWER_SERVICE);
//            //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
//            assert pm != null;
//            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
//                    | PowerManager.PARTIAL_WAKE_LOCK, "bright");
//            //点亮屏幕
//            wl.acquire();
//            try {
//                L.e("---PARTIAL WAKE LOCK---start");
//                Thread.sleep(mSensorTime * 1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            L.e("---PARTIAL WAKE LOCK---stop");
//            time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
//            mPrintWriter.write("---" + time + " 达到感应时间:");
//            //达到感应时间
//            if (sPirRunnable == null) return;
//            if (!mExecutorService.isShutdown()) {
//                mExecutorService.shutdown();
//            }
//            boolean pirStatus = ControlCenter.getBCManager().getPIRStatus();
//            mPrintWriter.write("--- 当前pir状态:" + pirStatus + "\n");
//            mPrintWriter.flush();
//            if (pirStatus) {
//                //表示有人
//                /*-------------------------------人脸声纹拓展start---------------------------*/
//                //检测人脸识别
////                if (ControlCenter.sIsFaceValing) return;
////                Intent intent;
////                boolean faceVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_FACE_VALI);
////                if (faceVali) {
////                    intent = new Intent(mContext, ObjectDetectingActivity.class);
////                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                    mContext.startActivity(intent);
////                    sPirRunnable = null;
////                    return;
////                } else {
////                    //检测声纹识别
////                    boolean audioVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_AUDIO_VALI);
////                    if (audioVali) {
////                        if (ServiceUtil.isServiceRunning(AudioValiService.class))
////                            ServiceUtil.stopService(AudioValiService.class);
////                        ServiceUtil.startService(AudioValiService.class);
////                        sPirRunnable = null;
////                        return;
////                    }
////                }
//                /*-------------------------------人脸声纹拓展end---------------------------*/
//
//                DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
//                if (doorbellConfig.getFaceRecognize() != 1) {
//                    //开启人脸识别，则停留报警不在此处理
//                    if (doorbellConfig.getDoorbellSensorParam().getRingAlarm() == 1) {
//                        //开启了停留报警
//                        play(ControlCenter.DOORBELL_TYPE_ALARM);
//                    }
//                }
//                if (ControlCenter.sIsVideo) return;
//                ControlCenter.sIsVideo = true;
//                mPrintWriter.write("--------------发起报警 \n");
//                mPrintWriter.flush();
//                Intent intent = new Intent(mContext, CameraActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM);
//                L.e("-----------发起报警");
//                mContext.startActivity(intent);
//            }
//            wl.setReferenceCounted(false);
//            wl.release();
//            sPirRunnable = null;
//        }
//    }
//}
//
//
////package cn.jcyh.peephole.receiver;
////
////import android.annotation.SuppressLint;
////import android.content.BroadcastReceiver;
////import android.content.Context;
////import android.content.Intent;
////import android.os.PowerManager;
////
////import org.greenrobot.eventbus.EventBus;
////
////import java.io.File;
////import java.io.FileNotFoundException;
////import java.io.FileOutputStream;
////import java.io.PrintWriter;
////import java.text.SimpleDateFormat;
////import java.util.Date;
////import java.util.concurrent.ExecutorService;
////
////import cn.jcyh.peephole.constant.Constant;
////import cn.jcyh.peephole.constant.ExtendFunction;
////import cn.jcyh.peephole.control.ControlCenter;
////import cn.jcyh.peephole.control.DoorbellAudioManager;
////import cn.jcyh.peephole.entity.DoorbellConfig;
////import cn.jcyh.peephole.event.DoorbellSystemAction;
////import cn.jcyh.peephole.http.HttpAction;
////import cn.jcyh.peephole.service.AudioValiService;
////import cn.jcyh.peephole.ui.activity.CameraActivity;
////import cn.jcyh.peephole.ui.activity.CameratestActivity;
////import cn.jcyh.peephole.ui.activity.ObjectDetectingActivity;
////import cn.jcyh.peephole.utils.FileUtil;
////import cn.jcyh.peephole.utils.L;
////import cn.jcyh.peephole.utils.ServiceUtil;
////import cn.jcyh.peephole.utils.SystemUtil;
////import cn.jcyh.peephole.utils.T;
////import cn.jcyh.peephole.video.AVChatProfile;
////
////public class BcReceiver extends BroadcastReceiver {
////    private static final String LOCK_DETECT = "kphone.intent.action.LOCK_DETECT";
////    private static final String PIR = "kphone.intent.action.PIR";
////    private static final String RING = "kphone.intent.action.RING";
////    private static final String MAGEINT = "kphone.intent.action.MAGEINT";
////    private static final String NORMAL = "normal";
////    private static final String SEPARATE = "separate";
////    private static final String PEOPLE_IN = "PeopleIn";
////    private static final String PEOPLE_OUT = "PeopleOut";
////    private static final String PRESSED = "pressed";
////    private static final String RELEASED = "released";
////    private static final String CLOSE = "close";
////    private static final String OPEN = "open";
////    private int mSensorTime;
////    private ExecutorService mExecutorService;
////    @SuppressLint("StaticFieldLeak")
////    private static PIRRunnable sPirRunnable;
////
////    @Override
////    public void onReceive(Context context, Intent intent) {
////        mSensorTime = ControlCenter.getDoorbellManager().getDoorbellConfig()
////                .getAutoSensorTime();
////        String act = intent.getAction();
////        if (act == null) return;
////        String extAct = intent.getStringExtra(Constant.VALUE);
////        switch (act) {
////            case LOCK_DETECT: { // TAMPER
////                tamperAction(extAct);
////                break;
////            }
////            case PIR: {
////                pirAction(context, extAct);
////                break;
////            }
////            case RING: {
////                //当前查看猫眼界面时不抓拍
////                ringAction(context, extAct);
//////		}else if (act.equals("kphone.intent.action.HOME_PRESS")) { // INDOOR_PRESS
//////			String extAct = intent.getStringExtra("value");
//////			if (extAct.equals("pressed")) {
//////				L( "HOME ---- pressed!");
//////				show(context,"HOME中断:按下HOME键");
//////			} else if (extAct.equals("released")) {
//////				L( "HOME ---- released!");
//////				show(context,"HOME中断:放开HOME键");
//////			}
////                break;
////            }
////            case MAGEINT:
////                mageintAction(extAct);
////                break;
////        }
////    }
////
////    /**
////     * 门铃
////     */
////    private void ringAction(Context context, String extAct) {
////        if (AVChatProfile.getInstance().isAVChatting()) return;
////        if (extAct.equals(PRESSED)) {
////            /*-------------------------------人脸声纹拓展start---------------------------*/
////            //检测人脸识别
////            Intent intent;
////            boolean faceVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_FACE_VALI);
////            if (faceVali) {
////                intent = new Intent(context, ObjectDetectingActivity.class);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                context.startActivity(intent);
////                return;
////            } else {
////                //检测声纹识别
////                boolean audioVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_AUDIO_VALI);
////                if (audioVali) {
////                    if (ServiceUtil.isServiceRunning(AudioValiService.class))
////                        ServiceUtil.stopService(AudioValiService.class);
////                    ServiceUtil.startService(AudioValiService.class);
////                    return;
////                }
////            }
////            /*-------------------------------人脸声纹拓展end---------------------------*/
////            play(ControlCenter.DOORBELL_TYPE_RING);
////            //启动播放服务后且服务未结束、抓拍界面未关闭时，不再重复抓拍
////            if (!ControlCenter.sIsVideo) {
////                ControlCenter.sIsVideo = true;
////                intent = new Intent(context, CameraActivity.class);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING);
////                context.startActivity(intent);
////            }
////            //发送
////            DoorbellSystemAction systemAction = new DoorbellSystemAction(DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING);
////            EventBus.getDefault().post(systemAction);
////        } else if (extAct.equals(RELEASED)) {
////            return;
////        }
////    }
////
////    /**
////     * 门磁
////     */
////    private void mageintAction(String extAct) {
////        L.e("------------extAct：" + extAct);
////        boolean isOpen = false;
////        if (CLOSE.equals(extAct)) {
////            T.show("门磁关闭");
////        } else if (OPEN.equals(extAct)) {
////            T.show("门磁打开");
////            isOpen = true;
////        }
////        HttpAction.getHttpAction().doorbellMagneticNotice(ControlCenter.getSN(), isOpen, null);
////    }
////
////    private PrintWriter mPrintWriter;
////
////    {
////        try {
////            FileOutputStream fileOutputStream = new FileOutputStream(new File(FileUtil.getSDCardPath() + "peephole_alarm_state.txt"), true);
////            mPrintWriter = new PrintWriter(fileOutputStream);
////        } catch (FileNotFoundException e) {
////            e.printStackTrace();
////        }
////    }
////
////    /**
////     * 人体感应
////     */
////    private void pirAction(Context context, String extAct) {
////        //通话过程、猫眼查看界面、播放铃声过程不报警
////        //视频通话过程不处理
////        int monitorSwitch = ControlCenter.getDoorbellManager().getDoorbellConfig().getDoorbellSensorParam().getMonitor();
////        if (monitorSwitch != 1) return;
////        if (AVChatProfile.getInstance().isAVChatting()) return;
////        if (extAct.equals(PEOPLE_IN)) {
////            String time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
////            mPrintWriter.write("---" + time + ": PIR中断有人来了" + "\n");
////            mPrintWriter.flush();
////            L.e("---------PIR中断:有人来了");
////            if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying()) return;
////            boolean pirStatus = ControlCenter.getBCManager().getPIRStatus();
////            mPrintWriter.write("--- 当前pir状态:" + pirStatus + "\n");
////            mPrintWriter.flush();
////            if (pirStatus) {
////                DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
////                if (doorbellConfig.getFaceRecognize() != 1) {
////                    //开启人脸识别，则停留报警不在此处理
////                    if (doorbellConfig.getDoorbellSensorParam().getRingAlarm() == 1) {
////                        //开启了停留报警
////                        play(ControlCenter.DOORBELL_TYPE_ALARM);
////                    }
////                }
////                if (ControlCenter.sIsVideo) return;
////                ControlCenter.sIsVideo = true;
////                mPrintWriter.write("--------------发起报警 \n");
////                mPrintWriter.flush();
////                Intent intent = new Intent(context, CameratestActivity.class);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM);
////                L.e("-----------发起报警");
////                context.startActivity(intent);
//////                SystemUtil.wakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
//////                        PowerManager.SCREEN_DIM_WAKE_LOCK);
//////                T.show("报警");
////            }
////
////        } else if (extAct.equals(PEOPLE_OUT)) {
////            L.e("----- PIR中断:人走了");
////            String time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
////            mPrintWriter.write("---" + time + " PIR中断:人走了" + "\n");
////            mPrintWriter.flush();
////        }
////    }
////
////    /**
////     * 防拆
////     */
////    private void tamperAction(String extAct) {
////        boolean antiBreak = false;
////        if (extAct.equals(NORMAL)) {
////            T.show("防拆中断:连接正常");
////        } else if (extAct.equals(SEPARATE)) {
////            T.show("防拆中断:设备拆开了");
////            antiBreak = true;
////        }
////        HttpAction.getHttpAction().antiBreakAlarm(ControlCenter.getSN(), antiBreak, null);
////    }
////
////
////    private void play(final int type) {
////        //如果正在留言中，则不再响铃
////        if (ControlCenter.sIsLeaveMsgRecording) {
////            return;
////        }
////        if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying(DoorbellAudioManager.RingerTypeEnum.DOORBELL_RING)) {
////            //正在播放门铃，不处理
////            return;
////        }
////        if (type == ControlCenter.DOORBELL_TYPE_RING) {
////            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager.RingerTypeEnum.DOORBELL_RING, null);
////        } else {
////            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager.RingerTypeEnum.DOORBELL_ALARM, null);
////        }
////    }
////
////    private class PIRRunnable implements Runnable {
////        private Context mContext;
////
////        PIRRunnable(Context context) {
////            mContext = context.getApplicationContext();
////        }
////
////        @Override
////        public void run() {
////            boolean pirSensorOn = ControlCenter.getBCManager().getPIRSensorOn();//人体监控是否打开
////            if (!pirSensorOn) {
////                if (!mExecutorService.isShutdown()) {
////                    mExecutorService.shutdown();
////                }
////                sPirRunnable = null;
////                return;
////            }
////            try {
////                Thread.sleep(mSensorTime * 1000);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
////            String time = SimpleDateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
////            mPrintWriter.write("---" + time + " 达到感应时间:");
////            //达到感应时间
////            if (sPirRunnable == null) return;
////            if (!mExecutorService.isShutdown()) {
////                mExecutorService.shutdown();
////            }
////            boolean pirStatus = ControlCenter.getBCManager().getPIRStatus();
////            mPrintWriter.write("--- 当前pir状态:" + pirStatus + "\n");
////            mPrintWriter.flush();
////            if (pirStatus) {
////                DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
////                if (doorbellConfig.getFaceRecognize() != 1) {
////                    //开启人脸识别，则停留报警不在此处理
////                    if (doorbellConfig.getDoorbellSensorParam().getRingAlarm() == 1) {
////                        //开启了停留报警
////                        play(ControlCenter.DOORBELL_TYPE_ALARM);
////                    }
////                }
////                if (ControlCenter.sIsVideo) return;
////                ControlCenter.sIsVideo = true;
////                mPrintWriter.write("--------------发起报警 \n");
////                mPrintWriter.flush();
//////                Intent intent = new Intent(mContext, CameraActivity.class);
//////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//////                intent.putExtra(Constant.TYPE, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM);
//////                L.e("-----------发起报警");
//////                mContext.startActivity(intent);
////                SystemUtil.wakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
////                        PowerManager.SCREEN_DIM_WAKE_LOCK);
////                T.show("报警");
////            }
////            sPirRunnable = null;
////        }
////    }
////}
