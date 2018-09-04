package cn.jcyh.peephole.ui.activity;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.entity.Version;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import io.netopen.hotbitmapgg.library.view.RingProgressBar;

public class SystemUpdateActivity extends BaseActivity {
    @BindView(R.id.rpb_update)
    RingProgressBar rpbUpdate;

    @Override
    public int getLayoutId() {
        return R.layout.activity_system_update;
    }

    @Override
    protected void init() {
        showProgressDialog(getString(R.string.check_update_msg));
        //检查版本
        HttpAction.getHttpAction().getVersion(new IDataListener<Version>() {
            @Override
            public void onSuccess(Version version) {

            }

            @Override
            public void onFailure(int errorCode, String desc) {

            }
        });
    }
}
