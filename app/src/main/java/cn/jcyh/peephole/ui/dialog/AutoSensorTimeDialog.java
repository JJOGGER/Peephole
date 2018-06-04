package cn.jcyh.peephole.ui.dialog;

import android.view.View;
import android.widget.RadioButton;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;

/**
 * Created by jogger on 2018/4/25.
 * 自动感应时间设置
 */

public class AutoSensorTimeDialog extends BaseDialogFragment {
    @BindView(R.id.rb_5s)
    RadioButton rb5s;
    @BindView(R.id.rb_10s)
    RadioButton rb10s;
    @BindView(R.id.rb_20s)
    RadioButton rb20s;
    @BindView(R.id.rb_40s)
    RadioButton rb40s;
    @BindView(R.id.rb_1m)
    RadioButton rb1m;
    private DoorbellConfig mDoorbellConfig;

    @Override
    int getLayoutId() {
        return R.layout.dialog_auto_sensor_time;
    }

    @Override
    public void onResume() {
        super.onResume();
        rb5s.setChecked(false);
        rb10s.setChecked(false);
        rb20s.setChecked(false);
        rb40s.setChecked(false);
        rb1m.setChecked(false);
        mDoorbellConfig = DoorBellControlCenter.getInstance().getDoorbellConfig();
        int time = mDoorbellConfig.getAutoSensorTime();
        switch (time) {
            case 5:
                rb5s.setChecked(true);
                break;
            case 10:
                rb10s.setChecked(true);
                break;
            case 20:
                rb20s.setChecked(true);
                break;
            case 40:
                rb40s.setChecked(true);
                break;
            case 60:
                rb1m.setChecked(true);
                break;
        }
    }

    @OnClick({R.id.rl_sensor_5s, R.id.rl_sensor_10s, R.id.rl_sensor_20s, R.id.rl_sensor_40s, R.id.rl_sensor_1m, R.id.tv_cancel})
    public void onClick(View v) {
        int time = 5;
        switch (v.getId()) {
            case R.id.rl_sensor_5s:
                time = 5;
                break;
            case R.id.rl_sensor_10s:
                time = 10;
                mOnDialogListener.onConfirm(10);
                break;
            case R.id.rl_sensor_20s:
                time = 20;
                mOnDialogListener.onConfirm(20);
                break;
            case R.id.rl_sensor_40s:
                time = 40;
                mOnDialogListener.onConfirm(40);
                break;
            case R.id.rl_sensor_1m:
                time = 60;
                mOnDialogListener.onConfirm(60);
                break;
            case R.id.tv_cancel:
                time = mDoorbellConfig.getAutoSensorTime();
                break;
        }
        if (mOnDialogListener != null) {
            mOnDialogListener.onConfirm(time);
        }
        mDoorbellConfig.setAutoSensorTime(time);
        DoorBellControlCenter.getInstance().saveDoorbellConfig(mDoorbellConfig);
        dismiss();
    }
}
