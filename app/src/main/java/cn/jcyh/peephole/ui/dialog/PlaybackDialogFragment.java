package cn.jcyh.peephole.ui.dialog;

import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.RecordingItem;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.L;

/**
 * 播放录音的 DialogFragment
 * <p>
 * Created by developerHaoz on 2017/8/12.
 */

public class PlaybackDialogFragment extends BaseDialogFragment {
    @BindView(R.id.sb_progress)
    SeekBar sbProgress;
    @BindView(R.id.tv_file_length)
    TextView tvFileLength;
    @BindView(R.id.tv_file_name)
    TextView tvFileName;
    @BindView(R.id.tv_current_progress)
    TextView tvCurrentProgress;
    @BindView(R.id.fab_play)
    FloatingActionButton fabPlay;
    private static final String LOG_TAG = "PlaybackFragment";
    private RecordingItem mRecordingItem;

    private Handler mHandler = new Handler();

    private MediaPlayer mMediaPlayer = null;

    private boolean mIsPlaying = false;

    long mMinutes = 0;
    long mSeconds = 0;

    public static PlaybackDialogFragment newInstance(RecordingItem item) {
        PlaybackDialogFragment f = new PlaybackDialogFragment();
        Bundle b = new Bundle();
        b.putParcelable(Constant.RECORIDING_ITEM, item);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        mRecordingItem = getArguments().getParcelable(Constant.RECORIDING_ITEM);
        assert mRecordingItem != null;
        long itemDuration = mRecordingItem.getLength();
        mMinutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        mSeconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(mMinutes);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_media_playback;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void init(View view) {
        super.init(view);
        ColorFilter filter = new LightingColorFilter
                (getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color
                        .colorPrimary));
        sbProgress.getProgressDrawable().setColorFilter(filter);
        sbProgress.getThumb().setColorFilter(filter);
        tvFileLength.setText(String.valueOf(mRecordingItem.getLength()));
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress);
                    mHandler.removeCallbacks(mRunnable);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer
                            .getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer
                            .getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    tvCurrentProgress.setText(String.format("%02d:%02d", minutes, seconds));
                    updateSeekBar();

                } else if (mMediaPlayer == null && fromUser) {
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable);
                    mMediaPlayer.seekTo(seekBar.getProgress());

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer
                            .getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer
                            .getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    tvCurrentProgress.setText(String.format("%02d:%02d", minutes, seconds));
                    updateSeekBar();
                }
            }
        });

        tvFileName.setText(mRecordingItem.getName());
        tvFileLength.setText(String.format("%02d:%02d", mMinutes, mSeconds));
        onPlay(mIsPlaying);
    }

    @OnClick({R.id.iv_close, R.id.fab_play, R.id.tv_save, R.id.tv_re_record})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                if (mRecordingItem.isRecord()) {
                    File file = new File(mRecordingItem.getFilePath());
                    if (file.exists())
                        file.delete();
                }
                dismiss();
                break;
            case R.id.fab_play:
                onPlay(mIsPlaying);
                break;
            case R.id.tv_save:
                Toast.makeText(getActivity(), "保存", Toast.LENGTH_LONG).show();
                save();
                dismiss();
                break;
            case R.id.tv_re_record:
                if (mRecordingItem.isRecord()) {
                    File file = new File(mRecordingItem.getFilePath());
                    if (file.exists())
                        file.delete();
                }
                final RecordAudioDialogFragment fragment = RecordAudioDialogFragment.newInstance(mRecordingItem.getType());
                fragment.show(getActivity().getSupportFragmentManager(),
                        RecordAudioDialogFragment.class.getSimpleName());
                dismiss();
                break;
        }
    }

    private void save() {
        if (mRecordingItem.isRecord()) return;
        //从文件夹中选取的，如果不在目录下，则拷到目录
        File file;
        if (mRecordingItem.getType() == Constant.TYPE_RING) {
            file=new File(FileUtil.getExpandRingPath());
        } else {
            file=new File(FileUtil.getExpandAlarmPath());
        }
        if (!file.exists()) return;
        File file2=new File(mRecordingItem.getFilePath());
        for (int i = 0; i < file.list().length; i++) {
            if (mRecordingItem.getName().equals(file2.getName())){
                return;
            }
        }
        FileUtil.copyFile(mRecordingItem.getFilePath(),file.getAbsolutePath()+File.separator+mRecordingItem.getName());
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    private void onPlay(boolean isPlaying) {
        if (!isPlaying) {
            if (mMediaPlayer == null) {
                startPlaying();
            } else {
                resumePlaying();
            }

        } else {
            pausePlaying();
        }
        mIsPlaying = !isPlaying;
    }

    private void startPlaying() {
        fabPlay.setImageResource(R.mipmap.ic_media_pause);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mRecordingItem.getFilePath());
            mMediaPlayer.prepare();
            sbProgress.setProgress(0);
            sbProgress.setMax(mMediaPlayer.getDuration());

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });
        updateSeekBar();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void prepareMediaPlayerFromPoint(int progress) {
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(mRecordingItem.getFilePath());
            mMediaPlayer.prepare();
            sbProgress.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(progress);

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void pausePlaying() {
        fabPlay.setImageResource(R.mipmap.ic_media_play);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.pause();
    }

    private void resumePlaying() {
        fabPlay.setImageResource(R.mipmap.ic_media_pause);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.start();
        updateSeekBar();
    }

    private void stopPlaying() {
        fabPlay.setImageResource(R.mipmap.ic_media_play);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        sbProgress.setProgress(sbProgress.getMax());
        mIsPlaying = !mIsPlaying;

        tvCurrentProgress.setText(tvFileLength.getText());
        sbProgress.setProgress(sbProgress.getMax());

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {

                int mCurrentPosition = mMediaPlayer.getCurrentPosition();
                long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition)
                        - TimeUnit.MINUTES.toSeconds(minutes);
                if (minutes == 0 && seconds == 0) {
                    sbProgress.setProgress(0);
                } else {
                    sbProgress.setProgress(mCurrentPosition);
                }
                L.e("--------mCurrentPosition：" + mCurrentPosition + "::" + minutes + ":" + seconds);
                tvCurrentProgress.setText(String.format("%02d:%02d", minutes, seconds));

                updateSeekBar();
            }
        }
    };

    private void updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000);
    }
}
