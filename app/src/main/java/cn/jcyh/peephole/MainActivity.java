package cn.jcyh.peephole;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.jcyh.peephole.adapter.MainPageAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.User;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.HttpUrlIble;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.service.KeepBackRemoteService;
import cn.jcyh.peephole.ui.activity.PictureActivity;
import timber.log.Timber;

import static cn.jcyh.peephole.utils.ConstantUtil.ACTION_DOORBELL_SYSTEM_EVENT;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_DOORBELL_SYSTEM_ALARM;
import static cn.jcyh.peephole.utils.ConstantUtil.TYPE_DOORBELL_SYSTEM_RING;

//按门铃，发消息--》app收到消息--》发起视频通话
public class MainActivity extends BaseActivity {
    @BindView(R.id.vp_main)
    ViewPager vp_main;
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
    protected void init() {
        mControlCenter = DoorBellControlCenter.getInstance(this);
        startService(new Intent(this, KeepBackRemoteService.class));
        vp_main.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
        vp_main.setOffscreenPageLimit(2);
        mReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DOORBELL_SYSTEM_EVENT);
        registerReceiver(mReceiver, intentFilter);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        Timber.e("---->h:" + heightPixels + "---w:" + widthPixels);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isFinishing() || getSupportFragmentManager() == null || intent.getAction() == null)
                return;
            switch (intent.getAction()) {
                case ACTION_DOORBELL_SYSTEM_EVENT:
                    String type = intent.getStringExtra("type");
                    if (TYPE_DOORBELL_SYSTEM_RING.equals(type)) {
                        // TODO: 2018/2/4 获取绑定猫眼的用户列表
//  mControlCenter.sendVideoCall();
                        startNewActivityForResult(PictureActivity.class, REQEUST_CAPTURE_RING, "type", TYPE_DOORBELL_SYSTEM_RING);
                    } else if (TYPE_DOORBELL_SYSTEM_ALARM.equals(type)) {
                        startNewActivityForResult(PictureActivity.class, REQEUST_CAPTURE_ALARM, "type", TYPE_DOORBELL_SYSTEM_ALARM);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.e("-------------onActivityResult" + resultCode + "---" + requestCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQEUST_CAPTURE_RING) {
                //获取拍照的图片
                mFilePath = data.getStringExtra("filePath");
                Map<String, Object> params = new HashMap<>();
                params.put("sn", IMEI);
                params.put("type", 1);
                HttpAction.getHttpAction(this).sendPostImg(HttpUrlIble.UPLOAD_DOORBELL_ALARM_URL,
                        mFilePath, params, null);
                DoorbellConfig doorbellConfig = mControlCenter.getDoorbellConfig();
                HttpAction.getHttpAction(this).getBindUsers(IMEI, new IDataListener<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        if (users != null && users.size() != 0) {
                            //通知用户
                            mControlCenter.sendVideoCall(users, requestCode, mFilePath);
                        }
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        Timber.e("------errorCode" + errorCode);
                    }
                });
//                mControlCenter.sendVideoCall();
            } else if (requestCode == REQEUST_CAPTURE_ALARM) {

            }
        }
    }
}
