package cn.jcyh.peephole.ui.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DownloadInfo;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.service.UpdateSystemService;
import cn.jcyh.peephole.utils.APKUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.NetworkUtil;
import cn.jcyh.peephole.utils.ServiceUtil;
import cn.jcyh.peephole.utils.SystemUtil;
import cn.jcyh.peephole.utils.T;

public class SystemUpdateActivity extends BaseActivity implements UpdateSystemService.OnUpdateListener {
    @BindView(R.id.tv_check)
    TextView tvCheck;
    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.tv_state)
    TextView tvState;
    @BindView(R.id.tv_pro)
    TextView tvPro;
    @BindView(R.id.iv_update)
    ImageView ivUpdate;
    @BindView(R.id.tv_immediately_setup)
    TextView tvImmediatelySetup;
    @BindView(R.id.tv_setup_wait)
    TextView tvSetupWait;
    private Version mVersion;
    private ServiceConnection mServiceConnection;
    private UpdateSystemService.UpdateBinder mUpdateBinder;
    private DownloadInfo mDownloadInfo;
    private ObjectAnimator mAnimator;

    @Override
    public int getLayoutId() {
        return R.layout.activity_system_update;
    }

    @Override
    protected void init() {
        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mUpdateBinder = (UpdateSystemService.UpdateBinder) iBinder;
                    mUpdateBinder.setUpdateListener(SystemUpdateActivity.this);
                    mDownloadInfo = mUpdateBinder.getLocalDownloadInfo();
                    //初始化视图
                    showCurrentView();
                    if (mDownloadInfo == null) {
                        checkVersion();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mUpdateBinder = null;
                }
            };
        }
        ServiceUtil.bindService(UpdateSystemService.class, mServiceConnection, BIND_AUTO_CREATE);
