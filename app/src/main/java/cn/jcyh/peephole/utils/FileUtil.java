package cn.jcyh.peephole.utils;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

/**
 * Created by Jogger on 2018/2/4.
 */

public class FileUtil {
    private static FileUtil sUtils;
    private SimpleDateFormat mSimpleDateFormat;

    private FileUtil() {
        mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
    public String getDoorbellImgPath() {
        return Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DCIM).getAbsolutePath() + File.separator + "Camera";
    }

    /**
     * 猫眼视频路径
     */
    public String getDoorbellVideoPath() {
        return Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_MOVIES).getAbsolutePath();
    }


    /**
     * 获取sd卡路径
     */
    public String getSDCardPath() {
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

    /**
     * 移动文件
     *
     * @param srcFileName 源文件完整路径
     * @param destDirName 目的目录完整路径
     * @return 文件移动成功返回true，否则返回false
     */
    public boolean moveFile(String srcFileName, String destDirName) {
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
        return result;
    }

    /**
     * 移动目录
     *
     * @param srcDirName  源目录完整路径
     * @param destDirName 目的目录完整路径
     * @return 目录移动成功返回true，否则返回false
     */
    public boolean moveDirectory(String srcDirName, String destDirName) {

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
    public boolean copyFile(String srcFileName, String destFileName) {
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
        BufferedOutputStream bos = null;
        try {
            in = new FileInputStream(filePath);
            baos = new ByteArrayOutputStream();
            bis = new BufferedInputStream(in);
            bos = new BufferedOutputStream(baos);
            int ret;
            byte[] buf = new byte[1024];
            while ((ret = bis.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, ret);
            }
            result = bos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (bis != null)
                    bis.close();
                if (baos != null)
                    baos.close();
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 写入文件。
     *
     * @return content
     */
    public static boolean writeFile(File file, String json) {
        boolean result = false;
        OutputStream out = null;
        ByteArrayInputStream bais = null;
        BufferedOutputStream bos = null;
        try {
            out = new FileOutputStream(file);
            bos = new BufferedOutputStream(out);
            bais = new ByteArrayInputStream(json.getBytes());
            int ret;
            byte[] buf = new byte[1024];
            while ((ret = bais.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, ret);
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
                if (bais != null)
                    bais.close();
                if (bos != null)
                    bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
