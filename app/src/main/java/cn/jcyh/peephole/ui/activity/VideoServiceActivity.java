package cn.jcyh.peephole.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.utils.ConstantUtil;

public class VideoServiceActivity extends BaseActivity {
    @BindView(R.id.tv_state)
    TextView tv_state;
    private int mRoomId;
    private int mUserId;
    private DoorBellControlCenter mControlCenter;


    @Override
    public int getLayoutId() {
        return R.layout.activity_video_service;
    }

    @Override
    protected void init() {
        mRoomId = getIntent().getExtras().getInt("roomId");
        mUserId = getIntent().getExtras().getInt("userId");
        tv_state.setText("正在与" + mUserId + "通话中");
        mControlCenter = DoorBellControlCenter.getInstance(this);
        mControlCenter.enterRoom(mRoomId, "");
    }

    @OnClick({R.id.btn_ring})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ring:
                break;
        }
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ConstantUtil.TYPE_ANYCHAT_ENTER_ROOM:
                    int dwErrorCode = intent.getIntExtra("dwErrorCode", -1);
                    if (dwErrorCode == 0) {
                        //进入房间成功，打开本地音视频

                    }
                    break;
                case ConstantUtil.ACTION_ANYCHAT_VIDEO_CALL_EVENT:
                    String type = intent.getStringExtra("type");
                    if (ConstantUtil.TYPE_BRAC_VIDEOCALL_EVENT_FINISH.equals(type)) {
                        tv_state.setText("正在连接...");
                    }
                    break;
            }
        }
    }
}
