package cn.jcyh.peephole.ui.fragment;

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
import cn.jcyh.peephole.ui.dialog.ChooseSetDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.ui.dialog.VolumeSetDialog;

/**
 * Created by jogger on 2018/4/28.
 * 设置铃声和音量
 */

public class DoorbellRingVolumeSetFragment extends BaseFragment {
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

    @Override
    public int getLayoutId() {
        return R.layout.fragment_doorbell_ring_volume_set;
    }

    @Override
    public void init() {
        mDoorbellRings = new ArrayList<>();
        mAlarmRings = new ArrayList<>();
        DoorbellConfig doorbellConfig = DoorBellControlCenter.getInstance(mActivity).getDoorbellConfig();
        tvDoorbellRing.setText(doorbellConfig.getDoorbellRingName());
        tvAlarmRing.setText(doorbellConfig.getDoorbellAlarmName());
        mDoorbellConfig = DoorBellControlCenter.getInstance(mActivity).getDoorbellConfig();
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
                if (mDoorbellRingDialog == null) {
                    ChooseSetDialog chooseSetDialog = new ChooseSetDialog();
                    chooseSetDialog.setType(DoorBellControlCenter.DOORBELL_TYPE_RING);
                    chooseSetDialog.setTitle(getString(R.string.doorbell_ring));
                    ChooseSetAdapter adapter = new ChooseSetAdapter(mDoorbellRings);
                    chooseSetDialog.setAdapter(adapter);
                    chooseSetDialog.setOnDialogListener(new OnDialogListener() {
                        @Override
                        public void onConfirm(Object content) {
                            tvDoorbellRing.setText(content.toString());
                        }
                    });
                    mDoorbellRingDialog = new DialogHelper((BaseActivity) mActivity, chooseSetDialog);
                }
                setCurrentItem(mDoorbellConfig.getDoorbellRingName());
                mDoorbellRingDialog.commit();
                break;
            case R.id.rl_alarm_ring:
                if (mAlarmRingDialog == null) {
                    ChooseSetDialog chooseSetDialog = new ChooseSetDialog();
                    chooseSetDialog.setType(DoorBellControlCenter.DOORBELL_TYPE_ALARM);
                    chooseSetDialog.setTitle(getString(R.string.alarm_ring));
                    ChooseSetAdapter adapter = new ChooseSetAdapter(mAlarmRings);
                    chooseSetDialog.setAdapter(adapter);
                    adapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(String data, int pos) {

                        }
                    });
                    chooseSetDialog.setOnDialogListener(new OnDialogListener() {
                        @Override
                        public void onConfirm(Object content) {

                            tvAlarmRing.setText(content.toString());
                        }
                    });
                    mAlarmRingDialog = new DialogHelper((BaseActivity) mActivity, chooseSetDialog);
                }
                setCurrentItem(mDoorbellConfig.getDoorbellAlarmName());
                mAlarmRingDialog.commit();
                break;
        }
    }

    private void setCurrentItem(String data) {
        ((ChooseSetDialog) mDoorbellRingDialog.getDialogFragment()).setCheckedItem(data);
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
    }
}
