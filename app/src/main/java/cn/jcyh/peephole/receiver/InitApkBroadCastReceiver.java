package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import cn.jcyh.peephole.utils.APKUtil;
import cn.jcyh.peephole.utils.SystemUtil;

/**
 * Created by jogger on 2018/8/8.
 */
public class InitApkBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
//        }
//        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
//        }
        //删除上次更新存储在本地的apk
        if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
            if (SystemUtil.getPackageName().equals(intent.getDataString())) {
                File file = new File(APKUtil.APK_PATH);
                if (file.exists())
                    file.delete();
            }
        }
    }

}
