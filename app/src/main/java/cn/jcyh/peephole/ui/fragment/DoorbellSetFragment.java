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
import cn.jcyh.peephole.entity.DoorbellModelParam;
import cn.jcyh.peephole.event.NIMMessageAction;
import cn.jcyh.peephole.widget.MyDeviceParam;


/**
 * Created by jogger on 2018/4/28.
 * 猫眼设置
 */

public class DoorbellSetFragment extends BaseFragment {
    @BindView(R.id.my_net_push)
    MyDeviceParam myNetPush;
    @BindView(R.id.my_leave_message)
    MyDeviceParam myLeaveMessage;
    @BindView(R.id.my_video_call)
    MyDeviceParam myVideoCall;
    //    @BindView(R.id.my_dial)
//    MyDeviceParam myDial;
//    @BindView(R.id.my_send_msg)
//    MyDeviceParam mySendMsg;
    @BindView(R.id.my_videotap)
    MyDeviceParam myVideotap;
    private DoorbellConfig mDoorbellConfig;
    private DoorbellModelParam mDoorbellModelParam;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_doorbell_set;
    }

    @Override
    public void init() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void loadData() {
        super.loadData();
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        mDoorbellModelParam = mDoorbellConfig.getDoorbellModelParam();
        myNetPush.setCheck(mDoorbellModelParam.getNetPush() == 1);
        boolean videotap = mDoorbellModelParam.getVideotap() == 1;
        myVideotap.setCheck(videotap);
        boolean videoCall = mDoorbellModelParam.getVideoCall() == 1;
        myVideoCall.setCheck(videoCall);
        boolean leaveMessage = mDoorbellModelParam.getLeaveMessage() == 1;
        myLeaveMessage.setCheck(leaveMessage);
        myLeaveMessage.setCheckable(!videoCall && !videotap);
        myVideotap.setCheckable(!leaveMessage);
        myVideoCall.setCheckable(!leaveMessage);
//        boolean sendMsg = mDoorbellConfig.getDoorbellSendMsg() == 1;
//        mySendMsg.setCheck(sendMsg);
//        myDial.setCheckable(!sendMsg);

//        boolean dial = mDoorbellConfig.getDoorbellDial() == 1;
//        myDial.setCheck(dial);
//        myVideoCall.setCheckable(!dial);
//        mySendMsg.setCheckable(!dial);
    }

    @OnClick({R.id.my_net_push, R.id.my_videotap, R.id.my_video_call, R.id.my_leave_message
//            R.id.my_send_msg, R.id.my_dial
    })
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_net_push:
                myNetPush.setCheck(!myNetPush.isChecked());
                mDoorbellModelParam.setNetPush(myNetPush.isChecked() ? 1 : 0);
                break;
            case R.id.my_videotap:
                if (myVideotap.isChecked()) {
                    myVideotap.setCheck(false);
                    if (!myVideoCall.isChecked())
                        myLeaveMessage.setCheckable(true);
                    mDoorbellModelParam.setVideotap(0);
                } else {
                    myVideotap.setCheck(true);
                    myVideotap.setCheckable(true);
                    myLeaveMessage.setCheck(false);
                    myLeaveMessage.setCheckable(false);
                    myVideoCall.setCheckable(true);
                    mDoorbellModelParam.setVideotap(1);
                    mDoorbellModelParam.setLeaveMessage(0);
                }
                break;
            case R.id.my_video_call:
                if (myVideoCall.isChecked()) {
                    myVideoCall.setCheck(false);
                    if (!myVideotap.isChecked())
                        myLeaveMessage.setCheckable(true);
                    mDoorbellModelParam.setVideoCall(0);
                } else {
                    myVideoCall.setCheck(true);
                    myVideoCall.setCheckable(true);
                    if (myLeaveMessage.isChecked()) {
                        myLeaveMessage.setCheck(false);
                    }
                    myLeaveMessage.setCheckable(false);
                    myVideotap.setCheckable(true);
//                    myDial.setCheck(false);
//                    if (!mySendMsg.isChecked())
//                        myDial.setCheckable(true);
//                    mySendMsg.setCheckable(true);
                    mDoorbellModelParam.setVideoCall(1);
                    mDoorbellModelParam.setLeaveMessage(0);
//                    mDoorbellModelParam.setDoorbellDial(0);
                }
                break;
//            case R.id.my_send_msg:
//                if (mySendMsg.isChecked()) {
//                    mySendMsg.setCheck(false);
//                    myDial.setCheckable(true);
//                    mDoorbellConfig.setDoorbellSendMsg(0);
//                } else {
//                    mySendMsg.setCheck(true);
//                    mySendMsg.setCheckable(true);
//                    myDial.setCheck(false);
//                    myDial.setCheckable(false);
//                    if (!myLeaveMessage.isChecked())
//                        myVideoCall.setCheckable(true);
//                    mDoorbellConfig.setDoorbellSendMsg(1);
//                    mDoorbellConfig.setDoorbellDial(0);
//                }
//                break;
//            case R.id.my_dial:
//                if (myDial.isChecked()) {
//                    myDial.setCheck(false);
//                    mySendMsg.setCheckable(true);
//                    if (!myLeaveMessage.isChecked())
//                        myVideoCall.setCheckable(true);
//                    mDoorbellConfig.setDoorbellDial(0);
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
//                    myLeaveMessage.setCheckable(true);
//                    mDoorbellConfig.setDoorbellDial(1);
//                    mDoorbellConfig.setDoorbellSendMsg(0);
//                    mDoorbellConfig.setDoorbellVideoCall(0);
//                }
//                break;
            case R.id.my_leave_message:
                if (myLeaveMessage.isChecked()) {
                    myLeaveMessage.setCheck(false);
                    myVideoCall.setCheckable(true);
                    myVideotap.setCheckable(true);
                    mDoorbellModelParam.setLeaveMessage(0);
                } else {
                    myLeaveMessage.setCheck(true);
                    myLeaveMessage.setCheckable(true);
                    myVideoCall.setCheck(false);
                    myVideoCall.setCheckable(false);
                    myVideotap.setCheck(false);
                    myVideotap.setCheckable(false);
                    mDoorbellModelParam.setLeaveMessage(1);
                    mDoorbellModelParam.setVideoCall(0);
                    mDoorbellModelParam.setVideotap(0);
                }
                break;
        }
        setParam();
    }

    /**
     * 设置参数
     */
    private void setParam() {
        //保存到本地
        mDoorbellConfig.setDoorbellModelParam(mDoorbellModelParam);
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
