package cn.jcyh.peephole;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.nimlib.sdk.misc.MiscService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.jcyh.peephole.adapter.MainPageAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.service.MainService;
import cn.jcyh.peephole.utils.APKUtil;
import cn.jcyh.peephole.utils.L;

//按门铃，发消息--》app收到消息--》发起视频通话
public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    @BindView(R.id.vp_main)
    ViewPager vpMain;
//    @BindView(R.id.iv_main)
//    ImageView ivMain;
//    @BindView(R.id.iv_menu)
//    ImageView ivMenu;

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
        L.e("----------------APK_PATH-" + APKUtil.APK_PATH + ":" + Environment.getDataDirectory().exists());
        startService(new Intent(this, MainService.class));
        vpMain.setAdapter(new MainPageAdapter(getSupportFragmentManager()));
        vpMain.setOffscreenPageLimit(2);
        vpMain.addOnPageChangeListener(this);
//        ivMain.setSelected(true);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        L.e("---->h:" + heightPixels + "---w:" + widthPixels);
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if(bluetoothAdapter != null){
//            bluetoothAdapter.enable();
//        }
        clearCache();
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
//        ivMain.setSelected(position == 0);
//        ivMenu.setSelected(position == 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

//    @Override
//    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        L.e("-------------onActivityResult" + resultCode + "---" + requestCode);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == REQEUST_CAPTURE_RING) {
////                mControlCenter.sendVideoCall();
//            } else if (requestCode == REQEUST_CAPTURE_ALARM) {
//
//            }
//        }
//    }
}
