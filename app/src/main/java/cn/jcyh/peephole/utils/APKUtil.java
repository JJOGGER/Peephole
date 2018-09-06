package cn.jcyh.peephole.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by jogger on 2018/8/28.
 */
public class APKUtil {
    public static final String APK_PATCH_PATH_ENCRYPT = FileUtil.getAppCacheDir() + File.separator + "peephole.encrypt";//服务器下载下来的加密的差分包
    //        public static final String APK_PATH = Util.getApp().getFilesDir().getAbsolutePath()+ File.separator + "peephole.apk";
//    public static final String APK_PATH = FileUtil.getAPKPath() + File.separator + ".peephole.apk";
    public static final String APK_PATH = FileUtil.getAPKPath() + File.separator + ".peephole.apk";//合成差分包后新包存储路径
    public static final String APK_PATCH_PATH = FileUtil.getAPKPath() + File.separator + "peephole.patch";//差分包存储路径
    public static final String SYSTEM_PATCH_PATH_ENCRYPT = FileUtil.getSDCardPath() + File.separator + "update.encrypt";//加密差分包存储路径
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

    /**
     * 获取当前apk的签名
     */
    public static String getCurrentSignature() {
        String packageName = Util.getApp().getApplicationInfo().packageName;
        PackageManager packageManager = Util.getApp().getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);
        for (PackageInfo packageInfo :
                installedPackages) {
            if (packageInfo.packageName.equals(packageName)) {
                return packageInfo.signatures[0].toCharsString();
            }
        }
        return null;
    }

    /**
     * 获取路径下文件签名
     */
    public static String getSignature(String path) {
        PackageInfo packageInfo = getPackageInfo(path, getPackageParser());
        assert packageInfo != null;
        return packageInfo.signatures[0].toCharsString();
    }

    /**
     * 获取类对象
     */
    private static Object getPackageParser() {
        try {
            Class<?> packageParserClazz = Class.forName("android.content.pm.PackageParser");
            Constructor<?> declaredConstructor = packageParserClazz.getDeclaredConstructor();
            return declaredConstructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PackageInfo getPackageInfo(String path, Object packageParser) {
        Class<?>[] paramClazz = new Class[2];
        paramClazz[0] = File.class;
        paramClazz[1] = int.class;
        try {
            Method declaredMethod = packageParser.getClass().getDeclaredMethod("parsePackage", paramClazz);
            Object[] paramObject = new Object[2];
            paramObject[0] = new File(path);
            paramObject[1] = 0;
            declaredMethod.setAccessible(true);
            return (PackageInfo) declaredMethod.invoke(packageParser, paramObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
