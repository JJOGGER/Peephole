package cn.jcyh.peephole;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import cn.jcyh.peephole.adapter.MainPageAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.service.KeepBackRemoteService;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.L;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_DOORBELL_SYSTEM_EVENT;

//按门铃，发消息--》app收到消息--》发起视频通话
public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    @BindView(R.id.vp_main)
    ViewPager vpMain;
    @BindView(R.id.iv_main)
    ImageView ivMain;
    @BindView(R.id.iv_menu)
    ImageView ivMenu;
    private static final int REQEUST_CAPTURE_RING = 0x001;
    private static final int REQEUST_CAPTURE_ALARM = 0x002;
    private MyReceiver mReceiver;
    private DoorBellControlCenter mControlCenter;
    private String mFilePath;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    protected void init() {
        mControlCenter = DoorBellControlCenter.getInstance();
        startService(new Intent(this, KeepBackRemoteService.class));
        vpMain.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
        vpMain.setOffscreenPageLimit(2);
        vpMain.addOnPageChangeListener(this);
        ivMain.setSelected(true);
        mReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DOORBELL_SYSTEM_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        L.e("---->h:" + heightPixels + "---w:" + widthPixels);
        List<String> sdCardPaths1 = FileUtil.getInstance().getSDCardPaths(getApplicationContext(), true);
        File file;
        if (sdCardPaths1 != null && sdCardPaths1.size() > 0) {
            file = new File(sdCardPaths1.get(0));
            L.e("--------file" + file.exists() + "--sdCardPaths1：" + sdCardPaths1);
        }
        List<String> sdCardPaths = FileUtil.getInstance().getSDCardPaths(getApplicationContext(), false);
        if (sdCardPaths != null && sdCardPaths.size() > 0) {
            file = new File(sdCardPaths.get(0));
            L.e("--------file" + file.exists() + "--sdCardPaths：" + sdCardPaths);
        }
        String sdCardPath = "/protect_s/prod_info";
        File file1 = new File(sdCardPath);
        searchFile(file1);
        HttpAction.getHttpAction().getBindUsers(IMEI, new IDataListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (users != null && users.size() > 0) {
                    DoorBellControlCenter.getInstance().saveBindUsers(users);
                }
                L.e("---user:" + users);
            }

            @Override
            public void onFailure(int errorCode) {

            }
        });
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if(bluetoothAdapter != null){
//            bluetoothAdapter.enable();
//        }
    }

    private void searchFile(File file) {
        if (file.isDirectory() && file.list() != null) {
            for (int i = 0; i < file.list().length; i++) {
                File file2 = new File(file.getAbsolutePath() + File.separator + file.list()[i]);
                searchFile(file2);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        ivMain.setSelected(position == 0);
        ivMenu.setSelected(position == 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isFinishing() || getSupportFragmentManager() == null || intent.getAction() == null)
                return;
//            switch (intent.getAction()) {
//                case ACTION_DOORBELL_SYSTEM_EVENT:
//                    String type = intent.getStringExtra("type");
//                    if (TYPE_DOORBELL_SYSTEM_RING.equals(type)) {
//                    } else if (TYPE_DOORBELL_SYSTEM_ALARM.equals(type)) {
//                        startNewActivityForResult(PictureActivity.class, REQEUST_CAPTURE_ALARM, "type", TYPE_DOORBELL_SYSTEM_ALARM);
//                    }
//                    break;
//            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        L.e("-------------onActivityResult" + resultCode + "---" + requestCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQEUST_CAPTURE_RING) {
//                mControlCenter.sendVideoCall();
            } else if (requestCode == REQEUST_CAPTURE_ALARM) {

            }
        }
    }
}
