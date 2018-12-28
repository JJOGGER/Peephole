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
import cn.jcyh.peephole.ui.dialog.ChooseCustomRingDialog;
import cn.jcyh.peephole.ui.dialog.ChooseRingDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.ui.dialog.VolumeSetDialog;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.ServiceUtil;

/**
 * Created by jogger on 2018/4/28.
 * 设置铃声和音量
 */

public class DoorbellRingVolumeSetFragment extends BaseFragment implements BaseDialogFragment.OnDialogDissmissListener {
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
        tvDoorbellRing.setText(doorbellConfig.getDoorbellRingName().replace(Constant.ASSET_RING + File.separator, ""));
        tvAlarmRing.setText(doorbellConfig.getDoorbellAlarmName().replace(Constant.ASSET_ALARM + File.separator, ""));
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        String[] rings = new String[0];
        try {
            rings = getResources().getAssets().list(Constant.ASSET_RING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert rings != null;
        Collections.addAll(mDoorbellRings, rings);
        String[] alarms = new String[0];
        try {
            alarms = getResources().getAssets().list(Constant.ASSET_ALARM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert alarms != null;
        Collections.addAll(mAlarmRings, alarms);
    }

    @OnClick({R.id.rl_volume, R.id.rl_doorbell_ring, R.id.rl_alarm_ring, R.id.rl_custom})
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
            final ChooseCustomRingDialog chooseCustomRingDialog = new ChooseCustomRingDialog();
            mCustomDialog = new DialogHelper((BaseActivity) mActivity, chooseCustomRingDialog);
        }
        mCustomDialog.commit();
    }

    private void showAlarmDialog() {
        final ChooseRingDialog chooseRingDialog = new ChooseRingDialog();
        chooseRingDialog.setType(ControlCenter.DOORBELL_TYPE_ALARM);
        chooseRingDialog.setTitle(getString(R.string.alarm_ring));
        chooseRingDialog.setDatas(mAlarmRings);
        chooseRingDialog.setOnChooseRingClickListener(new ChooseRingDialog.OnChooseRingClickListener() {
            @Override
            public void onItemClick(String data, int pos) {
                L.e("-----------data:" + data + "::" + pos);
//                    chooseRingDialog.setCheckedItem(data);
//                    Intent intent = new Intent(mActivity, MediaPlayService.class);
//                    intent.putExtra(Constant.RESOURCE_PATH, ASSET_ALARM + File.separator + data);
//                    intent.putExtra(Constant.VOLUME, mDoorbellConfig.getAlarmVolume() / 100f);
//                    mActivity.startService(intent);
//                    play(data, ControlCenter.DOORBELL_TYPE_ALARM);
            }
        });
        chooseRingDialog.setOnDismissListener(this);
        chooseRingDialog.setOnDialogListener(new OnDialogListener() {
            @Override
            public void onConfirm(Object content) {
                L.e("----------content:" + content);
//                    mDoorbellConfig.setDoorbellAlarmName(ASSET_ALARM + File.separator + content.toString());
//                    ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
//                    tvAlarmRing.setText(content.toString());
            }
        });
        mAlarmRingDialog = new DialogHelper((BaseActivity) mActivity, chooseRingDialog);
        ControlCenter.getBCManager().setMainSpeakerOn(false);
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();//重新获取数据
        mAlarmRingDialog.commit();
    }

    /**
     * 播放铃声dialog
     */
    private void showRingDialog() {
        final ChooseRingDialog chooseRingDialog = new ChooseRingDialog();
        chooseRingDialog.setType(ControlCenter.DOORBELL_TYPE_RING);
        chooseRingDialog.setTitle(getString(R.string.doorbell_ring));
        chooseRingDialog.setDatas(mDoorbellRings);
//            adapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
//                @Override
//                public void onItemClick(String data, int pos) {
//                    chooseRingDialog.setCheckedItem(data);
//                    Intent intent = new Intent(mActivity, MediaPlayService.class);
//                    intent.putExtra(Constant.RESOURCE_PATH, ASSET_RING + File.separator + data);
//                    intent.putExtra(Constant.VOLUME, mDoorbellConfig.getRingVolume() / 100f);
//                    mActivity.startService(intent);
////                    play(data, ControlCenter.DOORBELL_TYPE_RING);
//                }
//            });
        chooseRingDialog.setOnDismissListener(this);
        chooseRingDialog.setOnDialogListener(new OnDialogListener() {
            @Override
            public void onConfirm(Object content) {
//                    mDoorbellConfig.setDoorbellRingName(ASSET_RING + File.separator + content.toString());
//                    ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
                tvDoorbellRing.setText(content.toString());
            }
        });
        mDoorbellRingDialog = new DialogHelper((BaseActivity) mActivity, chooseRingDialog);
        ChooseRingDialog dialogFragment = (ChooseRingDialog) mDoorbellRingDialog.getDialogFragment();
//        if (dialogFragment != null)
//            dialogFragment.setCheckedItem(mDoorbellConfig.getDoorbellRingName().replace(ASSET_RING + File.separator, ""));
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
                descriptor = assets.openFd(Constant.ASSET_RING + File.separator + data);
                mPlayer.setVolume(mDoorbellConfig.getRingVolume() / 100f, mDoorbellConfig.getRingVolume() / 100f);
            } else {
                descriptor = assets.openFd(Constant.ASSET_ALARM + File.separator + data);
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
