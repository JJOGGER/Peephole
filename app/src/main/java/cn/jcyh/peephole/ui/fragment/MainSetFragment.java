package cn.jcyh.peephole.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.bean.DoorbellParam;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.BcManager;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.dialog.AutoSensorTimeDialog;
import cn.jcyh.peephole.ui.dialog.CommonEditDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.utils.ToastUtil;

import static cn.jcyh.peephole.utils.ConstantUtil.IMEI;

/**
 * Created by jogger on 2018/4/28.
 */

public class MainSetFragment extends BaseFragment {
    @BindView(R.id.cb_monitor)
    CheckBox cbMonitor;
    @BindView(R.id.tv_monitor_state)
    TextView tvMonitorState;
    @BindView(R.id.rl_sensor_time)
    RelativeLayout rlSensorTime;
    @BindView(R.id.rl_sensor_set)
    RelativeLayout rlSensorSet;
    @BindView(R.id.tv_sensor_time_title)
    TextView tvSensorTimeTitle;
    @BindView(R.id.tv_sensor_time)
    TextView tvSensorTime;
    @BindView(R.id.tv_sensor_set_title)
    TextView tvSensorSetTitle;
    @BindView(R.id.tv_sensor_set)
    TextView tvSensorSet;
    @BindView(R.id.tv_master_number)
    TextView tvMasterNumber;
    @BindView(R.id.tv_sos_number)
    TextView tvSOSNumber;
    @BindView(R.id.tv_doorbell_videotap_time)
    TextView tvDoorbellVideotapTime;
    @BindView(R.id.tv_doorbell_look_time)
    TextView tvDoorbellLookTime;
    private static final int SENSOR_SET_REQUEST = 0X001;
    private DoorbellParam mDoorbellParam;
    private DoorbellConfig mDoorbellConfig;
    private DoorBellControlCenter mControlCenter;
    private DialogHelper mAutoSensorTimeDialog, mMasterNumberDialog, mSOSNumberDialog,
            mVideotapTimeDialog, mDoorbellLookDialog;
    private FragmentManager mFragmentManager;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main_set;
    }

    @Override
    public void init() {
        super.init();
        mControlCenter = DoorBellControlCenter.getInstance(mActivity);
        mDoorbellConfig = mControlCenter.getDoorbellConfig();
        mFragmentManager = getFragmentManager();
        updateView();
    }

    @OnClick({R.id.rl_doorbell_set, R.id.rl_sensor_set, R.id.rl_monitor, R.id.rl_sensor_time,
            R.id.rl_ring_volume, R.id.rl_master_number, R.id.rl_sos_number, R.id.rl_doorbell_videotap_time,
            R.id.rl_doorbell_look_time})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_doorbell_set:
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                DoorbellSetFragment doorbellSetFragment = new DoorbellSetFragment();
                transaction.add(R.id.fl_container, doorbellSetFragment, DoorbellSetFragment.class.getName());
                transaction.hide(mFragmentManager.findFragmentByTag(MainSetFragment.class.getName()));
                transaction.commit();
                break;
            case R.id.rl_sensor_set:
                transaction = mFragmentManager.beginTransaction();
                SensorSetFragment sensorSetFragment = new SensorSetFragment();
                transaction.add(R.id.fl_container, sensorSetFragment, SensorSetFragment.class.getName());
                transaction.hide(mFragmentManager.findFragmentByTag(MainSetFragment.class.getName()));
                transaction.commit();
                break;
            case R.id.rl_monitor:
                switchMonitor();
                break;
            case R.id.rl_sensor_time:
                autoSensorTimeSet();
                break;
            case R.id.rl_ring_volume:
                transaction = mFragmentManager.beginTransaction();
                DoorbellRingVolumeSetFragment ringVolumeSetFragment = new DoorbellRingVolumeSetFragment();
                transaction.add(R.id.fl_container, ringVolumeSetFragment, DoorbellRingVolumeSetFragment.class.getName());
                transaction.hide(mFragmentManager.findFragmentByTag(MainSetFragment.class.getName()));
                transaction.commit();
                break;
            case R.id.rl_master_number:
                showMasterNumberDialog();
                break;
            case R.id.rl_sos_number:
                showSOSDialog();
                break;
            case R.id.rl_doorbell_videotap_time:
                showVideoTimeDialog();
                break;
            case R.id.rl_doorbell_look_time:
                showLookTimeDialog();
                break;
        }
    }

    /**
     * 猫眼查看时间
     */
    private void showLookTimeDialog() {
        if (mDoorbellLookDialog == null) {
            CommonEditDialog commonEditDialog = new CommonEditDialog();
            commonEditDialog.setTitle(getString(R.string.doorbell_look_time));
            commonEditDialog.setContent(String.valueOf(mDoorbellConfig.getDoorbellLookTime()));
            commonEditDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(Object o) {
                    mDoorbellConfig.setDoorbellLookTime(Integer.parseInt(o.toString()));
                    tvDoorbellLookTime.setText(o.toString());
                    DoorBellControlCenter.getInstance(mActivity).saveDoorbellConfig(mDoorbellConfig);
                }
            });
            mDoorbellLookDialog = new DialogHelper((BaseActivity) mActivity, commonEditDialog);
        }
        mDoorbellLookDialog.commit();
    }

    /**
     * 录像时间
     */
    private void showVideoTimeDialog() {
        if (mVideotapTimeDialog == null) {
            CommonEditDialog commonEditDialog = new CommonEditDialog();
            commonEditDialog.setTitle(getString(R.string.doorbell_videotap_time_set));
            commonEditDialog.setContent(String.valueOf(mDoorbellConfig.getVideotapTime()));
            commonEditDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(Object o) {
                    mDoorbellConfig.setVideotapTime(Integer.parseInt(o.toString()));
                    tvDoorbellVideotapTime.setText(o.toString());
                    DoorBellControlCenter.getInstance(mActivity).saveDoorbellConfig(mDoorbellConfig);
                }
            });
            mVideotapTimeDialog = new DialogHelper((BaseActivity) mActivity, commonEditDialog);
        }
        mVideotapTimeDialog.commit();
    }

    /**
     * 主人号码
     */
    private void showMasterNumberDialog() {
        if (mMasterNumberDialog == null) {
            CommonEditDialog commonEditDialog = new CommonEditDialog();
            commonEditDialog.setTitle(getString(R.string.master_number));
            if (!TextUtils.isEmpty(mDoorbellConfig.getMasterNumber())) {
                commonEditDialog.setContent(mDoorbellConfig.getMasterNumber());
            }
            commonEditDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(Object o) {
                    mDoorbellConfig.setMasterNumber(o.toString());
                    tvMasterNumber.setText(o.toString());
                    DoorBellControlCenter.getInstance(mActivity).saveDoorbellConfig(mDoorbellConfig);
                }
            });
            mMasterNumberDialog = new DialogHelper((BaseActivity) mActivity, commonEditDialog);
        }
        mMasterNumberDialog.commit();
    }

    /**
     * SOS设置
     */
    private void showSOSDialog() {
        if (mSOSNumberDialog == null) {
            CommonEditDialog commonEditDialog = new CommonEditDialog();
            commonEditDialog.setTitle(getString(R.string.master_number));
            if (!TextUtils.isEmpty(mDoorbellConfig.getSosNumber())) {
                commonEditDialog.setContent(mDoorbellConfig.getSosNumber());
            }
            commonEditDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(Object o) {
                    mDoorbellConfig.setSosNumber(o.toString());
                    tvSOSNumber.setText(o.toString());
                    DoorBellControlCenter.getInstance(mActivity).saveDoorbellConfig(mDoorbellConfig);
                }
            });
            mSOSNumberDialog = new DialogHelper((BaseActivity) mActivity, commonEditDialog);
        }
        mSOSNumberDialog.commit();
    }

    /**
     * 监控开关
     */
    private void switchMonitor() {
        cbMonitor.setChecked(!cbMonitor.isChecked());
        mDoorbellConfig.setMonitorSwitch(cbMonitor.isChecked() ? 1 : 0);
        HttpAction.getHttpAction(mActivity).setDoorbellConfig(IMEI, mDoorbellConfig, new IDataListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                mControlCenter.saveDoorbellConfig(mDoorbellConfig);
                BcManager.getManager(mActivity).setPIRSensorOn(mDoorbellConfig.getMonitorSwitch() == 1);
            }

            @Override
            public void onFailure(int errorCode) {
                ToastUtil.showToast(mActivity, getString(R.string.set_failure) + errorCode);
            }
        });
    }

    /**
     * 自动感应设置
     */
    private void autoSensorTimeSet() {
        if (mAutoSensorTimeDialog == null) {
            AutoSensorTimeDialog autoSensorTimeDialog = new AutoSensorTimeDialog();
            Bundle bundle = new Bundle();
            bundle.putInt("time", mDoorbellConfig.getAutoSensorTime());
            autoSensorTimeDialog.setArguments(bundle);
            autoSensorTimeDialog.setCancelable(false);
            autoSensorTimeDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(Object isConfirm) {
                    if ((int) isConfirm == 60)
                        tvSensorTime.setText(R.string.one_m);
                    else
                        tvSensorTime.setText(isConfirm + getString(R.string.second));
                }
            });
            mAutoSensorTimeDialog = new DialogHelper((BaseActivity) mActivity, autoSensorTimeDialog);
        }
        mAutoSensorTimeDialog.commit();

    }

    private void updateView() {
        //获取智能监控开关
        boolean isMonitor = mDoorbellConfig.getMonitorSwitch() == 1;
        cbMonitor.setChecked(isMonitor);
        tvSensorTimeTitle.setEnabled(isMonitor);
        tvSensorTime.setEnabled(isMonitor);
        if (mDoorbellConfig.getAutoSensorTime() == 60)
            tvSensorTime.setText(R.string.one_m);
        else
            tvSensorTime.setText(mDoorbellConfig.getAutoSensorTime() + getString(R.string.second));
        tvSensorSetTitle.setEnabled(isMonitor);
        tvSensorSet.setEnabled(isMonitor);
        rlSensorSet.setEnabled(isMonitor);
        rlSensorTime.setEnabled(isMonitor);
        if (mDoorbellConfig.getMonitorSwitch() != 1) {
            tvMonitorState.setText(R.string.monitor_closed);
            rlSensorTime.setEnabled(false);
            rlSensorSet.setEnabled(false);
        } else {
            tvMonitorState.setText(R.string.monitor_opened);
            rlSensorTime.setEnabled(true);
            rlSensorSet.setEnabled(true);
        }
        tvMasterNumber.setText(mDoorbellConfig.getMasterNumber());
        tvSOSNumber.setText(mDoorbellConfig.getSosNumber());
        tvDoorbellLookTime.setText(mDoorbellConfig.getDoorbellLookTime() + getString(R.string.second));
        tvDoorbellVideotapTime.setText(mDoorbellConfig.getVideotapTime() + getString(R.string.second));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAutoSensorTimeDialog != null)
            mAutoSensorTimeDialog.dismiss();
        if (mMasterNumberDialog != null)
            mMasterNumberDialog.dismiss();
        if (mSOSNumberDialog != null)
            mSOSNumberDialog.dismiss();
    }
}
