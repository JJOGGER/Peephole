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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.config.ConfigEntity;
import com.bairuitech.anychat.config.ConfigHelper;
import com.szjcyh.mysmart.IMyAidlInterface;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.jcyh.peephole.MainActivity;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.AnyChatTransDataEventAdapter;
import cn.jcyh.peephole.adapter.AnyChatUserInfoEventAdapter;
import cn.jcyh.peephole.adapter.AnyChatVideoCallEventAdapter;
import cn.jcyh.peephole.adapter.AnychatBaseEventAdapter;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.BcManager;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.receiver.AlarmReceiver;
import cn.jcyh.peephole.utils.FileUtil;
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
    private DoorBellControlCenter mControlCenter;
    private InputStreamReader mReader;
    private OutputStreamWriter mWriter;
    private SimpleDateFormat mSimpleDateFormat;
    private Date mDate;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mUserId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mDate = new Date(System
                .currentTimeMillis());
        mControlCenter = DoorBellControlCenter.getInstance(this);
        initConfig();
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
        try {
            File file = new File(FileUtil.getInstance().getSDCardPath() + File.separator +
                    "anychatlog.txt");
            if (file.exists()) {
                file.delete();
            }
            mWriter = new OutputStreamWriter(new
                    BufferedOutputStream(new FileOutputStream(file)));
            mReader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)));
            try {
                mWriter.write("-----oncreate");
                mWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        registerReceiver(mReceiver, intentFilter);
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mCount += 10;
                try {
                    mWriter.write("\n");
                    mDate.setTime(System.currentTimeMillis());
                    mWriter.write("------time:" + mSimpleDateFormat.format(mDate) +
                            "---------" + mCount);
                    mWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        mTimer.schedule(mTimerTask, 0, 10000);
    }

    private void initConfig() {
        //初始化配置
        BcManager manager = BcManager.getManager(getApplicationContext());
        DoorbellConfig doorbellConfig = mControlCenter.getDoorbellConfig();
        if (manager != null)
            manager.setPIRSensorOn(doorbellConfig.getMonitorSwitch() == 1);
        Timber.e("---------manager"+manager.getPIRSensorOn());
    }

    private int mCount;

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
        connectAnyChat();
        //注册闹钟广播
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intentAlarm, 0);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * 60, pi);
        try {
            mWriter.write("\n");
            mDate.setTime(System.currentTimeMillis());
            mWriter.write("------onstartcommand:" + mSimpleDateFormat.format(mDate) +
                    "---------" + mCount);
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    /**
     * 连接anychat
     */
    private void connectAnyChat() {
        String imei = DoorBellControlCenter.getIMEI(this);
        Timber.e("------imei:" + imei);
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
        try {
            mWriter.write("\n");
            mDate.setTime(System.currentTimeMillis());
            mWriter.write(mSimpleDateFormat.format(mDate) + "----->重新连接："
                    + imei);
            mWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            try {
                mWriter.write("\n");
                mDate.setTime(System.currentTimeMillis());
                mWriter.write(mSimpleDateFormat.format(mDate) + "-----LinkCloseMessage"
                        + dwErrorCode);
                mWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            connectAnyChat();
            //        if (dwErrorCode != 209) {
//        }
        }

        @Override
        public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
            super.OnAnyChatLoginMessage(dwUserId, dwErrorCode);
            mUserId = dwUserId;
            try {
                mWriter.write("\n");
                mDate.setTime(System.currentTimeMillis());
                mWriter.write(mSimpleDateFormat.format(mDate) + "----->OnAnyChatLoginMessage"
                        + dwErrorCode);
                mWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void OnAnyChatConnectMessage(boolean bSuccess) {
            super.OnAnyChatConnectMessage(bSuccess);
            try {
                mWriter.write("\n");
                mDate.setTime(System.currentTimeMillis());
                mWriter.write(mSimpleDateFormat.format(mDate) + "----->OnAnyChatConnectMessage"
                        + bSuccess);
                mWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Timber.e("---------ACTION_SCREEN_ON" + DoorBellControlCenter.sIsAnychatLogin);
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
        if (mWriter != null)
            try {
                mWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

}
