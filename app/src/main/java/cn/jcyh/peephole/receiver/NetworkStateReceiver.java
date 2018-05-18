package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import timber.log.Timber;

/**
 * Created by jogger on 2018/5/12.
 * 监听网络变化
 */

public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) return;
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {//监听wifi是否打开
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            Timber.e("-----------WIFI_STATE_CHANGED_ACTION:"+wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    break;
                default:
                    break;
            }

        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            Timber.e("-----------NETWORK_STATE_CHANGED_ACTION");
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null) {
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state == NetworkInfo.State.CONNECTED;
                if (isConnected) {
                    //连接上了
                }
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            Timber.e("-----------CONNECTIVITY_ACTION");
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                        Timber.e("当前WiFi连接可用 ");
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
                        Timber.e("当前移动网络连接可用 ");
                    }
                } else {
                    Timber.e("当前没有网络连接，请确保你已经打开网络 ");
                }


                Timber.e("info.getTypeName()" + activeNetwork.getTypeName());
                Timber.e("getSubtypeName()" + activeNetwork.getSubtypeName());
                Timber.e("getState()" + activeNetwork.getState());
                Timber.e("getDetailedState()"
                        + activeNetwork.getDetailedState().name());
                Timber.e("getDetailedState()" + activeNetwork.getExtraInfo());
                Timber.e("getType()" + activeNetwork.getType());
            } else {   // not connected to the internet
                Timber.e("当前没有网络连接，请确保你已经打开网络 ");
            }
        }
    }
}
