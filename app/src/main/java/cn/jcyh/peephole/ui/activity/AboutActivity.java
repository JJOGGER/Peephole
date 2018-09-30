package cn.jcyh.peephole.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.entity.DownloadInfo;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.service.UpdateSoftService;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.HintDialogFragmemt;
import cn.jcyh.peephole.utils.APKUtil;
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void init() {
        super.init();
        String androidVersion = android.os.Build.VERSION.RELEASE;
        tvVersion.setText(androidVersion);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        tvVersionCode.setText(SystemUtil.getVersionName() + "_" + widthPixels + "x" + heightPixels);
    }


    @OnClick({R.id.tv_wifi_update})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_wifi_update:
                checkUpdate();
                break;
        }
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        showProgressDialog();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        String solutions = widthPixels + "x" + heightPixels;
        HttpAction.getHttpAction().updateSoft(SystemUtil.getSystemVersion(), solutions, new IDataListener<Version>() {
            @Override
            public void onSuccess(final Version version) {
                cancelProgressDialog();
                try {
                    if (Integer.valueOf(version.getNumber()) > SystemUtil.getVersionCode()) {
                        update(version);
                    } else {
                        T.show(R.string.no_new_version);
                    }
                } catch (Exception e) {
                    T.show(R.string.no_new_version);
                }
            }

            @Override
            public void onFailure(int errorCode, String desc) {
                cancelProgressDialog();
                T.show(desc);
            }
        });
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
                        downloadInfo.setSaveFilePath(APKUtil.APK_PATCH_PATH_ENCRYPT);
                        downloadInfo.setUrl(version.getAddress());
                        downloadInfo.setType(DownloadInfo.TYPE_DOWNLOAD_APK_ID);
                        Intent intent = new Intent(AboutActivity.this, UpdateSoftService.class);
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
}
