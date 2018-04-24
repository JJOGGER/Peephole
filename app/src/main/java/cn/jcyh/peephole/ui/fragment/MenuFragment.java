package cn.jcyh.peephole.ui.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import java.util.Locale;

import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.ui.activity.BindActivity;
import cn.jcyh.peephole.ui.activity.SetActivity;
import cn.jcyh.peephole.ui.activity.VideoServiceActivity;

/**
 * Created by jogger on 2018/1/20.
 */

public class MenuFragment extends BaseFragment {
    private static MenuFragment sInstance;

    public static MenuFragment getInstance(Bundle bundle) {
        if (sInstance == null)
            sInstance = new MenuFragment();
        if (bundle != null)
            sInstance.setArguments(bundle);
        return sInstance;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_menu;
    }

    @OnClick({R.id.tv_doorbell_set, R.id.tv_system_set, R.id.tv_file_manager,
            R.id.tv_calendar, R.id.tv_browser, R.id.tv_video, R.id.tv_bind})
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.tv_doorbell_set:
                startNewActivity(SetActivity.class);
                break;
            case R.id.tv_system_set:
                intent.setAction(Settings.ACTION_SETTINGS);
                startActivity(intent);
                break;
            case R.id.tv_calendar:
                intent.setComponent(new ComponentName("com.android.calendar",
                        "com.android.calendar.LaunchActivity"));
                startActivity(intent);
                break;
            case R.id.tv_browser:
                intent.setAction("android.intent.action.VIEW");
                Uri content_url;
                if (isZh()) {
                    content_url = Uri.parse("http://www.baidu.com");
                } else {
                    content_url = Uri.parse("https://www.google.com");
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
            case R.id.tv_video:
                startNewActivity(VideoServiceActivity.class);
                break;
            case R.id.tv_bind:
                startNewActivity(BindActivity.class);
                break;
        }
    }

    private boolean isZh() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.endsWith("zh");
    }
}
