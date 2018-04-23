package cn.jcyh.peephole.control;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;

/**
 * Created by jogger on 2018/2/3.
 */

public class DoorbellVideoHelper {

    private final AnyChatCoreSDK mAnyChat;

    public DoorbellVideoHelper() {
        mAnyChat = AnyChatCoreSDK.getInstance(null);
    }

    public void initView(Context context, SurfaceView surfaceView) {
        if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            surfaceView.getHolder().addCallback(AnyChatCoreSDK.mCameraHelper);
            Log.i("ANYCHAT", "VIDEOCAPTRUE---" + "JAVA");
        }
        if (AnyChatCoreSDK
                .GetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_CAPDRIVER) == AnyChatDefine.VIDEOCAP_DRIVER_JAVA) {
            AnyChatCoreSDK.mCameraHelper.SetContext(context.getApplicationContext());
            if (AnyChatCoreSDK.mCameraHelper.GetCameraNumber() > 1) {
                // 默认打开前置摄像头
                AnyChatCoreSDK.mCameraHelper.SelectVideoCapture(AnyChatCoreSDK.mCameraHelper.CAMERA_FACING_BACK);
            }
        } else {
            String[] strVideoCaptures = mAnyChat.EnumVideoCapture();
            if (strVideoCaptures != null && strVideoCaptures.length > 1) {
                // 默认打开前置摄像头
                for (String strDevices : strVideoCaptures) {
                    if (strDevices.indexOf("Front") >= 0) {
                        mAnyChat.SelectVideoCapture(strDevices);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 返回摄像头状态
     */
    public int getCameraState(int userId) {
        return mAnyChat.GetCameraState(userId);
    }

    /**
     * 返回视频宽度
     */
    public int getUserVideoWidth(int userId) {
        return mAnyChat.GetUserVideoWidth(userId);
    }

    /**
     * 返回视频高度
     */
    public int getUserVideoHeight(int userId) {
        return mAnyChat.GetUserVideoHeight(userId);
    }

    /**
     * 判断摄像头是否打开
     */
    public boolean isVideoOpen() {
        return getCameraState(-1) == 2 && getUserVideoWidth(-1) != 0;
    }

    public void setVideoHolder(SurfaceView surfaceView) {
        if (AnyChatCoreSDK.GetSDKOptionInt(AnyChatDefine.BRAC_SO_VIDEOSHOW_DRIVERCTRL) != AnyChatDefine.VIDEOSHOW_DRIVER_JAVA) {
            surfaceView.getHolder().setFormat(PixelFormat.RGB_565);
            surfaceView.getHolder().setFixedSize(getUserVideoWidth(-1), getUserVideoHeight(-1));
        }
        Surface s = surfaceView.getHolder().getSurface();
        mAnyChat.SetVideoPos(-1, s, 0, 0, 0, 0);
    }

    /**
     * 切换摄像头
     */
    public void changeCamera() {
        AnyChatCoreSDK.mCameraHelper.SwitchCamera();
    }

    /**
     * 音频控制
     */
    public void userSpeakControl(int userId, int volume) {
        mAnyChat.UserSpeakControl(userId, volume);
    }

    /**
     * 视频控制
     */
    public void userCameraControl(int userId, int bOpen) {
        mAnyChat.UserCameraControl(userId, bOpen);
    }
}
