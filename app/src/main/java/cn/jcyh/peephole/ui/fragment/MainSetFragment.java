package cn.jcyh.peephole.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import cn.jcyh.peephole.event.NIMMessageAction;
import cn.jcyh.peephole.ui.dialog.AutoSensorTimeDialog;
import cn.jcyh.peephole.ui.dialog.ChooseSetDialog;
import cn.jcyh.peephole.ui.dialog.CommonEditDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.utils.Tool;


/**
 * Created by jogger on 2018/4/28.
 * 猫眼设置
 */

public class MainSetFragment extends BaseFragment {
    private static final int MIN_LOOK_TIME = 5;
    private static final int MAX_LOOK_TIME = 300;
    private static final int MIN_RECORD_TIME = 5;
    private static final int MAX_RECORD_TIME = 30;
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
    @BindView(R.id.rl_extend_function)
    RelativeLayout rlExtendFunction;
    //    @BindView(R.id.tv_master_number)
//    TextView tvMasterNumber;
//    @BindView(R.id.tv_sos_number)
//    TextView tvSOSNumber;
    @BindView(R.id.cb_face_switch)
    CheckBox cbFaceSwitch;
    @BindView(R.id.tv_face_set)
    TextView tvFaceSet;
    @BindView(R.id.tv_doorbell_videotap_time)
    TextView tvDoorbellVideotapTime;
    @BindView(R.id.tv_doorbell_look_time)
    TextView tvDoorbellLookTime;
    private DoorbellConfig mDoorbellConfig;
    private DialogHelper mAutoSensorTimeDialog, mMasterNumberDialog, mSOSNumberDialog,
            mVideotapTimeDialog, mDoorbellLookDialog, mDoorbellLeavelTimeDialog;
    private FragmentManager mFragmentManager;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main_set;
    }

    @Override
    public void init() {
        super.init();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        boolean siye = ControlCenter.getSN().startsWith(Constant.SIYE_SN);
        rlExtendFunction.setVisibility(siye ? View.VISIBLE : View.GONE);
        mFragmentManager = getFragmentManager();
        updateView();
    }


    @OnClick({R.id.rl_doorbell_set, R.id.rl_sensor_set, R.id.rl_monitor, R.id.rl_sensor_time,
            R.id.rl_ring_volume,
//            R.id.rl_master_number, R.id.rl_sos_number,
            R.id.rl_doorbell_leavel_time, R.id.rl_doorbell_videotap_time,
            R.id.rl_doorbell_look_time,
            R.id.rl_extend_function,
            R.id.rl_face_set
    })
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_doorbell_set://门铃设置
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                DoorbellSetFragment doorbellSetFragment = new DoorbellSetFragment();
                transaction.add(R.id.fl_container, doorbellSetFragment, DoorbellSetFragment.class.getName());
                transaction.hide(mFragmentManager.findFragmentByTag(MainSetFragment.class.getName()));
                transaction.commit();
                break;
            case R.id.rl_sensor_set://传感设置
                transaction = mFragmentManager.beginTransaction();
                SensorSetFragment sensorSetFragment = new SensorSetFragment();
                transaction.add(R.id.fl_container, sensorSetFragment, SensorSetFragment.class.getName());
                Fragment fragmentByTag = mFragmentManager.findFragmentByTag(MainSetFragment.class.getName());
                if (fragmentByTag != null)
                    transaction.hide(fragmentByTag);
                transaction.commit();
                break;
            case R.id.rl_monitor://监控开关
                switchMonitor();
                break;
            case R.id.rl_sensor_time://感应时长
                autoSensorTimeSet();
                break;
            case R.id.rl_ring_volume://铃声音量
                transaction = mFragmentManager.beginTransaction();
                DoorbellRingVolumeSetFragment ringVolumeSetFragment = new DoorbellRingVolumeSetFragment();
                transaction.add(R.id.fl_container, ringVolumeSetFragment, DoorbellRingVolumeSetFragment.class.getName());
                transaction.hide(mFragmentManager.findFragmentByTag(MainSetFragment.class.getName()));
                transaction.commit();
                break;
