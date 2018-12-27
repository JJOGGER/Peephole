package cn.jcyh.peephole.ui.dialog;

import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.ChooseSetAdapter;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.T;


public class ChooseRingDialog extends BaseDialogFragment {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    private String mTitle;
    private ChooseSetAdapter mChooseSetAdapter;
    private int mCurrentType;
    private String mCurrentData;
    private ChooseSetAdapter mFileAdapter;
    private int mCurrentShow = 0;
    private OnChooseRingClickListener mListener;
    private List<String> mDefaultDatas;

    public interface OnChooseRingClickListener {
        void onItemClick(String data, int pos);
    }

    public void setOnChooseRingClickListener(OnChooseRingClickListener listener) {
        mListener = listener;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setAdapter(ChooseSetAdapter chooseSetAdapter) {
        mChooseSetAdapter = chooseSetAdapter;
    }

    public void setDatas(List<String> defaultDatas) {
        mDefaultDatas = defaultDatas;
    }

    public void setType(int type) {
        mCurrentType = type;
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_choose_ring;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        if (!TextUtils.isEmpty(mTitle)) {
            tvTitle.setText(mTitle);
        }
        rvContent.setLayoutManager(new LinearLayoutManager(mActivity));
        mChooseSetAdapter = new ChooseSetAdapter(mDefaultDatas);
        rvContent.setAdapter(mChooseSetAdapter);
        mChooseSetAdapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String data, int pos) {
                mCurrentData = data;
                mChooseSetAdapter.setCheckedItem(mChooseSetAdapter.getPosition(data));
                if (mListener != null) {
                    mListener.onItemClick(data, pos);
                }
            }
        });
    }

    public void setCheckedItem(String data) {
        if (mChooseSetAdapter == null || rvContent == null || TextUtils.isEmpty(data)) return;
        mCurrentData = data;
        mChooseSetAdapter.setCheckedItem(mChooseSetAdapter.getPosition(data));
        rvContent.setAdapter(mChooseSetAdapter);
    }

    public void setCheckedItem2(String data) {
        if (mFileAdapter == null || rvContent == null) return;
        if (TextUtils.isEmpty(data)) {
            mCurrentData = mFileAdapter.getData().get(0);
        } else {
            mCurrentData = data;
        }
        mFileAdapter.setCheckedItem(mFileAdapter.getPosition(mCurrentData));
        rvContent.setAdapter(mFileAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick({R.id.tv_confirm, R.id.tv_cancel, R.id.ibtn_file})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                if (mOnDialogListener != null)
                    mOnDialogListener.onConfirm(mCurrentData);
                dismiss();
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.ibtn_file:
                if (mCurrentShow == 0) {
                    if (mFileAdapter == null) {
                        File file;
                        if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
                            file = new File(FileUtil.getExpandRingPath());
                        } else {
                            file = new File(FileUtil.getExpandAlarmPath());
                        }
                        if (!file.exists() || file.list() == null || file.list().length == 0) {
                            T.show(R.string.no_local_file);
                            return;
                        }
                        List<String> files = new ArrayList<>();
                        for (int i = 0; i < file.list().length; i++) {
                            String name = file.list()[i];
                            files.add(name);
                        }
                        L.e("----------files:" + files);
                        mFileAdapter = new ChooseSetAdapter(files);
                    }
                    setCheckedItem2(ControlCenter.getDoorbellManager().getDoorbellConfig().getCustomDoorbellAlarmName());
                    mFileAdapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(String data, int pos) {
                            setCheckedItem2(data);
                            if (mListener != null) {
                                mListener.onItemClick(data, pos);
                            }
                        }
                    });
                    mCurrentShow = 1;
                } else {
                    rvContent.setAdapter(mChooseSetAdapter);
                    mCurrentShow = 0;
                }
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
