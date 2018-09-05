package cn.jcyh.peephole.ui.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

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
import cn.jcyh.peephole.utils.APKUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.ServiceUtil;
import cn.jcyh.peephole.utils.SystemUtil;
import cn.jcyh.peephole.utils.T;

public class SystemUpdateActivity extends BaseActivity implements UpdateService.OnUpdateListener {
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
    @BindView(R.id.tv_left)
    TextView tvLeft;
    @BindView(R.id.tv_right)
    TextView tvRight;
    private Version mVersion;
    private ServiceConnection mServiceConnection;
    private UpdateService.UpdateBinder mUpdateBinder;
    private int mCurrentState;
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
                    L.e("-------------onServiceConnected:");
                    mUpdateBinder = (UpdateService.UpdateBinder) iBinder;
                    mUpdateBinder.setUpdateListener(SystemUpdateActivity.this);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    L.e("----------onServiceDisconnected;" + mDownloadInfo.getCurrentState());
                    mUpdateBinder = null;
                }
            };
        }
        ServiceUtil.bindService(UpdateService.class, mServiceConnection, BIND_AUTO_CREATE);
        //获取本地存储的下载状态
        mDownloadInfo = ControlCenter.getDownloadInfo();
        showCurrentView();
        if (mDownloadInfo == null) {
            checkVersion();
        }
    }


    @OnClick({R.id.tv_check, R.id.tv_left, R.id.tv_right})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_check:
                if (mDownloadInfo == null) {
                    checkVersion();
                    return;
                }
                if (mDownloadInfo.getCurrentState() == DownloadInfo.STATE_NO_DOWNLOAD) {
                    mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOADING);
                    Intent intent = new Intent(this, UpdateService.class);
                    intent.putExtra(Constant.DOWNLOAD_INFO, mDownloadInfo);
                    startService(intent);
                } else if (mDownloadInfo.getCurrentState() == DownloadInfo.STATE_DOWNLOADING) {
                    //取消下载
                    L.e("------------->取消下载:" + mUpdateBinder);
                    mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
                    if (mUpdateBinder != null) {
                        mUpdateBinder.cancelDownload();
                    }
                }
                showCurrentView();
                break;
            case R.id.tv_left:
                if (mDownloadInfo == null) return;
                if (mDownloadInfo.getCurrentState() == DownloadInfo.STATE_DOWNLOADING) {
                    //暂停
                } else if (mDownloadInfo.getCurrentState() == DownloadInfo.STATE_DOWNLOADED) {
                    //立即安装
                }
                break;
            case R.id.tv_right:
                if (mDownloadInfo == null) return;
                if (mDownloadInfo.getCurrentState() == DownloadInfo.STATE_DOWNLOADING) {
                    //取消下载
                    if (mUpdateBinder == null) return;
                } else if (mDownloadInfo.getCurrentState() == DownloadInfo.STATE_DOWNLOADED) {
                    //稍后安装
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateBinder != null)
            ServiceUtil.unbindService(mServiceConnection);
        L.e("----------------onDestroy:"+mDownloadInfo);
        ControlCenter.setDownloadInfo(mDownloadInfo);
    }

    private void checkVersion() {
        showProgressDialog(getString(R.string.check_update_msg));
        //检查版本
        HttpAction.getHttpAction().getVersion(SystemUtil.getVersionCode(), new IDataListener<Version>() {
            @Override
            public void onSuccess(Version version) {
                L.e("--------------:version:" + version);
                cancelProgressDialog();
                if (Integer.valueOf(version.getNumber()) > SystemUtil.getVersionCode()) {
                    tvState.setText("可下载更新");
                    tvCheck.setText("下载更新");
                    mVersion = version;
                    mDownloadInfo = new DownloadInfo();
                    mDownloadInfo.setTitle(getString(R.string.video_service));
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
        T.show("下载已暂停");
        mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOAD_PAUSE);
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
        mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOADED);
        showCurrentView();
    }

    @Override
    public void onDownloadFail() {
        T.show("下载失败");
        mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
        showCurrentView();
    }


    @SuppressLint("SetTextI18n")
    public void showCurrentView() {
        L.e("------------------mDownloadInfo>>"+mDownloadInfo);
        if (mDownloadInfo == null) {
            tvPro.setVisibility(View.GONE);
            tvState.setText("当前已是最新版本");
            tvState.setText("检查更新");
            return;
        }
        L.e("-----------::" + mDownloadInfo.getCurrentState());
        mCurrentState = mDownloadInfo.getCurrentState();
        switch (mCurrentState) {
            case DownloadInfo.STATE_NO_DOWNLOAD:
                tvPro.setVisibility(View.GONE);
                tvCheck.setText("下载更新");
                tvState.setText("可下载更新");
                endProAnim();
                break;
            case DownloadInfo.STATE_DOWNLOADING:
                startProAnim();
                tvState.setText("正在下载");
                tvCheck.setText("取消");
                break;
            case DownloadInfo.STATE_DOWNLOAD_PAUSE:
                tvState.setText("下载已暂停");
                endProAnim();
                break;
            case DownloadInfo.STATE_DOWNLOADED:
                tvState.setText("下载完成");
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