//            case R.id.rl_master_number://主人号码
//                showMasterNumberDialog();
//                break;
//            case R.id.rl_sos_number://sos
//                showSOSDialog();
//                break;
            case R.id.rl_doorbell_leavel_time://留言时间
                showLeavelTimeDialog();
                break;
            case R.id.rl_doorbell_videotap_time://录像时间
                showVideoTimeDialog();
                break;
            case R.id.rl_doorbell_look_time://猫眼查看时间
                showLookTimeDialog();
                break;
            case R.id.rl_extend_function:
                transaction = mFragmentManager.beginTransaction();
                ExtendFunctionFragment extendFunctionFragment = new ExtendFunctionFragment();
                transaction.add(R.id.fl_container, extendFunctionFragment, ExtendFunctionFragment.class.getName());
                transaction.hide(mFragmentManager.findFragmentByTag(MainSetFragment.class.getName()));
                transaction.commit();
                break;
            case R.id.rl_face_set:
                switchFace();
                break;
        }
    }

    /**
     * 开关人脸识别
     */
    private void switchFace() {
        cbFaceSwitch.setChecked(!cbFaceSwitch.isChecked());
        mDoorbellConfig.setFaceRecognize(cbFaceSwitch.isChecked() ? 1 : 0);
        ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
        tvFaceSet.setText(cbFaceSwitch.isChecked() ? R.string.face_set_opened : R.string.face_set_closed);
        ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getSN(), mDoorbellConfig, null);
    }

    /**
     * 猫眼留言时间
     */
    private void showLeavelTimeDialog() {
        if (mDoorbellLeavelTimeDialog == null) {
            final ChooseSetDialog chooseSetDialog = new ChooseSetDialog();
            chooseSetDialog.setTitle(getString(R.string.video_leave_msg_time));
            List<String> datas = new ArrayList<>();
            String[] stringArray = getResources().getStringArray(R.array.leave_time);
            Collections.addAll(datas, stringArray);
            final ChooseSetAdapter adapter = new ChooseSetAdapter(datas);
            chooseSetDialog.setAdapter(adapter);
            adapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String data, int pos) {
                    int number = 0;
                    if (Tool.hasDigit(data)) {
                        try {
                            number = Integer.parseInt(Tool.getNumbers(data));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mDoorbellConfig.setVideoLeaveMsgTime(number);
                    ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
                    chooseSetDialog.dismiss();
                }
            });
            mDoorbellLeavelTimeDialog = new DialogHelper((BaseActivity) mActivity, chooseSetDialog);
        }
        ((ChooseSetDialog) mDoorbellLeavelTimeDialog.getDialogFragment()).setCheckedItem(mDoorbellConfig.getVideoLeaveMsgTime() + getString(R.string.second));
        mDoorbellLeavelTimeDialog.commit();
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
                @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
                @Override
                public void onConfirm(Object o) {
                    int time = Integer.parseInt(o.toString());
                    if (time > MAX_LOOK_TIME) {
                        T.show(String.format(getString(R.string.more_than_time_msg), MAX_LOOK_TIME));
                        time = MAX_LOOK_TIME;
                    } else if (time < MIN_LOOK_TIME) {
                        T.show(String.format(getString(R.string.low_than_time_msg), MIN_LOOK_TIME));
                        time = MIN_LOOK_TIME;
                    }
                    mDoorbellConfig.setDoorbellLookTime(time);
                    tvDoorbellLookTime.setText(String.valueOf(time) + getString(R.string.second));
                    ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
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
                @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
                @Override
                public void onConfirm(Object o) {
                    int time = Integer.parseInt(o.toString());
                    if (time > MAX_RECORD_TIME) {
                        T.show(String.format(getString(R.string.more_than_time_msg), MAX_RECORD_TIME));
                        time = MAX_RECORD_TIME;
                    } else if (time < MIN_RECORD_TIME) {
                        T.show(String.format(getString(R.string.low_than_time_msg), MIN_RECORD_TIME));
                        time = MIN_RECORD_TIME;
                    }
                    mDoorbellConfig.setVideotapTime(time);
                    tvDoorbellVideotapTime.setText(String.valueOf(time) + getString(R.string.second));
                    ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
                }
            });
            mVideotapTimeDialog = new DialogHelper((BaseActivity) mActivity, commonEditDialog);
        } else {
            ((CommonEditDialog) mVideotapTimeDialog.getDialogFragment()).setContent(String.valueOf(mDoorbellConfig.getVideotapTime()));
        }
        mVideotapTimeDialog.commit();
    }

    /**
     * 主人号码
     */
//    private void showMasterNumberDialog() {
//        if (mMasterNumberDialog == null) {
//            CommonEditDialog commonEditDialog = new CommonEditDialog();
//            commonEditDialog.setTitle(getString(R.string.master_number));
//            if (!TextUtils.isEmpty(mDoorbellConfig.getMasterNumber())) {
//                commonEditDialog.setContent(mDoorbellConfig.getMasterNumber());
//            }
//            commonEditDialog.setOnDialogListener(new OnDialogListener() {
//                @Override
//                public void onConfirm(Object o) {
//                    if (!o.toString().matches(getString(R.string.regex_phone))) {
//                        T.show(getString(R.string.phone_no_regex));
//                        return;
//                    }
//                    mDoorbellConfig.setMasterNumber(o.toString());
//                    tvMasterNumber.setText(o.toString());
//                    ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
//                }
//            });
//            mMasterNumberDialog = new DialogHelper((BaseActivity) mActivity, commonEditDialog);
//        } else {
//            ((CommonEditDialog) mMasterNumberDialog.getDialogFragment()).setContent(mDoorbellConfig.getMasterNumber());
//        }
//        mMasterNumberDialog.commit();
//    }

    /**
     * SOS设置
     */
