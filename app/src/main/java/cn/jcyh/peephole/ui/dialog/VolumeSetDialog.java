package cn.jcyh.peephole.ui.dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.SeekBar;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.service.MediaPlayService;
import cn.jcyh.peephole.utils.ServiceUtil;

/**
 * Created by jogger on 2018/5/11.
 * 音量设置
 */

public class VolumeSetDialog extends BaseDialogFragment implements SeekBar.OnSeekBarChangeListener {
    @BindView(R.id.sb_doorbell)
    SeekBar sbDoorbell;
    @BindView(R.id.sb_alarm)
    SeekBar sbAlarm;
    private DoorbellConfig mDoorbellConfig;

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
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        sbDoorbell.setProgress(mDoorbellConfig.getRingVolume());
        sbAlarm.setProgress(mDoorbellConfig.getAlarmVolume());
    }

    @OnClick(R.id.tv_confirm)
    public void onClick(View v) {
        ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
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
        Intent intent = new Intent(mActivity, MediaPlayService.class);
        switch (seekBar.getId()) {
            case R.id.sb_doorbell:
                ControlCenter.getBCManager().setMainSpeakerOn(true);
                mDoorbellConfig.setRingVolume(seekBar.getProgress());
                intent.putExtra(Constant.RESOURCE_PATH, mDoorbellConfig.getDoorbellRingName());
                intent.putExtra(Constant.VOLUME, seekBar.getProgress() / 100f);
                mActivity.startService(intent);
//                play(TYPE_RING, seekBar.getProgress());
                break;
            case R.id.sb_alarm:
                ControlCenter.getBCManager().setMainSpeakerOn(false);
                mDoorbellConfig.setAlarmVolume(seekBar.getProgress());
                intent.putExtra(Constant.RESOURCE_PATH, mDoorbellConfig.getDoorbellAlarmName());
                intent.putExtra(Constant.VOLUME, seekBar.getProgress() / 100f);
                mActivity.startService(intent);
//                play(TYPE_ALARM, seekBar.getProgress());
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ServiceUtil.stopService(MediaPlayService.class);
    }
}
