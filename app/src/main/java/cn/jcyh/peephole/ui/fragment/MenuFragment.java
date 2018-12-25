package cn.jcyh.peephole.ui.fragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.ui.activity.AboutActivity;
import cn.jcyh.peephole.ui.activity.BindActivity;
import cn.jcyh.peephole.ui.activity.SetActivity;
import cn.jcyh.peephole.ui.activity.SystemSettingActivity;
import cn.jcyh.peephole.ui.activity.VideoServiceActivity;
import cn.jcyh.peephole.utils.Tool;

/**
 * Created by jogger on 2018/1/20.
 */

public class MenuFragment extends BaseFragment {
    private static final String BAIDU = "http://www.baidu.com";
    private static final String GOOGLE = "https://www.google.com";
    @SuppressLint("StaticFieldLeak")
    private static MenuFragment sInstance;


    public static MenuFragment getInstance(Bundle bundle) {
        if (sInstance == null) {
            synchronized (MenuFragment.class) {
                if (sInstance == null) {
                    sInstance = new MenuFragment();
                    if (bundle != null)
                        sInstance.setArguments(bundle);
                }
            }
        }
        return sInstance;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_menu;
    }

    @OnClick({R.id.tv_doorbell_set, R.id.tv_system_set, R.id.tv_file_manager,/*R.id.tv_abc,*/
            R.id.tv_calendar, R.id.tv_browser, R.id.tv_video, R.id.tv_bind, R.id.tv_about})
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.tv_doorbell_set:
                startNewActivity(SetActivity.class);
                break;
            case R.id.tv_system_set:
                startNewActivity(SystemSettingActivity.class);
//                intent.setAction(Settings.ACTION_SETTINGS);
//                startActivity(intent);
                break;
            case R.id.tv_calendar:
                intent.setComponent(new ComponentName("com.android.calendar",
                        "com.android.calendar.LaunchActivity"));
                startActivity(intent);

                break;
            case R.id.tv_browser:
                intent.setAction(Intent.ACTION_VIEW);
                Uri content_url;
                if (Tool.isZh()) {
                    content_url = Uri.parse(BAIDU);
                } else {
                    content_url = Uri.parse(GOOGLE);
                }
                intent.setData(content_url);
                startActivity(intent);
                break;
            case R.id.tv_file_manager:
                ComponentName componentName = new ComponentName("com.mediatek.filemanager",
                        "com.mediatek.filemanager.FileManagerOperationActivity");
                intent.setComponent(componentName);
                startActivity(intent);
                break;
            /*case R.id.tv_abc:
                intent.setComponent(new ComponentName("com.wualo7.iclass.phone",
                        "air.com.alo7.iclass.phone.AppEntry"));
                startActivity(intent);
                break;*/
            case R.id.tv_video:
                startNewActivity(VideoServiceActivity.class);
                break;
            case R.id.tv_bind:
                startNewActivity(BindActivity.class);
                break;
            case R.id.tv_about:
                startNewActivity(AboutActivity.class);
                break;
        }
    }
}
