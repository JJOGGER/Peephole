package cn.jcyh.peephole.video;

import android.app.Service;
import android.content.Context;
import android.widget.Toast;

import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoQuality;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import cn.jcyh.nimlib.config.AVChatConfigs;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.AVChatExitCode;
import cn.jcyh.peephole.service.video.AVChatControllerCallback;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.NetworkUtil;
import cn.jcyh.peephole.utils.T;

/**
 * 音视频控制器：用于实现音视频拨打接听，音视频切换的具体功能实现
 * Created by winnie on 2017/12/10.
 */

public class AVChatController {

    protected Context context;
    private long timeBase = 0;
    protected AVChatData avChatData;
    private AVChatCameraCapturer mVideoCapturer;
    private AVChatConfigs avChatConfigs;


    public AtomicBoolean isCallEstablish = new AtomicBoolean(false);
    private boolean destroyRTC = false;
    private boolean isRecording = false;

    /**
     * *************************** 初始化 ************************
     */

    public AVChatController(Context context, AVChatData avChatData) {
        this.context = context;
        this.avChatData = avChatData;
        this.avChatConfigs = new AVChatConfigs(context);
    }

    public void reCreateCameraCapturer() {
        try {
            L.e("-------------reCreateCameraCapturer");
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
//            mVideoCapturer.switchCamera();
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
            AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_DEFAULT_FRONT_CAMERA, false);
            AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
            AVChatManager.getInstance().enableVideo();
            AVChatManager.getInstance().startVideoPreview();
            AVChatManager.getInstance().accept2(avChatData.getChatId(), new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    isCallEstablish.set(true);
                    L.e("------onSuccess");
//                AVChatManager.getInstance().muteLocalVideo(false);
                }

                @Override
                public void onFailed(int code) {
                    L.e("------------建立连接失败:" + code);
                    if (code == -1) {
                        T.show(R.string.local_video_start_fail);
                    } else {
                        T.show(R.string.create_connect_fail);
                    }
                    hangUp(AVChatExitCode.CANCEL);
                }

                @Override
                public void onException(Throwable exception) {
                    L.e("----------exception:" + exception.getMessage());
                    AVChatManager.getInstance().stopVideoPreview();
                    AVChatManager.getInstance().disableVideo();
                    hangUp(AVChatExitCode.CANCEL);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AVChatCameraCapturer getVideoCapturer() {
        return mVideoCapturer;
    }

    public void receive(final AVChatControllerCallback<Void> callback) {
        AVChatManager.getInstance().enableRtc();
        if (mVideoCapturer == null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                mVideoCapturer = AVChatVideoCapturerFactory.createCamera2Capturer();
//            } else {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
//            }
//            mVideoCapturer.setZoom(10);
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
            try {
                AVChatManager.getInstance().setParameters(avChatConfigs.getAvChatParameters());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (NetworkUtil.NetworkType.NETWORK_WIFI.equals(NetworkUtil.getNetworkType())) {
            AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_QUALITY, AVChatVideoQuality.QUALITY_480P);
        } else {
            AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_QUALITY, AVChatVideoQuality.QUALITY_MEDIUM);
        }
        //设置流畅优先
//        AVChatManager.getInstance().setVideoQualityStrategy(AVChatVideoQualityStrategy.PreferImageQuality);
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FRAME_FILTER, true);
        AVChatManager.getInstance().enableVideo();
        AVChatManager.getInstance().startVideoPreview();
//        mVideoCapturer.setZoom(10);
        AVChatManager.getInstance().accept2(avChatData.getChatId(), new AVChatCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                isCallEstablish.set(true);
//                AVChatManager.getInstance().muteLocalVideo(false);
                callback.onSuccess(aVoid);



                L.e("----currentZoom:"+mVideoCapturer.getCurrentZoom()+
                        ";maxZoom:"+mVideoCapturer.getMaxZoom()+
                        ";hasMultipleCameras:"+AVChatCameraCapturer.hasMultipleCameras());
//                AVChatManager.getInstance().getParameters(AVChatParameters.)
//                L.e("----"+);
            }

            @Override
            public void onFailed(int code) {
                L.e("------------建立连接失败:" + code);
                if (code == -1) {
                    T.show(R.string.local_video_start_fail);
                } else {
                    T.show(R.string.create_connect_fail);
                }
                hangUp(AVChatExitCode.CANCEL);
                callback.onFailed(code, "");
            }

            @Override
            public void onException(Throwable exception) {
                AVChatManager.getInstance().stopVideoPreview();
                AVChatManager.getInstance().disableVideo();
                hangUp(AVChatExitCode.CANCEL);
                callback.onFailed(-1, exception.toString());
            }
        });
    }

    public void toggleMute() {
        if (!AVChatManager.getInstance().isLocalAudioMuted()) { // isMute是否处于静音状态
            // 关闭音频
            AVChatManager.getInstance().muteLocalAudio(true);
        } else {
            // 打开音频
            AVChatManager.getInstance().muteLocalAudio(false);
        }
    }


    // 设置扬声器是否开启
    public void toggleSpeaker() {
        AVChatManager.getInstance().setSpeaker(!AVChatManager.getInstance().speakerEnabled());
    }

    // 切换摄像头（主要用于前置和后置摄像头切换）
    public void switchCamera() {
        mVideoCapturer.switchCamera();
    }

    /**
     * ********************** 挂断相关操作 **********************
     */

    public void hangUp(int type) {
        if (destroyRTC) {
            return;
        }
        if ((type == AVChatExitCode.HANGUP || type == AVChatExitCode.PEER_NO_RESPONSE
                || type == AVChatExitCode.CANCEL || type == AVChatExitCode.REJECT || type == AVChatExitCode.FREQUENCY_LIMIT) && avChatData != null) {
            AVChatManager.getInstance().hangUp2(avChatData.getChatId(), new AVChatCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    L.e("-------------hangUp2");
                }

                @Override
                public void onFailed(int code) {
                    L.e("hangup onFailed->" + code);
                }

                @Override
                public void onException(Throwable exception) {
                    L.e("hangup onException->" + exception);
                }
            });
        }
        closeRtc();
