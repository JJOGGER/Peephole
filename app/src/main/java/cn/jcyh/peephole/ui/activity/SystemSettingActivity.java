package cn.jcyh.peephole.ui.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.SystemSettingAdapter;
import cn.jcyh.peephole.adapter.callback.OnSystemSettingListener;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.widget.MyLinearLayoutManager;

//系统设置自制界面
public class SystemSettingActivity extends BaseActivity implements OnSystemSettingListener {
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    private List<String> mTitles;

    @Override
    public int getLayoutId() {
        return R.layout.activity_system_setting;
    }

    @Override
    protected void init() {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        rvContent.setLayoutManager(new MyLinearLayoutManager(this));
        initTitle();
        rvContent.setAdapter(new SystemSettingAdapter(mTitles, this));
    }

    //    @Override
//    public boolean isImmersive() {
//        return false;
//    }
//
    @Override
    public boolean isFullScreen() {
        return false;
    }

    private void initTitle() {
        mTitles = new ArrayList<>();
        String[] titles = getResources().getStringArray(R.array.SETTINT_TITLE);
        Collections.addAll(mTitles, titles);
    }

    @Override
    public void onWLANClick() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onBluetoothClick() {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onFlowUseClick() {
        Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        ComponentName cName = new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");
        intent.setComponent(cName);
        startActivity(intent);
    }

    @Override
    public void onMoreClick() {
        Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onShowClick() {
        Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onBatteryClick() {
        Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(powerUsageIntent, 0);
        if (resolveInfo != null) {
            startActivity(powerUsageIntent);
        }
    }

    @Override
    public void onStorageClick() {
        Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onAppClick() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onLanguageClick() {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        startActivity(intent);
    }
//
//    @Override
//    public void onMarkResetClick() {
//        Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS );
//        startActivity(intent);
//    }

    @Override
    public void onDateTimeClick() {
        Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onSystemUpdateClick() {
        startNewActivity(SystemUpdateActivity.class);
    }

}
