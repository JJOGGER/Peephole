package cn.jcyh.peephole.ui.activity;

import android.app.ProgressDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.BindUsersAdapter;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.User;
import cn.jcyh.peephole.event.NIMFriendAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.dialog.CommonEditDialog;
import cn.jcyh.peephole.ui.dialog.DialogHelper;
import cn.jcyh.peephole.ui.dialog.HintDialogFragmemt;
import cn.jcyh.peephole.ui.dialog.OnDialogListener;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.T;

//绑定用户列表
public class BindUsersActivity extends BaseActivity implements BindUsersAdapter.OnItemClickListener {
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    private BindUsersAdapter mAdapter;
    private DialogHelper mUnbindDialog;
    private DialogHelper mAdminBindHintDialog;
    private ProgressDialog mProgressDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bind_users;
    }

    @Override
    protected void init() {
        super.init();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new BindUsersAdapter();
        mAdapter.setOnItemClickListener(this);
        rvContent.setAdapter(mAdapter);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.waitting));
    }

    @Override
    protected void loadData() {
        super.loadData();
        ControlCenter.getUserManager().getUserSync(new IDataListener<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                Collections.sort(users);
                mAdapter.loadData(users);
            }

            @Override
            public void onFailure(int errorCode, String desc) {
                T.show(desc);
            }
        });
    }

    @OnClick(R.id.ibtn_back)
    public void onClick(View v) {
        finish();
    }

    @Override
    public void onItemClick(final User user, int pos) {
        if (mUnbindDialog == null) {
            final CommonEditDialog commonEditDialog = new CommonEditDialog();
            commonEditDialog.setTitle(getString(R.string.unbind_user));
            commonEditDialog.setHintContent(getString(R.string.input_auth_code));
            commonEditDialog.setOnDialogListener(new OnDialogListener() {
                @Override
                public void onConfirm(final Object content) {
                    L.e("-------user:" + user);
                    if (user.isAdmin()) {
                        if (mAdminBindHintDialog == null) {
                            HintDialogFragmemt hintDialogFragmemt = new HintDialogFragmemt();
                            hintDialogFragmemt.setHintContent(getString(R.string.unbind_admin_msg));
                            hintDialogFragmemt.setOnHintDialogListener(new HintDialogFragmemt.OnHintDialogListener() {
                                @Override
                                public void onConfirm(boolean isConfirm) {
                                    if (isConfirm) {
                                        unbindUser(user, content.toString());
                                    }
                                    mAdminBindHintDialog.dismiss();
                                }
                            });
                            mAdminBindHintDialog = new DialogHelper(BindUsersActivity.this, hintDialogFragmemt);
                        }
                        mAdminBindHintDialog.commit();
                    } else {
                        unbindUser(user, content.toString());
                    }

                }
            });
            mUnbindDialog = new DialogHelper(this, commonEditDialog);
        }
        CommonEditDialog dialogFragment = (CommonEditDialog) mUnbindDialog.getDialogFragment();
        if (dialogFragment != null)
            dialogFragment.setContent("");
        mUnbindDialog.commit();
    }

    /**
     * 解绑设备
     */
    private void unbindUser(User user, String code) {
        showProgressDialog();
        ControlCenter.getUserManager().unbindUser(user.getUserId(), IMEI, code, new IDataListener<Boolean>() {
            @Override
            public void onSuccess(Boolean b) {
                cancelProgressDialog();
                T.show(R.string.unbind_success);
                loadData();
            }

            @Override
            public void onFailure(int errorCode, String desc) {
                cancelProgressDialog();
                T.show(desc);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendUpdateAction(NIMFriendAction friendAction) {
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (mUnbindDialog != null)
            mUnbindDialog.dismiss();
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
