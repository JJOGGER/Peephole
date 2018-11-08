package cn.jcyh.peephole.ui.fragment;

import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.DoorbellSensorParam;
import cn.jcyh.peephole.event.NIMMessageAction;
import cn.jcyh.peephole.widget.MyDeviceParam;


/**
 * Created by jogger on 2018/4/28.
 * 传感器设置
 */

public class SensorSetFragment extends BaseFragment {
    @BindView(R.id.my_net_push)
    MyDeviceParam myNetPush;
    @BindView(R.id.my_ring_alarm)
    MyDeviceParam myRingAlarm;
    @BindView(R.id.my_video_call)
    MyDeviceParam myVideoCall;
    //    @BindView(R.id.my_dial)
//    MyDeviceParam myDial;
//    @BindView(R.id.my_send_msg)
//    MyDeviceParam mySendMsg;
    @BindView(R.id.my_videotap)
    MyDeviceParam myVideotap;
    private DoorbellConfig mDoorbellConfig;
    private DoorbellSensorParam mDoorbellSensorParam;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_sensor_set;
    }

    @Override
    public void init() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void loadData() {
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        mDoorbellSensorParam = mDoorbellConfig.getDoorbellSensorParam();
        myNetPush.setCheck(mDoorbellSensorParam.getNetPush() == 1);
        myVideotap.setCheck(mDoorbellSensorParam.getVideotap() == 1);
        myRingAlarm.setCheck(mDoorbellSensorParam.getRingAlarm() == 1);


//        boolean sendMsg = mDoorbellConfig.getSensorSendMsg() == 1;
//        mySendMsg.setCheck(sendMsg);
//        myDial.setCheckable(!sendMsg);

//        boolean dial = mDoorbellConfig.getSensorDial() == 1;
//        myDial.setCheck(dial);
//        myVideoCall.setCheckable(!dial);
//        mySendMsg.setCheckable(!dial);

        boolean videoCall = mDoorbellSensorParam.getVideoCall() == 1;
        myVideoCall.setCheck(videoCall);
    }

    @OnClick({R.id.my_net_push, R.id.my_videotap, R.id.my_video_call,
//            R.id.my_send_msg,R.id.my_dial,
            R.id.my_ring_alarm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_net_push:
                myNetPush.setCheck(!myNetPush.isChecked());
                mDoorbellSensorParam.setNetPush(myNetPush.isChecked() ? 1 : 0);
                break;
            case R.id.my_videotap:
                if (myVideotap.isChecked()) {
                    myVideotap.setCheck(false);
                    mDoorbellSensorParam.setVideotap(0);
                } else {
                    myVideotap.setCheck(true);
                    myVideotap.setCheckable(true);
                    mDoorbellSensorParam.setVideotap(1);
                }
                break;
            case R.id.my_video_call:
                if (myVideoCall.isChecked()) {
                    myVideoCall.setCheck(false);
                    mDoorbellSensorParam.setVideoCall(0);
                } else {
                    myVideoCall.setCheck(true);
                    myVideoCall.setCheckable(true);
//                    myDial.setCheck(false);
//                    if (!mySendMsg.isChecked())
//                        myDial.setCheckable(true);
//                    mySendMsg.setCheckable(true);
                    mDoorbellSensorParam.setVideoCall(1);
//                    mDoorbellConfig.setSensorDial(0);
                }
                break;
//            case R.id.my_send_msg:
//                if (mySendMsg.isChecked()) {
//                    mySendMsg.setCheck(false);
//                    myDial.setCheckable(true);
//                    mDoorbellConfig.setSensorSendMsg(0);
//                } else {
//                    mySendMsg.setCheck(true);
//                    mySendMsg.setCheckable(true);
//                    myDial.setCheck(false);
//                    myDial.setCheckable(false);
//                    mDoorbellConfig.setSensorSendMsg(1);
//                    mDoorbellConfig.setSensorDial(0);
//                }
//                break;
//            case R.id.my_dial:
//                if (myDial.isChecked()) {
//                    myDial.setCheck(false);
//                    mySendMsg.setCheckable(true);
//                    mDoorbellConfig.setSensorDial(0);
//                } else {
//                    myDial.setCheck(true);
//                    myDial.setCheckable(true);
//                    if (mySendMsg.isChecked()) {
//                        mySendMsg.setCheck(false);
//                    }
//                    mySendMsg.setCheckable(false);
//                    if (myVideoCall.isChecked()) {
//                        myVideoCall.setCheck(false);
//                    }
//                    myVideoCall.setCheckable(false);
//                    mDoorbellConfig.setSensorDial(1);
//                    mDoorbellConfig.setSensorSendMsg(0);
//                    mDoorbellConfig.setSensorVideoCall(0);
//                }
//                break;
            case R.id.my_ring_alarm:
                myRingAlarm.setCheck(!myRingAlarm.isChecked());
                mDoorbellSensorParam.setRingAlarm(myRingAlarm.isChecked() ? 1 : 0);
                break;
        }
        setParam();
    }

    /**
     * 设置参数
     */
    private void setParam() {
        //保存到本地
        mDoorbellConfig.setDoorbellSensorParam(mDoorbellSensorParam);
        ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
        //保存到服务器
        ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getSN(), mDoorbellConfig, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageAction(NIMMessageAction action) {
        if (!NIMMessageAction.NIMMESSAGE_DOORBELL_CONFIG.equals(action.getType())) {
            return;
        }
        //更新门铃信息
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
