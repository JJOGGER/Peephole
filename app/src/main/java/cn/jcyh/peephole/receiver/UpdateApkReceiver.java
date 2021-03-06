package cn.jcyh.peephole.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.service.UpdateSystemService;
import cn.jcyh.peephole.utils.APKUtil;
import cn.jcyh.peephole.utils.DESUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.PatchUtil;
import cn.jcyh.peephole.utils.SPUtil;
import cn.jcyh.peephole.utils.ServiceUtil;
import cn.jcyh.peephole.utils.T;

/**
 * Created by jogger on 2018/8/24.更新apk
 */
public class UpdateApkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            long downloadAPKID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
            long id = SPUtil.getInstance().getLong(Constant.DOWNLOAD_APK_ID, -1L);
            if (downloadAPKID == id) {
                if (ServiceUtil.isServiceRunning(UpdateSystemService.class))
                    ServiceUtil.stopService(UpdateSystemService.class);
                checkDownloadStatus(context, downloadAPKID);
            } else {
                L.e("下载id跟本地存储id不一致");
                T.show("下载id跟本地存储id不一致,请尝试重新下载");
                SPUtil.getInstance().put(Constant.DOWNLOAD_APK_ID, -1L);
            }
        }
    }

    private void checkDownloadStatus(Context context, long downloadAPKID) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadAPKID);
        assert downloadManager != null;
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            L.i("下载状态：" + status);
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    APKUtil.installUpdateAPK();
//                    patchAPK(context);//合并差分包
                    break;
                case DownloadManager.STATUS_FAILED:
                    break;
                case DownloadManager.STATUS_RUNNING:
                    break;
                default:
                    break;
            }
        } else {
            L.e("遍历下载列表moveToFirst为false");
            T.show("遍历下载列表moveToFirst为false");
        }
        cursor.close();
    }

    private void patchAPK(final Context context) {
        //解密
        DESUtil.decrypt(APKUtil.APK_PATCH_PATH_ENCRYPT, APKUtil.APK_PATCH_PATH, DESUtil.KEY, new IDataListener<Boolean>() {
            @Override
            public void onSuccess(Boolean decrypt) {
                if (decrypt) {
                    //解密成功
                    String oldVersionPath = APKUtil.getOldVersionPath();
                    //合成差分包
                    L.e("-------合成差分包");
                    PatchUtil.patch(oldVersionPath, APKUtil.APK_PATH, APKUtil.APK_PATCH_PATH);
                    L.e("-------合成差分包end");
                    //签名校验
                    String currentSignature = APKUtil.getCurrentSignature();
                    String signature = APKUtil.getSignature(APKUtil.APK_PATH);
                    if (TextUtils.isEmpty(signature) || signature.equals(currentSignature)) {
                        //启动安装
//                        Intent intent = new Intent();
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.setAction(android.content.Intent.ACTION_VIEW);
//                        intent.setDataAndType(Uri.fromFile(new File(APKUtil.APK_PATH)),
//                                "application/vnd.android.package-archive");
//                        context.startActivity(intent);
                        DoorbellSystemAction systemAction = new DoorbellSystemAction(DoorbellSystemAction.TYPE_DOORBELL_INSTALL_APK);
                        EventBus.getDefault().post(systemAction);
//                        installAPK();
                    } else {
                        L.e("签名校验失败");
                        T.show(R.string.download_file_des_failure);
                    }
                } else {
                    T.show(R.string.download_file_des_failure);
                }
            }

            @Override
            public void onFailure(int errorCode, String desc) {
                T.show(R.string.download_file_des_failure);
            }
        });
    }


}
