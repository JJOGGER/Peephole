package cn.jcyh.peephole.video.cameraact;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.control.DoorbellAudioManager;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.activity.CameraActivity;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.video.VideoCameraHelper;

/**
 * Created by jogger on 2018/8/18.
 */
public class AlarmAction {
    private DoorbellConfig mDoorbellConfig;
    private VideoCameraHelper mCameraHelper;
    private CameraActivity mActivity;

    public AlarmAction(CameraActivity activity, DoorbellConfig doorbellConfig, VideoCameraHelper cameraHelper) {
        mActivity = activity;
        mDoorbellConfig = doorbellConfig;
        mCameraHelper = cameraHelper;
    }

    private void alarmRing() {
        if (mDoorbellConfig.getDoorbellSensorParam().getRingAlarm() == 1) {
            //报警
            if (DoorbellAudioManager.getDoorbellAudioManager().isPlaying(DoorbellAudioManager.RingerTypeEnum.DOORBELL_RING)) {
                //正在播放门铃，不处理
                return;
            }
            DoorbellAudioManager.getDoorbellAudioManager().play(DoorbellAudioManager.RingerTypeEnum.DOORBELL_ALARM, null);
        }
    }

    public void onPictureTaken(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        IDataListener listener;
        //如果开启了人脸识别，且开启了停留报警，则要等回调后再报警
        if (mDoorbellConfig.getFaceRecognize() == 1 && mDoorbellConfig.getDoorbellSensorParam().getRingAlarm() == 1) {
            ControlCenter.getDoorbellManager().sendDoorbellImg(ControlCenter.getSN(),
                    bitmap,
                    DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM,
                    new IDataListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            alarmRing();
                        }

                        @Override
                        public void onFailure(int errorCode, String desc) {
                            L.e("---------errorCode:" + errorCode);
                            if (202 != errorCode) {
                                alarmRing();
                            }
                        }
                    });
        } else {
            ControlCenter.getDoorbellManager().sendDoorbellImg(ControlCenter.getSN(),
                    bitmap,
                    DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM,
                    null);
        }
        //判断是否开启拨打电话
//        int sensorDial = mDoorbellConfig.getSensorDial();
//        String masterNumber = mDoorbellConfig.getMasterNumber();
//        if (sensorDial == 1 && !TextUtils.isEmpty(masterNumber)) {
//            拨打电话
//            PhoneUtil.callPhone(masterNumber);
//        }
        //判断是否发送短信
//        int sensorSendMsg = mDoorbellConfig.getSensorSendMsg();
//        if (sensorSendMsg == 1 && !TextUtils.isEmpty(masterNumber)) {
//            PhoneUtil.sendMsg(masterNumber, String.format(mActivity.getString(R.string.send_msg_content_format), mActivity.getString(R.string.app_name),
//                    mActivity.getString(R.string.someone_doorbell), TextUtils.isEmpty(mDoorbellConfig.getNickName()) ? ControlCenter.getSN() :
//                            mDoorbellConfig.getNickName() + "(" + ControlCenter.getSN() + ")"));
//        }
        if (mDoorbellConfig.getDoorbellSensorParam().getVideotap() == 1) {
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
        mActivity.cRecord.setVisibility(View.VISIBLE);
        mCameraHelper.startRecord(mDoorbellConfig, DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM, new VideoCameraHelper.OnRecordListener() {
            @Override
            public void onRecordStart() {
                mActivity.cRecord.start();
            }

            @Override
            public void onRecordCompleted() {
                mActivity.finish();
            }
        });
    }
}
