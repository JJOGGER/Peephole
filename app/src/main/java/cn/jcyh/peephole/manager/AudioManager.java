package cn.jcyh.peephole.manager;

import android.os.Bundle;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;

import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.Util;

/**
 * Created by jogger on 2018/6/29.
 */
public class AudioManager {
    private SpeechSynthesizer mSpeechSynthesizer;//语音合成对象
    private SynthesizerListenerAdapter mSynthesizerListener;
    // 默认发音人
    private String voicer = "xiaoyan";
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private OnSpeechListener mOnSpeechListener;

    public interface OnSpeechListener {
        void onSpeakCompleted();
    }

    public void setOnSpeechListener(OnSpeechListener listener) {
        mOnSpeechListener = listener;
    }

    public AudioManager() {
        initSynthesizer();
    }

    public void startSpeaking(String result) {
        if (mSpeechSynthesizer == null) return;
        mSpeechSynthesizer.startSpeaking(result, mSynthesizerListener);
    }

    public void startSpeaking(int resultId) {
        if (mSpeechSynthesizer == null) return;
        mSpeechSynthesizer.startSpeaking(Util.getApp().getString(resultId), mSynthesizerListener);
    }

    public void stopSpeaking() {
        if (mSpeechSynthesizer == null) return;
        mSpeechSynthesizer.stopSpeaking();
    }

    /**
     * 初始化语音合成
     */
    private void initSynthesizer() {
        // 初始化合成对象
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(Util.getApp(), mSynthesizerInitListener);
        // 移动数据分析，收集开始合成事件
        FlowerCollector.onEvent(Util.getApp(), "tts_play");
        mSynthesizerListener = new SynthesizerListenerAdapter();
        if (mSpeechSynthesizer == null) return;
        setSynthesizerParam();
    }

    /**
     * 初始化监听。
     */
    private InitListener mSynthesizerInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
            }
        }
    };

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
    }

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
            if (mOnSpeechListener != null)
                mOnSpeechListener.onSpeakCompleted();
            if (error == null) {
                L.e("------>播放完成");
            } else {
                L.e("------>error");
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    }
}
