package cn.jcyh.peephole.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.SurfaceView;

import com.bairuitech.anychat.AnyChatCameraHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import cn.jcyh.eaglelock.constant.Constant;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.ImgUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.T;

//猫眼侦测界面
public class CameraActivity extends BaseActivity implements AnyChatCameraHelper.OnSurfaceViewCallback {
    @BindView(R.id.surface_camera)
    SurfaceView surfaceCamera;
    private String mImgPath;
    private String mType;
    private DoorbellConfig mDoorbellConfig;
    private boolean mIsLeaveMsgRecording;//标记是否正在留言录像
    private MyReceiver mReceiver;
    private MediaPlayer mMediaPlayer;
    AnyChatCameraHelper mCameraHelper;

    @Override
    public int getLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    protected void init() {
        DoorBellControlCenter.sIsVideo = true;
        mType = getIntent().getStringExtra(Constant.TYPE);
        mCameraHelper = new AnyChatCameraHelper();
        mDoorbellConfig = DoorBellControlCenter.getInstance().getDoorbellConfig();
        mCameraHelper.SetContext(getApplicationContext());
        surfaceCamera.getHolder().addCallback(mCameraHelper);
        int cameraNumber = mCameraHelper.GetCameraNumber();
        if (cameraNumber == 0) {
            T.show(R.string.no_camera);
            finish();
            return;
        } else {
            //默认打开后置
            mCameraHelper.SelectVideoCapture(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        mCameraHelper.setOnSurfaceViewCallback(this);
    }

    @Override
    public void onBackPressed() {
        L.e("-------------->onBackPressed" + mDoorbellConfig.getDoorbellLeaveMessage());
        if (mDoorbellConfig.getDoorbellLeaveMessage() == 1) {
            //留言模式
            leavemsgEnd();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.e("--------------onDestroy");
        DoorBellControlCenter.sIsVideo = false;
        mCameraHelper.CloseCamera();
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onSurfaceCreated() {
        //开始拍照
        mCameraHelper.takePicture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                onTakePhoto(data);
            }
        });
    }

    private void onTakePhoto(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        mImgPath = FileUtil.getInstance().getDoorbellImgPath() + File.separator + "IMG_" + time +
                ".jpg";
        String thumbPath = FileUtil.getInstance().getDoorbellImgThumbnailPath() + File.separator + "IMG_" + time +
                ".jpg";
        simpleDateFormat.applyPattern("yyyy/MM/dd HH:mm:ss");
        time = simpleDateFormat.format(date);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        boolean waterMaskWidthText = ImgUtil.createWaterMaskWidthText(getApplicationContext(),
                mImgPath, thumbPath, bitmap,
                BitmapFactory.decodeResource(getResources(), R.mipmap.eagleking),
                time, heightPixels, widthPixels);
        Intent intent = getIntent();
        intent.putExtra(Constant.FILE_PATH, mImgPath);
        setResult(RESULT_OK, intent);
        //获取拍照的图片
        int type = 0;
        if (ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(mType)) {
            type = DoorBellControlCenter.DOORBELL_TYPE_RING;
        } else if (ConstantUtil.TYPE_DOORBELL_SYSTEM_ALARM.equals(mType)) {
            type = DoorBellControlCenter.DOORBELL_TYPE_ALARM;
        }

        HttpAction.getHttpAction().sendDoorbellImg(IMEI, type,
                thumbPath, null);
        if (ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(mType)) {
            if (mDoorbellConfig.getDoorbellVideotap() == 1) {
                //开启了录像
                startRecord();
            } else {

                if (mDoorbellConfig.getDoorbellLeaveMessage() == 1) {
                    leaveMessageEvent();
//                    startRecord();
                } else {
                    endDeal();
                }
            }
        } else {
            if (mDoorbellConfig.getSensorVideotap() == 1) {
                startRecord();
            } else {
                endDeal();
            }
        }

    }

    private void leaveMessageEvent() {
        // TODO: 2018/6/29 开启留言模式，做出语音提示，一定时间内监听门铃，开始录像
        AssetFileDescriptor descriptor;
        final AssetManager assets = getResources().getAssets();
        mMediaPlayer = null;
        try {
            descriptor = assets.openFd("doorbell_leave_msg_start.mp3");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setLooping(false);
            mMediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mMediaPlayer != null)
            mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mReceiver = new MyReceiver();
                IntentFilter intentFilter = new IntentFilter(ConstantUtil.ACTION_DOORBELL_SYSTEM_EVENT);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, intentFilter);
                final Timer timer = new Timer();
                final TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (!mIsLeaveMsgRecording) {
                            leavemsgEnd();
                        }
                        timer.cancel();
                        cancel();
                    }
                };
                timer.schedule(timerTask, 5000);
            }
        });

