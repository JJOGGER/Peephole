package cn.jcyh.peephole.ui.dialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.entity.RecordingItem;

import static android.content.Context.MODE_PRIVATE;

/**
 * 开始录音的 DialogFragment
 * <p>
 * Created by developerHaoz on 2017/8/12.
 */

public class RecordAudioDialogFragment extends BaseDialogFragment {
    @BindView(R.id.c_audio_time)
    Chronometer cAudioTime;
    @BindView(R.id.fab_play)
    FloatingActionButton fabPlay;
    @BindView(R.id.iv_close)
    ImageView ivClose;
    private static final String TAG = "RecordAudioDialogFragme";
    private int mRecordPromptCount = 0;
    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;
    public static RecordAudioDialogFragment newInstance(){
        return new RecordAudioDialogFragment();
    }
    long timeWhenPaused = 0;
    private OnAudioCancelListener mListener;

    @Override
    protected void init(View view) {
        super.init(view);
    }

    @Override
    int getLayoutId() {
        return R.layout.fragment_record_audio;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick({R.id.iv_close,R.id.fab_play})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.iv_close:
                mListener.onCancel();
                break;
            case R.id.fab_play:
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
                break;
        }
    }
    private void onRecord(boolean start) {

        Intent intent = new Intent(getActivity(), RecordingService.class);

        if (start) {
            // start recording
            fabPlay.setImageResource(R.mipmap.ic_media_stop);
            //mPauseButton.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "开始录音...", Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                folder.mkdir();
            }
            cAudioTime.setBase(SystemClock.elapsedRealtime());
            cAudioTime.start();
            getActivity().startService(intent);
        } else {
            fabPlay.setImageResource(R.mipmap.ic_mic_white_36dp);
            cAudioTime.stop();
            timeWhenPaused = 0;
            Toast.makeText(getActivity(), "录音结束...", Toast.LENGTH_SHORT).show();
            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            RecordingItem recordingItem = new RecordingItem();
            SharedPreferences sharePreferences = getActivity().getSharedPreferences
                    ("sp_name_audio", MODE_PRIVATE);
            final String filePath = sharePreferences.getString("audio_path", "");
            long elpased = sharePreferences.getLong("elpased", 0);
            recordingItem.setFilePath(filePath);
            recordingItem.setLength((int) elpased);
            PlaybackDialogFragment fragmentPlay = PlaybackDialogFragment.newInstance(recordingItem);
            fragmentPlay.show(getActivity().getSupportFragmentManager(), PlaybackDialogFragment
                    .class.getSimpleName());
            dismiss();
        }
    }

    public void setOnCancelListener(OnAudioCancelListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    onRecord(mStartRecording);
                }
                break;
        }
    }

    public interface OnAudioCancelListener {
        void onCancel();
    }
}