//        ServiceUtil.startService(UpdateSystemService.class);
    }


    @OnClick({R.id.tv_check, R.id.tv_immediately_setup, R.id.tv_setup_wait})
    public void onClick(View v) {
        if (mUpdateBinder == null) return;
        switch (v.getId()) {
            case R.id.tv_check:
                if (mDownloadInfo == null) {
                    checkVersion();
                    return;
                }
                if (mUpdateBinder.getCurrentState() == DownloadInfo.STATE_NO_DOWNLOAD) {
                    //开始下载
                    mUpdateBinder.startDownload(mDownloadInfo);
                } else if (mUpdateBinder.getCurrentState() == DownloadInfo.STATE_DOWNLOADING ||
                        mUpdateBinder.getCurrentState() == DownloadInfo.STATE_DOWNLOAD_PAUSE) {
                    //取消下载
                    mUpdateBinder.cancelDownload();
                }
                showCurrentView();
                break;
            case R.id.tv_immediately_setup:
                setup();
                break;
            case R.id.tv_setup_wait:
                if (mUpdateBinder.getLocalDownloadInfo() == null) return;
                if (mUpdateBinder.getLocalDownloadInfo().getCurrentState() == DownloadInfo.STATE_DOWNLOADED) {
                    //稍后安装
                    finish();
                }
                break;
        }
    }

    /**
     * 安装
     */
    private void setup() {
        if (ControlCenter.sCurrentBattery <= 30) {
            T.show(R.string.battery_low_update_msg);
            return;
        }
        showProgressDialog();
        mUpdateBinder.installSystem();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateBinder != null) {
            int currentState = mUpdateBinder.getCurrentState();
            ServiceUtil.unbindService(mServiceConnection);
            if (currentState == DownloadInfo.STATE_DOWNLOADED) {
                ServiceUtil.stopService(UpdateSystemService.class);
            }
        }
        ControlCenter.setDownloadInfo(mDownloadInfo);
    }

    private void checkVersion() {
        if (!NetworkUtil.isConnected()) {
            T.show(R.string.network_is_not_available);
            return;
        }
        showProgressDialog(getString(R.string.check_update_msg));
        //检查版本
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        String solutions = widthPixels + "x" + heightPixels;
        HttpAction.getHttpAction().updateSystem(SystemUtil.getVersionCode(), SystemUtil.getSystemVersion(), solutions, new IDataListener<Version>() {
            @Override
            public void onSuccess(Version version) {
                cancelProgressDialog();
                L.e("-------------------version:"+version);
                if (Integer.valueOf(version.getNumber()) > SystemUtil.getVersionCode()) {
                    tvState.setText(R.string.downloadable_update);
                    tvCheck.setText(R.string.download_update);
                    mVersion = version;
                    mDownloadInfo = new DownloadInfo();
                    mDownloadInfo.setTitle(getString(R.string.system_updating));
                    mDownloadInfo.setDesc(getString(R.string.updating));
                    mDownloadInfo.setSaveFilePath(APKUtil.SYSTEM_PATCH_PATH);
                    mDownloadInfo.setUrl(mVersion.getAddress());
                    mDownloadInfo.setType(DownloadInfo.TYPE_DOWNLOAD_SYSTEM_ID);
                    mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
                    ControlCenter.setDownloadInfo(mDownloadInfo);
                } else {
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

    @Override
    public void onDownloadPause() {
        if (isDestroyed() || isFinishing() || getSupportFragmentManager() == null) return;
        showCurrentView();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onProgress(int pro) {
        if (isDestroyed() || isFinishing() || getSupportFragmentManager() == null) return;
        tvPro.setVisibility(View.VISIBLE);
        tvPro.setText(pro + "%");
    }

    @Override
    public void onDownloadCompleted() {
        if (isDestroyed() || isFinishing() || getSupportFragmentManager() == null) return;
        showCurrentView();
    }

    @Override
    public void onDownloadFail() {
        if (isDestroyed() || isFinishing() || getSupportFragmentManager() == null) return;
        showCurrentView();
    }

    @Override
    public void onDownloadRunning() {
        if (isDestroyed() || isFinishing() || getSupportFragmentManager() == null) return;
        showCurrentView();
    }

    @Override
    public void onEncryptFail() {
        if (isDestroyed() || isFinishing() || getSupportFragmentManager() == null) return;
        cancelProgressDialog();
        showCurrentView();
    }

    @Override
    public void onEncryptSuccess() {
        if (isDestroyed() || isFinishing() || getSupportFragmentManager() == null) return;
        cancelProgressDialog();
    }

    @Override
    public void onStartDownload() {
        if (isDestroyed() || isFinishing() || getSupportFragmentManager() == null) return;
        showCurrentView();
    }


    @SuppressLint("SetTextI18n")
    public void showCurrentView() {
        if (mUpdateBinder == null || mUpdateBinder.getLocalDownloadInfo() == null) {
            tvPro.setVisibility(View.GONE);
            tvState.setText(R.string.current_newst_version);
            tvState.setText(R.string.check_update);
            return;
        }
        int currentState = mUpdateBinder.getLocalDownloadInfo().getCurrentState();
        switch (currentState) {
            case DownloadInfo.STATE_NO_DOWNLOAD:
                tvPro.setVisibility(View.GONE);
                tvCheck.setVisibility(View.VISIBLE);
                tvCheck.setText(R.string.download_update);
                tvState.setText(R.string.downloadable_update);
                llBottom.setVisibility(View.GONE);
                endProAnim();
                break;
            case DownloadInfo.STATE_DOWNLOADING:
                startProAnim();
                tvPro.setVisibility(View.VISIBLE);
                tvState.setText(R.string.downloading);
                tvCheck.setText(R.string.cancel);
                break;
            case DownloadInfo.STATE_DOWNLOAD_PAUSE:
                tvState.setText(R.string.download_pause);
                tvCheck.setText(R.string.cancel);
                endProAnim();
                break;
            case DownloadInfo.STATE_DOWNLOADED:
                tvState.setText(R.string.download_completed);
                tvPro.setVisibility(View.VISIBLE);
                tvPro.setText("100%");
                tvCheck.setVisibility(View.GONE);
                llBottom.setVisibility(View.VISIBLE);
                endProAnim();
                //判断文件是否存在
                File file = new File(APKUtil.SYSTEM_PATCH_PATH);
                if (!file.exists()) {
                    mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
                    showCurrentView();
                }
                break;
        }
    }

    private void startProAnim() {
        if (mAnimator == null) {
            mAnimator = ObjectAnimator.ofFloat(ivUpdate, "rotation", 0f, 360f);
            mAnimator.setDuration(1000);
            mAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            mAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        if (!mAnimator.isStarted())
            mAnimator.start();
    }

    private void endProAnim() {
        if (mAnimator == null) return;
        if (mAnimator.isStarted())
            mAnimator.end();
    }
}
