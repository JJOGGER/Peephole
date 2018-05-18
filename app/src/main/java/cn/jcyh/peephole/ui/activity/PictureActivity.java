package cn.jcyh.peephole.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.User;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.ImgUtil;
import timber.log.Timber;

public class PictureActivity extends BaseActivity {
    @BindView(R.id.surface_picture)
    SurfaceView mSurfaceView;
    private Camera mCamera;
    private String mType;
    private MediaRecorder mRecorder;
    private DoorbellConfig mDoorbellConfig;
    private boolean mIsRecording = false;
    private String mImgPath;

    @Override
    public int getLayoutId() {
        return R.layout.activity_picture;
    }

    @Override
    protected void init() {
        DoorBellControlCenter.sIsVideo = true;
        mSurfaceView.getHolder().addCallback(mCallback);
        mType = getIntent().getStringExtra("type");
        mDoorbellConfig = DoorBellControlCenter.getInstance(this).getDoorbellConfig();
    }

    private void initCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                try {
                    mCamera = Camera.open(camIdx);
                    break;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
//        Camera.Parameters parameters = mCamera.getParameters();
//
//        parameters.setPictureFormat(PixelFormat.JPEG);
//
//        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
//
//        setDispaly(parameters, mCamera);
//
//        mCamera.setParameters(parameters);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureSize(480, 360);//1024, 600
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());//进行预览
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e("-------------<" + e.getMessage());
        }
        mCamera.startPreview();//开始预览，必须在调用相机拍照之前
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
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
        simpleDateFormat.applyPattern("yyyy/MM/dd HH:mm:ss");
        time = simpleDateFormat.format(date);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        boolean isCompleted = ImgUtil.createWaterMaskWidthText(getApplicationContext(),
                mImgPath, bitmap,
                BitmapFactory.decodeResource(getResources(), R.mipmap.eagleking),
                time, heightPixels, widthPixels);
        Intent intent = getIntent();
        intent.putExtra("filePath", mImgPath);
        setResult(RESULT_OK, intent);
        closeCamera();
        //获取拍照的图片
        int type = 0;
        if (ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(mType)) {
            type = DoorBellControlCenter.DOORBELL_TYPE_RING;
        } else if (ConstantUtil.TYPE_DOORBELL_SYSTEM_ALARM.equals(mType)) {
            type = DoorBellControlCenter.DOORBELL_TYPE_ALARM;
        }
        HttpAction.getHttpAction(this).sendDoorbellImg(IMEI, type,
                mImgPath, null);
        if (ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(mType)) {
            if (mDoorbellConfig.getDoorbellVideotap() == 1) {
                //开启了录像
                startRecord();
            } else {
                if (mDoorbellConfig.getDoorbellLeaveMessage() == 1) {
                    startRecord();
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

    private void endDeal() {
        if (ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(mType)) {
            if (mDoorbellConfig.getDoorbellVideoCall() == 1) {
                sendVideoCall();
            }
        } else {
            if (mDoorbellConfig.getSensorVideoCall() == 1) {
                sendVideoCall();
            }
        }

    }

    /**
     * 录像(停留报警，按门铃/留言)
     */
    private void startRecord() {
        mSurfaceView.getHolder().setFixedSize(1024, 600);
        mSurfaceView.getHolder().setKeepScreenOn(true);
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置采集声音
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//设置采集图像
        //2.设置视频，音频的输出格式
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //3.设置音频的编码格式
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //设置图像的编码格式
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        File saveFile;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        final String tempPath = FileUtil.getInstance().getDoorbellVideoPath() + File.separator +
                "VID_" + time + ".mp4";
        saveFile = new File(tempPath);
        mRecorder.setOutputFile(saveFile.getAbsolutePath());
        mRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //开始录制
        mRecorder.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(mType)) {
                        if (mDoorbellConfig.getSensorVideotap() == 1) {
                            Thread.sleep(mDoorbellConfig.getVideotapTime() * 1000);
                        } else {
                            //留言
                            Thread.sleep(mDoorbellConfig.getVideoLeaveMsgTime() * 1000);
                        }
                    } else {
                        Thread.sleep(mDoorbellConfig.getVideotapTime() * 1000);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecorder.stop();
                            endDeal();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 发视频推送
     */
    private void sendVideoCall() {
        //发视频推送
        HttpAction.getHttpAction(this).getBindUsers(IMEI, new IDataListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users != null && users.size() != 0) {
                    //通知用户
                    DoorBellControlCenter.getInstance(getApplicationContext()).sendVideoCall
                            (users, mType, mImgPath);
                    finish();
                }
            }

            @Override
            public void onFailure(int errorCode) {
                Timber.e("------errorCode" + errorCode);
                finish();
            }
        });

    }

    private void closeCamera() {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.cancelAutoFocus();
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("---->" + e.getMessage());
        }
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    //控制图像的正确显示方向
    private void setDispaly(Camera.Parameters parameters, Camera camera) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            setDisplayOrientation(camera, 90);
        } else {
            parameters.setRotation(90);
        }

    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", int.class);
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, i);
            }
        } catch (Exception ignored) {
        }
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Timber.e("----------->a:");
            initCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Timber.e("----------->b:");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Timber.e("----------->c:");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DoorBellControlCenter.sIsVideo = false;
    }
}
