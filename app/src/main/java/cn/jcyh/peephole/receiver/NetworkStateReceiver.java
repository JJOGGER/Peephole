package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.entity.LogRecord;
import cn.jcyh.peephole.event.NetworkAction;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.manager.impl.LocationManager;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.GsonUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.Util;


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
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            assert manager != null;
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork == null || !activeNetwork.isConnected()) {
                L.e("-------当前连接的网络不可用");
                LocationManager.stopLocation();
                recordLog();
                return;
            }
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                LocationManager.startLocation();
            }
            L.i("-------当前连接的网络可用");
            NetworkAction networkAction = new NetworkAction();
            networkAction.setType(NetworkAction.TYPE_NETWORK_CONNECTED);
            EventBus.getDefault().post(networkAction);
            final DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
            ControlCenter.connectNIM();
            boolean existOfflineData = doorbellConfig.isExistOfflineData();
            if (existOfflineData) {
                ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getSN(), doorbellConfig, new IDataListener<Boolean>() {
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
            uploadLog();
        }

    }

    /**
     * 上传日志
     */
    private void uploadLog() {
        String logPath = FileUtil.getAPKPath() + File.separator + "log.txt";
        LogRecord logRecord = null;
        File file = new File(logPath);
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf8");
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            L.e("---------:" + line);
            if (!TextUtils.isEmpty(line)) {
                logRecord = GsonUtil.fromJson(line, LogRecord.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpAction.getHttpAction().uploadLog(logRecord, null);
    }

    /**
     * 记录日志
     */
    private void recordLog() {
        String logPath = FileUtil.getAPKPath();
        File file = new File(logPath + File.separator + "log.txt");
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
            LogRecord logRecord = new LogRecord();
            logRecord.setAlarmType(LogRecord.ALARM_DOORBELL_OFFLINE);
            logRecord.setDeviceId(ControlCenter.getSN());
            logRecord.setDeviceType(LogRecord.TYPE_DOORBELL);
            logRecord.setRemark(Util.getApp().getString(R.string.network_is_not_available));
            osw.write(GsonUtil.toJson(logRecord));
            osw.flush();
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
