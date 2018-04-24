package cn.jcyh.peephole.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.User;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.HttpUrlIble;
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

    @Override
    public int getLayoutId() {
        return R.layout.activity_picture;
    }

    @Override
    protected void init() {
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
        mCamera = Camera.open();
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
        final String tempPath = FileUtil.getInstance().getDoorbellMediaPath() + File.separator + "IMG_" + time +
                ".jpg";
        simpleDateFormat.applyPattern("yyyy/MM/dd HH:mm:ss");
        time = simpleDateFormat.format(date);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        boolean isCompleted = ImgUtil.createWaterMaskWidthText(getApplicationContext(),
                tempPath, bitmap,
                BitmapFactory.decodeResource(getResources(), R.mipmap.eagleking),
                time, heightPixels, widthPixels);
        Intent intent = getIntent();
        intent.putExtra("filePath", tempPath);
        setResult(RESULT_OK, intent);
        closeCamera();
        if (ConstantUtil.TYPE_DOORBELL_SYSTEM_RING.equals(mType)) {
            if (mDoorbellConfig.getDoorbellVideotap() == 0) {
                //开启了录像
                startRecord();
            } else {
                endDeal(tempPath);
            }
        } else {

        }

    }

    private void endDeal(final String tempPath) {
        //获取拍照的图片
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", IMEI);
        params.put("type", 1);
        HttpAction.getHttpAction(this).sendPostImg(HttpUrlIble.UPLOAD_DOORBELL_ALARM_URL,
                tempPath, params, null);
        HttpAction.getHttpAction(this).getBindUsers(IMEI, new IDataListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users != null && users.size() != 0) {
                    //通知用户
                    DoorBellControlCenter.getInstance(getApplicationContext()).sendVideoCall(users, mType, tempPath);
                }
            }

            @Override
            public void onFailure(int errorCode) {
                Timber.e("------errorCode" + errorCode);
            }
        });
    }

    private void startRecord() {
        mSurfaceView.getHolder().setFixedSize(1024, 600);
        mSurfaceView.getHolder().setKeepScreenOn(true);
        mRecorder = new MediaRecorder();
        mRecorder.reset();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置采集声音
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//设置采集图像
        //2.设置视频，音频的输出格式
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //3.设置音频的编码格式
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //设置图像的编码格式
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        File saveFile = null;
        try {
            saveFile = new File(Environment.getExternalStorageDirectory()
                    .getCanonicalFile() + "/myvideo.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    Thread.sleep(mDoorbellConfig.getVideoTime() * 1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecorder.stop();
                            finish();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

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
}
