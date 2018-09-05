package cn.jcyh.peephole.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.File;

/**
 * Created by jogger on 2018/8/28.
 */
public class APKUtil {
    public static final String APK_PATCH_PATH_ENCRYPT = FileUtil.getAppCacheDir() + File.separator + "peephole.encrypt";//服务器下载下来的加密的差分包
    //        public static final String APK_PATH = Util.getApp().getFilesDir().getAbsolutePath()+ File.separator + "peephole.apk";
//    public static final String APK_PATH = FileUtil.getAPKPath() + File.separator + ".peephole.apk";
    public static final String APK_PATH = FileUtil.getAPKPath() + File.separator + ".peephole.apk";//合成差分包后新包存储路径
    public static final String APK_PATCH_PATH = FileUtil.getAPKPath() + File.separator + "peephole.patch";//差分包存储路径
    public static final String SYSTEM_PATCH_PATH = FileUtil.getSDCardPath() + File.separator + "update.patch";//差分包存储路径

    /**
     * 获取本机apk存储路径
     */
    public static String getOldVersionPath() {
        ApplicationInfo info;
        try {
            info = Util.getApp().getPackageManager().getApplicationInfo(Util.getApp().getPackageName(), 0);
            return info.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
