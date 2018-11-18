package cn.jcyh.peephole.utils;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.http.IDataListener;

/**
 * Created by jogger on 2018/8/28.
 */
public class APKUtil {
    public static final String APK_PATCH_PATH_ENCRYPT = FileUtil.getAppCacheDir() + File.separator + "peephole.encrypt";//服务器下载下来的加密的差分包
    //        public static final String APK_PATH = Util.getApp().getFilesDir().getAbsolutePath()+ File.separator + "peephole.apk";
//    public static final String APK_PATH = FileUtil.getAPKPath() + File.separator + ".peephole.apk";
    public static final String APK_PATH = FileUtil.getAPKPath() + File.separator + ".peephole.apk";//合成差分包后新包存储路径
    public static final String APK_PATCH_PATH = FileUtil.getAPKPath() + File.separator + ".peephole.patch";//差分包存储路径
    public static final String SYSTEM_PATCH_PATH = FileUtil.getAPKPath() + File.separator + "update.zip";//差分包存储路径
//    public static final String SYSTEM_PATCH_PATH = "/storage/sdcard1/Android/data" + File.separator + Util.getApp().getPackageName() + File.separator + "update.zip";//差分包存储路径
//    public static final String SYSTEM_PATCH_PATH="/storage/sdcard1/Download/update.zip"

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
        try {
            // 1.反射实例化PackageParser对象
            Object packageParser = getPackageParser();
            // 2.反射获取parsePackage方法
            Object packageObject = getPackageInfo(path, packageParser);
            // 3.调用collectCertificates方法
            if (packageObject == null || packageParser == null) {
                PackageParser packagePar = new PackageParser();
                PackageParser.Package packageser = packagePar.parsePackage(new File(path), 0);
                L.e("------------->>sign:" + packageser.mSignatures[0].toCharsString());
                return packageser.mSignatures[0].toCharsString();
            }
            Method collectCertificatesMethod = packageParser.getClass().
                    getDeclaredMethod("collectCertificates", packageObject.getClass(), int.class);
            collectCertificatesMethod.invoke(packageParser, packageObject, 0);
            // 4.获取mSignatures属性
            Field signaturesField = packageObject.getClass().getDeclaredField("mSignatures");
            signaturesField.setAccessible(true);
            Signature[] mSignatures = (Signature[]) signaturesField.get(packageObject);
            return mSignatures[0].toCharsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    private static Object getPackageInfo(String path, Object packageParser) {
        Class<?>[] paramClazz = new Class[2];
        paramClazz[0] = File.class;
        paramClazz[1] = int.class;
        try {
            Method declaredMethod = packageParser.getClass().getDeclaredMethod("parsePackage", paramClazz);
            Object[] paramObject = new Object[2];
            paramObject[0] = new File(path);
            paramObject[1] = 0;
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(packageParser, paramObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void installUpdateAPK(){
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
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(APKUtil.APK_PATH)),
                                "application/vnd.android.package-archive");
                        Util.getApp().startActivity(intent);
//                        DoorbellSystemAction systemAction = new DoorbellSystemAction(DoorbellSystemAction.TYPE_DOORBELL_INSTALL_APK);
//                        EventBus.getDefault().post(systemAction);
//                        installAPK();
                    } else {
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
