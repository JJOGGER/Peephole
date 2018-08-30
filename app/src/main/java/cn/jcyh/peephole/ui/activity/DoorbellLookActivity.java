package cn.jcyh.peephole.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.ImgUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.video.VideoCameraHelper;

//猫眼查看界面
public class DoorbellLookActivity extends BaseActivity {
    @BindView(R.id.surface_picture)
    SurfaceView mSurfaceView;
    @BindView(R.id.iv_change)
    ImageView ivChange;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mCount = 0;
    private int mDoorbellLookTime;
    private VideoCameraHelper mCameraHelper;

    @Override
    public int getLayoutId() {
        return R.layout.activity_doorbell_look;
    }

    @Override
    protected void init() {
        mCameraHelper = new VideoCameraHelper();
        mSurfaceView.getHolder().addCallback(mCameraHelper);
        ControlCenter.sIsVideo = true;
        DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
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
                            mCameraHelper.CloseCamera();
                            finish();
                        }
                    });
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);
    }


    private void onTakePhoto(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", getResources().getConfiguration().locale);
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        String imgPath = FileUtil.getDoorbellImgPath() + File.separator + "IMG_" + time +
                ".jpg";
        simpleDateFormat.applyPattern("yyyy/MM/dd HH:mm:ss");
        time = simpleDateFormat.format(date);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        ImgUtil.createWaterMaskWidthText(imgPath, null, bitmap,
                BitmapFactory.decodeResource(getResources(), R.mipmap.eagleking),
                time, heightPixels, widthPixels);
        Intent intent = getIntent();
        intent.putExtra(Constant.FILE_PATH, imgPath);
        finish();
    }

    @OnClick({R.id.fl_capture, R.id.iv_change})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_capture:
                mCameraHelper.takePicture(new Camera.PictureCallback() {//拍照
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        onTakePhoto(data);
                    }
                });
                break;
            case R.id.iv_change:
                mCameraHelper.SwitchCamera();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        L.e("----------onResume");
        initCamera();
    }
    private void initCamera() {
        int cameraNumber = mCameraHelper.GetCameraNumber();
        if (cameraNumber == 0) {
            T.show(R.string.no_camera);
            finish();
        } else {
            //默认打开后置
            if (cameraNumber == 1) {
                ivChange.setVisibility(View.GONE);
            }
            mCameraHelper.SelectVideoCapture(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraHelper.CloseCamera();
        finish();
        L.e("-----------------onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.e("-----------------onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ControlCenter.sIsVideo = false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        mCameraHelper.CloseCamera();
    }
}
