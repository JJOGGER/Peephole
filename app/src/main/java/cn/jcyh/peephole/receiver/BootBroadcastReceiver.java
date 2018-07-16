package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import cn.jcyh.peephole.MainActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(intent.getAction())) return;
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent bootActivityIntent = new Intent(context, MainActivity.class);
            bootActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(bootActivityIntent);
            WifiManager wifiManager= (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);

        }
    }
}
