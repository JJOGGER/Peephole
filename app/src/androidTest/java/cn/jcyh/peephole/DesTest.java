package cn.jcyh.peephole;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.L;

/**
 * Created by jogger on 2018/8/4.
 */
public class DesTest {
    @Test
    public void test1() {
        // Context of the app under test.
        String key = "EagleKin";
        File file = new File(FileUtil.getSDCardPath() + "test.apk");
        try {
            InputStream is = new FileInputStream(file);
            OutputStream os = new FileOutputStream(FileUtil.getSDCardPath() + "dest.apk");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = null;
            desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
            cipher.init(Cipher.DECRYPT_MODE, secretKey,iv);
            CipherOutputStream cos = new CipherOutputStream(os, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = is.read(buffer)) >= 0) {
                cos.write(buffer, 0, r);
            }
            L.e("-----------解密");
            cos.close();
            os.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
//        String value = "E5C51B313F590A5EBD1C85D0DCC9C766CA7300AC629E77841D61D16B921FD59B";
//        String jiami = null;
//        try {
//            jiami = java.net.URLEncoder.encode(value, "utf-8").toLowerCase();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("-----------加密数据:" + jiami);
//        String a = null;
//        try {
//            a = DESUtil.toHexString(DESUtil.encrypt(jiami, key)).toUpperCase();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


//        System.out.println("--------加密后的数据为:" + value);
//        String b = null;
//        try {
//            b = java.net.URLDecoder.decode(DESUtil.decrypt(value, key), "utf-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println("----------解密后的数据:" + b);
    }

    private Key key;

    /**
     * 根据参数生成KEY
     */
    public void getKey(String strKey) {
        try {
            KeyGenerator _generator = KeyGenerator.getInstance("DES");
            _generator.init(new SecureRandom(strKey.getBytes()));
            this.key = _generator.generateKey();
            _generator = null;
        } catch (Exception e) {
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

}
