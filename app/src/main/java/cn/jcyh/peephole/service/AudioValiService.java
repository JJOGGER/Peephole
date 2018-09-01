package cn.jcyh.peephole.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.record.PcmRecorder;
import com.iflytek.sunflower.FlowerCollector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cn.jcyh.peephole.utils.L;

/**
 * Created by jogger on 2018/9/1.声纹识别
 */
public class AudioValiService extends Service {
    // 密码类型
    // 默认为数字密码
    private int mPwdType = 3;
    // 会话类型
    private int mSST = 0;
    // 注册
    private static final int SST_ENROLL = 0;
    // 验证
    private static final int SST_VERIFY = 1;
    // 身份验证对象
    private IdentityVerifier mIdVerifier;
    // 数字声纹密码
    private String mNumPwd = "";
    // 用于验证的数字密码
    private String mVerifyNumPwd = "好的";
    // 是否可以录音
    private boolean mCanStartRecord = false;
    // 是否可以录音
    private boolean isStartWork = false;
    // 录音采样率
    private final int SAMPLE_RATE = 16000;
    // pcm录音机
    private PcmRecorder mPcmRecorder;
    // 默认发音人
    private String voicer = "xiaoyan";
    private SpeechSynthesizer mSpeechSynthesizer;//语音合成对象
    private SynthesizerListenerAdapter mSynthesizerListener;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private Timer mValiTimer;
    private TimerTask mValiTask;
    private boolean mIsSatrt = true;
    private boolean mIsFinish = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.e("---------onCreate");
        isStartWork = false;
        if (mIdVerifier != null)
            mIdVerifier.cancel();
        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }
        mIdVerifier = IdentityVerifier.createVerifier(this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    L.i("----引擎初始化成功");
                    // 设置会话类型为验证
                    mSST = SST_VERIFY;
//                    mVerifyNumPwd = VerifierUtil.generateNumberPassword(8);
                    initSynthesizer();
                } else {
                    L.i("-----引擎初始化失败，错误码：" + errorCode);
                }
            }
        });
    }

    private void initUi() {
        mIsSatrt = false;
        if (null == mIdVerifier) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
//            ToastUtil.showToast(getApplicationContext(), "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            L.i("---------->创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }
        if (!isStartWork) {
            // 根据业务类型调用服务
            vocalVerify();
            isStartWork = true;
            mCanStartRecord = true;
        }
        try {
            mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
            mPcmRecorder.startRecording(mPcmRecordListener);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    if (isFinishing() && getSupportFragmentManager() == null) return;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (mIdVerifier != null)
//                                mIdVerifier.stopWrite("ivp");
//                            if (null != mPcmRecorder) {
//                                mPcmRecorder.stopRecord(true);
//                            }
//                            finish();
//                        }
//                    });
                }
            }).start();
        } catch (SpeechError e) {
            e.printStackTrace();
        }
    }

    /**
     * 声纹验证监听器
     */
    private IdentityListener mVerifyListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            L.e("----------IdentityListenerresult" + result.getResultString());
            try {
                JSONObject object = new JSONObject(result.getResultString());
                String decision = object.getString("decision");
                mIsFinish = true;
                if ("accepted".equalsIgnoreCase(decision)) {
                    L.e("-----------验证通过");
                    mSpeechSynthesizer.startSpeaking("验证通过", mSynthesizerListener);
                } else {
                    L.e("-----------验证失败");
                    mSpeechSynthesizer.startSpeaking("验证失败", mSynthesizerListener);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            isStartWork = false;
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_VOLUME == eventType) {
                L.i("----音量：" + arg1);
            } else if (SpeechEvent.EVENT_VAD_EOS == eventType) {
                L.i("----录音结束");
            }
        }

        @Override
        public void onError(SpeechError error) {
            isStartWork = false;
            mCanStartRecord = false;

            StringBuffer errorResult = new StringBuffer();
            errorResult.append("验证失败！\n");
            errorResult.append("错误信息：" + error.getPlainDescription(true) + "\n");
            errorResult.append("请长按“按住说话”重新验证!");
            L.e("-------onError" + errorResult.toString());
            mIsFinish = true;
            mSpeechSynthesizer.startSpeaking("验证失败", mSynthesizerListener);
            stopSelf();
        }
    };

    /**
     * 初始化语音合成
     */
    private void initSynthesizer() {
        // 初始化合成对象
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(this, mSynthesizerInitListener);
        // 移动数据分析，收集开始合成事件
        FlowerCollector.onEvent(this, "tts_play");
        mSynthesizerListener = new SynthesizerListenerAdapter();
        setSynthesizerParam();
    }

    /**
     * 初始化监听。
     */
    private InitListener mSynthesizerInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                L.e("---------------失败还玩毛线");
            }
        }
    };

    private class SynthesizerListenerAdapter implements SynthesizerListener {

        @Override
        public void onSpeakBegin() {
            L.e("------>开始播放");
        }

        @Override
        public void onSpeakPaused() {
            L.e("------>暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            L.e("------>继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            L.e("------>暂停播放");
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                L.e("------>播放完成");
                if (mIsSatrt)
                    initUi();
                if (mIsFinish)
                    stopSelf();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    }

    /**
     * 设置语音合成参数
     */
    private void setSynthesizerParam() {
        // 清空参数
        mSpeechSynthesizer.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
            //设置合成音调
            mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
            //设置合成音量
            mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        } else {
            mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        //设置播放器音频流类型
        mSpeechSynthesizer.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mSpeechSynthesizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        mSpeechSynthesizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        int code = mSpeechSynthesizer.startSpeaking("欢迎回家，需要为您开门么", mSynthesizerListener);
        if (code != ErrorCode.SUCCESS) {
            L.e("---------语音合成失败,错误码: " + code);
            stopSelf();
            return;
        }
        mValiTimer = new Timer();
        mValiTask = new TimerTask() {
            @Override
            public void run() {
                stopSelf();
                //验证结束
                L.e("------------timer");
                if (mIdVerifier != null)
                    mIdVerifier.stopWrite("ivp");
                if (null != mPcmRecorder) {
                    mPcmRecorder.stopRecord(true);
                }
                cancelTimer();
            }
        };
        mValiTimer.schedule(mValiTask, 5000, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.e("-----------onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private void cancelTimer() {
        if (mValiTimer != null) {
            mValiTimer.cancel();
            mValiTimer.purge();
            mValiTimer = null;
        }
    }

    /**
     * 录音机监听器
     */
    private PcmRecorder.PcmRecordListener mPcmRecordListener = new PcmRecorder.PcmRecordListener() {

        @Override
        public void onRecordStarted(boolean success) {
            L.e("--------onRecordStarted:" + success);
        }

        @Override
        public void onRecordReleased() {
            L.e("------onRecordReleased" + mIdVerifier);
        }

        @Override
        public void onRecordBuffer(byte[] data, int offset, int length) {
            StringBuffer params = new StringBuffer();
            L.e("------onRecordBuffer" + mSST);
            switch (mSST) {
                case SST_ENROLL:
                    params.append("rgn=5,");
                    params.append("ptxt=" + mNumPwd + ",");
                    params.append("pwdt=" + mPwdType + ",");
                    mIdVerifier.writeData("ivp", params.toString(), data, 0, length);
                    break;
                case SST_VERIFY:
                    params.append("ptxt=" + mVerifyNumPwd + ",");
                    params.append("pwdt=" + mPwdType + ",");
                    mIdVerifier.writeData("ivp", params.toString(), data, 0, length);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(SpeechError e) {
            L.e("-------->" + e.getMessage());
        }
    };

    private void vocalVerify() {
        L.e("------------vocalVerify");
        // 设置声纹验证参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
        // 设置会话类型
        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "verify");
        // 验证模式，单一验证模式：sin
        mIdVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
        // 用户的唯一标识，在声纹业务获取注册、验证、查询和删除模型时都要填写，不能为空
//        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, IMEI);
        // 设置监听器，开始会话
        mIdVerifier.startWorking(mVerifyListener);
    }

    @Override
    public void onDestroy() {
        if (null != mIdVerifier) {
            mIdVerifier.destroy();
            mIdVerifier = null;
            L.e("--------onDestroy");
        }
        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }
        cancelTimer();
        super.onDestroy();
    }

}
