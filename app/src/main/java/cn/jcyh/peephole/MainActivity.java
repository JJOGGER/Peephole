package cn.jcyh.peephole;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;

import com.bairuitech.anychat.AnyChatCoreSDK;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import cn.jcyh.peephole.adapter.MainPageAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.CommandJson;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.service.KeepBackRemoteService;
import cn.jcyh.peephole.service.VideoService;
import cn.jcyh.peephole.ui.activity.PictureActivity;
import cn.jcyh.peephole.utils.ToastUtil;
import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_BASE_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_USER_INFO_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_DOORBELL_SYSTEM_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_ENTER_ROOM;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_FRIEND_STATUS;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_TRANS_BUFFER;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_TRANS_FILE;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_USER_INFO_UPDATE;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_FINISH;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_REPLY;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_REQUEST;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_START;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_DOORBELL_SYSTEM_ALARM;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_DOORBELL_SYSTEM_RING;

//按门铃，发消息--》app收到消息--》发起视频通话
public class MainActivity extends BaseActivity {
    @BindView(R.id.vp_main)
    ViewPager vp_main;
    private static final int REQEUST_CAPTURE = 0x001;
    private MyReceiver mReceiver;
    private DoorBellControlCenter mControlCenter;
    private int mRoomId;
    public static final int MSG_CHECKAV = 1;
    boolean bOtherVideoOpened = false;
    private MyHandler mHandler;
    private boolean mIsCheckAv;
    private AnyChatCoreSDK mAnyChat;
    private String mFilePath;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        mControlCenter = DoorBellControlCenter.getInstance(this);
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
        intentFilter.addAction(ACTION_DOORBELL_SYSTEM_EVENT);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsCheckAv = false;
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
                Timber.e("-------------->" + mAnyChat.GetCameraState(-1) + "---" + mAnyChat
                        .GetUserVideoWidth(-1));
                if (mAnyChat.GetCameraState(-1) == 2
                        && mAnyChat.GetUserVideoWidth(-1) != 0) {
                    bOtherVideoOpened = true;
                    mIsCheckAv = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isFinishing() || getSupportFragmentManager() == null || intent.getAction() == null)
                return;
            switch (intent.getAction()) {
                case ACTION_DOORBELL_SYSTEM_EVENT:
                    String type = intent.getStringExtra("type");
                    if (TYPE_DOORBELL_SYSTEM_RING.equals(type)) {
                        // TODO: 2018/2/4 获取绑定猫眼的用户列表
//                    mControlCenter.sendVideoCall();
                        startNewActivityForResult(PictureActivity.class, REQEUST_CAPTURE);
                    } else if (TYPE_DOORBELL_SYSTEM_ALARM.equals(type)) {

                    }
                    break;
                case ACTION_ANYCHAT_USER_INFO_EVENT:
                    type = intent.getStringExtra("type");

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
                case ACTION_ANYCHAT_TRANS_DATA_EVENT:
                    dealTansDataEvent(intent);
                    break;
            }
        }
    }

    /**
     * 处理文件传输事件
     */
    private void dealTansDataEvent(Intent intent) {
        String type = intent.getStringExtra("type");
        if (TYPE_ANYCHAT_TRANS_BUFFER.equals(type)) {
            CommandJson command = intent.getParcelableExtra("command");
            int dwUserid = intent.getIntExtra("dwUserid", -1);
            switch (command.getCommandType()) {
                case CommandJson.CommandType.UNLOCK_DOORBELL_REQUEST:
                    ToastUtil.showToast(getApplicationContext(), "执行解锁操作");
                    command.setCommandType(CommandJson.CommandType.UNLOCK_DOORBELL_RESPONSE);
                    command.setCommand("success");
                    mControlCenter.sendUnlockResponse(dwUserid, command);
                    break;
                case CommandJson.CommandType.DOORBELL_CALL_IMG_REQUEST:
                    //视频呼叫图片请求
                    mControlCenter.sendVideoCallImg(dwUserid, mFilePath);
                    break;
            }
        } else if (TYPE_ANYCHAT_TRANS_FILE.equals(type)) {

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
            case TYPE_BRAC_VIDEOCALL_EVENT_REPLY:// < 呼叫请求回复

                break;
            case TYPE_BRAC_VIDEOCALL_EVENT_START:// 视频呼叫会话开始事件
                Timber.e("--------->开始进入会话窗口");
                mRoomId = dwParam;
                Intent videoIntent = new Intent(MainActivity.this, VideoService.class);
                videoIntent.putExtra("roomId", dwParam);
                videoIntent.putExtra("userId", dwUserId);
                startService(videoIntent);
                break;
            case TYPE_BRAC_VIDEOCALL_EVENT_FINISH:// < 挂断（结束）呼叫会话
//                        DialogFactory.getDialogFactory().dismiss();
                Timber.e("--------结束通话");
                stopService(new Intent(this, VideoService.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQEUST_CAPTURE) {
                //获取拍照的图片
                mFilePath = data.getStringExtra("filePath");
                // TODO: 2018/2/4 获取绑定猫眼的用户列表
//                mControlCenter.sendVideoCall();
            }
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