//    private void showSOSDialog() {
//        if (mSOSNumberDialog == null) {
//            CommonEditDialog commonEditDialog = new CommonEditDialog();
//            commonEditDialog.setTitle(getString(R.string.sos_number));
//            if (!TextUtils.isEmpty(mDoorbellConfig.getSosNumber())) {
//                commonEditDialog.setContent(mDoorbellConfig.getSosNumber());
//            }
//            commonEditDialog.setOnDialogListener(new OnDialogListener() {
//                @Override
//                public void onConfirm(Object o) {
//                    if (!o.toString().matches(getString(R.string.regex_phone))) {
//                        T.show(getString(R.string.phone_no_regex));
//                        return;
//                    }
//                    mDoorbellConfig.setSosNumber(o.toString());
//                    tvSOSNumber.setText(o.toString());
//                    ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
//                }
//            });
//            mSOSNumberDialog = new DialogHelper((BaseActivity) mActivity, commonEditDialog);
//        } else {
//            ((CommonEditDialog) mSOSNumberDialog.getDialogFragment()).setContent(mDoorbellConfig.getSosNumber());
//        }
//        mSOSNumberDialog.commit();
//    }

    /**
     * 监控开关
     */
    private void switchMonitor() {
        cbMonitor.setChecked(!cbMonitor.isChecked());
        mDoorbellConfig.getDoorbellSensorParam().setMonitor(cbMonitor.isChecked() ? 1 : 0);
        ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
        tvMonitorState.setText(cbMonitor.isChecked() ? R.string.monitor_opened : R.string.monitor_closed);
        rlSensorTime.setEnabled(cbMonitor.isChecked());
        tvSensorTime.setEnabled(cbMonitor.isChecked());
        tvSensorTimeTitle.setEnabled(cbMonitor.isChecked());
        rlSensorSet.setEnabled(cbMonitor.isChecked());
        tvSensorSetTitle.setEnabled(cbMonitor.isChecked());
        tvSensorSet.setEnabled(cbMonitor.isChecked());
        ControlCenter.getBCManager().setPIRSensorOn(mDoorbellConfig.getDoorbellSensorParam().getMonitor() == 1);
        ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getSN(), mDoorbellConfig, null);
    }

    /**
     * 自动感应设置
     */
    private void autoSensorTimeSet() {
        if (mAutoSensorTimeDialog == null) {
            AutoSensorTimeDialog autoSensorTimeDialog = new AutoSensorTimeDialog();
            Bundle bundle = new Bundle();
            bundle.putInt(Constant.TIME, mDoorbellConfig.getAutoSensorTime());
            autoSensorTimeDialog.setArguments(bundle);
            autoSensorTimeDialog.setCancelable(false);
            autoSensorTimeDialog.setOnDialogListener(new OnDialogListener() {
                @SuppressLint("SetTextI18n")
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

    @SuppressLint("SetTextI18n")
    private void updateView() {
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        //获取智能监控开关
        boolean isMonitor = mDoorbellConfig.getDoorbellSensorParam().getMonitor() == 1;
        cbMonitor.setChecked(isMonitor);
        cbFaceSwitch.setChecked(mDoorbellConfig.getFaceRecognize() == 1);
        tvSensorTimeTitle.setEnabled(isMonitor);
        tvSensorTime.setEnabled(isMonitor);
        if (mDoorbellConfig.getAutoSensorTime() == 60)
            tvSensorTime.setText(R.string.one_m);
        else
            tvSensorTime.setText(mDoorbellConfig.getAutoSensorTime() + getString(R.string.second));
        rlSensorTime.setEnabled(isMonitor);
        tvSensorSetTitle.setEnabled(isMonitor);
        tvSensorSet.setEnabled(isMonitor);
        rlSensorSet.setEnabled(isMonitor);
        tvFaceSet.setText(cbFaceSwitch.isChecked() ? R.string.face_set_opened : R.string.face_set_closed);
        if (mDoorbellConfig.getDoorbellSensorParam().getMonitor() != 1) {
            tvMonitorState.setText(R.string.monitor_closed);
            rlSensorTime.setEnabled(false);
            rlSensorSet.setEnabled(false);
        } else {
            tvMonitorState.setText(R.string.monitor_opened);
            rlSensorTime.setEnabled(true);
            rlSensorSet.setEnabled(true);
        }
//        tvMasterNumber.setText(mDoorbellConfig.getMasterNumber());
//        tvSOSNumber.setText(mDoorbellConfig.getSosNumber());
        tvDoorbellLookTime.setText(mDoorbellConfig.getDoorbellLookTime() + getString(R.string.second));
        tvDoorbellVideotapTime.setText(mDoorbellConfig.getVideotapTime() + getString(R.string.second));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageAction(NIMMessageAction action) {
        if (!NIMMessageAction.NIMMESSAGE_DOORBELL_CONFIG.equals(action.getType())) {
            return;
        }
        //更新门铃信息
        updateView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mAutoSensorTimeDialog != null)
            mAutoSensorTimeDialog.dismiss();
        if (mMasterNumberDialog != null)
            mMasterNumberDialog.dismiss();
        if (mSOSNumberDialog != null)
            mSOSNumberDialog.dismiss();
        if (mDoorbellLookDialog != null)
            mDoorbellLookDialog.dismiss();
        if (mVideotapTimeDialog != null)
            mVideotapTimeDialog.dismiss();
        if (mDoorbellLeavelTimeDialog != null)
            mDoorbellLeavelTimeDialog.dismiss();
    }
}
