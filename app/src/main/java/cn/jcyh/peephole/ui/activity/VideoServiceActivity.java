package cn.jcyh.peephole.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.widget.TextView;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.utils.ConstantUtil;

public class VideoServiceActivity extends BaseActivity {
    @BindView(R.id.tv_state)
    TextView tv_state;
    @BindView(R.id.tv_device_number)
    TextView tvDeviceNumber;
    private int mRoomId;
    private int mUserId;
    private MyReceiver mReceiver;

    private DoorBellControlCenter mControlCenter;


    @Override
    public int getLayoutId() {
        return R.layout.activity_video_service;
    }

    @Override
    protected void init() {
        tvDeviceNumber.setText(String.format(getString(R.string.device_no_), IMEI));
        if (DoorBellControlCenter.sIsAnychatLogin) {
            if (DoorBellControlCenter.sIsVideo)
                tv_state.setText("正在与" + mUserId + "通话中");
            else
                tv_state.setText(R.string.ready_connect);
        } else {
            tv_state.setText(R.string.connecting);
        }
        mControlCenter = DoorBellControlCenter.getInstance(this);
        mReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter(ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) return;
            String type = intent.getStringExtra("type");
            switch (action) {
                case ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT:
                    if (ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_START.equals(type)) {
                        // TODO: 2018/4/27 猫眼需把用户列表数据存储下来，绑定和解绑时需更新，在此可取得手机号并显示
                    } else if (ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_FINISH.equals(type)) {
                        tv_state.setText(getText(R.string.connecting));
                    }
                    break;
                case ConstantUtil.ACTION_ANYCHAT_BASE_EVENT:
                    if (ConstantUtil.TYPE_ANYCHAT_LOGIN_STATE.equals(type)) {
                        int errorCode = intent.getIntExtra("dwErrorCode", -1);
                        if (errorCode > 0) {
                            tv_state.setText(getText(R.string.ready_connect));
                        } else {
                            tv_state.setText(getText(R.string.connecting));
                        }
                    } else if (ConstantUtil.TYPE_ANYCHAT_LINK_CLOSE.equals(type)) {
                        tv_state.setText(getText(R.string.connecting));
                    }
                    break;
            }
        }
    }
}
