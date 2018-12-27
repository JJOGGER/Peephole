package cn.jcyh.peephole.ui.dialog;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import butterknife.OnCheckedChanged;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.ui.dialog.BaseDialogFragment;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.RecordAudioDialogFragment;
import cn.jcyh.peephole.utils.L;

/**
 * 作者：jogger
 * 时间：2018/12/27 11:38
 * 描述：
 */
public class ChooseCustomRingDialog extends BaseDialogFragment {
    private DialogHelper mCustomDialog;
    private int mCurrentType = Constant.TYPE_RING;

    @Override
    public int getLayoutId() {
        return R.layout.dialog_choose_custom_ring;
    }

    @Override
    protected void init(View view) {
        super.init(view);
    }

    private void showCustomDialog() {
        final RecordAudioDialogFragment recordAudioDialogFragment =RecordAudioDialogFragment.newInstance(mCurrentType);
        mCustomDialog = new DialogHelper((BaseActivity) mActivity, recordAudioDialogFragment);
        mCustomDialog.commit();
    }

    @OnCheckedChanged({R.id.rb_ring, R.id.rb_alarm})
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        L.e("---------------->onCheckedChanged:"+b);
        if (!b)return;
        switch (compoundButton.getId()) {
            case R.id.rb_ring:
                mCurrentType = Constant.TYPE_RING;
                break;
            case R.id.rb_alarm:
                mCurrentType = Constant.TYPE_ALARM;
                break;
        }
        showCustomDialog();
        mCurrentType=ControlCenter.DOORBELL_TYPE_RING;
        compoundButton.setChecked(false);
        dismiss();
    }
}
