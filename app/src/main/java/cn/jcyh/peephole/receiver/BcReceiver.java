package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import cn.jcyh.peephole.utils.ConstantUtil;

public class BcReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();

        if (act.equals("kphone.intent.action.LOCK_DETECT")) { // TAMPER
            String extAct = intent.getStringExtra("value");
            if (extAct.equals("normal")) {
                showToast(context, "防拆中断:连接正常");
            } else if (extAct.equals("separate")) {
                showToast(context, "防拆中断:设备拆开了");
            }
        } else if (act.equals("kphone.intent.action.PIR")) {// PIR
            String extAct = intent.getStringExtra("value");
            if (extAct.equals("PeopleIn")) {
                showToast(context, "PIR中断:有人来了");
                intent = new Intent();
                intent.setAction(ConstantUtil.ACTION_DOORBELL_SYSTEM_EVENT);
                intent.putExtra("type", ConstantUtil.TYPE_DOORBELL_SYSTEM_ALARM);
                context.sendBroadcast(intent);
            } else if (extAct.equals("PeopleOut")) {
                showToast(context, "PIR中断:人走了");
            }
        } else if (act.equals("kphone.intent.action.RING")) { // OURDOOR_PRESS
            String extAct = intent.getStringExtra("value");
            if (extAct.equals("pressed")) {
                showToast(context, "RING中断:按下门铃键");
                intent = new Intent();
                intent.setAction(ConstantUtil.ACTION_DOORBELL_SYSTEM_EVENT);
                intent.putExtra("type", ConstantUtil.TYPE_DOORBELL_SYSTEM_RING);
                context.sendBroadcast(intent);
            } else if (extAct.equals("released")) {
                showToast(context, "RING中断:放开门铃键");
            }
//		}else if (act.equals("kphone.intent.action.HOME_PRESS")) { // INDOOR_PRESS
//			String extAct = intent.getStringExtra("value");
//			if (extAct.equals("pressed")) {
//				Timber( "HOME ---- pressed!");
//				showToast(context,"HOME中断:按下HOME键");
//			} else if (extAct.equals("released")) {
//				Timber( "HOME ---- released!");
//				showToast(context,"HOME中断:放开HOME键");
//			}
        }
    }

    public void showToast(Context context, String mes) {
        Toast.makeText(context, mes, Toast.LENGTH_SHORT).show();
    }
}
