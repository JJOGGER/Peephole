package cn.jcyh.peephole.ui.fragment;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.TextView;

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
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.ui.dialog.BaseDialogFragment;
import cn.jcyh.peephole.ui.dialog.ChooseSetDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.ui.dialog.VolumeSetDialog;

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
        DoorbellConfig doorbellConfig = DoorBellControlCenter.getInstance().getDoorbellConfig();
        tvDoorbellRing.setText(doorbellConfig.getDoorbellRingName());
        tvAlarmRing.setText(doorbellConfig.getDoorbellAlarmName());
        mDoorbellConfig = DoorBellControlCenter.getInstance().getDoorbellConfig();
        String[] rings = new String[0];
        try {
            rings = getResources().getAssets().list("ring");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.addAll(mDoorbellRings, rings);
        String[] alarms = new String[0];
        try {
            alarms = getResources().getAssets().list("alarm");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.addAll(mAlarmRings, alarms);
    }

    @OnClick({R.id.rl_volume, R.id.rl_doorbell_ring, R.id.rl_alarm_ring})
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
        }
    }

    private void showAlarmDialog() {
        if (mAlarmRingDialog == null) {
            final ChooseSetDialog chooseSetDialog = new ChooseSetDialog();
            chooseSetDialog.setType(DoorBellControlCenter.DOORBELL_TYPE_ALARM);
            chooseSetDialog.setTitle(getString(R.string.alarm_ring));
            ChooseSetAdapter adapter = new ChooseSetAdapter(mAlarmRings);
            chooseSetDialog.setAdapter(adapter);
            adapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String data, int pos) {
                    chooseSetDialog.setCheckedItem(data);
                    play(data, DoorBellControlCenter.DOORBELL_TYPE_ALARM);
                }
            });
            chooseSetDialog.setOnDismissListener(this);
            chooseSetDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(Object content) {
                    mDoorbellConfig.setDoorbellAlarmName(content.toString());
                    DoorBellControlCenter.getInstance().saveDoorbellConfig(mDoorbellConfig);
                    tvAlarmRing.setText(content.toString());
                }
            });
            mAlarmRingDialog = new DialogHelper((BaseActivity) mActivity, chooseSetDialog);
        }
        ChooseSetDialog dialogFragment = (ChooseSetDialog) mAlarmRingDialog.getDialogFragment();
        if (dialogFragment != null)
            dialogFragment.setCheckedItem(mDoorbellConfig.getDoorbellAlarmName());
        mAlarmRingDialog.commit();
    }

    private void showRingDialog() {
        if (mDoorbellRingDialog == null) {
            final ChooseSetDialog chooseSetDialog = new ChooseSetDialog();
            chooseSetDialog.setType(DoorBellControlCenter.DOORBELL_TYPE_RING);
            chooseSetDialog.setTitle(getString(R.string.doorbell_ring));
            ChooseSetAdapter adapter = new ChooseSetAdapter(mDoorbellRings);
            chooseSetDialog.setAdapter(adapter);
            adapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String data, int pos) {
                    chooseSetDialog.setCheckedItem(data);
                    play(data, DoorBellControlCenter.DOORBELL_TYPE_RING);
                }
            });
            chooseSetDialog.setOnDismissListener(this);
            chooseSetDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(Object content) {
                    mDoorbellConfig.setDoorbellRingName(content.toString());
                    DoorBellControlCenter.getInstance().saveDoorbellConfig(mDoorbellConfig);
                    tvDoorbellRing.setText(content.toString());
                }
            });
            mDoorbellRingDialog = new DialogHelper((BaseActivity) mActivity, chooseSetDialog);
        }
        ChooseSetDialog dialogFragment = (ChooseSetDialog) mDoorbellRingDialog.getDialogFragment();
        if (dialogFragment != null)
            dialogFragment.setCheckedItem(mDoorbellConfig.getDoorbellRingName());
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
            if (type == DoorBellControlCenter.DOORBELL_TYPE_RING)
                descriptor = assets.openFd("ring/" + data);
            else descriptor = assets.openFd("alarm/" + data);
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
                mPlayer.setLooping(false);
            } else {
                mPlayer.stop();
                mPlayer.reset();
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
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
}
