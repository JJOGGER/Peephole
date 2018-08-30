package cn.jcyh.peephole.video.cameraact;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.utils.PhoneUtil;
import cn.jcyh.peephole.video.VideoCameraHelper;

/**
 * Created by jogger on 2018/8/18.
 */
public class AlarmAction {
    private DoorbellConfig mDoorbellConfig;
    private VideoCameraHelper mCameraHelper;
    private Activity mActivity;

    public AlarmAction(Activity activity, DoorbellConfig doorbellConfig, VideoCameraHelper cameraHelper) {
        mActivity = activity;
        mDoorbellConfig = doorbellConfig;
        mCameraHelper = cameraHelper;
    }

    public void onPictureTaken(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        ControlCenter.getDoorbellManager().sendDoorbellImg(ControlCenter.getIMEI(), bitmap, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM, null);
        //判断是否开启拨打电话
        int sensorDial = mDoorbellConfig.getSensorDial();
        String masterNumber = mDoorbellConfig.getMasterNumber();
        if (sensorDial == 1 && !TextUtils.isEmpty(masterNumber)) {
            //拨打电话
            PhoneUtil.callPhone(masterNumber);
        }
        //判断是否发送短信
        int sensorSendMsg = mDoorbellConfig.getSensorSendMsg();
        if (sensorSendMsg == 1 && !TextUtils.isEmpty(masterNumber)) {
            PhoneUtil.sendMsg(masterNumber, String.format(mActivity.getString(R.string.send_msg_content_format), mActivity.getString(R.string.app_name),
                    mActivity.getString(R.string.someone_doorbell), TextUtils.isEmpty(mDoorbellConfig.getNickName()) ? ControlCenter.getIMEI() :
                            mDoorbellConfig.getNickName() + "(" + ControlCenter.getIMEI() + ")"));
        }
        if (mDoorbellConfig.getSensorVideotap() == 1) {
            startRecord();
        } else {
            mActivity.finish();
        }
    }

    public void stop() {
        if (mCameraHelper.isRecording())
            mCameraHelper.stopRecord();
    }

    /**
     * 录像(停留报警，按门铃/留言)
     */
    private void startRecord() {
        mCameraHelper.startRecord(mDoorbellConfig, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM, new VideoCameraHelper.OnRecordListener() {
            @Override
            public void onRecordCompleted() {
                mActivity.finish();
            }
        });
    }
}
