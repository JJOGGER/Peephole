package cn.jcyh.peephole.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

/**
 * Created by jogger on 2018/8/3.
 */
public class PhoneUtil {
    @SuppressLint("MissingPermission")
    public static void callPhone(String phone) {
        if (!isPhoneCallable()) return;
        if (!PhoneNumberUtils.isGlobalPhoneNumber(phone)) return;
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phone);
        intent.setData(data);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Util.getApp().startActivity(intent);
    }

    //发送短信
    public static void sendMsg(String masterNumber, String message) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(masterNumber)) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(masterNumber, null, message, null, null);
        }
    }

    /**
     * 检查是否有手机卡可用
     */
    public static boolean isPhoneCallable() {
        TelephonyManager telMgr = (TelephonyManager)
                Util.getApp().getSystemService(Context.TELEPHONY_SERVICE);
        assert telMgr != null;
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }
}
