package cn.jcyh.peephole.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;

import java.lang.ref.WeakReference;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.control.DoorbellVideoHelper;
import cn.jcyh.peephole.utils.ConstantUtil;
import timber.log.Timber;

/**
 * Created by jogger on 2018/2/2.
 */

public class VideoService extends Service {
    private FrameLayout flSurfaceContainer;
    private WindowManager.LayoutParams mParams;
    private SurfaceView mSurfaceView;
    private AnyChatCoreSDK mAnychat;
    boolean bSelfVideoOpened = false;
    public static final int MSG_CHECKAV = 1;
    public static final int MSG_TIMEUPDATE = 2;
    public static final int PROGRESSBAR_HEIGHT = 5;
    private WindowManager mWindowManager;
    private static final String TAG = "VideoService";
    private int mRoomId;
    private int mUserId;
    private MyReceiver mReceiver;
    private DoorBellControlCenter mControlCenter;
    private DoorbellVideoHelper mDoorbellVideoHelper;
    private boolean mIsCheckAv;
    private MyHandler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DoorBellControlCenter.sIsVideo = true;
        // 初始化Camera上下文句柄
        AnyChatCoreSDK.mCameraHelper.SetContext(getApplicationContext());
        mAnychat = AnyChatCoreSDK.getInstance(this);
        mControlCenter = DoorBellControlCenter.getInstance(this);
        mDoorbellVideoHelper = mControlCenter.getDoorbellVideoHelper();
        mReceiver = new MyReceiver();
        mHandler = new MyHandler(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT);
        intentFilter.addAction(ConstantUtil.ACTION_ANYCHAT_BASE_EVENT);
        intentFilter.addAction(ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intentFilter.addAction(ConstantUtil.ACTION_ANYCHAT_RECORD_EVENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, intentFilter);
        createToucher();

    }

