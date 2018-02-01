package cn.jcyh.peephole;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import cn.jcyh.peephole.adapter.MainPageAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.service.KeepBackRemoteService;
import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_BASE_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_USER_INFO_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_ENTER_ROOM;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_FRIEND_STATUS;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_USER_INFO_UPDATE;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_FINISH;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_REPLY;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_REQUEST;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_START;

//按门铃，发消息--》app收到消息--》发起视频通话
public class MainActivity extends BaseActivity {
    @BindView(R.id.vp_main)
    ViewPager vp_main;
    private MyReceiver mReceiver;
    private DoorBellControlCenter mControlCenter;
    private int mRoomId;
    public static final int MSG_CHECKAV = 1;
    boolean bOtherVideoOpened = false;
    private MyHandler mHandler;
    int videoIndex = 0;
    private boolean mIsCheckAv;
    private AnyChatCoreSDK mAnyChat;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        mControlCenter = DoorBellControlCenter.getInstance(this);
        // 初始化Camera上下文句柄
        AnyChatCoreSDK.mCameraHelper.SetContext(getApplicationContext());
        startService(new Intent(this, KeepBackRemoteService.class));
        vp_main.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
        vp_main.setOffscreenPageLimit(2);
        mReceiver = new MyReceiver();
        mHandler = new MyHandler(this);
        mAnyChat = AnyChatCoreSDK.getInstance(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ANYCHAT_BASE_EVENT);
        intentFilter.addAction(ACTION_ANYCHAT_USER_INFO_EVENT);
        intentFilter.addAction(ACTION_ANYCHAT_VIDEO_CALL_EVENT);
        registerReceiver(mReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * 检查视频
     */
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

    // 判断视频是否已打开
    private void CheckVideoStatus() {
        try {
            if (!bOtherVideoOpened) {
                Timber.e("-------------->" + mAnyChat.GetCameraState(-1)+"---"+mAnyChat.GetUserVideoWidth(-1));
                if (mAnyChat.GetCameraState(-1) == 2
                        && mAnyChat.GetUserVideoWidth(-1) != 0) {
                    bOtherVideoOpened = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isFinishing() || getSupportFragmentManager() == null)
                return;
            switch (intent.getAction()) {
                case ACTION_ANYCHAT_USER_INFO_EVENT:
                    String type = intent.getStringExtra("type");
                    if (TYPE_ANYCHAT_FRIEND_STATUS.equals(type) || TYPE_ANYCHAT_USER_INFO_UPDATE
                            .equals(type)) {
                        int dwUserId = intent.getIntExtra("dwUserId", 0);
                        int dwStatus = intent.getIntExtra("dwStatus", 0);
                    }
                    break;
                case ACTION_ANYCHAT_VIDEO_CALL_EVENT:
                    dealCallEvent(intent);
                    break;
                case ACTION_ANYCHAT_BASE_EVENT:
                    dealBaseEvent(intent);
                    break;
            }
        }
    }

    /**
     * 处理基本事件
     *
     * @param intent
     */
    private void dealBaseEvent(Intent intent) {
        String type = intent.getStringExtra("type");
        switch (type) {
            case TYPE_ANYCHAT_ENTER_ROOM:
                int dwRoomId = intent.getIntExtra("dwRoomId", -1);
                int dwErrorCode = intent.getIntExtra("dwErrorCode", -1);
                initTimerCheckAv();//检查视频
                if (dwErrorCode == 0) {
                    if (dwRoomId == mRoomId) {
                        Timber.e("-------------打开本地音视频");
                        // 初始化Camera上下文句柄
                        AnyChatCoreSDK.mCameraHelper.SetContext(getApplicationContext());
                        AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION, 1);
                        // 如果是采用Java视频采集，则设置Surface的CallBack
                        // 判断是否显示本地摄像头切换图标
                        if (AnyChatCoreSDK
                                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
                            if (AnyChatCoreSDK.mCameraHelper.GetCameraNumber() > 1) {
                                // 默认打开前置摄像头
                                Timber.e("---------默认打开前置摄像头");
                                AnyChatCoreSDK.mCameraHelper.SelectVideoCapture(AnyChatCoreSDK.mCameraHelper.CAMERA_FACING_BACK);
                            }
                        } else {
                            String[] strVideoCaptures = AnyChatCoreSDK.getInstance(this).EnumVideoCapture();
                            if (strVideoCaptures != null && strVideoCaptures.length > 1) {
                                Timber.e("---------strVideoCaptures");
                                // 默认打开前置摄像头
                                for (int i = 0; i < strVideoCaptures.length; i++) {
                                    String strDevices = strVideoCaptures[i];
                                    if (strDevices.indexOf("Front") >= 0) {
                                        Timber.e("---------Front");
                                        AnyChatCoreSDK.getInstance(this).SelectVideoCapture(strDevices);
                                        break;
                                    }
                                }
                            }
                        }
                        //进入通话房间
                        mControlCenter.userCameraControl(-1, 1);
                        mControlCenter.userSpeakControl(-1, 1);
                        Timber.e("------------state:" + AnyChatCoreSDK.getInstance(this).GetCameraState(-1));
                    } else {
                        Timber.e("-------------dwroomid:" + dwRoomId + "---" + mRoomId);
                    }
                }
                break;
        }
    }

    /**
     * 处理呼叫事件
     *
     * @param intent
     */
    private void dealCallEvent(Intent intent) {
        String type = intent.getStringExtra("type");
        int dwUserId = intent.getIntExtra("dwUserId", 0);
        int dwErrorCode = intent.getIntExtra("dwErrorCode", 0);
        int dwFlags = intent.getIntExtra("dwFlags", 0);
        int dwParam = intent.getIntExtra("dwParam", 0);
        String userStr = intent.getStringExtra("userStr");
        switch (type) {
            case TYPE_BRAC_VIDEOCALL_EVENT_REQUEST:// < 呼叫请求
                Timber.e("----有人发呼叫请求过来了");
                //猫眼端未打开摄像头/未在通话中，则接受请求
                mControlCenter.acceptVideoCall(dwUserId);
                break;
            case TYPE_BRAC_VIDEOCALL_EVENT_REPLY:// < 呼叫请求回复 开始向设备端发送视频请求
//                        mControlCenter.VideoCall_SessionReply(dwUserId,
//                                dwErrorCode, dwFlags, dwParam, userStr);
//                        if (dwErrorCode == VideoCallContrlHandler.ERRORCODE_SUCCESS) {
//                            Timber.e("------------请求得到猫眼回复");
//                            ((CommonProgressDialog) DialogFactory.getDialogFactory().getDialog()).setHintContent(getString(R.string.connecting));
//                        } else if (dwErrorCode == VideoCallContrlHandler.ERRORCODE_SESSION_REFUSE) {
//                            // 目标用户拒绝会话
//                            Timber.i("----目标用户拒绝会话");
//                            DialogFactory.getDialogFactory().dismiss();
//                            if (userStr != null && !userStr.contentEquals("")) {
//                                //设备端在用户绑定房间
//                                Timber.i("------设备端在用户绑定房间");
//                                ToastUtil.showToast(getApplicationContext(), userStr);
//                            } else {
//                                // 设备端拒绝视频即设备端解除了和手机端绑定
//                                // 门铃端解除了绑定，要实现1、更新本地数据库；2、更新服务器数据库
//                                //updataLink(userName);
//                                ToastUtil.showToast(getApplicationContext(), R.string
//                                        .str_returncode_requestrefuse);
//                            }
//
//                        } else if (dwErrorCode == VideoCallContrlHandler.ERRORCODE_SESSION_BUSY) {// 用户忙,
//                            // 两种情况：1、对方正在和别人在通话，2、对方的摄像头正被别应用使用
//                            ToastUtil.showToast(getApplicationContext(), getString(R.string
//                                    .device_busy));
//                            DialogFactory.getDialogFactory().dismiss();
//                        } else {// 超时，网络中断，不在线
//                            ToastUtil.showToast(getApplicationContext(), R.string.connect_fail);
//                            DialogFactory.getDialogFactory().dismiss();
//                        }
                break;
            case TYPE_BRAC_VIDEOCALL_EVENT_START:// 视频呼叫会话开始事件
                Timber.e("--------->开始进入会话窗口");
                mRoomId = dwParam;
                mControlCenter.enterRoom(mRoomId, "");
//                        Bundle bundle = new Bundle();
//                        bundle.putInt("roomId", dwParam);
//                        bundle.putInt("userId", dwUserId);
//                        startNewActivity(VideoServiceActivity.class, bundle);
//                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                            mProgressDialog.dismiss();
//                        }
//                        mControlCenter.VideoCall_SessionStart(DoorBellHomeActivity.this, dwUserId,
//                                dwFlags, dwParam, userStr);
//                        DialogFactory.getDialogFactory().dismiss();
                break;
            case TYPE_BRAC_VIDEOCALL_EVENT_FINISH:// < 挂断（结束）呼叫会话
//                        DialogFactory.getDialogFactory().dismiss();
                Timber.e("--------结束通话");
                mControlCenter.leaveRoom(-1);
                break;
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity videoActivity = mActivity.get();
            switch (msg.what) {
                case MSG_CHECKAV:
                    if (videoActivity != null) {
                        videoActivity.CheckVideoStatus();
                    }
                    // videoActivity.updateVolume();
                    break;
            }
        }
    }
}
