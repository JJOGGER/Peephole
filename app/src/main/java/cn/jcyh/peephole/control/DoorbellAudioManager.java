package cn.jcyh.peephole.control;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.text.TextUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.jcyh.peephole.constant.AssetConstant;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.Util;

/**
 * SoundPool 铃声尽量不要超过1M
 * 在不同的系统下 SoundPool 表现可能存在不一致
 */
public class DoorbellAudioManager {

    private boolean mLoop;
    private String mResPath;
    private float mVolume;
    private ExecutorService mExecutorService;
    private static DoorbellAudioManager instance = null;
    private boolean mIsPlaying = false;
    private MediaPlayer mPlayer;
    private int mPlayCount = 1;//播放次数
    private Context mContext;
    private RingerTypeEnum mCurrentType;

    public enum RingerTypeEnum {
        LEAVE_MSG_START,
        LEAVE_MSG_END,
        DOORBELL_RING,
        DOORBELL_ALARM,
        DOORBELL_RING_CUSTOM,
        DOORBELL_ALARM_CUSTOM
    }

    public interface OnCompletionListener {
        void onCompletion();
    }

    public static DoorbellAudioManager getDoorbellAudioManager() {
        if (instance == null) {
            synchronized (DoorbellAudioManager.class) {
                if (instance == null) {
                    instance = new DoorbellAudioManager();
                }
            }
        }
        return instance;
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public boolean isPlaying(RingerTypeEnum typeEnum) {
        return mCurrentType == typeEnum && mIsPlaying;
    }

    private DoorbellAudioManager() {
        mExecutorService = Executors.newSingleThreadExecutor();
        mContext = Util.getApp();
    }

    /**
     * 播放资源文件
     */
    public synchronized void playAssets(RingerTypeEnum typeEnum) {
        switch (typeEnum) {
            case DOORBELL_RING_CUSTOM:
                break;
            case DOORBELL_ALARM_CUSTOM:
                break;
        }
    }

    public synchronized void playFile(RingerTypeEnum typeEnum, String path) {
        mCurrentType = typeEnum;
        switch (typeEnum) {
            case DOORBELL_RING_CUSTOM:
                mResPath = path;
                mVolume = 0.5f;
                mPlayCount = 1;
                ControlCenter.getBCManager().setMainSpeakerOn(true);
                break;
            case DOORBELL_ALARM_CUSTOM:
                break;
        }
        mLoop = false;
        playFile();
    }

    private void playFile() {
        if (TextUtils.isEmpty(mResPath)) return;
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    L.e("--------------播放文件：" + mResPath);
                    AssetFileDescriptor descriptor;
                    AssetManager assets = mContext.getResources().getAssets();
                    if (mPlayer != null) {
                        mPlayer.stop();
                        mPlayer.reset();
                        mPlayer.release();
                        mPlayer = null;
                    }
                    mPlayer = new MediaPlayer();
                    mPlayer.setLooping(false);
                    mPlayer.setDataSource(mResPath);
                    mPlayer.setVolume(mVolume, mVolume);
                    mPlayer.prepare();
                    mPlayer.start();
                    mIsPlaying = true;
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mPlayCount--;
                            if (mPlayCount <= 0) {
                                mPlayer.release();
                                mPlayer = null;
                                mPlayCount = 1;
                                mIsPlaying = false;
                            } else {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mPlayer.start();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized void play(RingerTypeEnum typeEnum, OnCompletionListener listener) {
        mCurrentType = typeEnum;
        mLoop = false;
        switch (typeEnum) {
            case LEAVE_MSG_START:
                mResPath = AssetConstant.DOORBELL_LEAVE_MSG_START;
                mVolume = 0.5f;
                ControlCenter.getBCManager().setMainSpeakerOn(false);
                play(listener);
                break;
            case LEAVE_MSG_END:
                mResPath = AssetConstant.DOORBELL_LEAVE_MSG_END;
                mVolume = 0.5f;
                ControlCenter.getBCManager().setMainSpeakerOn(false);
                play(listener);
                break;
            case DOORBELL_RING:
                DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager()
                        .getDoorbellConfig();
                mVolume = doorbellConfig.getRingVolume() / 100f;
                ControlCenter.getBCManager().setMainSpeakerOn(true);
                L.e("--------->"+doorbellConfig.getDoorbellRingName());
                mResPath = doorbellConfig.getDoorbellRingName();
                mPlayCount = 3;
                play(listener);
                break;
            case DOORBELL_ALARM:
                doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
                mResPath = doorbellConfig.getDoorbellAlarmName();
                mVolume = doorbellConfig.getAlarmVolume() / 100f;
                ControlCenter.getBCManager().setMainSpeakerOn(false);
                play(listener);
                break;
            case DOORBELL_RING_CUSTOM:
                doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
                mVolume = doorbellConfig.getRingVolume() / 100f;
                ControlCenter.getBCManager().setMainSpeakerOn(true);
                L.e("--------doorbellConfig.getCustomDoorbellRingName():" + doorbellConfig
                        .getCustomDoorbellRingName());
                if (!TextUtils.isEmpty(doorbellConfig.getCustomDoorbellRingName())) {
                    mResPath = doorbellConfig.getCustomDoorbellRingName();
                    mPlayCount = 1;
                    playFile();
                } else {
                    mResPath = doorbellConfig.getDoorbellRingName();
                    mPlayCount = 3;
                    play(listener);
                }
                break;
            case DOORBELL_ALARM_CUSTOM:
                doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
                mResPath = doorbellConfig.getDoorbellAlarmName();
                mVolume = doorbellConfig.getAlarmVolume() / 100f;
                ControlCenter.getBCManager().setMainSpeakerOn(false);
                if (!TextUtils.isEmpty(doorbellConfig.getCustomDoorbellAlarmName())) {
                    mResPath = doorbellConfig.getCustomDoorbellAlarmName();
                    playFile();
                } else {
                    mResPath = doorbellConfig.getDoorbellAlarmName();
                    play(listener);
                }
                break;
        }
    }


    public void stop() {
        L.e("-----结束播放:" + mExecutorService.isShutdown());
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        mIsPlaying = false;
    }

    private void play(final OnCompletionListener listener) {
        L.e("------------>>"+mCurrentType+":"+mResPath);
        if (TextUtils.isEmpty(mResPath)) return;
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    L.e("--------------播放文件：" + mResPath);
                    AssetFileDescriptor descriptor;
                    AssetManager assets = mContext.getResources().getAssets();
                    if (mPlayer != null) {
                        mPlayer.stop();
                        mPlayer.reset();
                        mPlayer.release();
                        mPlayer = null;
                    }
                    mPlayer = new MediaPlayer();
                    mPlayer.setLooping(false);
                    descriptor = assets.openFd(mResPath);
                    mPlayer.setVolume(mVolume, mVolume);
                    mPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor
                            .getStartOffset(), descriptor.getLength());
                    mPlayer.prepare();
                    mPlayer.start();
                    mIsPlaying = true;
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mPlayCount--;
                            if (mPlayCount <= 0) {
                                mPlayer.release();
                                mPlayer = null;
                                mPlayCount = 1;
                                mIsPlaying = false;
                                if (listener != null)
                                    listener.onCompletion();
                            } else {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mPlayer.start();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
