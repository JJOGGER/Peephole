package cn.jcyh.peephole.ui.dialog;

import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.ChooseSetAdapter;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;


public class ChooseSetDialog extends BaseDialogFragment implements ChooseSetAdapter.OnItemClickListener {
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
            mChooseSetAdapter.setOnItemClickListener(this);
        }
    }

    public void setCheckedItem(int position) {
        if (mChooseSetAdapter == null) return;
        mChooseSetAdapter.setCheckedItem(position);
    }

    public void setCheckedItem(String data) {
        if (mChooseSetAdapter == null) return;
        mCurrentData = data;
        mChooseSetAdapter.setCheckedItem(mChooseSetAdapter.getPosition(data));
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (mCurrentType == DoorBellControlCenter.DOORBELL_TYPE_RING) {
//            mCurrentData = mDoorbellConfig.getDoorbellRingName();
//            mChooseSetAdapter.setCheckedItem(mChooseSetAdapter.getPosition(mCurrentData));
//        }
    }

    @OnClick({R.id.tv_confirm, R.id.tv_cancel})
    public void onClick(View v) {
        if (mOnDialogListener == null) return;
        switch (v.getId()) {
            case R.id.tv_confirm:
                mOnDialogListener.onConfirm(mCurrentData);
                if (mCurrentType == DoorBellControlCenter.DOORBELL_TYPE_RING) {
                    mDoorbellConfig.setDoorbellRingName(mCurrentData);
                } else {
                    mDoorbellConfig.setDoorbellAlarmName(mCurrentData);
                }
                DoorBellControlCenter.getInstance(mActivity).saveDoorbellConfig(mDoorbellConfig);
                break;
            case R.id.tv_cancel:
                break;
        }
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    @Override
    public void onItemClick(String data, int pos) {
        mCurrentData = data;
        //播放铃声
        try {
            AssetFileDescriptor descriptor;
            AssetManager assets = getResources().getAssets();
            if (mCurrentType == DoorBellControlCenter.DOORBELL_TYPE_RING)
                descriptor = assets.openFd("ring/" + data);
            else descriptor = assets.openFd("alarm/" + data);
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
                mPlayer.setLooping(false);
            } else {
                mPlayer.stop();
                mPlayer.reset();
            }
            mPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
