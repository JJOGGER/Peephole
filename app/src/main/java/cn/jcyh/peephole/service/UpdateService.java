package cn.jcyh.peephole.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
public class UpdateService extends Service {
    private UpdateApkReceiver mUpdateApkReceiver;
    private ScheduledExecutorService mExecutorService;
    private long mId;
    private int[] mBytesAndStatus;
    private DownloadManager.Query mQuery;
    private DownloadManager mDownloadManager;
    private OnUpdateListener mListener;
    private DownloadInfo mDownloadInfo;

    public interface OnUpdateListener {
        void onDownloadPause();

        void onProgress(int pro);

        void onDownloadCompleted();

        void onDownloadFail();
    }

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
            mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(mDownloadInfo.getUrl());

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(mDownloadInfo.getTitle());
            request.setDescription(mDownloadInfo.getDesc());
            File file = new File(mDownloadInfo.getSaveFilePath());
            if (file.exists())
                file.delete();
            request.setDestinationUri(Uri.fromFile(file));
            assert mDownloadManager != null;
            mId = mDownloadManager.enqueue(request);
            SPUtil.getInstance().put(mDownloadInfo.getType(), mId);
            T.show(R.string.start_download);
//            startUpdate(mFilePath, new OnUpdateListener() {
//                @Override
//                public void onProgress(ProgressData progressData) {
//
//                }
//            });
            mExecutorService = Executors.newSingleThreadScheduledExecutor();
            ProRunnable proRunnable = new ProRunnable();
            mExecutorService.scheduleAtFixedRate(proRunnable, 0, 2, TimeUnit.SECONDS);
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
        L.e("---------------bind");
        return new UpdateBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.e("--------------onDestroy"+mDownloadInfo);
        ControlCenter.sIsDownloadUpdate = false;
        if (mDownloadInfo != null)
            SPUtil.getInstance().put(mDownloadInfo.getType(), -1);
        unregisterReceiver(mUpdateApkReceiver);
        if (mExecutorService != null && !mExecutorService.isTerminated())
            mExecutorService.isShutdown();
        ControlCenter.setDownloadInfo(mDownloadInfo);
    }

    private void installAPK(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }


    /**
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
     */
    private int[] getBytesAndStatus(long downloadId) {
        if (mBytesAndStatus == null)
            mBytesAndStatus = new int[]{
                    -1, -1, 0
            };
        if (mQuery == null)
            mQuery = new DownloadManager.Query();
        mQuery.setFilterById(downloadId);
        Cursor cursor = null;
        try {
            cursor = mDownloadManager.query(mQuery);
            if (cursor != null && cursor.moveToFirst()) {
                //已经下载文件大小
                mBytesAndStatus[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                //下载文件的总大小
                mBytesAndStatus[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                //下载状态
                mBytesAndStatus[2] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mBytesAndStatus;
    }

    public class UpdateBinder extends Binder {
        public void setUpdateListener(OnUpdateListener listener) {
            mListener = listener;
        }

        public void cancelDownload() {
            if (mDownloadManager == null) return;
            mDownloadManager.remove(mId);
            mBytesAndStatus = null;
            if (mExecutorService != null && !mExecutorService.isShutdown()) {
                mExecutorService.shutdownNow();
            }
            L.e("----------取消下载");
            stopSelf();
        }
    }

    private class ProRunnable implements Runnable {
        private Handler mHandler;

        ProRunnable() {
            mHandler = new Handler();
        }

        @Override
        public void run() {
            final int[] bytesAndStatus = getBytesAndStatus(mId);
            if (bytesAndStatus[1] == bytesAndStatus[0]) {
                if (!mExecutorService.isShutdown())
                    mExecutorService.shutdownNow();
            }
            L.e("--------------状态：" + bytesAndStatus[2]);
            final int pro = (int) (bytesAndStatus[0] * 100 / (float) bytesAndStatus[1]);
            if (mListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        switch (bytesAndStatus[2]) {
                            case DownloadManager.STATUS_SUCCESSFUL:
                                mListener.onDownloadCompleted();
                                break;
                            case DownloadManager.STATUS_PAUSED:
                                mListener.onDownloadPause();
                                break;
                            case DownloadManager.STATUS_FAILED:
                                mListener.onDownloadFail();
                                break;
                        }
                        mListener.onProgress(pro);
                    }
                });
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.e("---------------onUnbind");
        return super.onUnbind(intent);
    }
}
