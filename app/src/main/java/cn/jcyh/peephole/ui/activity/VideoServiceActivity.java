package cn.jcyh.peephole.ui.activity;

import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.event.AVChatAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.dialog.CommonEditDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.video.AVChatProfile;

public class VideoServiceActivity extends BaseActivity {
    @BindView(R.id.tv_state)
    TextView tvState;
    @BindView(R.id.tv_device_number)
    TextView tvDeviceNumber;
    @BindView(R.id.tv_device_name)
    TextView tvDeviceName;
    private DialogHelper mNameDialog;
    private DoorbellConfig mDoorbellConfig;

    @Override
    public int getLayoutId() {
        return R.layout.activity_video_service;
    }

    @Override
    protected void init() {
        tvDeviceNumber.setText(String.format(getString(R.string.device_no_), IMEI));
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        if (mDoorbellConfig.getDoorbell() != null && TextUtils.isEmpty(mDoorbellConfig.getDoorbell().getName())) {
            tvDeviceName.setText(R.string.click_update_name);
        } else {
            tvDeviceName.setText(mDoorbellConfig.getDoorbell().getName());
        }
        StatusCode status = NIMClient.getStatus();
        showState(status);
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(mUserStatusObserver, true);
    }

    private void showState(StatusCode status) {
        if (status == StatusCode.LOGINED) {
            if (AVChatProfile.getInstance().isAVChatting()) {
                String chattingAccount = AVChatProfile.getInstance().getChattingAccount();
                User userByUserID = ControlCenter.getUserManager().getUserByUserID(chattingAccount);
                tvState.setText(String.format(getString(R.string.video_with_user_format), userByUserID == null ? chattingAccount :
                        userByUserID.getNickname() + "(" + userByUserID.getUserName() + ")"));
            } else
                tvState.setText(R.string.ready_connect);
        } else {
            tvState.setText(R.string.connecting);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNameDialog != null && mNameDialog.isShowing())
            mNameDialog.dismiss();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        //用户状态监听
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(mUserStatusObserver, false);
    }

    @OnClick({R.id.ibtn_menu, R.id.btn_exit, R.id.tv_device_name})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_menu:
                startNewActivity(VideoMenuActivity.class);
//        audioManager.setParameters("ForceUseSpecificMic=1");
//        audioManager.setMicrophoneMute(true);
                break;
            case R.id.btn_exit:
                finish();
                break;
            case R.id.tv_device_name:
                updateNickname();
                break;
        }
    }

    /**
     * 修改设备名称
     */
    private void updateNickname() {
        if (mNameDialog == null) {
            final CommonEditDialog commonEditDialog = new CommonEditDialog();
            commonEditDialog.setTitle(getString(R.string.update_nickname));
            commonEditDialog.setType(InputType.TYPE_CLASS_TEXT);
            commonEditDialog.setContent(mDoorbellConfig.getNickName() == null ? "" : mDoorbellConfig.getNickName());
            commonEditDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(final Object content) {
                    ControlCenter.getDoorbellManager().updateNickname(IMEI, content.toString(), new IDataListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean o) {
                            L.e("-------------onSuccess:" + content);
                            if (isFinishing() || getSupportFragmentManager() == null) return;
                            T.show(R.string.update_success);
                            tvDeviceName.setText(content.toString());
                            DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
                            doorbellConfig.getDoorbell().setName(content.toString());
                            ControlCenter.getDoorbellManager().setDoorbellConfig(doorbellConfig);
                            mNameDialog.dismiss();
                        }

                        @Override
                        public void onFailure(int errorCode, String desc) {
                            mNameDialog.dismiss();
                            T.show(desc);
                        }
                    });
                }
            });
            mNameDialog = new DialogHelper(VideoServiceActivity.this, commonEditDialog);
        }
        mNameDialog.commit();
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAVChatAction(AVChatAction avChatAction) {
        if (isFinishing() || isDestroyed() || getSupportFragmentManager() == null) return;
        if (AVChatAction.AVCHAT_USER_JOIN.equals(avChatAction.getType())) {
            String account = avChatAction.getStringExtra(Constant.FROM_ACCOUNT);
            User userByAccount = ControlCenter.getUserManager().getUserByUserID(account);
            tvState.setText(String.format(getString(R.string.video_with_user_format), userByAccount == null ? account :
                    userByAccount.getNickname() + "(" + userByAccount.getUserName() + ")"));
        } else if (AVChatAction.AVCHAT_HANG_UP.equals(avChatAction.getType())) {
            StatusCode status = NIMClient.getStatus();
            if (StatusCode.LOGINED == status)
                tvState.setText(R.string.ready_connect);
            else
                tvState.setText(getText(R.string.connecting));
        }
    }

    private Observer<StatusCode> mUserStatusObserver = new Observer<StatusCode>() {
        @Override
        public void onEvent(StatusCode statusCode) {
            if (isDestroyed()||isFinishing()||getSupportFragmentManager()==null)return;
            showState(statusCode);
        }
    };
}

