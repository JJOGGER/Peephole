package cn.jcyh.peephole.service;

import android.app.DownloadManager;
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
import cn.jcyh.peephole.entity.DownloadInfo;
import cn.jcyh.peephole.receiver.UpdateApkReceiver;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.SPUtil;
import cn.jcyh.peephole.utils.T;

/**
 * Created by jogger on 2018/8/7.
 */
public class UpdateSoftService extends Service {
    private UpdateApkReceiver mUpdateApkReceiver;
    private DownloadInfo mDownloadInfo;


    @Override
    public void onCreate() {
        super.onCreate();
        ControlCenter.sIsDownloadUpdate = true;
        //注册广播接收者，监听下载状态
        mUpdateApkReceiver = new UpdateApkReceiver();
        registerReceiver(mUpdateApkReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.e("---------------intent:" + intent);
        if (intent != null) {
            mDownloadInfo = intent.getParcelableExtra(Constant.DOWNLOAD_INFO);
            if (mDownloadInfo == null) return super.onStartCommand(intent, flags, startId);
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(mDownloadInfo.getUrl());
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(mDownloadInfo.getTitle());
            request.setDescription(mDownloadInfo.getDesc());
            File file = new File(mDownloadInfo.getSaveFilePath());
            if (file.exists())
                file.delete();
            request.setDestinationUri(Uri.fromFile(file));
            assert downloadManager != null;
            long id = downloadManager.enqueue(request);
            SPUtil.getInstance().put(mDownloadInfo.getType(), id);
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
        return START_NOT_STICKY;
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
        if (mDownloadInfo != null)
            SPUtil.getInstance().put(mDownloadInfo.getType(), -1);
        unregisterReceiver(mUpdateApkReceiver);
    }

    private void installAPK(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

}
