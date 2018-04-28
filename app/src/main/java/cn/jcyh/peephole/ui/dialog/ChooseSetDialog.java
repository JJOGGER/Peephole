package cn.jcyh.peephole.ui.dialog;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.ChooseSetAdapter;
import timber.log.Timber;


public class ChooseSetDialog extends BaseDialogFragment implements ChooseSetAdapter.OnItemClickListener {
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    private String mTitle;
    private ChooseSetAdapter mChooseSetAdapter;


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setAdapter(ChooseSetAdapter chooseSetAdapter) {
        mChooseSetAdapter = chooseSetAdapter;
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
        rvContent.setLayoutManager(new LinearLayoutManager(mActivity));
        if (mChooseSetAdapter != null) {
            rvContent.setAdapter(mChooseSetAdapter);
            mChooseSetAdapter.setOnItemClickListener(this);
        }
    }

    @OnClick({R.id.tv_confirm, R.id.tv_cancel})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                break;
            case R.id.tv_cancel:
                break;
        }
    }

    @Override
    public void onItemClick(String data, int pos) {
//        mChooseSetAdapter.setCheckedItem(pos);
        Timber.e("------data:" + data + "==" + pos);
        //播放铃声
//        MediaPlayer.create(mActivity,)
    }
}
