package cn.jcyh.peephole.ui.fragment;

import android.os.Bundle;
import android.view.View;

import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.ui.activity.BindActivity;
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

    @OnClick({R.id.tv_doorbell_set, R.id.tv_video, R.id.tv_bind})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_doorbell_set:
                break;
            case R.id.tv_video:
                startNewActivity(VideoServiceActivity.class);
                break;
            case R.id.tv_bind:
                startNewActivity(BindActivity.class);
                break;
        }
    }

}
