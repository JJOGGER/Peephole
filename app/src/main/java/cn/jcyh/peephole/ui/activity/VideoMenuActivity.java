package cn.jcyh.peephole.ui.activity;

import android.view.View;

import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;

public class VideoMenuActivity extends BaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_video_menu;
    }

    @OnClick({R.id.ibtn_back, R.id.tv_binded_user_list, R.id.tv_user_bind_window})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_back:
                finish();
                break;
            case R.id.tv_binded_user_list:
                startNewActivity(BindUsersActivity.class);
                break;
            case R.id.tv_user_bind_window:
//                List<NimUserInfo> bindUsers = ControlCenter.getUserManager().getBindUsers();
//                if (bindUsers.size() >= 5) {
//                    T.show(R.string.bind_user_number_max);
//                    return;
//                }
                startNewActivity(BindActivity.class);
                finish();
                break;
        }
    }
}
