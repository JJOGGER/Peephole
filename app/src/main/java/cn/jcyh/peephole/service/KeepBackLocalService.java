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
import com.bairuitech.anychat.AnyChatRecordEvent;
import com.bairuitech.anychat.config.ConfigEntity;
import com.bairuitech.anychat.config.ConfigHelper;
import com.google.gson.Gson;
import com.szjcyh.mysmart.IMyAidlInterface;

import java.lang.ref.WeakReference;

import cn.jcyh.peephole.MainActivity;
import cn.jcyh.peephole.MyApp;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.AnyChatTransDataEventAdapter;
import cn.jcyh.peephole.adapter.AnyChatUserInfoEventAdapter;
import cn.jcyh.peephole.adapter.AnyChatVideoCallEventAdapter;
import cn.jcyh.peephole.adapter.AnychatBaseEventAdapter;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_RECORD_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_RECORD;


/**
 * Created by jogger on 2017/12/4.
 */

public class KeepBackLocalService extends Service implements AnyChatRecordEvent {
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
        connectAnyChat(MyApp.sImei);
//        DoorBellControlCenter.getInstance(this).login2DoorBell(new DoorBellControlCenter
// .OnLoginDoorBellListener() {
//            @Override
//            public void onSuccess(String uid) {
//
//                if (mFirstLogin) {
//                    Intent intent = new Intent(ACTION_DOORBELL_LOGIN_RESULT);
//                    intent.putExtra("errorCode", "0");
//                    intent.putExtra("uid", uid);
//                    sendBroadcast(intent);
//                }
//            }
//
//            @Override
//            public void onFailure(String errorCode) {
//                if (mFirstLogin) {//为true时还会走登录网关摇头机等操作
//                    Intent intent = new Intent(ACTION_DOORBELL_LOGIN_RESULT);
//                    intent.putExtra("errorCode", errorCode);
//                    sendBroadcast(intent);
//                }
//            }
//        });
        return START_STICKY;
    }

    /**
     * 连接anychat
     */
    private void connectAnyChat(String uid) {
        mAnyChat.Logout();
        mAnyChat.Release();
        mAnyChat = null;
        mAnyChat = AnyChatCoreSDK.getInstance(getApplicationContext());
        mAnyChat.SetBaseEvent(new AnychatBaseEventAdapter(this));//anyChat基本事件接口
        mAnyChat.SetUserInfoEvent(new AnyChatUserInfoEventAdapter(this));//更新设备信息
        mAnyChat.SetVideoCallEvent(new AnyChatVideoCallEventAdapter(this));//视频呼叫事件接口
        mAnyChat.SetTransDataEvent(new AnyChatTransDataEventAdapter(this));//数据传输通知接口
        mAnyChat.SetRecordSnapShotEvent(KeepBackLocalService.this);//截图录制接口
        mAnyChat.InitSDK(Build.VERSION.SDK_INT, 0);
        ConfigHelper configHelper = ConfigHelper.getConfigHelper(KeepBackLocalService.this);
        configHelper.applyVideoConfig();//根据配置文件设置视频参数
        ConfigEntity configEntity = configHelper.LoadConfig();
        mAnyChat.Connect(configEntity.ip, configEntity.port);//连接anychat
        mAnyChat.Login(uid, uid);
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

    @Override
    public void OnAnyChatRecordEvent(int dwUserId, int dwErrorCode, String lpFileName, int
            dwElapse, int dwFlags, int dwParam, String lpUserStr) {
        Timber.e("------OnAnyChatRecordEvent" + dwErrorCode);
        Intent intent = new Intent();
        intent.putExtra("dwErrorCode", dwErrorCode);
        intent.putExtra("lpFileName", lpFileName);
        intent.putExtra("dwUserId", dwUserId);
        intent.putExtra("dwElapse", dwElapse);
        intent.putExtra("dwFlags", dwFlags);
        intent.putExtra("dwParam", dwParam);
        intent.putExtra("lpUserStr", lpUserStr);
        intent.setAction(ACTION_ANYCHAT_RECORD_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_RECORD);
        sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatSnapShotEvent(int dwUserId, int dwErrorCode, String lpFileName, int
            dwFlags, int dwParam, String lpUserStr) {
        Timber.e("------OnAnyChatSnapShotEvent" + dwErrorCode);
//        Intent intent = new Intent();
//        String fileName = "IMG_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date(System
//                .currentTimeMillis())) + ".jpg";
//        String targetPath = FileUtils.getInstance().getDoorBellRecordFileSrc(fileName);
//        if (targetPath != null) {
//            boolean b = FileUtils.getInstance().moveFile(lpFileName, targetPath);
//            if (b) {
//                intent.putExtra("dwErrorCode", dwErrorCode);
//                intent.putExtra("targetPath", targetPath);
//                intent.putExtra("dwUserId", dwUserId);
//                intent.putExtra("dwFlags", dwFlags);
//                intent.putExtra("dwParam", dwParam);
//                intent.putExtra("lpUserStr", lpUserStr);
//                intent.setAction(ACTION_ANYCHAT_RECORD_EVENT);
//                intent.putExtra("type", TYPE_ANYCHAT_SNAP_SHOT);
//                sendBroadcast(intent);
//            }
//        }
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
