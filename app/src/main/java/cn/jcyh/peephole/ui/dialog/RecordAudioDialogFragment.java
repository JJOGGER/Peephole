package cn.jcyh.peephole.ui.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.entity.RecordingItem;
import cn.jcyh.peephole.service.RecordingService;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.GetImagePath;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.T;

import static android.app.Activity.RESULT_OK;

/**
 * 开始录音的 DialogFragment
 */

public class RecordAudioDialogFragment extends BaseDialogFragment {
    public static final int MAX_RECORDING = 9;
    private static final int CODE_GALLERY_REQUEST = 0X0c;
    @BindView(R.id.c_audio_time)
    Chronometer cAudioTime;
    @BindView(R.id.fab_play)
    FloatingActionButton fabPlay;
    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.tv_max)
    TextView tvMax;
    private static final String TAG = "RecordAudioDialogFragme";
    private boolean mStartRecording = true;
    private int mType;
    private RecordingItem mRecordingItem;

    public static RecordAudioDialogFragment newInstance(int type) {
        RecordAudioDialogFragment recordAudioDialogFragment = new RecordAudioDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.TYPE, type);
        recordAudioDialogFragment.setArguments(bundle);
        return recordAudioDialogFragment;
    }

    long timeWhenPaused = 0;

    @Override
    protected void init(View view) {
        super.init(view);
        setCancelable(false);
        tvMax.setText(String.format(getString(R.string.max_record_format), MAX_RECORDING-1));
        mType = getArguments().getInt(Constant.TYPE, Constant.TYPE_RING);
        cAudioTime.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if ((SystemClock.elapsedRealtime() - cAudioTime.getBase()) > MAX_RECORDING * 1000) {
                    cAudioTime.setBase(SystemClock.elapsedRealtime());
                    //停止录制
                    mStartRecording=false;
                    onRecord(false);
                }
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_record_audio;
    }

    @OnClick({R.id.iv_close, R.id.fab_play, R.id.ibtn_file})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.fab_play:
                onRecord(mStartRecording);
                break;
            case R.id.ibtn_file:
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, CODE_GALLERY_REQUEST);
                break;
        }
    }

    private void onRecord(boolean start) {
        Intent intent = new Intent(getActivity(), RecordingService.class);
        if (start) {
            L.e("------开始录音");
            resetRecordingItem();
            mRecordingItem = new RecordingItem();
            mRecordingItem.setName(mType == Constant.TYPE_RING ? "ring" : "alarm" + System
                    .currentTimeMillis());
            mRecordingItem.setFilePath((mType == Constant.TYPE_RING ? FileUtil.getExpandRingPath
                    () : FileUtil.getExpandAlarmPath())
                    + System.currentTimeMillis() + ".mp3");
            mRecordingItem.setRecord(true);
            intent.putExtra(Constant.RECORIDING_ITEM, mRecordingItem);
            fabPlay.setImageResource(R.mipmap.ic_media_stop);
            cAudioTime.setBase(SystemClock.elapsedRealtime());
            cAudioTime.start();
            getActivity().startService(intent);
        } else {
            fabPlay.setImageResource(R.mipmap.ic_mic_white_36dp);
            cAudioTime.stop();
            timeWhenPaused = 0;
            T.show(R.string.record_end);
            getActivity().stopService(intent);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            PlaybackDialogFragment fragmentPlay = PlaybackDialogFragment.newInstance
                    (mRecordingItem);
            fragmentPlay.show(getActivity().getSupportFragmentManager(), PlaybackDialogFragment
                    .class.getSimpleName());
            dismiss();
        }
        mStartRecording = !mStartRecording;
    }

    private void resetRecordingItem() {
        if (mRecordingItem != null) {
            File file = new File(mRecordingItem.getFilePath());
            if (file.exists())
                file.delete();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CODE_GALLERY_REQUEST:
                    assert data != null;
                    String path = GetImagePath.getPath(data.getData());
                    Log.e(TAG, "-----PATH:" + path);
                    if (!TextUtils.isEmpty(path)) {
                        File file = new File(path);
                        if (!file.exists()) return;
                        resetRecordingItem();
                        mRecordingItem = new RecordingItem();
                        mRecordingItem.setName(file.getName());
                        mRecordingItem.setFilePath(path);
                        mRecordingItem.setType(mType);
                        PlaybackDialogFragment fragmentPlay = PlaybackDialogFragment.newInstance
                                (mRecordingItem);
                        fragmentPlay.show(getActivity().getSupportFragmentManager(),
                                PlaybackDialogFragment.class.getSimpleName());
                    }
                    dismiss();
                    break;
            }
        }
    }
}