//        final AudioManager audioManager = new AudioManager();
//        audioManager.startSpeaking(R.string.leave_message_audio_start_msg);
//        audioManager.setOnSpeechListener(new AudioManager.OnSpeechListener() {
//            @Override
//            public void onSpeakCompleted() {
//                //开启门铃监听
//                L.e("-------onSpeakCompleted");
//                //开启倒计时
//
//            }
//        });
    }

    private void leavemsgEnd() {
        final AssetManager assets = getResources().getAssets();
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            try {
                AssetFileDescriptor assetFileDescriptor = assets.openFd("doorbell_leave_msg_end.mp3");
                mMediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        finish();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 录像(停留报警，按门铃/留言)
     */
    private void startRecord() {
        File saveFile;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        final String tempPath = FileUtil.getInstance().getDoorbellVideoPath() + File.separator +
                "VID_" + time + ".mp4";
        saveFile = new File(tempPath);
        int recordTime;
        if (ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(mType)) {
            if (mDoorbellConfig.getDoorbellVideotap() == 1) {
                recordTime = mDoorbellConfig.getVideotapTime();
            } else {
                //留言
                recordTime = mDoorbellConfig.getVideoLeaveMsgTime();
            }
        } else {
            recordTime = mDoorbellConfig.getVideotapTime();
        }
        mCameraHelper.startRecord(saveFile.getAbsolutePath(), recordTime, new AnyChatCameraHelper.OnRecordListener() {
            @Override
            public void onRecordCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endDeal();
                    }
                });

            }
        });
    }

    private void endDeal() {
        if (ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(mType)) {
            if (mDoorbellConfig.getDoorbellVideoCall() == 1) {
                sendVideoCall();
            }
            if (mDoorbellConfig.getDoorbellLeaveMessage() == 1) {
                //留言模式
                leavemsgEnd();
            } else {
                finish();
            }
        } else {
            if (mDoorbellConfig.getSensorVideoCall() == 1) {
                sendVideoCall();
            }
            finish();
        }
    }

    /**
     * 发视频推送
     */
    private void sendVideoCall() {
        L.e("---------------sendVideoCall");
        //发视频推送
        HttpAction.getHttpAction().getBindUsers(IMEI, new IDataListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users != null && users.size() != 0) {
                    //通知用户
                    DoorBellControlCenter.getInstance().sendVideoCall
                            (users, mType, mImgPath);
                }
                finish();
            }

            @Override
            public void onFailure(int errorCode) {
                L.e("------errorCode" + errorCode);
                finish();
            }
        });
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConstantUtil.ACTION_DOORBELL_SYSTEM_EVENT.equals(intent.getAction())) {
                if (!ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(intent.getStringExtra(Constant.TYPE)))
                    return;
                if (mDoorbellConfig.getDoorbellLeaveMessage() == 1) {
                    if (mIsLeaveMsgRecording) {
                        //结束留言
                        leavemsgEnd();
                    } else {
                        mIsLeaveMsgRecording = true;
                        startRecord();
                    }
                }
            }
        }
    }
}
