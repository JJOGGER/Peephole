package cn.jcyh.peephole.utils;

import android.content.Context;

import java.security.MessageDigest;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tool {
    public static String MD5(String pwd) {
        //用于加密的字符
        char md5String[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            //使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
            byte[] btInput = pwd.getBytes();

            //信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
            MessageDigest mdInst = MessageDigest.getInstance("MD5");

            //MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
            mdInst.update(btInput);

            // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
            byte[] md = mdInst.digest();

            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {   //  i = 0
                byte byte0 = md[i];  //95
                str[k++] = md5String[byte0 >>> 4 & 0xf];    //    5
                str[k++] = md5String[byte0 & 0xf];   //   F
            }

            //返回经过加密后的字符串
            return new String(str);

        } catch (Exception e) {
            return null;
        }
    }

    public static String BytesToHexString(byte[] src) throws Exception {
        StringBuilder stringBuilder = new StringBuilder("");
        if ((src == null) || (src.length <= 0)) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] HexStringToBytes(String hexString) throws Exception {
        if (hexString != null && !hexString.equals("")) {
            if (hexString.length() % 2 != 0) {
                hexString = hexString + "0";
            }

            hexString = hexString.toUpperCase(Locale.getDefault());
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];

            for (int i = 0; i < length; ++i) {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }

            return d;
        } else {
            return null;
        }
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * @param num
     * @return 高位在前，低位在后
     */
    public static byte[] IntToBytes(int num) {
        byte[] result = new byte[4];
        result[0] = (byte) ((num >> 24) & 0xff);
        result[1] = (byte) ((num >> 16) & 0xff);
        result[2] = (byte) ((num >> 8) & 0xff);
        result[3] = (byte) ((num >> 0) & 0xff);
        return result;
    }

    /**
     * @param bytes
     * @return 高位在前，低位在后
     */
    public static int BytesToInt(byte[] bytes) {
        int result = 0;
        if (bytes.length == 4) {
            int a = (bytes[0] & 0xff) << 24;
            int b = (bytes[1] & 0xff) << 16;
            int c = (bytes[2] & 0xff) << 8;
            int d = (bytes[3] & 0xff);
            result = a | b | c | d;
        }
        return result;
    }

    /**
     * 字符串转换成16进制字节数组
     *
     * @param nm
     * @return
     */
    public static byte[] decodeHex(String nm) {
        int len = nm.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i++) {
            char c = nm.charAt(i);
            byte b = Byte.decode("0x" + c);
            if (i + 1 == len)
                break;
            c = nm.charAt(++i);
            result[i / 2] = (byte) (b << 4 | Byte.decode("0x" + c));
        }
        return result;
    }

    /**
     * 计算累加和
     *
     * @param be
     * @return
     */
    public static String HexsToInt(byte[] be) {
        int date = 0;
        for (int i = 0; i < be.length; i++) {
            date += (int) (be[i] & 0xff);
        }
        return Integer.toHexString(date).substring(Integer.toHexString(date).length() - 2);
    }

    /**
     * 功能键编码转换成地位在前高位在后的字符串
     *
     * @param i
     * @return
     */
    public static String hexToString(int i) {
        String date = Integer.toHexString(i);

        if (date.length() == 1) {
            date = "0" + date + "00";
        } else if (date.length() == 2) {
            date = date + "00";
        } else if (date.length() == 3) {
            date = date.substring(1, 3) + "0" + date.substring(0, 1);
        }
        return date;
    }

    /**
     * 判断一个字符串是否含有数字
     */
    public static boolean hasDigit(String content) {
        boolean flag = false;

        Pattern p = Pattern.compile(".*\\d+.*");

        Matcher m = p.matcher(content);

        if (m.matches())

            flag = true;

        return flag;
    }

    //截取数字
    public static String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * 判断当前语言环境
     */
    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.endsWith("zh");
    }

    /**
     * 判断当前语言环境
     */
    public static boolean isFr(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.endsWith("fr");
    }
}
