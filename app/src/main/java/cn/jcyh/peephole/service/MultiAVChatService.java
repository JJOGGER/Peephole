package cn.jcyh.peephole.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatSessionStats;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import cn.jcyh.peephole.constant.AVChatExitCode;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.AVChatAction;
import cn.jcyh.peephole.observer.AVChatTimeoutObserver;
import cn.jcyh.peephole.observer.PhoneCallStateObserver;
import cn.jcyh.peephole.observer.SimpleAVChatStateObserver;
import cn.jcyh.peephole.service.video.AVChatControllerCallback;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.Util;
import cn.jcyh.peephole.video.AVChatController;
import cn.jcyh.peephole.video.AVChatProfile;

/**
 * Created by jogger on 2018/2/2.
 */

public class MultiAVChatService extends Service {
    public int mCurrentVideoVolume = 1;
    private AVChatController mAVChatController;
    private static final byte IS_DUAL_CAMERA = AVChatControlCommand.NOTIFY_CUSTOM_BASE + 1;
    private static final byte SWITCH_CAMERA = AVChatControlCommand.NOTIFY_CUSTOM_BASE + 2;//切换摄像头
    private static final byte UNLOCK = AVChatControlCommand.NOTIFY_CUSTOM_BASE + 3;
    private long mSessionDuration;
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            AudioManager audioManager = (AudioManager) Util.getApp().getSystemService(Context
                    .AUDIO_SERVICE);
            assert audioManager != null;
            int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            L.i("---streamVolume:" + streamVolume);
            if (streamVolume != mCurrentVideoVolume) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mCurrentVideoVolume,
                        0);
            }
            streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            L.i("---streamVolume:" + streamVolume);

        }
    };
    private Timer mTimer = new Timer();
    //    private FrameLayout flSurfaceContainer;
//    private SurfaceView mSurfaceView;
//    private WindowManager mWindowManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        mCurrentVideoVolume = doorbellConfig.getVideoVolume();
        mTimer.schedule(mTimerTask, 0, 1000);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAVChatController != null) { //界面销毁时强制尝试挂断
                    try {
                        mAVChatController.leaveRoom();
                        //退出房间，结束通话
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                stopSelf();
            }
        }, doorbellConfig.getVideoConfig().getVideoTimeLimit() * 1000);
        //通话过程暂时关闭停留报警
        AVChatProfile.getInstance().setAVChatting(true);
        registerObserves(true);
