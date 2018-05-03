package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import cn.jcyh.peephole.MainActivity;
import timber.log.Timber;

/**
 * Created by jogger on 2018/1/16.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(intent.getAction())) return;
        Timber.e("------------>onReceive" + intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent bootActivityIntent = new Intent(context, MainActivity.class);
            bootActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(bootActivityIntent);
        }
    }
}
