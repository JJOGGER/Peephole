package cn.jcyh.peephole.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.control.BcManager;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.ToastUtil;
import timber.log.Timber;

public class VideoServiceActivity extends BaseActivity {
    @BindView(R.id.tv_state)
    TextView tvState;
    @BindView(R.id.tv_device_number)
    TextView tvDeviceNumber;
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
            if (DoorBellControlCenter.sIsVideo && DoorBellControlCenter.sCurrentVideoUser != null) {
                tvState.setText(String.format(getString(R.string.video_with_user_format), DoorBellControlCenter.sCurrentVideoUser.getAccount()));
            } else
                tvState.setText(R.string.ready_connect);
        } else {
            tvState.setText(R.string.connecting);
        }
        mControlCenter = DoorBellControlCenter.getInstance();
        mReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter(ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT);
        intentFilter.addAction(ConstantUtil.ACTION_ANYCHAT_BASE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.e("---------------onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @OnClick({R.id.ibtn_menu, R.id.btn_exit})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_menu:
                startNewActivity(VideoMenuActivity.class);
                break;
            case R.id.btn_exit:
                ToastUtil.showToast(getApplicationContext(), "开锁");
                BcManager.getManager(this).setLock(!BcManager.getManager(this).getLockStatus());
                boolean lockStatus = BcManager.getManager(this).getLockStatus();
                Timber.e("--------lockStatus:" + lockStatus);
                BcManager.getManager(this).setInfraredLightPowerOn(!BcManager.getManager(this).getInfraredLightStatus());
//                if (DoorBellControlCenter.sIsVideo) {
//                    mControlCenter.finishVideoCall(-1, DoorBellControlCenter.sCurrentVideoUser.getAid());
//                }
//                finish();
                break;
        }
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isFinishing() || getSupportFragmentManager() == null) return;
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) return;
            String type = intent.getStringExtra("type");
            switch (action) {
                case ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT:
                    Timber.e("---------ACTION_ANYCHAT_VIDEO_CALL_EVENT");
                    if (ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_START.equals(type)) {
                        if (!TextUtils.isEmpty(DoorBellControlCenter.sCurrentVideoUser.getAccount())) {
                            tvState.setText(String.format(getString(R.string.video_with_user_format), DoorBellControlCenter.sCurrentVideoUser.getAccount()));
                        }
                    } else if (ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_FINISH.equals(type)) {
                        if (DoorBellControlCenter.sIsAnychatLogin)
                            tvState.setText(R.string.ready_connect);
                        else
                            tvState.setText(getText(R.string.connecting));
                    }
                    break;
                case ConstantUtil.ACTION_ANYCHAT_BASE_EVENT:
                    if (ConstantUtil.TYPE_ANYCHAT_LOGIN_STATE.equals(type)) {
                        int errorCode = intent.getIntExtra("dwErrorCode", -1);
                        if (errorCode > 0) {
                            tvState.setText(getText(R.string.ready_connect));
                        } else {
                            tvState.setText(getText(R.string.connecting));
                        }
                    } else if (ConstantUtil.TYPE_ANYCHAT_LINK_CLOSE.equals(type)) {
                        tvState.setText(getText(R.string.connecting));
                    }
                    break;
            }
        }
    }
}
