package cn.jcyh.peephole.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatUserInfoEvent;
import com.bairuitech.anychat.AnyChatVideoCallEvent;

import java.util.Timer;
import java.util.TimerTask;

import cn.jcyh.peephole.R;

/**
 * Created by jogger on 2018/2/2.
 */

public class VideoService extends Service implements AnyChatBaseEvent,
        View.OnClickListener, AnyChatVideoCallEvent, AnyChatUserInfoEvent {
    private FrameLayout fl_surface_container;
    private WindowManager.LayoutParams mParams;
    private SurfaceView mSurfaceView;
    private AnyChatCoreSDK anychat;
    private Handler mHandler;
    private Timer mTimerCheckAv;
    private Timer mTimerShowVideoTime;
    private TimerTask mTimerTask;
    boolean bSelfVideoOpened = false;
    public static final int MSG_CHECKAV = 1;
    public static final int MSG_TIMEUPDATE = 2;
    public static final int PROGRESSBAR_HEIGHT = 5;
    private WindowManager mWindowManager;
    private static final String TAG = "VideoService";
    private int mRoomId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "---------->onCreate");
        initSdk();
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
        mParams.width = 300;
        mParams.height = 300;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        fl_surface_container = (FrameLayout) inflater.inflate(R.layout.video_float, null);
        //添加toucherlayout
        mWindowManager.addView(fl_surface_container, mParams);
        Log.e("TAG", "------------------>创建window");
        Log.i(TAG, "toucherlayout-->left:" + fl_surface_container.getLeft());
        Log.i(TAG, "toucherlayout-->right:" + fl_surface_container.getRight());
        Log.i(TAG, "toucherlayout-->top:" + fl_surface_container.getTop());
        Log.i(TAG, "toucherlayout-->bottom:" + fl_surface_container.getBottom());
        mSurfaceView = (SurfaceView) fl_surface_container.findViewById(R.id.surface_local);
        if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            mSurfaceView.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
            Log.i("ANYCHAT", "VIDEOCAPTRUE---" + "JAVA");
        }
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            if (AnyChatCoreSDK.mCameraHelper.GetCameraNumber() > 1) {
                // 默认打开前置摄像头
                AnyChatCoreSDK.mCameraHelper.SelectVideoCapture(AnyChatCoreSDK.mCameraHelper.CAMERA_FACING_FRONT);
            }
        } else {
            String[] strVideoCaptures = anychat.EnumVideoCapture();
            if (strVideoCaptures != null && strVideoCaptures.length > 1) {
                // 默认打开前置摄像头
                for (int i = 0; i < strVideoCaptures.length; i++) {
                    String strDevices = strVideoCaptures[i];
                    if (strDevices.indexOf("Front") >= 0) {
                        anychat.SelectVideoCapture(strDevices);
                        break;
                    }
                }
            }
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_CHECKAV:
                        CheckVideoStatus();
                        break;
                    case MSG_TIMEUPDATE:
                        break;
                }

            }
        };
        initTimerCheckAv();
        initTimerShowTime();


        //主动计算出当前View的宽高信息.
        fl_surface_container.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//
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
        }
        if (mRoomId != -1) {
            anychat.EnterRoom(mRoomId, "");
        }
        return START_STICKY;

    }

    public void adjustLocalVideo(boolean bLandScape) {
        float width;
        float height = 0;
        DisplayMetrics dMetrics = new DisplayMetrics();
        width = (float) dMetrics.widthPixels / 4;
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) fl_surface_container
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
        layoutParams.width = (int) 100;
        layoutParams.height = (int) 100;
        fl_surface_container.setLayoutParams(layoutParams);
    }

    private void initSdk() {
        if (anychat == null)
            anychat = AnyChatCoreSDK.getInstance(this);
        anychat.SetBaseEvent(this);
        anychat.SetVideoCallEvent(this);
        anychat.SetUserInfoEvent(this);
        anychat.mSensorHelper.InitSensor(getApplicationContext());
        // 初始化Camera上下文句柄
        AnyChatCoreSDK.mCameraHelper.SetContext(getApplicationContext());

    }

    // 判断视频是否已打开
    private void CheckVideoStatus() {
        Log.e(TAG, "---------->GetCameraState" + anychat.GetCameraState(-1) + "--->" + anychat.GetUserVideoWidth(-1));
        if (!bSelfVideoOpened) {
            if (anychat.GetCameraState(-1) == 2 && anychat.GetUserVideoWidth(-1) != 0) {
                if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) != AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
                    mSurfaceView.getHolder().setFormat(PixelFormat.RGB_565);
                    mSurfaceView.getHolder().setFixedSize(anychat.GetUserVideoWidth(-1), anychat.GetUserVideoHeight(-1));
                }
                Surface s = mSurfaceView.getHolder().getSurface();
                anychat.SetVideoPos(-1, s, 0, 0, 0, 0);
                bSelfVideoOpened = true;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        anychat.UserCameraControl(-1, 0);
        anychat.UserSpeakControl(-1, 0);
        mTimerCheckAv.cancel();
        mTimerShowVideoTime.cancel();
        anychat.LeaveRoom(-1);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void OnAnyChatVideoCallEvent(int dwEventType, int dwUserId, int dwErrorCode, int dwFlags, int dwParam, String userStr) {

    }

    @Override
    public void OnAnyChatConnectMessage(boolean bSuccess) {

    }

    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {

    }

    @Override
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
        Log.e(TAG, "-------------OnAnyChatEnterRoomMessage");
        if (dwErrorCode == 0) {
            anychat.UserCameraControl(-1, 1);
            anychat.UserSpeakControl(-1, 1);
            bSelfVideoOpened = false;
        }
    }

    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
        Log.e(TAG, "-------------OnAnyChatOnlineUserMessage");
    }

    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        Log.e(TAG, "-------------OnAnyChatUserAtRoomMessage");
    }

    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        Log.e(TAG, "-------------OnAnyChatLinkCloseMessage");
    }

    @Override
    public void OnAnyChatUserInfoUpdate(int dwUserId, int dwType) {

    }

    @Override
    public void OnAnyChatFriendStatus(int dwUserId, int dwStatus) {

    }

//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
//        mSurfaceHolder = holder;
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
//        mSurfaceHolder = holder;
//        Log.i("process", Thread.currentThread().getName());
//        //视频通话
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        // surfaceDestroyed的时候同时对象设置为null
//        mSurfaceView = null;
//        mSurfaceHolder = null;
//    }

    private void initTimerCheckAv() {
        if (mTimerCheckAv == null)
            mTimerCheckAv = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mHandler.sendEmptyMessage(MSG_CHECKAV);
            }
        };
        mTimerCheckAv.schedule(mTimerTask, 1000, 100);
    }

    private void initTimerShowTime() {
        if (mTimerShowVideoTime == null)
            mTimerShowVideoTime = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mHandler.sendEmptyMessage(MSG_TIMEUPDATE);
            }
        };
        mTimerShowVideoTime.schedule(mTimerTask, 100, 1000);
    }
}
