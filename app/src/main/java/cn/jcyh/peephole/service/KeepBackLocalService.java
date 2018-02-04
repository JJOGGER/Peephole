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

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatRecordEvent;
import com.bairuitech.anychat.AnyChatTransDataEvent;
import com.bairuitech.anychat.AnyChatUserInfoEvent;
import com.bairuitech.anychat.AnyChatVideoCallEvent;
import com.bairuitech.anychat.config.ConfigEntity;
import com.bairuitech.anychat.config.ConfigHelper;
import com.google.gson.Gson;
import com.szjcyh.mysmart.IMyAidlInterface;

import java.lang.ref.WeakReference;

import cn.jcyh.peephole.MainActivity;
import cn.jcyh.peephole.MyApp;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.bean.CommandJson;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.utils.ConstantUtil;
import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_BASE_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_LOGIN_RESULT_MSG;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_RECORD_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_TRANS_DATA_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_USER_INFO_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_ENTER_ROOM;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_FRIEND_STATUS;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_LINK_CLOSE;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_ONLINE_USER;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_RECORD;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_TRANS_BUFFER;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_USER_AT_ROOM;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_ANYCHAT_USER_INFO_UPDATE;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_FINISH;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_REPLY;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_REQUEST;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_START;


/**
 * Created by jogger on 2017/12/4.
 */

