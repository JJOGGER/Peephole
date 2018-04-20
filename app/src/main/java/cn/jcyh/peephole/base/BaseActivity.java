package cn.jcyh.peephole.base;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import java.io.Serializable;

import butterknife.ButterKnife;
import cn.jcyh.peephole.utils.ConstantUtil;
import cn.jcyh.peephole.utils.SharePreUtil;
import cn.jcyh.peephole.utils.StatusUtil;

/**
 * Created by jogger on 2018/1/10.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final int STATUS_COLOR = Color.parseColor("#3f000000");
    public String IMEI;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
//        getWindow().setBackgroundDrawable(null);
        ButterKnife.bind(this);
        //开启沉浸式状态栏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        StatusUtil statusUtil = new StatusUtil();
        if (isImmersive()) {
            statusUtil.immersive(this, immersiveColor());
        }
        if (isFullScreen()) {
            statusUtil.setActivityFullScreen(this);
        }
        IMEI = SharePreUtil.getInstance(this).getString(ConstantUtil.IMEI, "");
        if (TextUtils.isEmpty(IMEI)) {
            String imei = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
            SharePreUtil.getInstance(this).getString(ConstantUtil.IMEI, imei);
        }
        init();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void init() {
    }

    protected void loadData() {
    }


    public abstract int getLayoutId();

    /**
     * 是否开启沉浸式状态栏
     */
    public boolean isImmersive() {
        return true;
    }

    /**
     * 状态栏颜色
     */
    public int immersiveColor() {
        return STATUS_COLOR;
    }


    /**
     * 是否全屏
     */
    public boolean isFullScreen() {
        return true;
    }

//    public void startNewActivity(Class cls) {
//        Intent intent = new Intent(this, cls);
//        startActivity(intent);
//    }

    public void startNewActivity(Class<? extends AppCompatActivity> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    public void startNewActivityForResult(Class<? extends AppCompatActivity> cls, int result) {
        Intent intent = new Intent(this, cls);
        startActivityForResult(intent, result);
    }


    /**
     * 弹出权限提示
     */

    public void startNewActivity(Class cls, String key, Object value) {
        Intent intent = new Intent(this, cls);
        if (value instanceof Integer) {
            intent.putExtra(key, (Integer) value);
        }
        if (value instanceof Integer[]) {
            intent.putExtra(key, (Integer[]) value);
        }
        if (value instanceof String) {
            intent.putExtra(key, (String) value);
        }
        if (value instanceof String[]) {
            intent.putExtra(key, (String[]) value);
        }
        if (value instanceof Boolean) {
            intent.putExtra(key, (Boolean) value);
        }
        if (value instanceof Byte) {
            intent.putExtra(key, (Byte) value);
        }
        if (value instanceof Byte[]) {
            intent.putExtra(key, (Byte[]) value);
        }
        if (value instanceof Serializable) {
            intent.putExtra(key, (Serializable) value);
        }

        if (value instanceof Serializable[]) {
            intent.putExtra(key, (Serializable[]) value);
        }
        if (value instanceof Parcelable) {
            intent.putExtra(key, (Parcelable) value);
        }
        if (value instanceof Parcelable[]) {
            intent.putExtra(key, (Parcelable[]) value);
        }
        if (value instanceof Float[]) {
            intent.putExtra(key, (Float[]) value);
        }
        startActivity(intent);
    }


    public void startNewActivity(Class cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
