package cn.jcyh.peephole.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.entity.User;

public class BindUsersAdapter extends BaseQuickAdapter<User, BaseViewHolder> {

    public BindUsersAdapter(@Nullable List<User> data) {
        super(R.layout.rv_bind_users_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, User user) {
        helper.getView(R.id.tv_admin).setVisibility(user.isAdmin() ? View.VISIBLE : View.GONE);
        helper.setText(R.id.tv_bind_users, user.getNickname() + "(" + user.getUserName() + ")");
    }
}
