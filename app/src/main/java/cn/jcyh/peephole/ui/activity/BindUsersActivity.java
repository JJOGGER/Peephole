package cn.jcyh.peephole.ui.activity;

import android.app.ProgressDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.BindUsersAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.bean.User;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.HintDialogFragmemt;

//绑定用户列表
public class BindUsersActivity extends BaseActivity implements BindUsersAdapter.OnItemClickListener {
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    private List<User> mUsers;
    private BindUsersAdapter mAdapter;
    private DialogHelper mDialogHelper;
    private ProgressDialog mProgressDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind_users;
    }

    @Override
    protected void init() {
        super.init();
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new BindUsersAdapter();
        mAdapter.setOnItemClickListener(this);
        rvContent.setAdapter(mAdapter);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.waitting));
        mProgressDialog.show();
        HttpAction.getHttpAction(getApplicationContext()).getBindUsers(IMEI, new IDataListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                if (users != null) {
                    mAdapter.loadData(users);
                }
            }

            @Override
            public void onFailure(int errorCode) {
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }
        });

    }

    @OnClick(R.id.ibtn_back)
    public void onClick(View v) {
        finish();
    }

    @Override
    public void onItemClick(final User user, int pos) {
        if (mDialogHelper == null) {
            final HintDialogFragmemt hintDialogFragmemt = new HintDialogFragmemt();
            hintDialogFragmemt.setHintContent(getString(R.string.unbind_user_msg));
            hintDialogFragmemt.setOnHintDialogListener(new HintDialogFragmemt.OnHintDialogListener() {
                @Override
                public void onConfirm(boolean isConfirm) {
                    if (isConfirm) {
                        mProgressDialog.show();
                        unbindUser(user);
                    } else
                        hintDialogFragmemt.dismiss();
                }
            });
            mDialogHelper = new DialogHelper(this, hintDialogFragmemt);
        }
        mDialogHelper.commit();
    }

    /**
     * 解绑设备
     */
    private void unbindUser(User user) {
        // TODO: 2018/4/29 解绑设备
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialogHelper != null)
            mDialogHelper.dismiss();
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