//        destroyRTC = true;
//        AVChatSoundPlayer.instance().stop();
        showQuitToast(type);
    }

    // 收到挂断通知，自己的处理
    public void onHangUp(int exitCode) {
        if (destroyRTC) {
            return;
        }
//        AVChatSoundPlayer.instance().stop();
        closeRtc();
//        AVChatManager.getInstance().disableRtc();
//        destroyRTC = true;
        showQuitToast(exitCode);
        ((Service) context).stopSelf();
    }

    // 显示退出toast
    public void showQuitToast(int code) {
        switch (code) {
            case AVChatExitCode.NET_CHANGE: // 网络切换
            case AVChatExitCode.NET_ERROR: // 网络异常
            case AVChatExitCode.CONFIG_ERROR: // 服务器返回数据错误
                Toast.makeText(context, R.string.avchat_net_error_then_quit, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.REJECT:
                Toast.makeText(context, R.string.avchat_call_reject, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PEER_HANGUP:
            case AVChatExitCode.HANGUP:
                if (isCallEstablish.get()) {
                    Toast.makeText(context, R.string.avchat_call_finish, Toast.LENGTH_SHORT).show();
                }
                break;
            case AVChatExitCode.PEER_BUSY:
                Toast.makeText(context, R.string.avchat_peer_busy, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_PEER_LOWER:
                Toast.makeText(context, R.string.avchat_peer_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.PROTOCOL_INCOMPATIBLE_SELF_LOWER:
                Toast.makeText(context, R.string.avchat_local_protocol_low_version, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.INVALIDE_CHANNELID:
                Toast.makeText(context, R.string.avchat_invalid_channel_id, Toast.LENGTH_SHORT).show();
                break;
            case AVChatExitCode.LOCAL_CALL_BUSY:
                Toast.makeText(context, R.string.avchat_local_call_busy, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void closeRtc() {
        if (destroyRTC) {
            return;
        }
        AVChatManager.getInstance().stopVideoPreview();
        AVChatManager.getInstance().disableVideo();
        AVChatManager.getInstance().disableRtc();
        destroyRTC = true;
    }

//    private void handleAcceptFailed(CallStateEnum callingState) {
//        if (callingState == CallStateEnum.VIDEO_CONNECTING) {
//            AVChatManager.getInstance().stopVideoPreview();
//            AVChatManager.getInstance().disableVideo();
//        }
//        hangUp(AVChatExitCode.CANCEL);
//    }

    /**
     * ************************* 其他数据 ***********************
     */

    public long getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(long timeBase) {
        this.timeBase = timeBase;
    }

    public AVChatData getAvChatData() {
        return avChatData;
    }

    public void setAvChatData(AVChatData avChatData) {
        this.avChatData = avChatData;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

}
