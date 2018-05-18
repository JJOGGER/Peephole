package cn.jcyh.peephole.ui.dialog;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.ChooseSetAdapter;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;


public class ChooseSetDialog extends BaseDialogFragment {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    private String mTitle;
    private ChooseSetAdapter mChooseSetAdapter;
    private int mCurrentType;
    private String mCurrentData;
    private DoorbellConfig mDoorbellConfig;
    private MediaPlayer mPlayer;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setAdapter(ChooseSetAdapter chooseSetAdapter) {
        mChooseSetAdapter = chooseSetAdapter;
    }

    public void setType(int type) {
        mCurrentType = type;
    }

    @Override
    int getLayoutId() {
        return R.layout.dialog_choose_set;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        if (!TextUtils.isEmpty(mTitle)) {
            tvTitle.setText(mTitle);
        }
        mDoorbellConfig = DoorBellControlCenter.getInstance(mActivity).getDoorbellConfig();
        rvContent.setLayoutManager(new LinearLayoutManager(mActivity));
        if (mChooseSetAdapter != null) {
            rvContent.setAdapter(mChooseSetAdapter);
        }
    }

    public void setCheckedItem(String data) {
        if (mChooseSetAdapter == null) return;
        mCurrentData = data;
        mChooseSetAdapter.setCheckedItem(mChooseSetAdapter.getPosition(data));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick({R.id.tv_confirm, R.id.tv_cancel})
    public void onClick(View v) {
        if (mOnDialogListener == null) return;
        switch (v.getId()) {
            case R.id.tv_confirm:
                mOnDialogListener.onConfirm(mCurrentData);
                break;
            case R.id.tv_cancel:
                break;
        }
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

    }
}
