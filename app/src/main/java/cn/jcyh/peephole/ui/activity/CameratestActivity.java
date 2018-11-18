package cn.jcyh.peephole.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.PowerManager;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.utils.SystemUtil;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.video.VideoCameraHelper;
import cn.jcyh.peephole.video.cameraact.AlarmAction;
import cn.jcyh.peephole.video.cameraact.RingAction;

//猫眼侦测界面
public class CameratestActivity extends BaseActivity implements VideoCameraHelper.OnSurfaceViewCallback {
    @BindView(R.id.surface_camera)
    SurfaceView surfaceCamera;
    @BindView(R.id.c_record)
    public Chronometer cRecord;
    private DoorbellConfig mDoorbellConfig;
    VideoCameraHelper mCameraHelper;
    private boolean mIsDestroyed;
    private RingAction mRingAction;
    private AlarmAction mAlarmAction;

    @Override
    public int getLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    protected void init() {
        cRecord.setVisibility(View.GONE);
        SystemUtil.wakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.SCREEN_DIM_WAKE_LOCK);
        ControlCenter.sIsVideo = true;
        String type = getIntent().getStringExtra(Constant.TYPE);
        mCameraHelper = new VideoCameraHelper();
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
//        if (mDoorbellConfig.getDoorbellSensorParam().getMonitor() == 1) {
            //暂时关闭停留报警
//            ControlCenter.getBCManager().setPIRSensorOn(false);
//        }
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            destroy();
    }

    @Override
    public void onBackPressed() {
        if (mRingAction != null) {
            mRingAction.onBackPressed();
        } else {
            finish();
        }
    }

    private void destroy() {
        if (mIsDestroyed)
            return;
        ControlCenter.sIsVideo = false;
        if (mDoorbellConfig.getDoorbellSensorParam().getMonitor() == 1) {
            //还原停留报警
//            ControlCenter.getBCManager().setPIRSensorOn(true);
        }
        if (mRingAction != null) {
            mRingAction.stop();
            mRingAction = null;
        }
        if (mAlarmAction != null) {
            mAlarmAction.stop();
            mAlarmAction = null;
        }
        if (mCameraHelper != null)
            mCameraHelper.CloseCamera();
        if (surfaceCamera != null && surfaceCamera.getHolder() != null)
            surfaceCamera.getHolder().removeCallback(mCameraHelper);
        mCameraHelper = null;
        mIsDestroyed = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy();
    }

    @Override
    public void onSurfaceCreated() {
        //开始拍照
        boolean result = mCameraHelper.takePicture(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                if (mIsDestroyed) return;
                pictureTaken(data);
            }
        });
        if (!result) {
            //抓拍失败
            T.show("抓拍失败");
            finish();
        }
    }

    public void pictureTaken(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        //如果开启了人脸识别，且开启了停留报警，则要等回调后再报警
        ControlCenter.getDoorbellManager().sendDoorbellImg(ControlCenter.getSN(),
                bitmap,
                DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_ALARM,
                null);
        finish();
    }
}
