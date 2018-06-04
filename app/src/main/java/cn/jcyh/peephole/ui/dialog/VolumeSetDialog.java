package cn.jcyh.peephole.ui.dialog;

import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.SeekBar;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;

/**
 * Created by jogger on 2018/5/11.
 * 音量设置
 */

public class VolumeSetDialog extends BaseDialogFragment implements SeekBar.OnSeekBarChangeListener {
    @BindView(R.id.sb_doorbell)
    SeekBar sbDoorbell;
    @BindView(R.id.sb_alarm)
    SeekBar sbAlarm;
    private MediaPlayer mPlayer;
    private DoorbellConfig mDoorbellConfig;
    private static final int TYPE_RING = 0X01;
    private static final int TYPE_ALARM = 0X02;

    @Override
    int getLayoutId() {
        return R.layout.dialog_volume_set;
    }

    @Override
    protected void init(View view) {
        sbDoorbell.setOnSeekBarChangeListener(this);
        sbAlarm.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDoorbellConfig = DoorBellControlCenter.getInstance().getDoorbellConfig();
        sbDoorbell.setProgress(mDoorbellConfig.getRingVolume());
        sbAlarm.setProgress(mDoorbellConfig.getAlarmVolume());
    }

    @OnClick(R.id.tv_confirm)
    public void onClick(View v) {
        DoorBellControlCenter.getInstance().saveDoorbellConfig(mDoorbellConfig);
        dismiss();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.sb_doorbell:
                mDoorbellConfig.setRingVolume(seekBar.getProgress());
                play(TYPE_RING, seekBar.getProgress());
                break;
            case R.id.sb_alarm:
                mDoorbellConfig.setAlarmVolume(seekBar.getProgress());
                play(TYPE_ALARM, seekBar.getProgress());
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void play(int type, int progress) {
        try {
            AssetFileDescriptor descriptor;
            AssetManager assets = getResources().getAssets();
            if (type == TYPE_RING) {
                descriptor = assets.openFd("ring/" + mDoorbellConfig.getDoorbellRingName());
            } else {
                descriptor = assets.openFd("alarm/" + mDoorbellConfig.getDoorbellAlarmName());
            }
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
                mPlayer.setLooping(false);
            } else {
                mPlayer.stop();
                mPlayer.reset();
            }
            mPlayer.setVolume(progress / 100f, progress / 100f);
            mPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
