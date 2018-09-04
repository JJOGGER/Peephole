package cn.jcyh.peephole.base;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.control.ActivityCollector;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.utils.StatusUtil;

/**
 * Created by jogger on 2018/1/10.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private static final int STATUS_COLOR = Color.parseColor("#3f000000");
    public String IMEI;
    private Unbinder mBind;
    private ProgressDialog mProgressDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        getWindow().setBackgroundDrawable(null);
        mBind = ButterKnife.bind(this);
        ActivityCollector.addActivity(this);
        //开启沉浸式状态栏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        StatusUtil statusUtil = new StatusUtil();
        if (isImmersive()) {
            statusUtil.immersive(this, immersiveColor());
        }
        if (isFullScreen()) {
            statusUtil.setActivityFullScreen(this);
        }
        IMEI = ControlCenter.getIMEI();
        init();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void startNewActivity(Class<? extends AppCompatActivity> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    public void startNewActivityForResult(Class<? extends AppCompatActivity> cls, int result) {
        Intent intent = new Intent(this, cls);
        startActivityForResult(intent, result);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void startNewActivityForResult(Class<? extends AppCompatActivity> cls, int result, String name, String value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(name, value);
        startActivityForResult(intent, result);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void startNewActivityForResult(Class<? extends AppCompatActivity> cls, int result, String name, int value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(name, value);
        startActivityForResult(intent, result);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

    public boolean isDialogShowing() {
        if (isFinishing() || getSupportFragmentManager() == null)
            return false;
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    public void showProgressDialog(String message) {
        if (isFinishing() || getSupportFragmentManager() == null)
            return;
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }
        mProgressDialog.setMessage(message);
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    public void cancelProgressDialog() {
        if (mProgressDialog == null) return;
        if (mProgressDialog.isShowing())
            mProgressDialog.cancel();
        mProgressDialog = null;
    }

    public void showProgressDialog() {
        if (isFinishing() || getSupportFragmentManager() == null)
            return;
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.waiting));
        }
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }


    public void startNewActivity(Class cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelProgressDialog();
        ActivityCollector.removeActivity(this);
//        Util.watch(this);
        mBind.unbind();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
