package cn.jcyh.peephole.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.DoorbellParam;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.BcManager;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.dialog.AutoSensorTimeDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.utils.ToastUtil;

public class SetActivity extends BaseActivity {
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
    private static final int SENSOR_SET_REQUEST = 0X001;
    private DoorbellParam mDoorbellParam;
    private DoorbellConfig mDoorbellConfig;
    private DoorBellControlCenter mControlCenter;
    private DialogHelper mAutoSensorTimeDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    public void init() {
        mControlCenter = DoorBellControlCenter.getInstance(this);
        mDoorbellConfig = mControlCenter.getDoorbellConfig();
        initView();
    }

    private void initView() {
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
        updateView();

    }

    @OnClick({R.id.rl_doorbell_set, R.id.rl_sensor_set, R.id.rl_monitor, R.id.rl_sensor_time})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_doorbell_set:
                startNewActivity(DoorbellSetActivity.class);
                break;
            case R.id.rl_sensor_set:
                Intent intent = new Intent(SetActivity.this, SensorSetActivity.class);
                intent.putExtra("doorbellParam", mDoorbellParam);
                startActivityForResult(intent, SENSOR_SET_REQUEST);
                break;
            case R.id.rl_monitor:
                switchMonitor();

                HttpAction.getHttpAction(getApplicationContext()).setDoorbellConfig(IMEI, mDoorbellConfig, new IDataListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        initView();
                        mControlCenter.saveDoorbellConfig(mDoorbellConfig);
                    }

                    @Override
                    public void onFailure(int errorCode) {

                    }
                });
                break;
            case R.id.rl_sensor_time:
                autoSensorTimeSet();
                break;
        }
    }

    private void switchMonitor() {
        final DoorbellConfig doorbellConfig = mControlCenter.getDoorbellConfig();
        cbMonitor.setChecked(!cbMonitor.isChecked());
        mDoorbellConfig.setMonitorSwitch(cbMonitor.isChecked() ? 1 : 0);
        HttpAction.getHttpAction(this).setDoorbellConfig(IMEI, doorbellConfig, new IDataListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                mControlCenter.saveDoorbellConfig(mDoorbellConfig);
                BcManager.getManager(getApplicationContext()).setPIRSensorOn(mDoorbellConfig.getMonitorSwitch() == 1);
            }

            @Override
            public void onFailure(int errorCode) {
                ToastUtil.showToast(getApplicationContext(), getString(R.string.set_failure) + errorCode);
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
            mAutoSensorTimeDialog = new DialogHelper(this, autoSensorTimeDialog);
        }
        mAutoSensorTimeDialog.commit();

    }

    private void updateView() {
        if (mDoorbellConfig.getMonitorSwitch() != 1) {
            tvMonitorState.setText(R.string.monitor_closed);
            rlSensorTime.setEnabled(false);
            rlSensorSet.setEnabled(false);
        } else {
            tvMonitorState.setText(R.string.monitor_opened);
            rlSensorTime.setEnabled(true);
            rlSensorSet.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAutoSensorTimeDialog != null)
            mAutoSensorTimeDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SENSOR_SET_REQUEST && resultCode == RESULT_OK) {
            mDoorbellParam = data.getParcelableExtra("doorbellParam");
        }
    }
}
