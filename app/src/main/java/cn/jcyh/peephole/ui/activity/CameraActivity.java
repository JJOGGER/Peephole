package cn.jcyh.peephole.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.ImgUtil;
import timber.log.Timber;

//猫眼查看界面
public class CameraActivity extends BaseActivity {
    @BindView(R.id.surface_picture)
    SurfaceView mSurfaceView;
    @BindView(R.id.iv_change)
    ImageView ivChange;
    private Camera mCamera;
    private String mImgPath;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mCount = 0;
    private int mDoorbellLookTime;
    private int mCurrentFacing;

    @Override
    public int getLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    protected void init() {
        DoorBellControlCenter.sIsVideo = true;
        mSurfaceView.getHolder().addCallback(mCallback);
        DoorbellConfig doorbellConfig = DoorBellControlCenter.getInstance().getDoorbellConfig();
        mDoorbellLookTime = doorbellConfig.getDoorbellLookTime();
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mCount++;
                if (mCount >= mDoorbellLookTime) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //结束
                            closeCamera();
                            finish();
                        }
                    });
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    private void initCamera(int facing) {
        closeCamera();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        Timber.e("----------cameraCount:" + cameraCount);
        if (cameraCount < 2) {
            ivChange.setVisibility(View.GONE);
            Camera.getCameraInfo(0, cameraInfo);
            try {
                mCamera = Camera.open(0);
            } catch (RuntimeException e) {
                e.printStackTrace();
                closeCamera();
            }
            mCurrentFacing = cameraInfo.facing;
        } else {
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                Timber.e("----------cameraInfo.facing:" + cameraInfo.facing);
                if (cameraInfo.facing == facing) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                    try {
                        mCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        closeCamera();
                        continue;
                    }
                    mCurrentFacing = cameraInfo.facing;
                    break;
                }
            }
        }
        if (mCamera == null) return;
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
        }
        try {
            mCamera.startPreview();//开始预览，必须在调用相机拍照之前
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
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
        ImgUtil.createWaterMaskWidthText(getApplicationContext(),
                mImgPath, bitmap,
                BitmapFactory.decodeResource(getResources(), R.mipmap.eagleking),
                time, heightPixels, widthPixels);
        Intent intent = getIntent();
        intent.putExtra("filePath", mImgPath);
        finish();
    }

    private void closeCamera() {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setPreviewCallback(null);
            mCamera.cancelAutoFocus();
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e("---->" + e.getMessage());
        }
        mSurfaceView.getHolder().removeCallback(mCallback);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @OnClick({R.id.fl_capture, R.id.iv_change})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_capture:
                mCamera.takePicture(null, null, new Camera.PictureCallback() {//拍照
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        onTakePhoto(data);
                    }
                });
                break;
            case R.id.iv_change:
                if (mCurrentFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    initCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    initCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
                break;
        }
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            initCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            closeCamera();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DoorBellControlCenter.sIsVideo = false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        closeCamera();
    }
}
