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
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.control.DoorbellAudioManager;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.PlayAudio;
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
    private String mCurrentData;//文件显示名称，不含路径
    private ChooseSetAdapter mFileAdapter;
    private int mCurrentShow = 0;
    private OnChooseRingClickListener mListener;
    private List<String> mDefaultDatas;
    private List<String> mLocalFiles;
    private DoorbellConfig mDoorbellConfig;

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
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        if (!TextUtils.isEmpty(mTitle)) {
            tvTitle.setText(mTitle);
        }
        mLocalFiles = new ArrayList<>();
        rvContent.setLayoutManager(new LinearLayoutManager(mActivity));
        boolean existLocal = false;
        File file;
        if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
            file = new File(FileUtil.getExpandRingPath());
            String customDoorbellRingName = mDoorbellConfig.getCustomDoorbellRingName();
            if (!TextUtils.isEmpty(customDoorbellRingName)) {
                File localFile = new File(customDoorbellRingName);
                if (localFile.exists()) {
                    existLocal = true;
                }
            }
        } else {
            file = new File(FileUtil.getExpandAlarmPath());
            String customDoorbellAlarmName = mDoorbellConfig.getCustomDoorbellAlarmName();
            if (!TextUtils.isEmpty(customDoorbellAlarmName)) {
                File localFile = new File(customDoorbellAlarmName);
                if (localFile.exists()) {
                    existLocal = true;
                }
            }
        }
        if (file.exists() && file.list() != null && file.list().length != 0) {
            for (int i = 0; i < file.list().length; i++) {
                if (!file.list()[i].endsWith(".mp3") && !file.list()[i].endsWith(".wav") && !file
                        .list()[i].endsWith(".3gpp"))
                    continue;
                String name = file.list()[i];
                mLocalFiles.add(name);
            }
        }
        mFileAdapter = new ChooseSetAdapter(mLocalFiles);
        mChooseSetAdapter = new ChooseSetAdapter(mDefaultDatas);
        mFileAdapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String data, int pos) {
                mCurrentData = data;
                //选中时播放
                PlayAudio playAudio;
                if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
                    playAudio = new PlayAudio(FileUtil.getExpandRingPath() + mCurrentData);
                    DoorbellAudioManager.getDoorbellAudioManager().playFile(DoorbellAudioManager
                            .RingerTypeEnum.DOORBELL_RING_CUSTOM, playAudio);
                } else {
                    playAudio = new PlayAudio(FileUtil.getExpandAlarmPath() + mCurrentData);
                    DoorbellAudioManager.getDoorbellAudioManager().playFile(DoorbellAudioManager
                            .RingerTypeEnum.DOORBELL_ALARM_CUSTOM, playAudio);
                }
                mFileAdapter.setCheckedItem(pos);
                if (mListener != null) {
                    mListener.onItemClick(data, pos);
                }
            }

            @Override
            public void onLongItemClick(final String data, final int pos) {
                final HintDialogFragmemt hintDialogFragmemt = new HintDialogFragmemt();
                hintDialogFragmemt.setHintContent(getString(R.string.delete_msg));
                hintDialogFragmemt.setOnHintDialogListener(new HintDialogFragmemt
                        .OnHintDialogListener() {
                    @Override
                    public void onConfirm(boolean isConfirm) {
                        if (isConfirm) {
                            L.e("---------删除:" + data);
                            File deleteFile;
                            // TODO: 2018/12/28 如果删除的是当前正在使用的铃声，则删除本地记录
                            if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
                                deleteFile = new File(FileUtil.getExpandRingPath(), data);
                                if (deleteFile.getAbsolutePath().equals(mDoorbellConfig
                                        .getCustomDoorbellRingName())) {
                                    mDoorbellConfig.setCustomDoorbellRingName(null);
                                    ControlCenter.getDoorbellManager().setDoorbellConfig
                                            (mDoorbellConfig);
                                }
                            } else {
                                deleteFile = new File(FileUtil.getExpandAlarmPath(), data);
                                if (deleteFile.getAbsolutePath().equals(mDoorbellConfig
                                        .getCustomDoorbellAlarmName())) {
                                    mDoorbellConfig.setCustomDoorbellAlarmName(null);
                                    ControlCenter.getDoorbellManager().setDoorbellConfig
                                            (mDoorbellConfig);
                                }
                            }
                            if (deleteFile.exists()) {
                                deleteFile.delete();
                            }
                            mFileAdapter.getData().remove(pos);
                            mFileAdapter.notifyItemRemoved(pos);
                            if (mFileAdapter.getItemCount() == 0) {
                                //默认选择一个
                                mChooseSetAdapter.setCheckedItem(0);
                                if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
                                    mDoorbellConfig.setDoorbellRingName(Constant.ASSET_RING +
                                            File.separator + mCurrentData);
                                } else {
                                    mDoorbellConfig.setDoorbellAlarmName(Constant.ASSET_ALARM +
                                            File.separator + mCurrentData);
                                }
                                rvContent.setAdapter(mChooseSetAdapter);
                            }
                        }
                        hintDialogFragmemt.dismiss();
                    }
                });
                hintDialogFragmemt.show(getFragmentManager(), HintDialogFragmemt.class
                        .getSimpleName());
            }
        });
        mChooseSetAdapter.setOnItemClickListener(new ChooseSetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String data, int pos) {
                mCurrentData = data;
                L.e("-----data" + data);
                PlayAudio playAudio;
                if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
                    playAudio = new PlayAudio(Constant.ASSET_RING + File.separator + data);
                    DoorbellAudioManager.getDoorbellAudioManager().playAssets(DoorbellAudioManager
                            .RingerTypeEnum.DOORBELL_RING, playAudio);
                } else {
                    playAudio = new PlayAudio(Constant.ASSET_ALARM + File.separator + data);
                    DoorbellAudioManager.getDoorbellAudioManager().playAssets(DoorbellAudioManager
                            .RingerTypeEnum.DOORBELL_ALARM, playAudio);
                }
                mChooseSetAdapter.setCheckedItem(pos);

                if (mListener != null) {
                    mListener.onItemClick(data, pos);
                }
            }

            @Override
            public void onLongItemClick(String data, int pos) {

            }
        });
        if (existLocal) {
            String fileName;
            if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
                fileName = mDoorbellConfig.getCustomDoorbellRingName();
            } else {
                fileName = mDoorbellConfig.getCustomDoorbellAlarmName();
            }
            File local = new File(fileName);
            mCurrentData = local.getName();
            mFileAdapter.setCheckedItem(mFileAdapter.getPosition(mCurrentData));
            mCurrentShow = 1;
            rvContent.setAdapter(mFileAdapter);
        } else {
            String assetName;
            if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
                assetName = mDoorbellConfig.getShowDoorbellRingName();
            } else {
                assetName = mDoorbellConfig.getShowDoorbellAlarmName();
            }
            mCurrentData = assetName;
            L.e("--------------这里存在本地，则有问题");
            mChooseSetAdapter.setCheckedItem(mChooseSetAdapter.getPosition(assetName));
            mCurrentShow = 0;
            rvContent.setAdapter(mChooseSetAdapter);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick({R.id.tv_confirm, R.id.tv_cancel, R.id.ibtn_file})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                if (mCurrentShow == 1) {
                    if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
                        L.e("----FileUtil.getExpandRingPath()+mCurrentData:" + FileUtil
                                .getExpandRingPath() + mCurrentData);
                        mDoorbellConfig.setCustomDoorbellRingName(FileUtil.getExpandRingPath() +
                                mCurrentData);
                    } else {
                        mDoorbellConfig.setCustomDoorbellAlarmName(FileUtil.getExpandAlarmPath()
                                + mCurrentData);
                    }
                } else {
                    if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
                        mDoorbellConfig.setDoorbellRingName(Constant.ASSET_RING + File.separator
                                + mCurrentData);
                        mDoorbellConfig.setCustomDoorbellRingName(null);
                    } else {
                        mDoorbellConfig.setDoorbellAlarmName(Constant.ASSET_ALARM + File
                                .separator + mCurrentData);
                        mDoorbellConfig.setCustomDoorbellAlarmName(null);
                    }
                }
                ControlCenter.getDoorbellManager().setDoorbellConfig(mDoorbellConfig);
                if (mOnDialogListener != null)
                    mOnDialogListener.onConfirm(mCurrentData);
                dismiss();
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.ibtn_file:
                if (mCurrentShow == 0) {
                    setCheckedItemLocal();
                } else {
                    rvContent.setAdapter(mChooseSetAdapter);
                    mCurrentShow = 0;
                }
                break;
        }
    }

    private void setCheckedItemLocal() {
        if (mFileAdapter == null || rvContent == null) return;
        File file;
        String name;
        if (mFileAdapter.getData().isEmpty()) {
            T.show(R.string.no_local_file);
            return;
        }
        if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
            file = new File(FileUtil.getExpandRingPath());
            if (!file.exists() || file.list() == null || file.list().length == 0) {
                T.show(R.string.no_local_file);
                return;
            }
            name = mDoorbellConfig.getCustomDoorbellRingName();
        } else {
            file = new File(FileUtil.getExpandAlarmPath());
            if (!file.exists() || file.list() == null || file.list().length == 0) {
                T.show(R.string.no_local_file);
                return;
            }
            name = mDoorbellConfig.getCustomDoorbellAlarmName();
        }
        //存在本地文件，但没有配置到猫眼
        mCurrentData = null;
        if (!TextUtils.isEmpty(name)) {
            for (int i = 0; i < file.list().length; i++) {
                if (name.contains(file.list()[i])) {
                    mCurrentData = file.list()[i];
                    break;
                }
            }
        }
