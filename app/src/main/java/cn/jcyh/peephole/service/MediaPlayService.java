package cn.jcyh.peephole.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import cn.jcyh.peephole.constant.Constant;

/**
 * Created by jogger on 2018/7/17.
 * 铃声播放
 */
public class MediaPlayService extends Service {
    private int mPlayCount = 1;//播放次数
    private MediaPlayer mPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String resourcePath = intent.getStringExtra(Constant.RESOURCE_PATH);
            float volume = intent.getFloatExtra(Constant.VOLUME, 0.5f);
            mPlayCount = intent.getIntExtra(Constant.PLAY_COUNT, 1);
            if (TextUtils.isEmpty(resourcePath)) stopSelf();
            try {
                AssetFileDescriptor descriptor;
                AssetManager assets = getResources().getAssets();
                if (mPlayer == null) {
                    mPlayer = new MediaPlayer();
                    mPlayer.setLooping(false);
                } else {
                    mPlayer.stop();
                    mPlayer.reset();
                }
                descriptor = assets.openFd(resourcePath);
                mPlayer.setVolume(volume, volume);
                mPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                mPlayer.prepare();
                mPlayer.start();
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mPlayCount--;
                        if (mPlayCount <= 0) {
                            mPlayer.release();
                            mPlayer = null;
                            mPlayCount = 1;
                            stopSelf();
                        } else {
                            mPlayer.start();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {

        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
