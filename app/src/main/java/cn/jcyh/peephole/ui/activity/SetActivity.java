package cn.jcyh.peephole.ui.activity;

import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.MyApp;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.DoorbellParam;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.HttpErrorCode;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.utils.ToastUtil;
import timber.log.Timber;

public class SetActivity extends BaseActivity {
    @BindView(R.id.cb_monitor)
    CheckBox cbMonitor;
    @BindView(R.id.tv_monitor_state)
    TextView tvMonitorState;
    @BindView(R.id.rl_sensor_time)
    RelativeLayout rlSensorTime;
    @BindView(R.id.rl_sensor_distance)
    RelativeLayout rlSensorDistance;
    @BindView(R.id.rl_sensor_set)
    RelativeLayout rlSensorSet;
    private FragmentManager mFragmentManager;
    private DoorbellParam mDoorbellParam;

    @Override
    public int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    public void init() {
        mFragmentManager = getSupportFragmentManager();
        //获取智能监控开关
        HttpAction.getHttpAction(this).getDoorbellParams(MyApp.sImei, DoorBellControlCenter.DOORBELL_PARAMS_TYPE_SENSOR, new IDataListener<DoorbellParam>() {
            @Override
            public void onSuccess(DoorbellParam doorbellParam) {
                Timber.e("------doorbell:" + doorbellParam);
                mDoorbellParam = doorbellParam;
                cbMonitor.setChecked(mDoorbellParam.getMonitor() == 0);
                updateView(mDoorbellParam.getMonitor() == 0);
            }

            @Override
            public void onFailure(int errorCode) {
                if (errorCode == HttpErrorCode.NO_DATA_EXISTS) {
                    mDoorbellParam = new DoorbellParam();
                    cbMonitor.setChecked(false);
                    updateView(false);
                }
            }
        });
    }

    @OnClick({R.id.rl_doorbell_set, R.id.rl_sensor_set, R.id.rl_monitor})
    public void onClick(View v) {
        if (mDoorbellParam == null) {
            ToastUtil.showToast(this, R.string.loading);
            return;
        }
        switch (v.getId()) {
            case R.id.rl_doorbell_set:
                startNewActivity(DoorbellSetActivity.class);
                break;
            case R.id.rl_sensor_set:
                startNewActivity(SensorSetActivity.class, "doorbellParam", mDoorbellParam);
                break;
            case R.id.rl_monitor:
                cbMonitor.setChecked(!cbMonitor.isChecked());
                updateView(false);
                break;
        }
    }

    private void updateView(boolean isMonitor) {
        if (isMonitor) {
            tvMonitorState.setText(R.string.monitor_closed);
            rlSensorTime.setEnabled(false);
            rlSensorDistance.setEnabled(false);
            rlSensorSet.setEnabled(false);
        } else {
            tvMonitorState.setText(R.string.monitor_opened);
            rlSensorTime.setEnabled(true);
            rlSensorDistance.setEnabled(true);
            rlSensorSet.setEnabled(true);
        }
    }
}
