package cn.jcyh.peephole.ui.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.kongqw.ObjectDetectingView;
import com.kongqw.ObjectDetector;
import com.kongqw.listener.OnOpenCVLoadListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.constant.ExtendFunction;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.service.AudioValiService;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.ServiceUtil;
import cn.jcyh.peephole.utils.SystemUtil;
import cn.jcyh.peephole.utils.T;

public class ObjectDetectingActivity extends BaseActivity {
    private static final String TAG = "ObjectDetectingActivity";
    @BindView(R.id.photograph_view)
    ObjectDetectingView photographView;
    private ObjectDetector mFaceDetector;
    //采用身份识别接口进行在线人脸识别
    private IdentityVerifier mIdVerifier;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean mVering;

    @Override
    public int getLayoutId() {
        return R.layout.activity_object_detecting;
    }

    @Override
    protected void init() {
        //点亮屏幕
        ControlCenter.sIsFaceValing = true;
        SystemUtil.wakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP);
        mIdVerifier = IdentityVerifier.createVerifier(ObjectDetectingActivity.this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    T.show("引擎初始化成功");
                } else {
                    T.show("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });
        photographView.setOnOpenCVLoadListener(new OnOpenCVLoadListener() {
            @Override
            public void onOpenCVLoadSuccess() {
                L.e("----------OpenCV 加载成功");
                mFaceDetector = new ObjectDetector(getApplicationContext(), R.raw.lbpcascade_frontalface, 6, 0.2F, 0.2F, new Scalar(255, 0, 0, 255));
                photographView.addDetector(mFaceDetector);
            }

            @Override
            public void onOpenCVLoadFail() {
                L.e("---------OpenCV 加载失败");
            }

            @Override
            public void onNotInstallOpenCVManager() {
                L.e("-------------onNotInstallOpenCVManager:");
            }
        });

        photographView.loadOpenCV(getApplicationContext());
        photographView.setOnCameraFrameListener(new ObjectDetectingView.OnCameraFrameListener() {
            @Override
            public void onDetect(Rect rect) {
                //检测到人脸
                mVering = true;
                photographView.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        onTakePhoto(data);
                    }
                });
            }
        });
        photographView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isDestroyed() || isFinishing() || getSupportFragmentManager() == null) {
                    return;
                }
                cancelTimer();
                if (mVering) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing()||isDestroyed()||getSupportFragmentManager()==null)return;
                        T.show("验证结束");
                        finish();
                    }
                });
            }
        };
        mTimer.schedule(mTimerTask, 6000, 1000);
    }

    private void onTakePhoto(byte[] data) {
        valiFace(data);
    }

    private void valiFace(byte[] data) {
        if (null != data) {
            // 设置人脸验证参数
            // 清空参数
            mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
            // 设置会话场景
            mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
            // 设置会话类型
            mIdVerifier.setParameter(SpeechConstant.MFV_SST, "verify");
            // 设置验证模式，单一验证模式：sin
            mIdVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
            // 用户id
            mIdVerifier.setParameter(SpeechConstant.AUTH_ID, SystemUtil.getANDROID_ID());
            // 设置监听器，开始会话
            mIdVerifier.startWorking(mVerifyListener);

            // 子业务执行参数，若无可以传空字符传
            StringBuffer params = new StringBuffer();
            // 向子业务写入数据，人脸数据可以一次写入
            mIdVerifier.writeData("ifr", params.toString(), data, 0, data.length);
            // 停止写入
            mIdVerifier.stopWrite("ifr");
        } else {
            T.show("请选择图片后再验证");
        }
    }

    /**
     * 切换摄像头
     *
     * @param view view
     */
    public void swapCamera(View view) {
        photographView.swapCamera();
    }


    /**
     * 人脸验证监听器
     */
    private IdentityListener mVerifyListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.e(TAG, result.getResultString());


            try {
                JSONObject object = new JSONObject(result.getResultString());
                String decision = object.getString("decision");

                if ("accepted".equalsIgnoreCase(decision)) {
                    T.show("通过验证");
                    audioCheck();
                } else {
                    T.show("验证失败");
                }
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            T.show("error:"+error.getPlainDescription(true));
            finish();
        }

    };

    /**
     * 声纹识别检测
     */
    private void audioCheck() {
        //检测声纹识别
        boolean audioVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_AUDIO_VALI);
        if (audioVali) {
//            if (ServiceUtil.isServiceRunning(AudioValiService.class))
//                ServiceUtil.stopService(AudioValiService.class);
            ServiceUtil.startService(AudioValiService.class);
        } else {
            ControlCenter.sIsFaceValing = false;
            //没有声纹，直接开锁
            ControlCenter.getBCManager().setLock(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        if (!ServiceUtil.isServiceRunning(AudioValiService.class)) {
            ControlCenter.sIsFaceValing = false;
        }
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }
}
