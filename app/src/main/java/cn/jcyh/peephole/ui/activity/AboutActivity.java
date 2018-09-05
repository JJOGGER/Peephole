package cn.jcyh.peephole.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DownloadInfo;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.service.UpdateService;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.HintDialogFragmemt;
import cn.jcyh.peephole.utils.APKUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.SystemUtil;
import cn.jcyh.peephole.utils.T;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.tv_version_code)
    TextView tvVersionCode;
    @BindView(R.id.tv_wifi_update)
    TextView tvWifiUpdate;
    private DialogHelper mUpdateDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void init() {
        super.init();
        String androidVersion = android.os.Build.VERSION.RELEASE;
        tvVersion.setText(androidVersion);
        tvVersionCode.setText(SystemUtil.getVersionName());
    }


    @OnClick({R.id.tv_wifi_update})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_wifi_update:
                if (ControlCenter.sIsDownloadUpdate) {
                    T.show(R.string.updating);
                    return;
                }
                checkUpdate();
                break;
        }
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        // TODO: 2018/8/7 先从猫眼文件中获取是否存在可更新的apk 
//        if (ControlCenter.getNewVersion() == null) {
        showProgressDialog();
        HttpAction.getHttpAction().updatePatch(new IDataListener<Version>() {
            @Override
            public void onSuccess(final Version version) {
                cancelProgressDialog();
                try {
                    if (Integer.valueOf(version.getNumber()) > SystemUtil.getVersionCode()) {
//                        ControlCenter.setNewVersion(version);
                        update(version);
                    } else {
                        T.show(R.string.no_new_version);
                    }
                } catch (Exception e) {
                    T.show(R.string.no_new_version);
                }

//                    else {
//                        ControlCenter.setNewVersion(null);
//                    }
//                    startNewActivity(AppUpdateActivity.class);
            }

            @Override
            public void onFailure(int errorCode, String desc) {
                cancelProgressDialog();
                L.e("--------------error:" + errorCode);
            }
        });
//        } else {
//            startNewActivity(AppUpdateActivity.class);
//        }
    }

    private void update(final Version version) {
        if (mUpdateDialog == null) {
            HintDialogFragmemt hintDialogFragmemt = new HintDialogFragmemt();
            hintDialogFragmemt.setHintContent(getString(R.string.new_version_msg));
            hintDialogFragmemt.setOnHintDialogListener(new HintDialogFragmemt.OnHintDialogListener() {
                @Override
                public void onConfirm(boolean isConfirm) {
                    if (isConfirm) {
                        DownloadInfo downloadInfo = new DownloadInfo();
                        downloadInfo.setTitle(getString(R.string.video_service));
                        downloadInfo.setDesc(getString(R.string.updating));
                        downloadInfo.setSaveFilePath(APKUtil.APK_PATCH_PATH);
                        downloadInfo.setUrl(version.getAddress());
                        downloadInfo.setType(DownloadInfo.TYPE_DOWNLOAD_APK_ID);
                        Intent intent = new Intent(AboutActivity.this, UpdateService.class);
                        intent.putExtra(Constant.DOWNLOAD_INFO, downloadInfo);
                        startService(intent);
                    }
                    mUpdateDialog.dismiss();
                }
            });
            mUpdateDialog = new DialogHelper(this, hintDialogFragmemt);
        }
        mUpdateDialog.commit();

    }
//
//    private void getsystem() {
//        mAndroid = android.os.Build.VERSION.RELEASE;
//        mBuildNumber = android.os.Build.MODEL;
//    }
//
//    private void initData() {
//
//        String title[] = getResources().getStringArray(R.array.aboutlist_item);
//        String get_data[] = {mAndroid, mBuildNumber, ""};
//        dataList = new ArrayList<Map<String, Object>>();
//        for (int i = 0; i < title.length; i++) {
//            Map<String, Object> map = new HashMap<String, Object>();
//            map.put("title", title[i]);
//            map.put("get_data", get_data[i]);
//            dataList.add(map);
//        }
//    }
}
