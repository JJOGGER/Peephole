package cn.jcyh.peephole.ui.activity;

import android.view.View;
import android.widget.CheckBox;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import timber.log.Timber;

//门铃设置
public class DoorbellSetActivity extends BaseActivity {
    @BindView(R.id.cb_net_push)
    CheckBox cbNetPush;
    @BindView(R.id.cb_videotape)
    CheckBox cbVideotape;
    @BindView(R.id.cb_video_call)
    CheckBox cbVideoCall;
    @BindView(R.id.cb_send_msg)
    CheckBox cbSendMsg;
    @BindView(R.id.cb_dial)
    CheckBox cbDial;
    @BindView(R.id.cb_leave_message)
    CheckBox cbLeaveMessage;
    private DoorbellConfig mDoorbellConfig;


    @Override
    public int getLayoutId() {
        return R.layout.activity_doorbell_set;
    }

    @Override
    public void init() {
        mDoorbellConfig = DoorBellControlCenter.getInstance(this).getDoorbellConfig();
        initView();
    }

    private void initView() {
        cbNetPush.setChecked(mDoorbellConfig.getDoorbellNetPush() == 1);
        cbVideotape.setChecked(mDoorbellConfig.getDoorbellVideotap() == 1);
        cbVideoCall.setChecked(mDoorbellConfig.getDoorbellVideoCall() == 1);
        cbSendMsg.setChecked(mDoorbellConfig.getDoorbellSendMsg() == 1);
        cbDial.setChecked(mDoorbellConfig.getDoorbellDial() == 1);
        cbLeaveMessage.setChecked(mDoorbellConfig.getDoorbellLeaveMessage() == 1);
    }

    @OnClick({R.id.rl_net_push, R.id.rl_videotape, R.id.rl_video_call, R.id.rl_send_msg,
            R.id.rl_dial, R.id.rl_leave_message})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_net_push:
                cbNetPush.setChecked(!cbNetPush.isChecked());
                mDoorbellConfig.setDoorbellNetPush(cbNetPush.isChecked() ? 1 : 0);
                break;
            case R.id.rl_videotape:
                cbVideotape.setChecked(!cbVideotape.isChecked());
                mDoorbellConfig.setDoorbellVideotap(cbVideotape.isChecked() ? 1 : 0);
                break;
            case R.id.rl_video_call:
                cbVideoCall.setChecked(!cbVideoCall.isChecked());
                mDoorbellConfig.setDoorbellVideoCall(cbVideoCall.isChecked() ? 1 : 0);
                break;
            case R.id.rl_send_msg:
                cbSendMsg.setChecked(!cbSendMsg.isChecked());
                mDoorbellConfig.setDoorbellSendMsg(cbSendMsg.isChecked() ? 1 : 0);
                break;
            case R.id.rl_dial:
                cbDial.setChecked(!cbDial.isChecked());
                mDoorbellConfig.setDoorbellDial(cbDial.isChecked() ? 1 : 0);
                break;
            case R.id.rl_leave_message:
                cbLeaveMessage.setChecked(!cbLeaveMessage.isChecked());
                mDoorbellConfig.setDoorbellLeaveMessage(cbLeaveMessage.isChecked() ? 1 : 0);
                break;
        }
        setParam();
    }

    /**
     * 设置参数
     */
    private void setParam() {
        //保存到服务器
        HttpAction.getHttpAction(this).setDoorbellConfig(IMEI, mDoorbellConfig, new IDataListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                Timber.e("----------设置成功");
                //保存到本地
                DoorBellControlCenter.getInstance(getApplicationContext()).saveDoorbellConfig(mDoorbellConfig);
            }

            @Override
            public void onFailure(int errorCode) {

            }
        });
    }
}
