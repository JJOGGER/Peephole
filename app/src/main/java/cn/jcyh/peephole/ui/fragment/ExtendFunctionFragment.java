package cn.jcyh.peephole.ui.fragment;

import android.bluetooth.BluetoothAdapter;
import android.widget.CompoundButton;

import butterknife.OnCheckedChanged;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;

/**
 * Created by jogger on 2018/6/25.扩展功能
 */
public class ExtendFunctionFragment extends BaseFragment {
    @Override
    public int getLayoutId() {
        return R.layout.fragment_extend_function;
    }

    @OnCheckedChanged({R.id.cb_bluetooth_switch})
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
        }
    }
}
