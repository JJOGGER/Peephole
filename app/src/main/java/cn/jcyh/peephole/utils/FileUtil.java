package cn.jcyh.peephole.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.mediatek.storage.StorageManagerEx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Jogger on 2018/2/4.
 */

public class FileUtil {
    private static FileUtil sUtils;
    public static final String DOORBELL_DATA_PATH = "/protect_s/prod_info";

    private FileUtil() {
    }

    public static FileUtil getInstance() {
        if (sUtils == null) {
            synchronized (FileUtil.class) {
                if (sUtils == null) {
                    sUtils = new FileUtil();
                }
            }
        }
        return sUtils;
    }

    /**
     * 猫眼图片路径
     */
    public static String getDoorbellImgPath() {
//        String path = Environment.getExternalStoragePublicDirectory(Environment
//                .DIRECTORY_DCIM).getAbsolutePath() + File.separator + "Camera";
        String path = StorageManagerEx.getDefaultPath() + File.separator + "DCIM" + File.separator + "Camera";
        File file = new File(path + File.separator + "thumbnail");
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (mkdirs) return path;
        }
        return path;
    }

    public static String getDoorbellImgThumbnailPath() {
        String doorbellDataPath = getDoorbellImgPath();
        return doorbellDataPath + File.separator + "thumbnail";
    }

    /**
     * 猫眼图片路径
     */
    public static String getDoorbellVideoPath() {
        String path = StorageManagerEx.getDefaultPath() + File.separator + "DCIM" + File.separator + "Camera";
        File file = new File(path);
        if (!file.exists()) {
            boolean mkdir = file.mkdir();
            if (mkdir) return path;
        }
        return path;
    }

    public static String getDoorbellMediaThumbnailPath() {
        return getDoorbellVideoPath() + File.separator + "thumbnail";
    }


    public static String getDoorbellDataPath() {
        return DOORBELL_DATA_PATH + File.separator + "doorbell_config.config";
    }

    /**
     * @param removable true返回外部存储，false返回内部存储
     *                  不存在则列表为空
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static List<String> getSDCardPaths(Context context, final boolean removable) {
        List<String> paths = new ArrayList<>();
        String methodName = "getVolumeList";
        String pathMethod = "getPath";
        String removableMethod = "isRemovable";
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = StorageManager.class.getMethod(methodName);
            Method getPath = storageVolumeClazz.getMethod(pathMethod);
            Method isRemovable = storageVolumeClazz.getMethod(removableMethod);
            Object result = getVolumeList.invoke(sm);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean res = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable == res) {
                    paths.add(path);
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return paths;
    }

    public static String getAPKPath() {
        String fileDir = getSDCardPath() + Util.getApp().getPackageName();
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return fileDir;
    }

    public static String getLogPath() {
        String logPath = getAPKPath() + File.separator + "log";
        File file = new File(logPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return logPath;
    }

    /**
     * 获取sd卡路径
     */
    public static String getSDCardPath() {
        File sdcardDir = null;
        // 判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED) || Environment.getExternalStorageState().equals
                (Environment.MEDIA_SHARED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        if (sdcardDir != null) {
            return sdcardDir.toString() + File.separator;
        } else {
            return null;
        }
    }

    public static String getAppCacheDir() {
        String storageRootPath = null;
        try {
            // SD卡应用扩展存储区(APP卸载后，该目录下被清除，用户也可以在设置界面中手动清除)，请根据APP对数据缓存的重要性及生命周期来决定是否采用此缓存目录.
            // 该存储区在API 19以上不需要写权限，即可配置 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18"/>
            if (Util.getApp().getExternalCacheDir() != null) {
                storageRootPath = Util.getApp().getExternalCacheDir().getCanonicalPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
            L.e("APP缓存路径获取失败" + e.getMessage());
        }
        if (TextUtils.isEmpty(storageRootPath)) {
            // SD卡应用公共存储区(APP卸载后，该目录不会被清除，下载安装APP后，缓存数据依然可以被加载。SDK默认使用此目录)，该存储区域需要写权限!
            storageRootPath = Environment.getExternalStorageDirectory() + "/" + Util.getApp().getPackageName();
        }

        return storageRootPath;
    }

    public static void saveBitmap2File(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            try {
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 移动文件
     *
     * @param srcFileName 源文件完整路径
     * @param destDirName 目的目录完整路径
     * @return 文件移动成功返回true，否则返回false
     */
    public static void moveFile(String srcFileName, String destDirName) {
        boolean result = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            File srcFile = new File(srcFileName);
            if (!srcFile.exists() || !srcFile.isFile()) {
                result = false;
            } else {
                File destFile = new File(destDirName);
//                if (!destDir.exists())
//                    destDir.mkdirs();

                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                destFile.createNewFile();
                fis = new FileInputStream(srcFile);
                fos = new FileOutputStream(destDirName);
                byte[] buf = new byte[1024];
                int ret;
                while ((ret = fis.read(buf, 0, buf.length)) != -1) {
                    fos.write(buf, 0, ret);
                }
                result = true;
                srcFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 移动目录
     *
     * @param srcDirName  源目录完整路径
     * @param destDirName 目的目录完整路径
     * @return 目录移动成功返回true，否则返回false
     */
    public static boolean moveDirectory(String srcDirName, String destDirName) {

        File srcDir = new File(srcDirName);
        if (!srcDir.exists() || !srcDir.isDirectory())
            return false;

        File destDir = new File(destDirName);
        if (!destDir.exists())
            destDir.mkdirs();

        /**
         * 如果是文件则移动，否则递归移动文件夹。删除最终的空源文件夹
         * 注意移动文件夹时保持文件夹的树状结构
         */
        File[] sourceFiles = srcDir.listFiles();
        for (File sourceFile : sourceFiles) {
            if (sourceFile.isFile())
                moveFile(sourceFile.getAbsolutePath(), destDir.getAbsolutePath());
            else if (sourceFile.isDirectory())
                moveDirectory(sourceFile.getAbsolutePath(),
                        destDir.getAbsolutePath() + File.separator + sourceFile.getName());
            else
                ;
        }
        return srcDir.delete();
    }

    /**
     * 复制文件
     *
     * @param srcFileName  源文件完整路径
     * @param destFileName 目的完整路径
     * @return 文件复制成功返回true，否则返回false
     */
    public static boolean copyFile(String srcFileName, String destFileName) {
        boolean result = false;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            File srcFile = new File(srcFileName);
            if (!srcFile.exists() || !srcFile.isFile()) {
                result = false;
            } else {
                File destFile = new File(destFileName);
                if (!destFile.exists())
                    destFile.createNewFile();
                fis = new FileInputStream(srcFile);
                fos = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int ret;
                while ((ret = fis.read(buf, 0, buf.length)) != -1) {
                    fos.write(buf, 0, ret);
                }
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 读取文件。
     *
     * @return content
     */
    public static String readFile(String filePath) {
        String result = "";
        InputStream in = null;
        ByteArrayOutputStream baos = null;
        BufferedInputStream bis = null;
//        BufferedOutputStream bos = null;
        File file = new File(filePath);
        try {
            in = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            bis = new BufferedInputStream(in);
//            bos = new BufferedOutputStream(baos);
            int ret;
            byte[] buf = new byte[1024];
            while ((ret = bis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, ret);
                baos.flush();
            }
            result = baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
//                if (bos != null)
//                    bos.close();
                if (baos != null)
                    baos.close();
                if (bis != null)
                    bis.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 写入文件。
     */
    public static void writeFile(File file, String json) {
        OutputStream out = null;
        ByteArrayInputStream bais = null;
        BufferedOutputStream bos = null;
        if (file.exists())
            file.delete();
        try {
            out = new FileOutputStream(file);
            bos = new BufferedOutputStream(out);
            bais = new ByteArrayInputStream(json.getBytes());
            int ret;
            byte[] buf = new byte[1024];
            while ((ret = bais.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, ret);
            }
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null)
                    bos.close();
                if (out != null)
                    out.close();
                if (bais != null)
                    bais.close();
            } catch (IOException e) {
                e.printStackTrace();
                L.e("----------e2:" + e.getMessage());
            }
        }
    }
}
