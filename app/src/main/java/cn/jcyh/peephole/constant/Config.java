package cn.jcyh.peephole.constant;

import java.util.Random;

import cn.jcyh.peephole.utils.Tool;

/**
 * Created by jogger on 2018/6/29.
 */
public class Config {
    public final static String AUDIO_APP_ID = "5bbc7ad3";//讯飞appid
    public final static String UMENG_APP_KEY = "5b50612e8f4a9d3b360000eb";//友盟appkey
    private static final String APP_KEY = "670A95AB9C184655B0BCA8B762706C2C";//猫眼appid
    private static final String APP_SECRET = "7D40129DCF59432197475F83BD11C689";
    private static final int NONCE_LENGTH = 32;

    public static HeaderConfig getHeaderConfig() {
        String nonce = getNonce();
        String timestamp = getTimestamp();
        String sign = getSign(nonce, timestamp);
        return new HeaderConfig(APP_KEY, nonce, timestamp, sign);
    }

    public static class HeaderConfig {
        private String appkey;
        private String nonce;
        private String timestamp;
        private String sign;

        HeaderConfig(String appkey, String nonce, String timestamp, String sign) {
            this.appkey = appkey;
            this.nonce = nonce;
            this.timestamp = timestamp;
            this.sign = sign;
        }

        public String getAppkey() {
            return appkey;
        }

        public String getNonce() {
            return nonce;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getSign() {
            return sign;
        }
    }

    private static String getNonce() {
        //定义一个字符串（A-Z，a-z，0-9）即62位；
        String str = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        //由Random生成随机数
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        //长度为几就循环几次
        for (int i = 0; i < NONCE_LENGTH; ++i) {
            //产生0-61的数字
            int number = random.nextInt(62);
            //将产生的数字通过length次承载到sb中
            sb.append(str.charAt(number));
        }
        //将承载的字符转换成字符串
        return sb.toString();
    }

    private static String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static String getSign(String nonce, String timestamp) {
        return Tool.MD5(APP_SECRET + nonce + timestamp);
    }
}
