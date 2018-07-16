package cn.jcyh.peephole.ui.activity;

import android.view.View;

import java.util.List;

import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.utils.T;

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
                List<User> bindUsers = DoorBellControlCenter.getInstance().getBindUsers();
                if (bindUsers != null && bindUsers.size() >= 5) {
                    T.show( R.string.bind_user_number_max);
                    return;
                }
                if (DoorBellControlCenter.sIsVideo) {
                    T.show( R.string.videoing_no_bind_msg);
                    return;
                }
                startNewActivity(BindActivity.class);
                finish();
                break;
        }
    }
}
