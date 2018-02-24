package cn.jcyh.peephole.ui.activity;

import android.view.View;
import android.widget.CheckBox;

import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.MyApp;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.DoorbellParam;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.HttpErrorCode;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.SharePreUtil;
import cn.jcyh.peephole.utils.ToastUtil;
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
    private DoorbellParam mDoorbellParam;

    @Override
    public int getLayoutId() {
        return R.layout.activity_doorbell_set;
    }

    @Override
    public void init() {
        HttpAction.getHttpAction(this).getDoorbellParams(MyApp.sImei, DoorBellControlCenter.DOORBELL_PARAMS_TYPE_MODE, new IDataListener<DoorbellParam>() {
            @Override
            public void onSuccess(DoorbellParam doorbellParam) {
                mDoorbellParam = doorbellParam;
                initView();
            }

            @Override
            public void onFailure(int errorCode) {
                if (errorCode == HttpErrorCode.NO_DATA_EXISTS) {
                    mDoorbellParam = new DoorbellParam();
                    initView();
                }
            }
        });
    }

    private void initView() {
        if (mDoorbellParam == null)
            return;
        cbNetPush.setChecked(mDoorbellParam.getNetPush() == 1);
        cbVideotape.setChecked(mDoorbellParam.getVideotap() == 1);
        cbVideoCall.setChecked(mDoorbellParam.getVideoCall() == 1);
        cbSendMsg.setChecked(mDoorbellParam.getSendMsg() == 1);
        cbDial.setChecked(mDoorbellParam.getDial() == 1);
        cbLeaveMessage.setChecked(mDoorbellParam.getLeaveMessage() == 1);
    }

    @OnClick({R.id.rl_net_push, R.id.rl_videotape, R.id.rl_video_call, R.id.rl_send_msg,
            R.id.rl_dial, R.id.rl_leave_message})
    public void onClick(View v) {
        if (mDoorbellParam == null)
            ToastUtil.showToast(getApplicationContext(), R.string.loading);
        switch (v.getId()) {
            case R.id.rl_net_push:
                cbNetPush.setChecked(!cbNetPush.isChecked());
                mDoorbellParam.setNetPush(cbNetPush.isChecked() ? 1 : 0);
                break;
            case R.id.rl_videotape:
                cbVideotape.setChecked(!cbVideotape.isChecked());
                mDoorbellParam.setVideotap(cbVideotape.isChecked() ? 1 : 0);
                break;
            case R.id.rl_video_call:
                cbVideoCall.setChecked(!cbVideoCall.isChecked());
                mDoorbellParam.setVideoCall(cbVideoCall.isChecked() ? 1 : 0);
                break;
            case R.id.rl_send_msg:
                cbSendMsg.setChecked(!cbSendMsg.isChecked());
                mDoorbellParam.setSendMsg(cbSendMsg.isChecked() ? 1 : 0);
                break;
            case R.id.rl_dial:
                cbDial.setChecked(!cbDial.isChecked());
                mDoorbellParam.setDial(cbDial.isChecked() ? 1 : 0);
                break;
            case R.id.rl_leave_message:
                cbLeaveMessage.setChecked(!cbLeaveMessage.isChecked());
                mDoorbellParam.setLeaveMessage(cbLeaveMessage.isChecked() ? 1 : 0);
                break;
        }
        setParam();
    }

    /**
     * 设置参数
     */
    private void setParam() {
        //保存到服务器
        HttpAction.getHttpAction(this).setDoorbellParams(MyApp.sImei, DoorBellControlCenter.DOORBELL_PARAMS_TYPE_MODE, mDoorbellParam, new IDataListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                Timber.e("----------设置成功");
                //保存到本地
                SharePreUtil.getInstance(getApplicationContext()).setString(ConstantUtil.DOORBELL_RING_PARAMS,new Gson().toJson(mDoorbellParam));
            }

            @Override
            public void onFailure(int errorCode) {

            }
        });
    }
}
