package cn.jcyh.peephole;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.jcyh.peephole.adapter.MainPageAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.User;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.HttpUrlIble;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.service.KeepBackRemoteService;
import cn.jcyh.peephole.service.VideoService;
import cn.jcyh.peephole.ui.activity.PictureActivity;
import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_BASE_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_USER_INFO_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_DOORBELL_SYSTEM_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_ENTER_ROOM;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_FRIEND_STATUS;
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
    private static final int REQEUST_CAPTURE_RING = 0x001;
    private static final int REQEUST_CAPTURE_ALARM = 0x002;
    private MyReceiver mReceiver;
    private DoorBellControlCenter mControlCenter;
    private int mRoomId;
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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ANYCHAT_BASE_EVENT);
        intentFilter.addAction(ACTION_ANYCHAT_USER_INFO_EVENT);
        intentFilter.addAction(ACTION_ANYCHAT_VIDEO_CALL_EVENT);
        intentFilter.addAction(ACTION_DOORBELL_SYSTEM_EVENT);
        intentFilter.addAction(ACTION_ANYCHAT_TRANS_DATA_EVENT);
        registerReceiver(mReceiver, intentFilter);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels=displayMetrics.widthPixels;
        Timber.e("---->h:"+heightPixels+"---w:"+widthPixels);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
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
//  mControlCenter.sendVideoCall();
                        startNewActivityForResult(PictureActivity.class, REQEUST_CAPTURE_RING);
                    } else if (TYPE_DOORBELL_SYSTEM_ALARM.equals(type)) {
                        startNewActivityForResult(PictureActivity.class, REQEUST_CAPTURE_ALARM);
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
//                    dealTansDataEvent(intent);
                    break;
            }
        }
    }

    /**
     * 处理文件传输事件
     */
//    private void dealTansDataEvent(Intent intent) {
//        String type = intent.getStringExtra("type");
//        if (TYPE_ANYCHAT_TRANS_BUFFER.equals(type)) {
//            CommandJson command = intent.getParcelableExtra("command");
//            int dwUserid = intent.getIntExtra("dwUserid", -1);
//            switch (command.getCommandType()) {
//                case CommandJson.CommandType.UNLOCK_DOORBELL_REQUEST:
//                    ToastUtil.showToast(getApplicationContext(), "执行解锁操作");
//                    command.setCommandType(CommandJson.CommandType.UNLOCK_DOORBELL_RESPONSE);
//                    command.setCommand("success");
//                    mControlCenter.sendUnlockResponse(dwUserid, command);
//                    break;
//                case CommandJson.CommandType.DOORBELL_CALL_IMG_REQUEST:
//                    //视频呼叫图片请求
//                    Timber.e("----------视频呼叫图片请求" + dwUserid + "---filepath:" + mFilePath);
//                    mControlCenter.sendVideoCallImg(dwUserid, mFilePath);
//                    break;
//                case CommandJson.CommandType.UNBIND_DOORBELL_COMPLETED:
//                    // TODO: 2018/2/26 有用户解绑成功
//                    Timber.e("---------收到用户解绑");
//                    break;
//            }
//        } else if (TYPE_ANYCHAT_TRANS_FILE.equals(type)) {
//
//        }
//    }

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
                if (DoorBellControlCenter.sIsBinding) {
                    //正在绑定中，不能会话
                    mControlCenter.rejectVideoCall(dwUserId);
                } else {
                    //猫眼端未打开摄像头/未在通话中，则接受请求
                    mControlCenter.acceptVideoCall(dwUserId);
                }
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
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.e("-------------onActivityResult" + resultCode + "---" + requestCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQEUST_CAPTURE_RING) {
                //获取拍照的图片
                mFilePath = data.getStringExtra("filePath");
                Map<String, Object> params = new HashMap<>();
                params.put("sn", IMEI);
                params.put("type", 1);
                HttpAction.getHttpAction(this).sendPostImg(HttpUrlIble.UPLOAD_DOORBELL_ALARM_URL,
                        mFilePath, params, null);
                HttpAction.getHttpAction(this).getBindUsers(IMEI, new IDataListener<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        if (users != null && users.size() != 0) {
                            //通知用户
                            mControlCenter.sendVideoCall(users, requestCode,mFilePath);
                        }
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        Timber.e("------errorCode" + errorCode);
                    }
                });
//                mControlCenter.sendVideoCall();
            } else if (requestCode == REQEUST_CAPTURE_ALARM) {

            }
        }
    }
}