public class KeepBackLocalService extends Service implements AnyChatBaseEvent,
        AnyChatVideoCallEvent, AnyChatUserInfoEvent, AnyChatTransDataEvent, AnyChatRecordEvent {
    private MyBinder mBinder;
    private MyServiceConnection mConnection;
    private AnyChatCoreSDK mAnyChat;
    private boolean mFirstLogin;
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
        if (intent != null)
            mFirstLogin = intent.getBooleanExtra("firstLogin", true);
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
        mAnyChat.SetBaseEvent(KeepBackLocalService.this);//anyChat基本事件接口
        mAnyChat.SetUserInfoEvent(KeepBackLocalService.this);//更新设备信息
        mAnyChat.SetVideoCallEvent(KeepBackLocalService.this);//视频呼叫事件接口
        mAnyChat.SetTransDataEvent(KeepBackLocalService.this);//数据传输通知接口
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
    public void OnAnyChatUserInfoUpdate(int dwUserId, int dwType) {
        Timber.e("------OnAnyChatUserInfoUpdate" + dwUserId);
//        if (dwUserId == 0 && dwType == 0) {
//            DoorBellControlCenter.getInstance(getApplicationContext()).getFriendDatas();
// mOnFriendItem第一次在此取到值
//        }
        Intent intent = new Intent();
        intent.putExtra("dwUserId", dwUserId);
        intent.putExtra("dwType", dwType);
        intent.setAction(ACTION_ANYCHAT_USER_INFO_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_USER_INFO_UPDATE);
        sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatFriendStatus(int dwUserId, int dwStatus) {
        Timber.e("------OnAnyChatFriendStatus" + dwUserId);
//        DoorBellControlCenter.getInstance(getApplicationContext()).getFriendDatas();//重新获取好友数据
        Intent intent = new Intent();
        intent.putExtra("dwUserId", dwUserId);
        intent.putExtra("dwStatus", dwStatus);
        intent.setAction(ACTION_ANYCHAT_USER_INFO_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_FRIEND_STATUS);
        sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatVideoCallEvent(int dwEventType, int dwUserId, int dwErrorCode, int
            dwFlags, int dwParam, String userStr) {
        Timber.e("---------OnAnyChatVideoCallEvent");
        Intent intent = new Intent(ACTION_ANYCHAT_VIDEO_CALL_EVENT);
        intent.putExtra("dwUserId", dwUserId);
        intent.putExtra("dwErrorCode", dwErrorCode);
        intent.putExtra("dwFlags", dwFlags);
        intent.putExtra("dwParam", dwParam);
        String type = "";
        intent.putExtra("userStr", userStr);
        switch (dwEventType) {
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST:// < 呼叫请求
                Timber.e("----有人发呼叫请求过来了");
                type = TYPE_BRAC_VIDEOCALL_EVENT_REQUEST;
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY:// < 呼叫请求回复 开始向设备端发送视频请求
                Timber.e("------呼叫请求得到回复");
                type = TYPE_BRAC_VIDEOCALL_EVENT_REPLY;
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_START:// 视频呼叫会话开始事件
                Timber.e("-----视频呼叫会话开始事件");
                type = TYPE_BRAC_VIDEOCALL_EVENT_START;
                break;
            case AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH:// < 挂断（结束）呼叫会话
                Timber.e(" -------挂断（结束）呼叫会话");
                type = TYPE_BRAC_VIDEOCALL_EVENT_FINISH;
                break;
            default:
                Timber.i(" -------?????");
                break;
        }
        intent.putExtra("type", type);
        sendBroadcast(intent);
    }

    public static String sTargetPath = null;

    @Override
    public void OnAnyChatTransFile(int dwUserid, String FileName, String TempFilePath, int
            dwFileLength, int wParam, int lParam, int dwTaskId) {
        Timber.e("---------OnAnyChatTransFile" + lParam + "-->" + FileName + "--" + TempFilePath);
//        String targetPath = null;
//        String mediaImgSrc = FileUtils.getInstance().getMediaImgSrc();
//        switch (lParam) {
//            case 3:
//                //视频呼叫文件传输
//                targetPath = FileUtils.getInstance().getDoorBellRecordFileSrc(FileName);
//                break;
//            case 10:
//                //传输照片文件
//                if (mediaImgSrc != null)
//                    targetPath = FileUtils.getInstance().getMediaImgSrc()
//                            + File.separator + dwUserid + File.separator + FileName;
//                break;
//            case 11:
//                //传输视频文件
//                break;
//            case 12:
//                //传输视频缩略图文件
//                if (mediaImgSrc != null)
//                    targetPath = FileUtils.getInstance().getMediaVideoSrc()
//                            + File.separator + dwUserid + File.separator + FileName;
//                break;
//        }
//        Timber.e("----TempFilePath:" + TempFilePath + "---targetPath:" + targetPath);
//        if (targetPath != null) {
//            boolean b = FileUtils.getInstance().moveFile(TempFilePath, targetPath);
//            if (lParam == 3) {
//                sTargetPath = targetPath;
//            }
//            if (b) {
        Intent intent = new Intent();
        intent.putExtra("dwUserid", dwUserid);
        intent.putExtra("targetPath", TempFilePath);
        intent.putExtra("dwFileLength", dwFileLength);
        intent.putExtra("wParam", wParam);
        intent.putExtra("lParam", lParam);
        intent.putExtra("dwTaskId", dwTaskId);
        intent.setAction(ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intent.putExtra("type", ConstantUtil.TYPE_ANYCHAT_TRANS_FILE);
        sendBroadcast(intent);
//            }
//        }
    }

    @Override
    public void OnAnyChatTransBuffer(int dwUserid, byte[] lpBuf, int dwLen) {
        String result = new String(lpBuf, 0, lpBuf.length);
        Intent intent = new Intent();
        intent.putExtra("dwUserid", dwUserid);
        intent.putExtra("result", result);
        intent.setAction(ACTION_ANYCHAT_TRANS_DATA_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_TRANS_BUFFER);
        CommandJson commandJson = mGson.fromJson(result, CommandJson.class);
        intent.putExtra("command", commandJson);
        Timber.e("-----OnAnyChatTransBuffer" + result + "---dwUserid:" + dwUserid);
        sendBroadcast(intent);
//        if (result.contains("command")) {
//            try {
//                JSONObject jsonObject_all = new JSONObject(result);
//                JSONObject jsonObject_command = jsonObject_all.getJSONObject("command");
//                String type = jsonObject_command.getString("type");
//                switch (type) {
//                    case CHANGE_CAMERA:
//                        intent.putExtra("type2", CHANGE_CAMERA);
//                        String status = jsonObject_command.getString("status");
//                        if ("success".equals(status))
//                            ToastUtil.showToast(getApplicationContext(), R.string.change_succ);
//                        break;
//                    case LASTED_PICS_NAMES:
//                        intent.putExtra("type2", LASTED_PICS_NAMES);
//                        break;
//                    case MEDIA_FILE:
//                        intent.putExtra("type2", MEDIA_FILE);
//                        break;
//                    case VIDEO_NAMES:
//                        intent.putExtra("type2", VIDEO_NAMES);
//                        break;
//                    case VIDEO_THUNBNAIL:
//                        intent.putExtra("type2", VIDEO_THUNBNAIL);
//                        break;
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        sendBroadcast(intent);
//        if ("action:doorbell".equals(result) || result.contains("notification")) {
//            DoorBellBean doorBell = DoorBellControlCenter.getInstance(getApplicationContext())
// .getUserItemByUserId(dwUserid);
//            Timber.e("------->doorbell" + doorBell);
//            if (doorBell != null) {
//                Bundle bundle = new Bundle();
//                if (result.contains("notification")) {
//                    try {
//                        JSONObject jsonObject = new JSONObject(result);
//                        JSONObject jsonObject_Notification = jsonObject.getJSONObject
// ("notification");
//                        String type = jsonObject_Notification.getString("type");
//                        String trigger = jsonObject_Notification.getString(" trigger");
//                        Timber.e("----type:" + type + "---tri:" + trigger);
//                        if ("videoCall".equals(type)) {
//                            bundle.putString("trigger", jsonObject_Notification.getString("
// trigger"));
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//                //存在一种情况，app解绑但设备未解绑仍收得到此信息
//                bundle.putSerializable("doorBell", doorBell);
//                //发送请求图片的指令
////            String action = "action:imageRequest";
////            mAnyChat.TransBuffer(dwUserid, action.getBytes(), action.getBytes().length);
//                intent = new Intent(KeepBackLocalService.this, CallActivity.class);
//                intent.putExtras(bundle);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
////                intent = new Intent(KeepBackLocalService.this, CallActivity.class);
////                intent.putExtras(bundle);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                ActivityCollector.finishActivity(AddDoorBellActivity.class);
////                startActivity(intent);
//            }
//        }

    }

    @Override
    public void OnAnyChatTransBufferEx(int dwUserid, byte[] lpBuf, int dwLen, int wparam, int
            lparam, int taskid) {
        Timber.e("-----------OnAnyChatTransBufferEx");
    }

    @Override
    public void OnAnyChatSDKFilterData(byte[] lpBuf, int dwLen) {
        Timber.e("-----------OnAnyChatSDKFilterData");
    }

    @Override
    public void OnAnyChatConnectMessage(boolean bSuccess) {
        Timber.e("------OnAnyChatConnectMessage" + bSuccess);
    }

    @Override
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
        Timber.e("------OnAnyChatLoginMessage" + dwErrorCode);
        if (dwErrorCode == 0) {//登录成功
            Timber.i("-----anychat登录成功！客户端dwUserId:" + dwUserId);
        } else {
            Timber.i("-------anychat登录失败！错误码:" + dwErrorCode);
        }
        Intent intent = new Intent(ACTION_ANYCHAT_LOGIN_RESULT_MSG);
        intent.putExtra("dwErrorCode", dwErrorCode);
        sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
        Timber.e("------OnAnyChatEnterRoomMessage--->" + dwRoomId);
        Intent intent = new Intent();
        intent.putExtra("dwRoomId", dwRoomId);
        intent.putExtra("dwErrorCode", dwErrorCode);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_ENTER_ROOM);
        sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
        Timber.e("------OnAnyChatOnlineUserMessage");
        Intent intent = new Intent();
        intent.putExtra("dwUserNum", dwUserNum);
        intent.putExtra("dwRoomId", dwRoomId);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_ONLINE_USER);
        sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
        Timber.e("------OnAnyChatUserAtRoomMessage");
        Intent intent = new Intent();
        intent.putExtra("dwUserId", dwUserId);
        intent.putExtra("bEnter", bEnter);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_USER_AT_ROOM);
        sendBroadcast(intent);
    }

    @Override
    public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
        Timber.e("------OnAnyChatLinkCloseMessage" + dwErrorCode);
        Intent intent = new Intent();
        intent.putExtra("dwErrorCode", dwErrorCode);
        intent.setAction(ACTION_ANYCHAT_BASE_EVENT);
        intent.putExtra("type", TYPE_ANYCHAT_LINK_CLOSE);
        sendBroadcast(intent);
//        if (dwErrorCode != 209) {
//            final String uid = SharePreUtil.getInstance(getApplicationContext()).getString
// (ConstansUtil.UID, "");
//            connectAnyChat(uid);
//        }
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
