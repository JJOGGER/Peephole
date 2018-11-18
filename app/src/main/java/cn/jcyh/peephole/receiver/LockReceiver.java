package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.jcyh.eaglelock.constant.Constant;
import cn.jcyh.eaglelock.constant.MyLockKey;
import cn.jcyh.peephole.entity.RequestUploadElectricQuantity;
import cn.jcyh.peephole.http.LockHttpAction;
import cn.jcyh.peephole.utils.L;

/**
 * Created by jogger on 2018/11/12.蓝牙锁相关
 */
public class LockReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        L.e("----------onReceive:" + intent.getAction());
        switch (intent.getAction()) {
            case Constant.ACTION_GET_OPERATE_LOG:
                String records = intent.getStringExtra(Constant.RECORDS);
                LockHttpAction.getHttpAction().lockUploadLog(MyLockKey.sCurrentKey.getLockId(), MyLockKey.sCurrentKey.getAccessToken(), records, null);
                break;
            case Constant.ACTION_UNLOCK:
                if (MyLockKey.sCurrentKey != null) {
                    RequestUploadElectricQuantity requestUploadElectricQuantity = new RequestUploadElectricQuantity(
                            null,
                            MyLockKey.sCurrentKey.getAccessToken(),
                            MyLockKey.sCurrentKey.getLockId(),
                            MyLockKey.sCurrentKey.getElectricQuantity(),
                            System.currentTimeMillis()
                    );
                    LockHttpAction.getHttpAction().lockUpdateElectricQuantity(requestUploadElectricQuantity, null);
                }
                break;
        }
    }
}