//        createToucher();
    }

    /**
     * 来电监听
     */
    private void registerObserves(boolean register) {
        AVChatManager.getInstance().observeAVChatState(mAVChatStateObserver, register);
        AVChatManager.getInstance().observeHangUpNotification(mCallHangupObserver, register);
        AVChatManager.getInstance().observeControlNotification(mCallControlObserver, register);
        AVChatTimeoutObserver.getInstance().observeTimeoutNotification(mTimeoutObserver,
                register, true);
        PhoneCallStateObserver.getInstance().observeAutoHangUpForLocalPhone
                (mAutohangupforlocalphoneobserver, register);
        ControlCenter.getBCManager().setMainSpeakerOn(!register);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        mAVChatController = new AVChatController(this, null);

        //接听来电
        mAVChatController.joinRoom(new AVChatControllerCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData avChatData) {
                L.e("--------------加入房间成功");
            }

            public void onFailed(int code, String errorMsg) {
                L.e("--------------加入房间onFailed");
                stopSelf();
            }
        });
        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        HttpAction.getHttpAction().userTaklTimeRecord(mAvChatData.getAccount(), mSessionDuration,
//                null);
        if (mAVChatController != null) { //界面销毁时强制尝试挂断
            try {
                mAVChatController.hangUp(AVChatExitCode.HANGUP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        registerObserves(false);
        AVChatAction avChatAction = new AVChatAction();
        avChatAction.setType(AVChatAction.AVCHAT_HANG_UP);
        EventBus.getDefault().post(avChatAction);
        AVChatProfile.getInstance().setAVChatting(false);
        ControlCenter.getDoorbellManager().setLastVideoTime(System.currentTimeMillis());
        cancelTimer();
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }


    //通话过程状态监听
    private SimpleAVChatStateObserver mAVChatStateObserver = new SimpleAVChatStateObserver() {

        @Override
        public void onDeviceEvent(int code, String desc) {
            super.onDeviceEvent(code, desc);
//            L.e("------------code:" + code + ":" + desc);
//            if (code == AVChatDeviceEvent.VIDEO_CAMERA_OPEN_ERROR) {
//                mAVChatController.reCreateCameraCapturer();
//            }
        }

        @Override
        public void onFirstVideoFrameAvailable(String account) {
            super.onFirstVideoFrameAvailable(account);
//            mAVChatController.getVideoCapturer().setZoom(10);
        }

        @Override
        public void onSessionStats(AVChatSessionStats sessionStats) {
            mSessionDuration = sessionStats.sessionDuration;
            if (ControlCenter.getDoorbellManager().getDoorbellConfig().getVideoConfig()
                    .getVideoTimeLimit()
                    * 1000 <= sessionStats.sessionDuration) {
                if (mAVChatController != null) { //界面销毁时强制尝试挂断
                    try {
                        mAVChatController.leaveRoom();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                stopSelf();
            }
        }

        @Override
        public void onUserJoined(String account) {
            if (mAVChatController == null) return;
            L.e("---------------onUserJoined");
//            if (TextUtils.isEmpty(account)) {
//                mAVChatController.leaveRoom();
//                stopSelf();
//            }
            boolean hasMultipleCameras = AVChatCameraCapturer.hasMultipleCameras();
            if (hasMultipleCameras) {
                AVChatManager.getInstance().sendControlCommand(AVChatManager.getInstance()
                        .getCurrentChatId(), IS_DUAL_CAMERA, null);
            }
            AVChatProfile.getInstance().setChattingAccount(account);
            AVChatAction avChatAction = new AVChatAction();
            avChatAction.setType(AVChatAction.AVCHAT_USER_JOIN);
            avChatAction.putExtra(Constant.FROM_ACCOUNT, account);
            EventBus.getDefault().post(avChatAction);
        }

        @Override
        public void onUserLeave(String account, int event) {
            if (mAVChatController == null) return;
            L.e("---------------onUserLeave");
            mAVChatController.leaveRoom();
//            mAVChatController.hangUp(AVChatExitCode.HANGUP);
            stopSelf();
        }

        //音视频连接建立，会回调
        @Override
        public void onCallEstablished() {
//            //移除超时监听
            L.e("---------------onCallEstablished");
            AVChatTimeoutObserver.getInstance().observeTimeoutNotification(mTimeoutObserver,
                    false, true);

        }

        @Override
        public boolean onVideoFrameFilter(AVChatVideoFrame frame, boolean maybeDualInput) {
            return true;
        }

        @Override
        public boolean onAudioFrameFilter(AVChatAudioFrame frame) {
            return true;
        }
    };
    // 通话过程中，收到对方挂断电话
    Observer<AVChatCommonEvent> mCallHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {
            if (mAVChatController == null) return;
            L.e("---------------通话过程中，收到对方挂断电话");
//            mAvChatData = mAVChatController.getAvChatData();
//            if (mAvChatData != null && mAvChatData.getChatId() == avChatHangUpInfo.getChatId()) {
//                mAVChatController.onHangUp(AVChatExitCode.HANGUP);
//            }

        }
    };
    Observer<Integer> mTimeoutObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer integer) {
            if (mAVChatController == null) return;
            L.e("---------------mTimeoutObserver");
//            mAVChatController.hangUp(AVChatExitCode.CANCEL);
            mAVChatController.leaveRoom();
            stopSelf();
        }
    };
    // 监听音视频模式切换通知, 对方音视频开关通知
    Observer<AVChatControlEvent> mCallControlObserver = new Observer<AVChatControlEvent>() {
        @Override
        public void onEvent(AVChatControlEvent netCallControlNotification) {
            handleCallControl(netCallControlNotification);
        }
    };
    Observer<Integer> mAutohangupforlocalphoneobserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer integer) {
            if (mAVChatController == null) return;
            mAVChatController.leaveRoom();
        }
    };

    // 处理音视频切换请求和对方音视频开关通知
    private void handleCallControl(AVChatControlEvent notification) {
        if (AVChatManager.getInstance().getCurrentChatId() != notification.getChatId()) {
            return;
        }
        if (mAVChatController == null) return;
        if (SWITCH_CAMERA == notification.getControlCommand()) {
            mAVChatController.switchCamera();
            AVChatManager.getInstance().sendControlCommand(AVChatManager.getInstance()
                    .getCurrentChatId(), SWITCH_CAMERA, null);
            if (AVChatManager.getInstance().isLocalVideoMuted()) {
                AVChatManager.getInstance().muteLocalVideo(false);
            }
        } else if (UNLOCK == notification.getControlCommand()) {
            //解锁
            ControlCenter.getBCManager().setLock(true);
            AVChatManager.getInstance().sendControlCommand(AVChatManager.getInstance()
                    .getCurrentChatId(), UNLOCK, null);
        }
    }

}
