package cn.jcyh.peephole.ui.activity;

import android.content.Intent;
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
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.SharePreUtil;

public class SensorSetActivity extends BaseActivity {
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
    @BindView(R.id.cb_ring_alarm)
    CheckBox cbRingAlarm;
    private DoorbellParam mDoorbellParam;

    @Override
    public int getLayoutId() {
        return R.layout.activity_sensor_set;
    }

    @Override
    protected void init() {
        mDoorbellParam = getIntent().getParcelableExtra("doorbellParam");
        if (mDoorbellParam == null)
            return;
        cbNetPush.setChecked(mDoorbellParam.getNetPush() == 1);
        cbVideotape.setChecked(mDoorbellParam.getVideotap() == 1);
        cbVideoCall.setChecked(mDoorbellParam.getVideoCall() == 1);
        cbSendMsg.setChecked(mDoorbellParam.getSendMsg() == 1);
        cbDial.setChecked(mDoorbellParam.getDial() == 1);
        cbRingAlarm.setChecked(mDoorbellParam.getRingAlarm() == 1);
    }

    @OnClick({R.id.rl_net_push, R.id.rl_videotape, R.id.rl_video_call, R.id.rl_send_msg,
            R.id.rl_dial, R.id.rl_ring_alarm})
    public void onClick(View v) {
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
            case R.id.rl_ring_alarm:
                cbRingAlarm.setChecked(!cbRingAlarm.isChecked());
                mDoorbellParam.setRingAlarm(cbRingAlarm.isChecked() ? 1 : 0);
                break;
        }
        setParam();
    }

    /**
     * 设置参数
     */
    private void setParam() {
        //保存到服务器
        HttpAction.getHttpAction(this).setDoorbellParams(IMEI, DoorBellControlCenter.DOORBELL_PARAMS_TYPE_SENSOR, mDoorbellParam, new IDataListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                SharePreUtil.getInstance(getApplicationContext()).setString(ConstantUtil.DOORBELL_SENSOR_PARAMS, new Gson().toJson(mDoorbellParam));
            }

            @Override
            public void onFailure(int errorCode) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("doorbellParam", mDoorbellParam);
        setResult(RESULT_OK, intent);
        finish();
    }
}
