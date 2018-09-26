package cn.jcyh.peephole.utils;

import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import cn.jcyh.peephole.http.IDataListener;

/**
 * Created by jogger on 2018/8/4.
 */
public class DESUtil {
    private byte[] desKey;
    public static final String KEY = "EagleKin";


    //解密数据
    public static String decrypt(String message, String key) throws Exception {

        byte[] bytesrc = convertHexString(message);
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));

        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] retByte = cipher.doFinal(bytesrc);
        return new String(retByte);
    }

    //解密文件数据

    /**
     * @param enfilePath 要解密的文件
     * @param deFilePath 解密后的文件
     * @param key        密钥
     */
    public static void decrypt(final String enfilePath, final String deFilePath, final String key, final IDataListener<Boolean> listener) {
        final Handler handler = new Handler();
        L.e("-----------解密文件数据");
        final File file = new File(enfilePath);
        if (!file.exists()) {
            listener.onFailure(-1, "");
            L.e("--------文件解密失败");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    L.e("------------>file:" + enfilePath);
                    InputStream is = new FileInputStream(enfilePath);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    OutputStream os = new FileOutputStream(deFilePath);
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    Cipher cipher = Cipher.getInstance("DES/CBC/NoPadding");
                    DESKeySpec desKeySpec;
                    desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
                    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
                    SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
                    IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
                    CipherOutputStream cos = new CipherOutputStream(bos, cipher);
                    byte[] buffer = new byte[1024];
                    int r;
                    while ((r = bis.read(buffer)) >= 0) {
                        cos.write(buffer, 0, r);
                    }
                    L.e("-----------解密");
                    //删除原文件
                    file.delete();
                    cos.close();
                    os.close();
                    is.close();
//                    bis.close();
//                    bos.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onSuccess(true);
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    L.e("-----------e:" + e.getMessage());
                    File file = new File(deFilePath);
                    if (file.exists())
                        file.delete();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onFailure(-1, "");
                            }
                        }
                    });
                }
            }
        }).start();
    }

    public static byte[] encrypt(String message, String key)
            throws Exception {
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        return cipher.doFinal(message.getBytes("UTF-8"));
    }

    public static byte[] convertHexString(String ss) {
        byte digest[] = new byte[ss.length() / 2];
        for (int i = 0; i < digest.length; i++) {
            String byteString = ss.substring(2 * i, 2 * i + 2);
            int byteValue = Integer.parseInt(byteString, 16);
            digest[i] = (byte) byteValue;
        }

        return digest;
    }


    public static void main(String[] args) throws Exception {
        String key = "12345678";
        String value = "test1234 ";
        String jiami = java.net.URLEncoder.encode(value, "utf-8").toLowerCase();

        System.out.println("加密数据:" + jiami);
        String a = toHexString(encrypt(jiami, key)).toUpperCase();


        System.out.println("加密后的数据为:" + a);
        String b = java.net.URLDecoder.decode(decrypt(a, key), "utf-8");
        System.out.println("解密后的数据:" + b);

    }


    public static String toHexString(byte b[]) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String plainText = Integer.toHexString(0xff & b[i]);
            if (plainText.length() < 2)
                plainText = "0" + plainText;
            hexString.append(plainText);
        }

        return hexString.toString();
    }
}
