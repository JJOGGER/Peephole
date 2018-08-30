package cn.jcyh.peephole.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.szjcyh.mysmart.IMyAidlInterface;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.jcyh.peephole.MainActivity;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.ConfigData;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.observer.AVChatObserver;
import cn.jcyh.peephole.observer.FriendServiceObserver;
import cn.jcyh.peephole.observer.MessageReceiveObserver;
import cn.jcyh.peephole.observer.NIMSystemMessageObserver;
import cn.jcyh.peephole.observer.UserStatusObserver;
import cn.jcyh.peephole.receiver.AlarmReceiver;
import cn.jcyh.peephole.utils.CacheUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.SPUtil;


/**
 * Created by jogger on 2017/12/4.后台服务
 */

public class MainService extends Service {
    private MyBinder mBinder;
    private MyServiceConnection mConnection;
    private static boolean sIsClock;//是否锁屏
    private static int sLockTime;//记录锁屏时间
    private MyReceiver mReceiver;
    private Observer<StatusCode> mUserStatusObserver;
    private Observer<SystemMessage> mSystemMessageObserver;
    private Observer<List<IMMessage>> mMessageReceiveObserver;
    private Observer<AVChatData> mAVChatObserver;
    private Observer<FriendChangedNotify> mFriendChangedNotifyObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initConfig();
        if (mBinder == null) mBinder = new MyBinder();
        if (mConnection == null) mConnection = new MyServiceConnection();
        Intent intent = new Intent(this, MainRemoteService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_IMPORTANT);
        //注册屏幕状态广播
        mReceiver = new MyReceiver();
        mUserStatusObserver = new UserStatusObserver();
        mSystemMessageObserver = new NIMSystemMessageObserver();
        mMessageReceiveObserver = new MessageReceiveObserver();
        mAVChatObserver = new AVChatObserver();
        mFriendChangedNotifyObserver = new FriendServiceObserver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        registerReceiver(mReceiver, intentFilter);
        registerObservers(true);
    }

    private void initConfig() {
        //初始化配置
        final DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        if (ControlCenter.getBCManager() != null) {
            ControlCenter.getBCManager().setPIRSensorOn(doorbellConfig.getMonitorSwitch() == 1);
        }
        //配置猫眼视频
        ControlCenter.getDoorbellManager().getDoorbellConfigFromServer(ControlCenter.getIMEI(), new IDataListener<ConfigData>() {
            @Override
            public void onSuccess(ConfigData configData) {
                doorbellConfig.setVideoConfig(configData.getCatEyeConfig());
                ControlCenter.getDoorbellManager().setDoorbellConfig(doorbellConfig);
                ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getIMEI(), doorbellConfig, null);
            }

            @Override
            public void onFailure(int errorCode, String desc) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent mIntent = new Intent(this, MainActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent
                .FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.push_layout)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.video_service))
                .setContentText(getString(R.string.running))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        startForeground(startId, builder.build());
        ControlCenter.connectNIM();//连接网易云
        //注册闹钟广播
        registAlarm();
        return START_STICKY;
    }

    private void registerObservers(boolean register) {
        //用户状态监听
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(mUserStatusObserver, register);
        //好友请求监听
        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(mSystemMessageObserver, register);
        //消息接收监听
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(mMessageReceiveObserver, register);
        //视频呼叫监听
        AVChatManager.getInstance().observeIncomingCall(mAVChatObserver, register);
        //好友更新监听
        NIMClient.getService(FriendServiceObserve.class).observeFriendChangedNotify(mFriendChangedNotifyObserver, register);
        //好友在线监听
//        OnlineStateEventManager.getOnlineStateChangeObservable().registerOnlineStateChangeListeners(mOnlineStateChangeObserver, register);
    }

    private void registAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intentAlarm, 0);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * 60, pi);
    }

    private class MyBinder extends IMyAidlInterface.Stub {

        @Override
        public void dealThings() {
        }
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //重新启动
            Intent intent = new Intent(MainService.this, MainRemoteService.class);
            intent.putExtra(Constant.FLAG, 1);
            startService(intent);
            bindService(intent, mConnection, Context.BIND_IMPORTANT);
        }
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) return;
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                sIsClock = false;
                sLockTime = 0;
                CacheUtil.clearCache();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                L.e("---------ACTION_SCREEN_OFF");
                sIsClock = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (sIsClock) {
                            sLockTime += 5;
                            if (sLockTime >= 60 * 10) {
                                //10分钟重连
//                                mMyHandler.sendEmptyMessage(0);
                            }
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
            switch (action) {
                case Intent.ACTION_BATTERY_CHANGED:
                    // 是否在充电
                    int battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
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
                                HttpAction.getHttpAction().uploadBattery(ControlCenter.getIMEI(), battery, null);
                            }
                        } else if (battery <= 5) {
                            boolean isBattery = SPUtil.getInstance().getBoolean(Constant.BATTERY);
                            if (isBattery) {
                                //发推送
                                HttpAction.getHttpAction().uploadBattery(ControlCenter.getIMEI(), battery, null);
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

    private static class MyHandler extends Handler {
        private WeakReference<MainService> mServiceWeakReference;

        MyHandler(MainService service) {
            mServiceWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //电量屏幕
//            L.e("-----------10分钟重连：" + ControlCenter.sIsAnychatLogin);
            MainService service = mServiceWeakReference.get();
            //获取电源管理器对象
            PowerManager pm = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
            //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            assert pm != null;
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            //点亮屏幕
            wl.acquire(10 * 60 * 1000L /*10 minutes*/);
            wl.release();
            sLockTime = 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.e("-------------onDestroy");
        stopForeground(true);
        registerObservers(false);
        unregisterReceiver(mReceiver);
    }
//    OnlineStateChangeObserver mOnlineStateChangeObserver = new OnlineStateChangeObserver() {
//        @Override
//        public void onlineStateChange(Set<String> accounts) {
//            L.e("-------------在线状态监听：" + accounts);
//        }
//    };
}
