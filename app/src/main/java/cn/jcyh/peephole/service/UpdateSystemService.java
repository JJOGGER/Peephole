package cn.jcyh.peephole.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import cn.jcyh.peephole.utils.APKUtil;
import cn.jcyh.peephole.utils.SPUtil;
import cn.jcyh.peephole.utils.T;

/**
 * Created by jogger on 2018/8/7.
 */
public class UpdateSystemService extends Service {
    private ScheduledExecutorService mExecutorService;
    private int[] mBytesAndStatus;
    private DownloadManager.Query mQuery;
    private OnUpdateListener mListener;
    private DownloadInfo mDownloadInfo;

    public interface OnUpdateListener {
        void onDownloadPause();

        void onProgress(int pro);

        void onDownloadCompleted();

        void onDownloadFail();

        void onDownloadRunning();

        void onEncryptFail();

        void onEncryptSuccess();

        void onStartDownload();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ControlCenter.sIsDownloadUpdate = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mDownloadInfo = ControlCenter.getDownloadInfo();
        if (mDownloadInfo != null) {
            //本地存在下载记录
            getBytesAndStatus(mDownloadInfo.getDownloadID());
            switch (mBytesAndStatus[2]) {
                case 0:
                    mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
                    break;
                case DownloadManager.STATUS_RUNNING:
                    mExecutorService = Executors.newSingleThreadScheduledExecutor();
                    ProRunnable proRunnable = new ProRunnable();
                    mExecutorService.scheduleAtFixedRate(proRunnable, 0, 2, TimeUnit.SECONDS);
                    mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOADING);
                    break;
                case DownloadManager.STATUS_FAILED:
                    mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOADED);
                    break;
                case DownloadManager.STATUS_PAUSED:
                    mExecutorService = Executors.newSingleThreadScheduledExecutor();
                    proRunnable = new ProRunnable();
                    mExecutorService.scheduleAtFixedRate(proRunnable, 0, 2, TimeUnit.SECONDS);
                    mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOAD_PAUSE);
                    break;
            }
        }
        return new UpdateBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ControlCenter.sIsDownloadUpdate = false;
        if (mDownloadInfo != null)
            SPUtil.getInstance().put(mDownloadInfo.getType(), -1);
        if (mExecutorService != null && !mExecutorService.isTerminated())
            mExecutorService.isShutdown();
        ControlCenter.setDownloadInfo(mDownloadInfo);
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
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            assert downloadManager != null;
            cursor = downloadManager.query(mQuery);
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

    private void start(DownloadInfo downloadInfo) {
        mDownloadInfo = downloadInfo;
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
        SPUtil.getInstance().put(downloadInfo.getType(), id);
        mDownloadInfo.setDownloadID(id);
        T.show(R.string.start_download);
        mExecutorService = Executors.newSingleThreadScheduledExecutor();
        ProRunnable proRunnable = new ProRunnable();
        mExecutorService.scheduleAtFixedRate(proRunnable, 0, 2, TimeUnit.SECONDS);
        mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOADING);
        if (mListener != null)
            mListener.onStartDownload();
    }

    public class UpdateBinder extends Binder {
        public void setUpdateListener(OnUpdateListener listener) {
            mListener = listener;
        }

        public DownloadInfo getLocalDownloadInfo() {
            return mDownloadInfo;
        }

        public int getCurrentState() {
            return mDownloadInfo == null ? DownloadInfo.STATE_NO_DOWNLOAD : mDownloadInfo.getCurrentState();
        }

        public void startDownload(DownloadInfo downloadInfo) {
            start(downloadInfo);
        }

        public void cancelDownload() {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            assert downloadManager != null;
            downloadManager.remove(mDownloadInfo.getDownloadID());
            mBytesAndStatus = null;
            mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
            if (mExecutorService != null && !mExecutorService.isShutdown()) {
                mExecutorService.shutdownNow();
            }
        }

        public void installSystem() {
            if (getLocalDownloadInfo().getCurrentState() == DownloadInfo.STATE_DOWNLOADED) {
                Intent intent = new Intent("android.intent.action.ACTION_OTA_UPGRADE");
                intent.putExtra(Constant.COMMAND_PATH, APKUtil.SYSTEM_PATCH_PATH);
                sendBroadcast(intent);
                //进行解密
//                DESUtil.decrypt(APKUtil.SYSTEM_PATCH_PATH_ENCRYPT, APKUtil.SYSTEM_PATCH_PATH, DESUtil.KEY, new IDataListener<Boolean>() {
//                    @Override
//                    public void onSuccess(Boolean decrypt) {
//                        if (decrypt) {
//                            //解密成功
//                            L.e("----------------解密成功。。。。。。。。");
//                            Intent intent = new Intent("android.intent.action.ACTION_OTA_UPGRADE");
//                            intent.putExtra(Constant.COMMAND_PATH, APKUtil.SYSTEM_PATCH_PATH);
//                            sendBroadcast(intent);
//                            mListener.onEncryptSuccess();
//                        } else {
//                            T.show(R.string.download_file_des_failure);
//                            mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
//                            mListener.onEncryptFail();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(int errorCode, String desc) {
//                        T.show(R.string.download_file_des_failure);
//                        mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
//                        mListener.onEncryptFail();
//                    }
//                });
            }
        }
    }

    private class ProRunnable implements Runnable {
        private Handler mHandler;

        ProRunnable() {
            mHandler = new Handler();
        }

        @Override
        public void run() {
            final int[] bytesAndStatus = getBytesAndStatus(mDownloadInfo.getDownloadID());
            if (bytesAndStatus[1] == bytesAndStatus[0]) {
                if (!mExecutorService.isShutdown()) {
                    mExecutorService.shutdownNow();
                }

            }
            if (mListener == null) return;
            final int pro = (int) (bytesAndStatus[0] * 100 / (float) bytesAndStatus[1]);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch (bytesAndStatus[2]) {
                        case 0:
                            if (mDownloadInfo.getCurrentState() != DownloadInfo.STATE_NO_DOWNLOAD) {
                                mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
                                mListener.onDownloadFail();
                            }
                            break;
                        case DownloadManager.STATUS_SUCCESSFUL:
                            if (mDownloadInfo.getCurrentState() != DownloadInfo.STATE_DOWNLOADED) {
                                mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOADED);
                                mListener.onDownloadCompleted();
                            }
                            break;
                        case DownloadManager.STATUS_PAUSED:
                            if (mDownloadInfo.getCurrentState() != DownloadInfo.STATE_DOWNLOAD_PAUSE) {
                                T.show("下载已暂停");
                                mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOAD_PAUSE);
                                mListener.onDownloadPause();
                            }
                            break;
                        case DownloadManager.STATUS_FAILED:
                            if (mDownloadInfo.getCurrentState() != DownloadInfo.STATE_NO_DOWNLOAD) {
                                T.show("下载失败");
                                mDownloadInfo.setCurrentState(DownloadInfo.STATE_NO_DOWNLOAD);
                                mListener.onDownloadFail();
                            }
                            break;
                        case DownloadManager.STATUS_RUNNING:
                            if (mDownloadInfo.getCurrentState() != DownloadInfo.STATE_DOWNLOADING) {
                                mDownloadInfo.setCurrentState(DownloadInfo.STATE_DOWNLOADING);
                                mListener.onDownloadRunning();
                            }
                            break;
                    }
                    if (bytesAndStatus[2] != 0)
                        mListener.onProgress(pro);
                }
            });
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mExecutorService != null && !mExecutorService.isShutdown()) {
            mExecutorService.shutdownNow();
        }
        return super.onUnbind(intent);
    }
}
