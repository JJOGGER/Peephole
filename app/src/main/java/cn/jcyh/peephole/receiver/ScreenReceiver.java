package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import cn.jcyh.peephole.utils.CacheUtil;

/**
 * Created by jogger on 2018/9/7.
 */
public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) return;
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            CacheUtil.clearCache();
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
        }
    }
}
