package cn.jcyh.peephole.ui.fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.constant.ExtendFunction;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.ui.activity.AudioValiActivity;
import cn.jcyh.peephole.ui.activity.FaceActivity;
import cn.jcyh.peephole.utils.T;

/**
 * Created by jogger on 2018/6/25.扩展功能
 */
public class ExtendFunctionFragment extends BaseFragment {
    @BindView(R.id.cb_face_switch)
    CheckBox cbFaceSwitch;
    @BindView(R.id.cb_audio_switch)
    CheckBox cbAudioSwitch;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_extend_function;
    }

    @Override
    public void init() {
        super.init();
        boolean faceVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_FACE_VALI);
        cbFaceSwitch.setChecked(faceVali);
        boolean audioVali = ControlCenter.isFunctionUse(ExtendFunction.FUNCTION_AUDIO_VALI);
        cbAudioSwitch.setChecked(audioVali);
    }

    @OnCheckedChanged({R.id.cb_bluetooth_switch, R.id.cb_face_switch, R.id.cb_audio_switch})
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_bluetooth_switch:
                BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                boolean enabled = defaultAdapter.isEnabled();
                if (isChecked) {
                    //开启蓝牙
                    if (!enabled) {
                        defaultAdapter.enable();
                    }
                } else {
                    if (enabled) {
                        defaultAdapter.disable();
                    }
                }
                break;
            case R.id.cb_face_switch:
                if (!ControlCenter.isRegistedFaceVali()) {
                    T.show("请先注册人脸图像");
                    cbFaceSwitch.setChecked(!isChecked);
                    return;
                }
                ControlCenter.setFunctionUse(ExtendFunction.FUNCTION_FACE_VALI, isChecked);
                break;
            case R.id.cb_audio_switch:
                if (!ControlCenter.isRegistedAudioVali()) {
                    T.show("请先注册声纹");
                    cbAudioSwitch.setChecked(!isChecked);
                    return;
                }
                ControlCenter.setFunctionUse(ExtendFunction.FUNCTION_AUDIO_VALI, isChecked);
                break;
        }
    }

    @OnClick({R.id.rl_face, R.id.rl_audio})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_face:
                startNewActivity(FaceActivity.class);
                break;
            case R.id.rl_audio:
                startNewActivity(AudioValiActivity.class);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        init();
    }
}
