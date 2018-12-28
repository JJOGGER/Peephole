package cn.jcyh.peephole.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.TimerTask;

import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.entity.RecordingItem;
import cn.jcyh.peephole.utils.L;

/**
 * 录音的 Service
 * <p>
 * Created by developerHaoz on 2017/8/12.
 */

public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";
    private MediaRecorder mRecorder = null;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private TimerTask mIncrementTimerTask = null;
    private RecordingItem mRecordingItem;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mRecordingItem = intent.getParcelableExtra(Constant.RECORIDING_ITEM);
            startRecording();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }
        super.onDestroy();
    }

    public void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        L.e("------------mRecordingItem"+mRecordingItem.getFilePath());
        mRecorder.setOutputFile(mRecordingItem.getFilePath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(192000);
        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();
        } catch (IOException e) {
            Log.e(LOG_TAG, "-----------"+e.getMessage());
        }
    }

    public void stopRecording() {
        mRecorder.setOnErrorListener(null);
        mRecorder.setOnInfoListener(null);
        mRecorder.setPreviewDisplay(null);
        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
            L.e("---------e" + e.getMessage());
        }
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }
        mRecorder = null;
    }

}
