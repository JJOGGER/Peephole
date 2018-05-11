package cn.jcyh.peephole.ui.dialog;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;

/**
 * Created by jogger on 2018/4/28.
 */

public class CommonEditDialog extends BaseDialogFragment {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.et_content)
    EditText etContent;
    private String mTitle;
    private String mContent;

    @Override
    int getLayoutId() {
        return R.layout.dialog_common_edit;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        if (!TextUtils.isEmpty(mTitle))
            tvTitle.setText(mTitle);
        if (!TextUtils.isEmpty(mContent))
            etContent.setText(mContent);
    }

    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                break;
            case R.id.tv_confirm:
                if (mOnDialogListener != null) {
                    String number = etContent.getText().toString().trim();
                    if (TextUtils.isEmpty(number)) {
                        etContent.setError(getString(R.string.input_no_null));
                        return;
                    }
                    mOnDialogListener.onConfirm(number);
                }
                break;
        }
        dismiss();
    }
}
