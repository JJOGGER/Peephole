package cn.jcyh.peephole.service;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.receiver.UpdateApkReceiver;
import cn.jcyh.peephole.utils.APKUtil;
import cn.jcyh.peephole.utils.SPUtil;
import cn.jcyh.peephole.utils.T;

/**
 * Created by jogger on 2018/8/7.
 */
public class UpdateService extends Service {
    private String mFilePath;
    private static final int DOWNLOAD_NOTIFY_ID = 0x11;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private UpdateApkReceiver mUpdateApkReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        ControlCenter.sIsDownloadUpdate = true;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //注册广播接收者，监听下载状态
        mUpdateApkReceiver = new UpdateApkReceiver();
        registerReceiver(mUpdateApkReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Version version = intent.getParcelableExtra(Constant.VERSION);
            if (version == null) return super.onStartCommand(intent, flags, startId);
            mFilePath = version.getAddress();
            DownloadManager downloadManager;
            downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(mFilePath);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(getString(R.string.video_service));
            request.setDescription(getString(R.string.updating));
            File file = new File(APKUtil.APK_PATCH_PATH);
            if (file.exists())
                file.delete();
            request.setDestinationUri(Uri.fromFile(file));
            assert downloadManager != null;
            long id = downloadManager.enqueue(request);
            SPUtil.getInstance().put(Constant.DOWNLOAD_ID, id);
            T.show(R.string.start_download);
//            startUpdate(mFilePath, new OnUpdateListener() {
//                @Override
//                public void onProgress(ProgressData progressData) {
//
//                }
//            });
        }
//        if (intent != null) {
//            String filePath = intent.getStringExtra(Constant.FILE_PATH);
//            File file = new File(SystemUtil.APK_PATH);
//            L.e("--------filePath:" + filePath + ";" + file.exists());
//            if (!file.exists()) {
//            startUpdate(filePath);
//            } else {
//                installAPK(file);
//                stopSelf();
//            }

//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ControlCenter.sIsDownloadUpdate = false;
        SPUtil.getInstance().put(Constant.DOWNLOAD_ID, -1L);
        unregisterReceiver(mUpdateApkReceiver);
    }

    private void installAPK(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

}
