package cn.jcyh.peephole.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.config.ConfigEntity;
import com.bairuitech.anychat.config.ConfigHelper;
import com.google.gson.Gson;
import com.szjcyh.mysmart.IMyAidlInterface;

import java.lang.ref.WeakReference;

import cn.jcyh.peephole.MainActivity;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.AnyChatTransDataEventAdapter;
import cn.jcyh.peephole.adapter.AnyChatUserInfoEventAdapter;
import cn.jcyh.peephole.adapter.AnyChatVideoCallEventAdapter;
import cn.jcyh.peephole.adapter.AnychatBaseEventAdapter;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.SharePreUtil;
import timber.log.Timber;


/**
 * Created by jogger on 2017/12/4.
 */

public class KeepBackLocalService extends Service {
    private MyBinder mBinder;
    private MyServiceConnection mConnection;
    private AnyChatCoreSDK mAnyChat;
    //    private OnepxReceiver mOnepxReceiver;
    private static boolean sIsClock;//是否锁屏
    private static int sLockTime;//记录锁屏时间
    private MyHandler mMyHandler;
    private MyReceiver mReceiver;
    private Gson mGson;
    private DoorBellControlCenter mControlCenter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGson = new Gson();
        mControlCenter = DoorBellControlCenter.getInstance(this);
        DoorbellConfig doorbellConfig = mControlCenter.getDoorbellConfig();
        if (mBinder == null) mBinder = new MyBinder();
        mAnyChat = AnyChatCoreSDK.getInstance(getApplicationContext());
        mMyHandler = new MyHandler(this);
        if (mConnection == null) mConnection = new MyServiceConnection();
        Intent mIntent = new Intent(this, KeepBackRemoteService.class);
        bindService(mIntent, mConnection, Context.BIND_IMPORTANT);
        //注册屏幕状态广播
        mReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mReceiver, intentFilter);
//        intentFilter.addAction("android.intent.action.USER_PRESENT");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent mIntent = new Intent(this, MainActivity.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mIntent, PendingIntent
                .FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("正在运行")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        startForeground(startId, builder.build());
        connectAnyChat();
        return START_STICKY;
    }

    private String getAndroidIMEI() {
        return android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.ANDROID_ID);
    }

    /**
     * 连接anychat
     */
    private void connectAnyChat() {
        String imei = SharePreUtil.getInstance(this).getString(ConstantUtil.IMEI,
                getAndroidIMEI());
        mAnyChat.Logout();
        mAnyChat.Release();
        mAnyChat = null;
        mAnyChat = AnyChatCoreSDK.getInstance(getApplicationContext());
        mAnyChat.SetBaseEvent(new MyBaseEventAdapter(this));//anyChat基本事件接口
        mAnyChat.SetUserInfoEvent(new AnyChatUserInfoEventAdapter(this));//更新设备信息
        mAnyChat.SetVideoCallEvent(new AnyChatVideoCallEventAdapter(this));//视频呼叫事件接口
        mAnyChat.SetTransDataEvent(new AnyChatTransDataEventAdapter(this));//数据传输通知接口
        mAnyChat.InitSDK(Build.VERSION.SDK_INT, 0);
        ConfigHelper configHelper = ConfigHelper.getConfigHelper(KeepBackLocalService.this);
        configHelper.applyVideoConfig();//根据配置文件设置视频参数
        ConfigEntity configEntity = configHelper.LoadConfig();
        mAnyChat.Connect(configEntity.ip, configEntity.port);//连接anychat
        mAnyChat.Login(imei, imei);
    }

    private class MyBinder extends IMyAidlInterface.Stub {

        @Override
        public void dealThings() throws RemoteException {
//            ToastUtil.showToast(getApplicationContext(), "dealThings");
            Timber.e("------------>dealThings");
//            login2DoorBell();
        }
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //重新启动
            Intent intent = new Intent(KeepBackLocalService.this, KeepBackRemoteService.class);
            intent.putExtra("flag", 1);
            startService(intent);
            bindService(intent, mConnection, Context.BIND_IMPORTANT);
        }
    }

    private class MyBaseEventAdapter extends AnychatBaseEventAdapter {

        public MyBaseEventAdapter(Context context) {
            super(context);
        }

        @Override
        public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
            super.OnAnyChatLinkCloseMessage(dwErrorCode);
            connectAnyChat();
            //        if (dwErrorCode != 209) {
//        }
        }
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Timber.e("---------ACTION_SCREEN_ON");
                sIsClock = false;
                sLockTime = 0;
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Timber.e("---------ACTION_SCREEN_OFF");
                sIsClock = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (sIsClock) {
                            sLockTime += 5;
                            if (sLockTime >= 60 * 10) {
                                //10分钟重连
                                mMyHandler.sendEmptyMessage(0);
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
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<KeepBackLocalService> mServiceWeakReference;

        MyHandler(KeepBackLocalService service) {
            mServiceWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //电量屏幕
//            Timber.e("-----------10分钟重连：" + DoorBellControlCenter.sIsAnychatLogin);
            KeepBackLocalService service = mServiceWeakReference.get();
            //获取电源管理器对象
            PowerManager pm = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
            //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            //点亮屏幕
            wl.acquire();
//            final String uid = SharePreUtil.getInstance(service).getString(ConstansUtil.UID, "");
//            service.connectAnyChat(uid);
            wl.release();
            sLockTime = 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.e("-------------onDestroy");
        stopForeground(true);
        unregisterReceiver(mReceiver);
    }

}
