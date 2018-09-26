package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.text.TextUtils;

import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.SPUtil;

/**
 * Created by jogger on 2018/9/7.
 */
public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) return;
        switch (action) {
            case Intent.ACTION_BATTERY_CHANGED:
                // 是否在充电
                int battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                ControlCenter.sCurrentBattery = battery;
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                //如果不在充电，且电量低于5或者电量低于10，发送推送
                if (!isCharging) {
                    if (battery > 10) {
                        SPUtil.getInstance().put(Constant.BATTERY, false);
                    }
                    if (battery > 5 & battery <= 10) {
                        boolean isBattery = SPUtil.getInstance().getBoolean(Constant.BATTERY);
                        if (isBattery) {
                            //不发推送
                        } else {
                            //发推送
                            SPUtil.getInstance().put(Constant.BATTERY, true);//置反
                            HttpAction.getHttpAction().uploadBattery(ControlCenter.getSN(), battery, null);
                        }
                    } else if (battery <= 5) {
                        boolean isBattery = SPUtil.getInstance().getBoolean(Constant.BATTERY);
                        if (isBattery) {
                            //发推送
                            HttpAction.getHttpAction().uploadBattery(ControlCenter.getSN(), battery, null);
                            SPUtil.getInstance().put(Constant.BATTERY, false);//置反
                        } else {
                            //不发推送
                        }
                    }
                }
                break;
            case Intent.ACTION_BATTERY_LOW:
                L.e("----------------ACTION_BATTERY_LOW");
                break;
        }
    }
}
