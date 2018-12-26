package cn.jcyh.peephole.ui.fragment;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.ChooseSetAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.service.MediaPlayService;
import cn.jcyh.peephole.ui.dialog.BaseDialogFragment;
import cn.jcyh.peephole.ui.dialog.ChooseSetDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.ui.dialog.RecordAudioDialogFragment;
import cn.jcyh.peephole.ui.dialog.VolumeSetDialog;
import cn.jcyh.peephole.utils.ServiceUtil;

/**
 * Created by jogger on 2018/4/28.
 * 设置铃声和音量
 */

public class DoorbellRingVolumeSetFragment extends BaseFragment implements BaseDialogFragment.OnDialogDissmissListener {
    private static final String ASSET_RING = "ring";
    private static final String ASSET_ALARM = "alarm";
    @BindView(R.id.tv_doorbell_ring)
    TextView tvDoorbellRing;
    @BindView(R.id.tv_alarm_ring)
    TextView tvAlarmRing;
    private DialogHelper mDoorbellRingDialog;
    private DialogHelper mAlarmRingDialog;
    private DialogHelper mVolumeDialog;
    private DialogHelper mCustomDialog;
    private List<String> mDoorbellRings;
    private List<String> mAlarmRings;
    private DoorbellConfig mDoorbellConfig;
    private MediaPlayer mPlayer;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_doorbell_ring_volume_set;
    }

    @Override
    public void init() {
        mDoorbellRings = new ArrayList<>();
        mAlarmRings = new ArrayList<>();
        DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        tvDoorbellRing.setText(doorbellConfig.getDoorbellRingName().replace(ASSET_RING + File.separator, ""));
        tvAlarmRing.setText(doorbellConfig.getDoorbellAlarmName().replace(ASSET_ALARM + File.separator, ""));
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        String[] rings = new String[0];
        try {
            rings = getResources().getAssets().list(ASSET_RING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.addAll(mDoorbellRings, rings);
        String[] alarms = new String[0];
        try {
            alarms = getResources().getAssets().list(ASSET_ALARM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.addAll(mAlarmRings, alarms);
    }

    @OnClick({R.id.rl_volume, R.id.rl_doorbell_ring, R.id.rl_alarm_ring,R.id.rl_custom})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_volume:
                if (mVolumeDialog == null) {
                    VolumeSetDialog volumeSetDialog = new VolumeSetDialog();
                    mVolumeDialog = new DialogHelper((BaseActivity) mActivity, volumeSetDialog);
                }
                mVolumeDialog.commit();
                break;
            case R.id.rl_doorbell_ring:
                showRingDialog();
                break;
            case R.id.rl_alarm_ring:
                showAlarmDialog();
                break;
            case R.id.rl_custom:
                showCustomDialog();
                break;
        }
    }

    private void showCustomDialog() {
        if (mCustomDialog == null) {
            final RecordAudioDialogFragment recordAudioDialogFragment = new RecordAudioDialogFragment();
//            chooseSetDialog.setType(ControlCenter.DOORBELL_TYPE_ALARM);
            mCustomDialog = new DialogHelper((BaseActivity) mActivity, recordAudioDialogFragment);
        }
        ChooseSetDialog dialogFragment = (ChooseSetDialog) mAlarmRingDialog.getDialogFragment();
        if (dialogFragment != null) {
            dialogFragment.setCheckedItem(mDoorbellConfig.getDoorbellAlarmName().replace(ASSET_ALARM + File.separator, ""));
        }
        ControlCenter.getBCManager().setMainSpeakerOn(false);
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();//重新获取数据
        mAlarmRingDialog.commit();
    }

    private void showAlarmDialog() {
        if (mAlarmRingDialog == null) {
            final ChooseSetDialog chooseSetDialog = new ChooseSetDialog();
//            chooseSetDialog.setType(ControlCenter.DOORBELL_TYPE_ALARM);
            chooseSetDialog.setTitle(getString(R.string.alarm_ring));
            ChooseSetAdapter adapter = new ChooseSetAdapter(mAlarmRings);
            chooseSetDialog.setAdapter(adapter);
            adapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String data, int pos) {
                    chooseSetDialog.setCheckedItem(data);
                    Intent intent = new Intent(mActivity, MediaPlayService.class);
                    intent.putExtra(Constant.RESOURCE_PATH, ASSET_ALARM + File.separator + data);
                    intent.putExtra(Constant.VOLUME, mDoorbellConfig.getAlarmVolume() / 100f);
                    mActivity.startService(intent);
//                    play(data, ControlCenter.DOORBELL_TYPE_ALARM);
                }
            });
            chooseSetDialog.setOnDismissListener(this);
            chooseSetDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(Object content) {
                    mDoorbellConfig.setDoorbellAlarmName(ASSET_ALARM + File.separator + content.toString());
                    ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
                    tvAlarmRing.setText(content.toString());
                }
            });
            mAlarmRingDialog = new DialogHelper((BaseActivity) mActivity, chooseSetDialog);
        }
        ChooseSetDialog dialogFragment = (ChooseSetDialog) mAlarmRingDialog.getDialogFragment();
        if (dialogFragment != null) {
            dialogFragment.setCheckedItem(mDoorbellConfig.getDoorbellAlarmName().replace(ASSET_ALARM + File.separator, ""));
        }
        ControlCenter.getBCManager().setMainSpeakerOn(false);
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();//重新获取数据
        mAlarmRingDialog.commit();
    }

    /**
     * 播放铃声dialog
     */
    private void showRingDialog() {
        if (mDoorbellRingDialog == null) {
            final ChooseSetDialog chooseSetDialog = new ChooseSetDialog();
//            chooseSetDialog.setType(ControlCenter.DOORBELL_TYPE_RING);
            chooseSetDialog.setTitle(getString(R.string.doorbell_ring));
            ChooseSetAdapter adapter = new ChooseSetAdapter(mDoorbellRings);
            chooseSetDialog.setAdapter(adapter);
            adapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String data, int pos) {
                    chooseSetDialog.setCheckedItem(data);
                    Intent intent = new Intent(mActivity, MediaPlayService.class);
                    intent.putExtra(Constant.RESOURCE_PATH, ASSET_RING + File.separator + data);
                    intent.putExtra(Constant.VOLUME, mDoorbellConfig.getRingVolume() / 100f);
                    mActivity.startService(intent);
//                    play(data, ControlCenter.DOORBELL_TYPE_RING);
                }
            });
            chooseSetDialog.setOnDismissListener(this);
            chooseSetDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(Object content) {
                    mDoorbellConfig.setDoorbellRingName(ASSET_RING + File.separator + content.toString());
                    ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
                    tvDoorbellRing.setText(content.toString());
                }
            });
            mDoorbellRingDialog = new DialogHelper((BaseActivity) mActivity, chooseSetDialog);
        }
        ChooseSetDialog dialogFragment = (ChooseSetDialog) mDoorbellRingDialog.getDialogFragment();
        if (dialogFragment != null)
            dialogFragment.setCheckedItem(mDoorbellConfig.getDoorbellRingName().replace(ASSET_RING + File.separator, ""));
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();//重新获取数据
        ControlCenter.getBCManager().setMainSpeakerOn(true);
        mDoorbellRingDialog.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAlarmRingDialog != null)
            mAlarmRingDialog.dismiss();
        if (mDoorbellRingDialog != null)
            mDoorbellRingDialog.dismiss();
        if (mVolumeDialog != null)
            mVolumeDialog.dismiss();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    public void play(String data, int type) {
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
            if (type == ControlCenter.DOORBELL_TYPE_RING) {
                descriptor = assets.openFd(ASSET_RING + File.separator + data);
                mPlayer.setVolume(mDoorbellConfig.getRingVolume() / 100f, mDoorbellConfig.getRingVolume() / 100f);
            } else {
                descriptor = assets.openFd(ASSET_ALARM + File.separator + data);
                mPlayer.setVolume(mDoorbellConfig.getAlarmVolume() / 100f, mDoorbellConfig.getAlarmVolume() / 100f);
            }
            mPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDismiss() {
        ServiceUtil.stopService(MediaPlayService.class);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
}
