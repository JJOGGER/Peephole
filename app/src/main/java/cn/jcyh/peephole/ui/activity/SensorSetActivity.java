package cn.jcyh.peephole.ui.activity;

import android.view.View;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.widget.MyDeviceParam;
import timber.log.Timber;

public class SensorSetActivity extends BaseActivity {
    @BindView(R.id.my_net_push)
    MyDeviceParam myNetPush;
    @BindView(R.id.my_ring_alarm)
    MyDeviceParam myRingAlarm;
    @BindView(R.id.my_video_call)
    MyDeviceParam myVideoCall;
    @BindView(R.id.my_dial)
    MyDeviceParam myDial;
    @BindView(R.id.my_send_msg)
    MyDeviceParam mySendMsg;
    @BindView(R.id.my_videotap)
    MyDeviceParam myVideotap;
    private DoorbellConfig mDoorbellConfig;

    @Override
    public int getLayoutId() {
        return R.layout.activity_sensor_set;
    }

    @Override
    protected void init() {
        mDoorbellConfig = DoorBellControlCenter.getInstance(this).getDoorbellConfig();
        initView();
    }

    private void initView() {
        myNetPush.setCheck(mDoorbellConfig.getDoorbellNetPush() == 1);
        myVideotap.setCheck(mDoorbellConfig.getDoorbellVideotap() == 1);
        myRingAlarm.setCheck(mDoorbellConfig.getSensorRingAlarm() == 1);

        boolean sendMsg = mDoorbellConfig.getDoorbellSendMsg() == 1;
        mySendMsg.setCheck(sendMsg);
        myDial.setCheckable(!sendMsg);

        boolean dial = mDoorbellConfig.getDoorbellDial() == 1;
        myDial.setCheck(dial);
        myVideoCall.setCheckable(!dial);
        mySendMsg.setCheckable(!dial);

        boolean videoCall = mDoorbellConfig.getDoorbellVideoCall() == 1;
        myVideoCall.setCheck(videoCall);
    }

    @OnClick({R.id.my_net_push, R.id.my_videotap, R.id.my_video_call, R.id.my_send_msg,
            R.id.my_dial, R.id.my_ring_alarm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_net_push:
                myNetPush.setCheck(!myNetPush.isChecked());
                mDoorbellConfig.setDoorbellNetPush(myNetPush.isChecked() ? 1 : 0);
                break;
            case R.id.my_videotap:
                if (myVideotap.isChecked()) {
                    myVideotap.setCheck(false);
                    mDoorbellConfig.setDoorbellVideotap(0);
                } else {
                    myVideotap.setCheck(true);
                    myVideotap.setCheckable(true);
                    mDoorbellConfig.setDoorbellVideotap(1);
                    mDoorbellConfig.setDoorbellLeaveMessage(0);
                }
                break;
            case R.id.my_video_call:
                if (myVideoCall.isChecked()) {
                    myVideoCall.setCheck(false);
                    mDoorbellConfig.setDoorbellVideoCall(0);
                } else {
                    myVideoCall.setCheck(true);
                    myVideoCall.setCheckable(true);
                    myDial.setCheck(false);
                    if (!mySendMsg.isChecked())
                        myDial.setCheckable(true);
                    mySendMsg.setCheckable(true);
                    mDoorbellConfig.setDoorbellVideoCall(1);
                    mDoorbellConfig.setDoorbellLeaveMessage(0);
                    mDoorbellConfig.setDoorbellDial(0);
                }
                break;
            case R.id.my_send_msg:
                if (mySendMsg.isChecked()) {
                    mySendMsg.setCheck(false);
                    myDial.setCheckable(true);
                    mDoorbellConfig.setDoorbellSendMsg(0);
                } else {
                    mySendMsg.setCheck(true);
                    mySendMsg.setCheckable(true);
                    myDial.setCheck(false);
                    myDial.setCheckable(false);
                    mDoorbellConfig.setDoorbellSendMsg(1);
                    mDoorbellConfig.setDoorbellDial(0);
                }
                break;
            case R.id.my_dial:
                if (myDial.isChecked()) {
                    myDial.setCheck(false);
                    mySendMsg.setCheckable(true);
                    mDoorbellConfig.setDoorbellDial(0);
                } else {
                    myDial.setCheck(true);
                    myDial.setCheckable(true);
                    if (mySendMsg.isChecked()) {
                        mySendMsg.setCheck(false);
                    }
                    mySendMsg.setCheckable(false);
                    if (myVideoCall.isChecked()) {
                        myVideoCall.setCheck(false);
                    }
                    myVideoCall.setCheckable(false);
                    mDoorbellConfig.setDoorbellDial(1);
                    mDoorbellConfig.setDoorbellSendMsg(0);
                    mDoorbellConfig.setDoorbellVideoCall(0);
                }
                break;
            case R.id.my_ring_alarm:
                myRingAlarm.setCheck(!myRingAlarm.isChecked());
                mDoorbellConfig.setDoorbellNetPush(myRingAlarm.isChecked() ? 1 : 0);
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
