package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.NetworkAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.NetworkUtil;


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
            L.e("-----------WIFI_状态改变动作:" + wifiState);
            /*
             *  WifiManager.WIFI_STATE_ENABLING: WiFi正要开启的状态, 是 Enabled 和 Disabled 的临界状态;
             *  WifiManager.WIFI_STATE_ENABLED: WiFi已经完全开启的状态;
             *  WifiManager.WIFI_STATE_DISABLING: WiFi正要关闭的状态, 是 Disabled 和 Enabled 的临界状态;
             *  WifiManager.WIFI_STATE_DISABLED: WiFi已经完全关闭的状态;
             *  WifiManager.WIFI_STATE_UNKNOWN: WiFi未知的状态, WiFi开启, 关闭过程中出现异常, 或是厂家未配备WiFi外挂模块会出现的情况;
             */
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    //表示断开了连接
                    DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
                    doorbellConfig.setExistOfflineData(true);
                    ControlCenter.getDoorbellManager().setDoorbellConfig(doorbellConfig);//标记有离线数据
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

        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            if (NetworkUtil.isConnected()) {
//                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                L.e("-------当前连接的网络可用");
                NetworkAction networkAction = new NetworkAction();
                networkAction.setType(NetworkAction.TYPE_NETWORK_CONNECTED);
                EventBus.getDefault().post(networkAction);
                final DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
                ControlCenter.connectNIM();
                boolean existOfflineData = doorbellConfig.isExistOfflineData();
                if (existOfflineData) {
                    ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getIMEI(), doorbellConfig, new IDataListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            //离线数据同步完成
                            doorbellConfig.setExistOfflineData(false);
                            ControlCenter.getDoorbellManager().setDoorbellConfig(doorbellConfig);
                        }

                        @Override
                        public void onFailure(int errorCode, String desc) {

                        }
                    });
                }
//                }
//                if (NIMClient.getStatus() == StatusCode.LOGINED) {
//                    L.e("-------------网络更新发布状态");
//                    OnlineStateEventManager.publishOnlineStateEvent();
//                }
            }
        }
//        else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
//            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//            L.e("-----------NETWORK_STATE_CHANGED_ACTION" + networkInfo);
//            if (networkInfo != null) {
//                NetworkInfo.State state = networkInfo.getState();
//                boolean isConnected = state == NetworkInfo.State.CONNECTED;
//                if (isConnected) {
//                    //连接上了
//                    L.e("------------连接上了网络");
//                }
//            }
//        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
//            L.e("-----------CONNECTIVITY_ACTION");
//            ConnectivityManager manager = (ConnectivityManager) context
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//            assert manager != null;
//            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
//            if (activeNetwork != null) { // connected to the internet
//                if (activeNetwork.isConnected()) {
//                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                        // connected to wifi
//                        L.e("-------当前连接的WiFi可用 ");
//                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                        // connected to the mobile provider's data plan
//                        L.e("---------当前移动网络连接可用 ");
//                    }
//                } else {
//                    L.e("---------当前没有网络连接，请确保你已经打开网络 ");
//                }
//            } else {   // not connected to the internet
//                L.e("---------activeNetwork为null当前没有网络连接，请确保你已经打开网络 ");
//            }
//        }
    }

}
