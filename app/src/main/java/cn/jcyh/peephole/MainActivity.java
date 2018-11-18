package cn.jcyh.peephole;

import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.nimlib.sdk.misc.MiscService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.jcyh.eaglelock.api.MyLockAPI;
import cn.jcyh.eaglelock.api.MyLockCallback;
import cn.jcyh.peephole.adapter.MainPageAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.CommandJson;
import cn.jcyh.peephole.entity.DownloadInfo;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.event.NIMMessageAction;
import cn.jcyh.peephole.event.NetworkAction;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.service.MainService;
import cn.jcyh.peephole.service.UpdateSoftService;
import cn.jcyh.peephole.ui.dialog.ADPopupDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.HintDialogFragmemt;
import cn.jcyh.peephole.utils.APKUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.SPUtil;
import cn.jcyh.peephole.utils.SystemUtil;

//按门铃，发消息--》app收到消息--》发起视频通话
public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    @BindView(R.id.vp_main)
    ViewPager vpMain;
    private DialogHelper mUpdateDialog;
    private Handler mHandler;
    private DialogHelper mAdPopupDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    protected void init() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        mHandler = new Handler();
        startService(new Intent(this, MainService.class));
        vpMain.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
        vpMain.setOffscreenPageLimit(2);
        vpMain.addOnPageChangeListener(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        clearCache();
        boolean siye = ControlCenter.getSN().startsWith(Constant.SIYE_SN);
        if (siye) {
            siyeInit();
        }
    }///storage/sdcard0

    /**
     * 四叶草
     */
    private void siyeInit() {
        MyLockAPI.init(this, new MyLockCallback(this));
        MyLockAPI lockAPI = MyLockAPI.getLockAPI();
        lockAPI.startBleService(this);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        lockAPI.startBTDeviceScan();
    }

    /**
     * 清理缓存
     */
    private void clearCache() {
        List<DirCacheFileType> fileTypes = new ArrayList<>();
        fileTypes.add(DirCacheFileType.LOG);
        fileTypes.add(DirCacheFileType.THUMB);
        fileTypes.add(DirCacheFileType.IMAGE);
        fileTypes.add(DirCacheFileType.AUDIO);
        NIMClient.getService(MiscService.class).clearDirCache(fileTypes, 0, 0);
    }


    @Override
    public void onBackPressed() {
        if (vpMain.getCurrentItem() == 1) {
            vpMain.setCurrentItem(0);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNIMMessageAction(NIMMessageAction messageAction) {
        if (CommandJson.ServerCommand.DOORBELL_POPUP_UPDATE.equals(messageAction.getType())) {
            CommandJson commandJson = messageAction.getParcelableExtra(Constant.COMMAND);
            if (TextUtils.isEmpty(commandJson.getFlag())) return;
            if (mAdPopupDialog == null) {
                ADPopupDialog adPopupDialog = new ADPopupDialog();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.URL, commandJson.getFlag());
                adPopupDialog.setArguments(bundle);
                mAdPopupDialog = new DialogHelper(this, adPopupDialog);
            } else {
                ((ADPopupDialog) mAdPopupDialog.getDialogFragment()).updateAD(commandJson.getFlag());
            }
            mAdPopupDialog.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUpdateDialog != null)
            mUpdateDialog.dismiss();
        if (mAdPopupDialog != null)
            mAdPopupDialog.dismiss();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkAction(NetworkAction networkAction) {
        if (NetworkAction.TYPE_NETWORK_CONNECTED.equals(networkAction.getType())) {
            checkUpdate();
        }
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(SPUtil.getInstance().getLong(Constant.DOWNLOAD_APK_ID));
        assert downloadManager != null;
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToNext()) {
            int state = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
//            if (DownloadManager.STATUS_SUCCESSFUL == state) {
//                //已经下载成功，直接安装
//                APKUtil.installUpdateAPK();
//                return;
//            } else
            if (DownloadManager.STATUS_FAILED != state && DownloadManager.STATUS_SUCCESSFUL != state) {
                L.e("-------------当前状态:" + state);
                cursor.close();
                return;
            }
        }
        cursor.close();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        String solutions = widthPixels + "x" + heightPixels;
        HttpAction.getHttpAction().updateSoft(SystemUtil.getSystemVersion(), solutions, new IDataListener<Version>() {
            @Override
            public void onSuccess(final Version version) {
                try {
                    if (Integer.valueOf(version.getNumber()) > SystemUtil.getVersionCode()) {
                        update(version);
                    }
                } catch (Exception ignore) {
                }
            }

            @Override
            public void onFailure(int errorCode, String desc) {
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
                        Intent intent = new Intent(MainActivity.this, UpdateSoftService.class);
                        intent.putExtra(Constant.DOWNLOAD_INFO, downloadInfo);
                        startService(intent);
                    }
                    mUpdateDialog.dismiss();
                }
            });
            mUpdateDialog = new DialogHelper(this, hintDialogFragmemt);
        }
        mUpdateDialog.commit();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mUpdateDialog != null)
                    mUpdateDialog.dismiss();
            }
        }, 5000);

    }
}
