package cn.jcyh.peephole.video.cameraact;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.control.DoorbellAudioManager;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.PhoneUtil;
import cn.jcyh.peephole.video.VideoCameraHelper;

/**
 * Created by jogger on 2018/8/18.按门铃事件
 */
public class RingAction {
    private DoorbellConfig mDoorbellConfig;
    private VideoCameraHelper mCameraHelper;
    private Activity mActivity;
    private int mRingCount = 0;//标记收到的门铃声

    public RingAction(Activity activity, DoorbellConfig doorbellConfig, VideoCameraHelper cameraHelper) {
        mActivity = activity;
        mDoorbellConfig = doorbellConfig;
        mCameraHelper = cameraHelper;
    }

    public void onPictureTaken(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        ControlCenter.getDoorbellManager().sendDoorbellImg(ControlCenter.getIMEI(), bitmap, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING, null);
        //判断是否开启拨打电话
        int doorbellDial = mDoorbellConfig.getDoorbellDial();
        String masterNumber = mDoorbellConfig.getMasterNumber();
        if (doorbellDial == 1 && !TextUtils.isEmpty(masterNumber)) {
            //拨打电话
            PhoneUtil.callPhone(masterNumber);
        }
        //判断是否发送短信
        int doorbellSendMsg = mDoorbellConfig.getDoorbellSendMsg();
        if (doorbellSendMsg == 1 && !TextUtils.isEmpty(masterNumber)) {
            PhoneUtil.sendMsg(masterNumber, String.format(mActivity.getString(R.string.send_msg_content_format), mActivity.getString(R.string.app_name),
                    mActivity.getString(R.string.someone_doorbell), TextUtils.isEmpty(mDoorbellConfig.getNickName()) ? ControlCenter.getIMEI() :
                            mDoorbellConfig.getNickName() + "(" + ControlCenter.getIMEI() + ")"));
        }
        if (mDoorbellConfig.getDoorbellVideotap() == 1) {
            //开启了录像
            startRecord();
        } else {
            //开启了留言模式
            if (mDoorbellConfig.getDoorbellLeaveMessage() == 1) {
                leaveMessageEvent();
            } else {
                mActivity.finish();
            }
        }
    }

    /**
     * 录像(停留报警，按门铃/留言)
     */
    private void startRecord() {
        mCameraHelper.startRecord(mDoorbellConfig, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING, new VideoCameraHelper.OnRecordListener() {
            @Override
            public void onRecordCompleted() {
                if (mDoorbellConfig.getDoorbellLeaveMessage() == 1) {
                    leavemsgEnd();
                } else {
                    mActivity.finish();
                }
            }
        });
    }


    /**
     * 留言事件处理
     */
    private void leaveMessageEvent() {
        ControlCenter.sIsLeaveMsgRecording = true;//标记开始留言
        DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager.RingerTypeEnum.LEAVE_MSG_START, null);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        final Timer timer = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //8秒后仍未开始留言，则结束留言
                if (mRingCount == 0) {
                    if (!mCameraHelper.isRecording()) {
                        leavemsgEnd();
                    }
                }
                timer.cancel();
                cancel();

            }
        };
        timer.schedule(timerTask, 8000);
    }

    public void stop() {
        ControlCenter.sIsLeaveMsgRecording = false;
        //结束语音播报
        if (mDoorbellConfig.getDoorbellLeaveMessage() == 1 || mDoorbellConfig.getDoorbellVideotap() == 1)
            DoorbellAudioManager.getDoorbellAudioManager().stop();
        if (mCameraHelper.isRecording())
            mCameraHelper.stopRecord();
        mRingCount = 0;
    }

    /**
     * 留言结束
     */
    private void leavemsgEnd() {
        L.e("------------留言结束:" + ControlCenter.sIsLeaveMsgRecording + ":" +
                DoorbellAudioManager.getDoorbellAudioManager().isPlaying());
        if (ControlCenter.sIsLeaveMsgRecording) {
            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager.RingerTypeEnum.LEAVE_MSG_END, new DoorbellAudioManager.OnCompletionListener() {
                @Override
                public void onCompletion() {
                    ControlCenter.sIsLeaveMsgRecording = false;
                    if (mActivity != null)
                        mActivity.finish();
                }
            });
        } else {
            if (mActivity != null)
                mActivity.finish();
        }

    }

    public void onBackPressed() {
        if (mDoorbellConfig.getDoorbellLeaveMessage() == 1) {
            leavemsgEnd();
        } else {
            if (mActivity != null)
                mActivity.finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDoorbellSystemAction(DoorbellSystemAction systemAction) {
        if (!DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING.equals(systemAction.getType())) return;
        if (mDoorbellConfig.getDoorbellLeaveMessage() != 1) return;
        mRingCount++;
        L.e("--------------------------收到门铃事件：" + mCameraHelper.isRecording());
        if (mCameraHelper.isRecording()) {
            //收到第二个门铃，结束留言
            if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying(DoorbellAudioManager.RingerTypeEnum.LEAVE_MSG_END)) {
                return;
            }
            leavemsgEnd();
        } else {
            if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying(DoorbellAudioManager.RingerTypeEnum.LEAVE_MSG_END)) {
                //如果正在播放留言end，则不处理
                return;
            }
            //收到第一个按门铃，开始留言
            if (ControlCenter.sIsLeaveMsgRecording) {
                //先结束语音播报
                DoorbellAudioManager.getDoorbellAudioManager().stop();
                startRecord();
            }
        }
    }
}