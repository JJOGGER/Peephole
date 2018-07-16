package cn.jcyh.peephole.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cn.jcyh.eaglelock.constant.Constant;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.BcManager;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.ui.activity.CameraActivity;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.T;

import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_DOORBELL_SYSTEM_ALARM;

public class BcReceiver extends BroadcastReceiver {
    private Timer mTimer;
    private MyTimeTask mTimerTask;
    private int mSensorTime;
    private int mCount;
    private MediaPlayer mPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        mSensorTime = DoorBellControlCenter.getInstance().getDoorbellConfig()
                .getAutoSensorTime();
        String act = intent.getAction();
        switch (act) {
            case "kphone.intent.action.LOCK_DETECT": { // TAMPER
                String extAct = intent.getStringExtra("value");
                if (extAct.equals("normal")) {
                    showToast(context, "防拆中断:连接正常");
                } else if (extAct.equals("separate")) {
                    showToast(context, "防拆中断:设备拆开了");
                }
                break;
            }
            case "kphone.intent.action.PIR": {// PIR
                String extAct = intent.getStringExtra("value");
                if (extAct.equals("PeopleIn")) {
                    L.e("------->mTimer:" + mTimer + "-->PIR中断:有人来了");
                    if (mTimer != null && mTimerTask != null) {
                        return;
                    }
                    mTimer = new Timer();
                    mTimerTask = new MyTimeTask(context);
                    mCount = 0;
                    mTimer.schedule(mTimerTask, 0, 1000);
                } else if (extAct.equals("PeopleOut")) {
                    L.e("------->mTimer:" + mTimer + "-->PIR中断:人走了");
                }
                break;
            }
            case "kphone.intent.action.RING": { // OURDOOR_PRESS
                String extAct = intent.getStringExtra("value");
                if (extAct.equals("pressed")) {
                    play(context, DoorBellControlCenter.DOORBELL_TYPE_RING);
                    if (!DoorBellControlCenter.sIsVideo) {
                        DoorBellControlCenter.sIsVideo = true;
                        //播放铃声
                        intent = new Intent(context, CameraActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Constant.TYPE, ConstantUtil.TYPE_DOORBELL_SYSTEM_RING);
                        context.startActivity(intent);
                    }
                    //发送广播
                    intent = new Intent();
                    intent.setAction(ConstantUtil.ACTION_DOORBELL_SYSTEM_EVENT);
                    intent.putExtra(Constant.TYPE, ConstantUtil.TYPE_DOORBELL_SYSTEM_RING);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                } else if (extAct.equals("released")) {
                }
//		}else if (act.equals("kphone.intent.action.HOME_PRESS")) { // INDOOR_PRESS
//			String extAct = intent.getStringExtra("value");
//			if (extAct.equals("pressed")) {
//				L( "HOME ---- pressed!");
//				show(context,"HOME中断:按下HOME键");
//			} else if (extAct.equals("released")) {
//				L( "HOME ---- released!");
//				show(context,"HOME中断:放开HOME键");
//			}
                break;
            }
            case "kphone.intent.action.MAGEINT":
                String extAct = intent.getStringExtra("value");
                if ("close".equals(extAct)) {
                    T.show("门磁关闭");
                } else if ("open".equals(extAct)) {
                    T.show("门磁打开");
                }
                break;
        }
    }

    private void play(final Context context, final int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AssetFileDescriptor descriptor;
                    AssetManager assets = context.getResources().getAssets();
                    DoorbellConfig doorbellConfig = DoorBellControlCenter.getInstance().getDoorbellConfig();
                    if (mPlayer == null) {
                        mPlayer = new MediaPlayer();
                        mPlayer.setLooping(false);
                    } else {
                        mPlayer.stop();
                        mPlayer.reset();
                    }
                    if (type == DoorBellControlCenter.DOORBELL_TYPE_RING) {
                        descriptor = assets.openFd("ring/" + doorbellConfig.getDoorbellRingName());
                        mPlayer.setVolume(doorbellConfig.getRingVolume() / 100f, doorbellConfig.getRingVolume() / 100f);
                    } else {
                        descriptor = assets.openFd("alarm/" + doorbellConfig.getDoorbellAlarmName());
                        mPlayer.setVolume(doorbellConfig.getAlarmVolume() / 100f, doorbellConfig.getAlarmVolume() / 100f);
                    }
                    mPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    mPlayer.prepare();
                    mPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    L.e("----" + e.getMessage());
                }
            }
        }).start();

    }

    public void showToast(Context context, String mes) {
        Toast.makeText(context, mes, Toast.LENGTH_SHORT).show();
    }

    private class MyTimeTask extends TimerTask {
        private Context mContext;

        MyTimeTask(Context context) {
            mContext = context;
        }

        @Override
        public void run() {
            mCount++;
            L.e("--------count:" + mCount + "--->" + mSensorTime);
            if (mCount >= mSensorTime) {//达到感应时间
                mCount = 0;
                mTimer.cancel();
                mTimer = null;
                mTimerTask.cancel();
                mTimerTask = null;
                boolean pirStatus = BcManager.getManager().getPIRStatus();
                L.e("----------pirStatus" + pirStatus);
                if (pirStatus) {
                    //表示有人
                    DoorbellConfig doorbellConfig = DoorBellControlCenter.getInstance().getDoorbellConfig();
                    L.e("----------getSensorRingAlarm" + doorbellConfig.getSensorRingAlarm());
                    if (doorbellConfig.getSensorRingAlarm() == 1) {
                        //开启了停留报警
                        play(mContext, DoorBellControlCenter.DOORBELL_TYPE_ALARM);
                    }
                    Intent intent = new Intent(mContext, CameraActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Constant.TYPE, TYPE_DOORBELL_SYSTEM_ALARM);
                    mContext.startActivity(intent);
                }
            }
        }
    }
}