    private void createToucher() {
        mParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置窗口初始停靠位置.
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.x = 10;
        mParams.y = 10;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        mParams.width = 1;
        mParams.height = 1;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        flSurfaceContainer = (FrameLayout) inflater.inflate(R.layout.video_float, null);
        //添加toucherlayout
        mWindowManager.addView(flSurfaceContainer, mParams);
        mSurfaceView = (SurfaceView) flSurfaceContainer.findViewById(R.id.surface_local);
        mDoorbellVideoHelper.initView(this, mSurfaceView);
        initTimerCheckAv();
//        // 根据屏幕方向改变本地surfaceview的宽高比
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            adjustLocalVideo(true);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            adjustLocalVideo(false);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mRoomId = intent.getIntExtra("roomId", -1);
            mUserId = intent.getIntExtra("userId", -1);
        }
        if (mRoomId != -1) {
            mControlCenter.enterRoom(mRoomId, "");
        }
        return START_STICKY;

    }

    public void adjustLocalVideo(boolean bLandScape) {
        float width;
        float height = 0;
        DisplayMetrics dMetrics = new DisplayMetrics();
        width = (float) dMetrics.widthPixels / 4;
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) flSurfaceContainer
                .getLayoutParams();
        if (bLandScape) {

            if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL) != 0)
                height = width * AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL)
                        / AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL)
                        + PROGRESSBAR_HEIGHT;
            else
                height = (float) 3 / 4 * width + PROGRESSBAR_HEIGHT;
        } else {

            if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL) != 0)
                height = width * AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL)
                        / AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL)
                        + PROGRESSBAR_HEIGHT;
            else
                height = (float) 4 / 3 * width + PROGRESSBAR_HEIGHT;
        }
        layoutParams.width = (int) width;
        layoutParams.height = (int) height;
        flSurfaceContainer.setLayoutParams(layoutParams);
    }

    // 判断视频是否已打开
    private void checkVideoStatus() {
        Log.e(TAG, "---------->GetCameraState" + mAnychat.GetCameraState(-1) + "--->" + mAnychat.GetUserVideoWidth(-1));
        if (!bSelfVideoOpened) {
            if (mDoorbellVideoHelper.isVideoOpen()) {
                mDoorbellVideoHelper.setVideoHolder(mSurfaceView);
                bSelfVideoOpened = true;
            }
        } else {
            mIsCheckAv = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DoorBellControlCenter.sIsVideo = false;
        mDoorbellVideoHelper.userCameraControl(-1, 0);
        mDoorbellVideoHelper.userSpeakControl(-1, 0);
        mIsCheckAv = false;
        if (flSurfaceContainer != null) {
            mWindowManager.removeView(flSurfaceContainer);
        }
        mControlCenter.leaveRoom(-1);
        unregisterReceiver(mReceiver);
    }


    private void initTimerCheckAv() {
        mIsCheckAv = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsCheckAv) {
                    mHandler.sendEmptyMessage(MSG_CHECKAV);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

//    private void initTimerShowTime() {
//        if (mTimerShowVideoTime == null)
//            mTimerShowVideoTime = new Timer();
//        mTimerTask = new TimerTask() {
//
//            @Override
//            public void run() {
//                mHandler.sendEmptyMessage(MSG_TIMEUPDATE);
//            }
//        };
//        mTimerShowVideoTime.schedule(mTimerTask, 100, 1000);
//    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            switch (intent.getAction()) {
                case ConstantUtil.ACTION_ANYCHAT_BASE_EVENT:
                    switch (type) {
                        case ConstantUtil.TYPE_ANYCHAT_ENTER_ROOM:
                            Timber.e("------------进入房间");
                            int dwErrorCode = intent.getIntExtra("dwErrorCode", -1);
                            if (dwErrorCode == 0) {
                                mDoorbellVideoHelper.userCameraControl(-1, 1);
                                mDoorbellVideoHelper.userSpeakControl(-1, 1);
                                mDoorbellVideoHelper.userSpeakControl(mUserId, 1);
                                bSelfVideoOpened = false;
                            }
                            break;
                        case ConstantUtil.TYPE_ANYCHAT_ONLINE_USER:
                            break;
                        case ConstantUtil.TYPE_ANYCHAT_USER_AT_ROOM:
                            break;
                        case ConstantUtil.TYPE_ANYCHAT_LINK_CLOSE:
                            finishVideoCall();
                            break;
                    }
                    break;
                case ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT:
                    if (ConstantUtil.TYPE_ANYCHAT_TRANS_BUFFER.equals(type)) {
                        String result = intent.getStringExtra("result");
                        if ("success".equals(result)) {
//                            ToastUtil.showToast(getApplicationContext(), R.string.open_door_succ);
//                            ibtn_open_door.setImageResource(R.drawable.icon_lock_p);
//                            tvVideoLock.setSelected(true);
//                            lockTime = 0;
//                            if (mLockTimer != null) {
//                                mLockTimer.cancel();
//                                mLockTimer.purge();
//                            }
////                            if (mLockTask != null) {
////                                mLockTask.cancel();
////                            }
//                            mLockTimer = new Timer();
//                            mLockTask = new LockTask();
//                            mLockTimer.schedule(mLockTask, 0, 1000);
                        }
                    }
                    break;
                case ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT:
                    if (ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_FINISH.equals(type)) {
                        Timber.e("------------TYPE_BRAC_VIDEOCALL_EVENT_FINISH");
                        finishVideoCall();
                    }
                    break;
                case ConstantUtil.ACTION_ANYCHAT_RECORD_EVENT:
                    if (ConstantUtil.TYPE_ANYCHAT_RECORD.equals(type)) {
                        String lpFileName = intent.getStringExtra("lpFileName");
                        Uri data = Uri.parse("file://" + lpFileName);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
                    } else if (ConstantUtil.TYPE_ANYCHAT_SNAP_SHOT.equals(type)) {
                        //获取到截屏文件
//                        ToastUtil.showToast(getApplicationContext(), R.string.cut_picture);
                    }
                    break;
            }
        }
    }

    /**
     * 结束视频
     */
    private void finishVideoCall() {
        stopSelf();
    }

    private static class MyHandler extends Handler {
        private WeakReference<VideoService> mService;

        MyHandler(VideoService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            VideoService videoService = mService.get();
            switch (msg.what) {
                case MSG_CHECKAV:
                    if (videoService != null) {
                        videoService.checkVideoStatus();
                    }
                    // videoActivity.updateVolume();
                    break;
//                case MSG_TIMEUPDATE:
//                    //?
//                    break;
            }
        }
    }
}
