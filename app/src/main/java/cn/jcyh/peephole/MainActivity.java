package cn.jcyh.peephole;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.nimlib.sdk.misc.MiscService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.jcyh.eaglelock.api.MyLockAPI;
import cn.jcyh.eaglelock.api.MyLockCallback;
import cn.jcyh.peephole.adapter.MainPageAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.service.MainService;
import cn.jcyh.peephole.utils.L;

//按门铃，发消息--》app收到消息--》发起视频通话
public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    @BindView(R.id.vp_main)
    ViewPager vpMain;
    public static final int MSG_INSTALL_APK = 0;
    public static final int MSG_INSTALL_APK_RESULT = 2;
    private Messenger mService = null;
//    private ServiceConnection mConnection = new ServiceConnection() {//静默安装服务
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            L.e("------onServiceConnected");
//            mService = new Messenger(service);
//            installApk();
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            mService = null;
//        }
//    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    protected void init() {
//        if (!EventBus.getDefault().isRegistered(this))
//            EventBus.getDefault().register(this);
        startService(new Intent(this, MainService.class));
        vpMain.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
        vpMain.setOffscreenPageLimit(2);
        vpMain.addOnPageChangeListener(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        L.e("---->h:" + heightPixels + "---w:" + widthPixels);
        clearCache();
//        boolean siye = ControlCenter.getSN().startsWith(Constant.SIYE_SN);
//        if (siye) {
        siyeInit();
//        }
    }///storage/sdcard0

    /**
     * 四叶草
     */
    private void siyeInit() {
        MyLockAPI.init(this, new MyLockCallback(this));
        MyLockAPI lockAPI = MyLockAPI.getLockAPI();
        lockAPI.startBleService(this);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        lockAPI.startBTDeviceScan();
    }

    /**
     * 清理缓存
     */
    private void clearCache() {
        List<DirCacheFileType> fileTypes = new ArrayList<>();
        fileTypes.add(DirCacheFileType.LOG);
        fileTypes.add(DirCacheFileType.THUMB);
        fileTypes.add(DirCacheFileType.IMAGE);
        fileTypes.add(DirCacheFileType.AUDIO);
        NIMClient.getService(MiscService.class).clearDirCache(fileTypes, 0, 0);
    }


    @Override
    public void onBackPressed() {
        if (vpMain.getCurrentItem() == 1) {
            vpMain.setCurrentItem(0);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onSystemAction(DoorbellSystemAction systemAction) {
//        L.e("---------onSystemAction:"+systemAction.getType());
//        if (!DoorbellSystemAction.TYPE_DOORBELL_INSTALL_APK.equals(systemAction.getType())) return;
//        doBindService(this);
//    }

//    // 取消绑定到服务
//    private void doUnbindService(Context context) {
//        if (mService != null) {
//            mService = null;
//            if (context != null) {
//                context.unbindService(mConnection);
//            }
//        }
//    }

    // 绑定到服务
//    private boolean doBindService(Context context) {
//        boolean isbind = false;
//        if (context != null) {
//            //Intent intent = new Intent();
//            //intent.setComponent(new ComponentName("com.kphone.installcontrol",
//            //        "com.kphone.installcontrol.InstallService"));
//
//            Intent intent = new Intent();
//            intent.setAction("android.intent.action.apkcontrol");
//            intent.setPackage("com.kphone.installcontrol");
//            isbind = context.bindService(intent,
//                    mConnection, Context.BIND_AUTO_CREATE);
//        }
//        return isbind;
//    }
//
//    public void installApk() {
//        if (mService != null) {
//            Message msg = Message.obtain(null, MSG_INSTALL_APK);
//            msg.replyTo = new Messenger(new ResultHandler());
//            ;
//            Bundle data = new Bundle();
//            data.putString("path", APKUtil.APK_PATH);
//            data.putInt("flag", 0x00000002);
//            msg.setData(data);
//
//            try {
//                mService.send(msg);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * 处理程序安装结果
     *
     * @author fengmeiyin
     */
//    class ResultHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//
//            Bundle data = msg.getData();
//
//            switch (msg.what) {
//
//                case MSG_INSTALL_APK_RESULT:
//                    L.e("-------------MSG_INSTALL_APK_RESULT安装完成");
//                    doUnbindService(MainActivity.this);
//                    break;
////                case MSG_UNINSTALL_APK_RESULT:
////                    Log.d(TAG, "RST:" +  data.getString("basePackageName") + " returnCode:" + data.getInt("returnCode"));
////                    break;
//                default:
//                    super.handleMessage(msg);
//            }
//        }
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}
