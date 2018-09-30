package cn.jcyh.peephole.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

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

public class AVChatService extends Service {
    public static final int CURRENT_AUDIO_VOLUME = 1;
    private AVChatController mAVChatController;
    private AVChatData mAvChatData;
    private static final byte IS_DUAL_CAMERA = AVChatControlCommand.NOTIFY_CUSTOM_BASE + 1;
    private static final byte SWITCH_CAMERA = AVChatControlCommand.NOTIFY_CUSTOM_BASE + 2;//切换摄像头
    private static final byte UNLOCK = AVChatControlCommand.NOTIFY_CUSTOM_BASE + 3;
    private int mMonitorSwitch;
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {

            AudioManager audioManager = (AudioManager) Util.getApp().getSystemService(Context.AUDIO_SERVICE);
            assert audioManager != null;
            int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            L.i("streamVolume:" + streamVolume);
            if (streamVolume != CURRENT_AUDIO_VOLUME) {
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, CURRENT_AUDIO_VOLUME, 0);
            }
            streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            L.i("streamVolume:" + streamVolume);

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
        mTimer.schedule(mTimerTask, 0, 1000);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAVChatController != null) { //界面销毁时强制尝试挂断
                    try {
                        mAVChatController.hangUp(AVChatExitCode.HANGUP);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                stopSelf();
            }
        }, ControlCenter.getDoorbellManager().getDoorbellConfig().getVideoConfig().getVideoTimeLimit() * 1000);

        L.e("-----------------onCreate");
        //通话过程暂时关闭停留报警
        mMonitorSwitch = ControlCenter.getDoorbellManager().getDoorbellConfig().getMonitorSwitch();
        if (mMonitorSwitch == 1) {
            ControlCenter.getBCManager().setPIRSensorOn(false);
        }
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
        AVChatTimeoutObserver.getInstance().observeTimeoutNotification(mTimeoutObserver, register, true);
//        AVChatManager.getInstance().observeOnlineAckNotification(onlineAckObserver, register);
        PhoneCallStateObserver.getInstance().observeAutoHangUpForLocalPhone(mAutohangupforlocalphoneobserver, register);
//        //放到所有UI的基类里面注册，所有的UI实现onKickOut接口
//        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
        ControlCenter.getBCManager().setMainSpeakerOn(!register);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        mAvChatData = (AVChatData) intent.getSerializableExtra(Constant.AVCHAT_DATA);
        mAVChatController = new AVChatController(this, mAvChatData);

        //接听来电
        mAVChatController.receive(new AVChatControllerCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            public void onFailed(int code, String errorMsg) {
                stopSelf();
            }
        });
        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAVChatController != null) { //界面销毁时强制尝试挂断
            try {
                mAVChatController.hangUp(AVChatExitCode.HANGUP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mMonitorSwitch == 1) {
            ControlCenter.getBCManager().setPIRSensorOn(true);
        }
        registerObserves(false);
        AVChatAction avChatAction = new AVChatAction();
        avChatAction.setType(AVChatAction.AVCHAT_HANG_UP);
        EventBus.getDefault().post(avChatAction);
        AVChatProfile.getInstance().setAVChatting(false);
        ControlCenter.getDoorbellManager().setLastVideoTime(System.currentTimeMillis());
        L.e("----------------onDestroy");
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
            L.e("---------------zoom:" + mAVChatController.getVideoCapturer().getCurrentZoom() + ":" + mAVChatController.getVideoCapturer().getMaxZoom());
        }

        @Override
        public void onSessionStats(AVChatSessionStats sessionStats) {
            if (ControlCenter.getDoorbellManager().getDoorbellConfig().getVideoConfig().getVideoTimeLimit() * 1000 <= sessionStats.sessionDuration) {
                if (mAVChatController != null) { //界面销毁时强制尝试挂断
                    try {
                        mAVChatController.hangUp(AVChatExitCode.HANGUP);
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
            if (TextUtils.isEmpty(account)) {
                mAVChatController.hangUp(AVChatExitCode.HANGUP);
                stopSelf();
            }
            boolean hasMultipleCameras = AVChatCameraCapturer.hasMultipleCameras();
            if (hasMultipleCameras) {
                AVChatManager.getInstance().sendControlCommand(AVChatManager.getInstance().getCurrentChatId(), IS_DUAL_CAMERA, null);
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
            mAVChatController.hangUp(AVChatExitCode.HANGUP);
            stopSelf();
        }

        //音视频连接建立，会回调
        @Override
        public void onCallEstablished() {
//            //移除超时监听
            AVChatTimeoutObserver.getInstance().observeTimeoutNotification(mTimeoutObserver, false, true);

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
            mAvChatData = mAVChatController.getAvChatData();
            if (mAvChatData != null && mAvChatData.getChatId() == avChatHangUpInfo.getChatId()) {
                mAVChatController.onHangUp(AVChatExitCode.HANGUP);
            }

        }
    };
    Observer<Integer> mTimeoutObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer integer) {
            if (mAVChatController == null) return;
            mAVChatController.hangUp(AVChatExitCode.CANCEL);
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
            mAVChatController.onHangUp(AVChatExitCode.PEER_BUSY);
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
            AVChatManager.getInstance().sendControlCommand(AVChatManager.getInstance().getCurrentChatId(), SWITCH_CAMERA, null);
//            AVChatManager.getInstance().enableVideo();
//            AVChatManager.getInstance().startVideoPreview();
            if (AVChatManager.getInstance().isLocalVideoMuted()) {
//                AVChatManager.getInstance().enableVideo();
//                AVChatManager.getInstance().startVideoPreview();
                AVChatManager.getInstance().muteLocalVideo(false);
            }
        } else if (UNLOCK == notification.getControlCommand()) {
            //解锁
            ControlCenter.getBCManager().setLock(true);
            AVChatManager.getInstance().sendControlCommand(AVChatManager.getInstance().getCurrentChatId(), UNLOCK, null);
        }
    }


//    private void createToucher() {
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        mWindowManager = (WindowManager) Util.getApp().getSystemService(WINDOW_SERVICE);
//        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;//TYPE_SYSTEM_ALERT
//        params.format = PixelFormat.RGBA_8888;
//        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        //设置窗口初始停靠位置.
//        params.gravity = Gravity.LEFT | Gravity.TOP;
//        params.x = 110;
//        params.y = 110;
//
//        //设置悬浮窗口长宽数据.
//        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
//        //如果你想完全对应布局设置，需要先获取到机器的dpi
//        //px与dp的换算为px = dp * (dpi / 160).
//        params.width = 110;
//        params.height = 110;
//
//        LayoutInflater inflater = LayoutInflater.from(getApplication());
//        //获取浮动窗口视图所在布局.
//        flSurfaceContainer = (FrameLayout) inflater.inflate(R.layout.video_float, null);
//        //添加toucherlayout
//        mWindowManager.addView(flSurfaceContainer, params);
//        mSurfaceView = (SurfaceView) flSurfaceContainer.findViewById(R.id.surface_local);
////        mDoorbellVideoHelper.initView(this, mSurfaceView);
////        initTimerCheckAv();
////        // 根据屏幕方向改变本地surfaceview的宽高比
//        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) flSurfaceContainer
//                .getLayoutParams();
//        layoutParams.width = 110;
//        layoutParams.height = 110;
//        flSurfaceContainer.setLayoutParams(layoutParams);

//
//    }
}