//        if (!TextUtils.isEmpty(name)) {
//            mCurrentData = name;
//        }
        L.e("----------mCurrentData:::" + mCurrentData);
        mFileAdapter.setCheckedItem(mFileAdapter.getPosition(mCurrentData));
        rvContent.setAdapter(mFileAdapter);
        mCurrentShow = 1;
    }

    private void setCheckedItemDefault() {
        if (mChooseSetAdapter == null || rvContent == null) return;
        File file;
        String name;
        if (mCurrentType == ControlCenter.DOORBELL_TYPE_RING) {
            file = new File(FileUtil.getExpandRingPath());
            if (!file.exists() || file.list() == null || file.list().length == 0) {
                T.show(R.string.no_local_file);
                return;
            }
            name = mDoorbellConfig.getCustomDoorbellRingName();
        } else {
            file = new File(FileUtil.getExpandAlarmPath());
            if (!file.exists() || file.list() == null || file.list().length == 0) {
                T.show(R.string.no_local_file);
                return;
            }
            name = mDoorbellConfig.getCustomDoorbellAlarmName();
        }
        if (!TextUtils.isEmpty(name)) {
            mCurrentData = name;
        }
        mFileAdapter.setCheckedItem(mFileAdapter.getPosition(mCurrentData));
        rvContent.setAdapter(mFileAdapter);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        DoorbellAudioManager.getDoorbellAudioManager().stop();
    }
}
