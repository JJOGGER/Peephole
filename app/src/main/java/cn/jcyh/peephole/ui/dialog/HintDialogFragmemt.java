package cn.jcyh.peephole.ui.dialog;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;

/**
 * Created by it on 2017/3/9.
 * 提示对话框
 */

public class HintDialogFragmemt extends BaseDialogFragment {
    @BindView(R.id.tv_content)
    TextView tv_content;
    @BindView(R.id.tv_confirm)
    TextView tv_confirm;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    private String mHintContent = "";
    private String confirmText = "";
    private String cancelText = "";

    public String getHintContent() {
        return mHintContent;
    }

    public void setHintContent(String hintContent) {
        mHintContent = hintContent;
    }

    private OnDialogListener mListener;

    public void setConfirmText(String text) {
        confirmText = text;
    }

    public void setCancelText(String text) {
        cancelText = text;
    }

    public String getConfirmText() {
        return confirmText;
    }

    public String getCancelText() {
        return cancelText;
    }

    @Override
    protected void setOnDialogListener(OnDialogListener onDialogListener) {
        super.setOnDialogListener(onDialogListener);
        mListener = onDialogListener;
    }

    @Override
    int getLayoutId() {
        return R.layout.dialog_hint;
    }

    @Override
    protected void init(View view) {
        tv_content.setText(getHintContent());
        if (!TextUtils.isEmpty(getConfirmText())) {
            tv_confirm.setText(getConfirmText());
        }
        if (!TextUtils.isEmpty(getCancelText())) {
            tv_cancel.setText(getCancelText());
        }
    }

    @OnClick({R.id.tv_confirm, R.id.tv_cancel})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                if (mListener != null) {
                    mListener.onConfirm(true);
                }
                break;
            case R.id.tv_cancel:
                if (mListener != null) {
                    mListener.onConfirm(false);
                }
                break;
        }
    }
}
