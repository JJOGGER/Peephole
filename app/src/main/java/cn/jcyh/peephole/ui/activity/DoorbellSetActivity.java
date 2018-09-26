package cn.jcyh.peephole.ui.activity;

import android.view.View;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.widget.MyDeviceParam;

//门铃设置
public class DoorbellSetActivity extends BaseActivity {
    @BindView(R.id.my_net_push)
    MyDeviceParam myNetPush;
    @BindView(R.id.my_leave_message)
    MyDeviceParam myLeaveMessage;
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
        return R.layout.activity_doorbell_set;
    }

    @Override
    public void init() {
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        initView();
    }

    private void initView() {
        myNetPush.setCheck(mDoorbellConfig.getDoorbellNetPush() == 1);
        myVideotap.setCheck(mDoorbellConfig.getDoorbellVideotap() == 1);

        boolean leaveMessage = mDoorbellConfig.getDoorbellLeaveMessage() == 1;
        myLeaveMessage.setCheck(leaveMessage);
        myVideoCall.setCheckable(!leaveMessage);
        myVideotap.setCheckable(!leaveMessage);

        boolean sendMsg = mDoorbellConfig.getDoorbellSendMsg() == 1;
        mySendMsg.setCheck(sendMsg);
        myDial.setCheckable(!sendMsg);

        boolean dial = mDoorbellConfig.getDoorbellDial() == 1;
        myDial.setCheck(dial);
        myVideoCall.setCheckable(!dial);
        mySendMsg.setCheckable(!dial);

        boolean videoCall = mDoorbellConfig.getDoorbellVideoCall() == 1;
        myVideoCall.setCheck(videoCall);
        myLeaveMessage.setCheckable(!videoCall);
    }

    @OnClick({R.id.my_net_push, R.id.my_videotap, R.id.my_video_call, R.id.my_send_msg,
            R.id.my_dial, R.id.my_leave_message})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_net_push:
                myNetPush.setCheck(!myNetPush.isChecked());
                mDoorbellConfig.setDoorbellNetPush(myNetPush.isChecked() ? 1 : 0);
                break;
            case R.id.my_videotap:
                if (myVideotap.isChecked()) {
                    myVideotap.setCheck(false);
                    myLeaveMessage.setCheckable(true);
                    mDoorbellConfig.setDoorbellVideotap(0);
                } else {
                    myVideotap.setCheck(true);
                    myVideotap.setCheckable(true);
                    myLeaveMessage.setCheck(false);
                    myLeaveMessage.setCheckable(false);
                    mDoorbellConfig.setDoorbellVideotap(1);
                    mDoorbellConfig.setDoorbellLeaveMessage(0);
                }
                break;
            case R.id.my_video_call:
                if (myVideoCall.isChecked()) {
                    myVideoCall.setCheck(false);
                    if (!myVideotap.isChecked())
                        myLeaveMessage.setCheckable(true);
                    mDoorbellConfig.setDoorbellVideoCall(0);
                } else {
                    myVideoCall.setCheck(true);
                    myVideoCall.setCheckable(true);
                    if (myLeaveMessage.isChecked()) {
                        myLeaveMessage.setCheck(false);
                    }
                    myLeaveMessage.setCheckable(false);
                    myVideotap.setCheckable(true);
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
                    if (!myLeaveMessage.isChecked())
                        myVideoCall.setCheckable(true);
                    mDoorbellConfig.setDoorbellSendMsg(1);
                    mDoorbellConfig.setDoorbellDial(0);
                }
                break;
            case R.id.my_dial:
                if (myDial.isChecked()) {
                    myDial.setCheck(false);
                    mySendMsg.setCheckable(true);
                    if (!myLeaveMessage.isChecked())
                        myVideoCall.setCheckable(true);
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
                    myLeaveMessage.setCheckable(true);
                    mDoorbellConfig.setDoorbellDial(1);
                    mDoorbellConfig.setDoorbellSendMsg(0);
                    mDoorbellConfig.setDoorbellVideoCall(0);
                }
                break;
            case R.id.my_leave_message:
                if (myLeaveMessage.isChecked()) {
                    myLeaveMessage.setCheck(false);
                    if (!myDial.isChecked())
                        myVideoCall.setCheckable(true);
                    myVideotap.setCheckable(true);
                    mDoorbellConfig.setDoorbellLeaveMessage(0);
                } else {
                    myLeaveMessage.setCheck(true);
                    myLeaveMessage.setCheckable(true);
                    myVideoCall.setCheck(false);
                    if (!myDial.isChecked())
                        myVideoCall.setCheckable(false);
                    myVideotap.setCheck(false);
                    myVideotap.setCheckable(false);
                    mDoorbellConfig.setDoorbellLeaveMessage(1);
                    mDoorbellConfig.setDoorbellVideoCall(0);
                    mDoorbellConfig.setDoorbellVideotap(0);
                }
                break;
        }
        setParam();
    }

    /**
     * 设置参数
     */
    private void setParam() {
        //保存到服务器
        ControlCenter.getDoorbellManager().setDoorbellConfig2Server(SN, mDoorbellConfig, new IDataListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                L.e("----------设置成功"+mDoorbellConfig);
                //保存到本地
                ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
            }

            @Override
            public void onFailure(int errorCode, String desc) {

            }
        });
    }
}
