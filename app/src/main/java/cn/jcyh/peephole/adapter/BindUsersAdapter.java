package cn.jcyh.peephole.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.bean.User;

public class BindUsersAdapter extends RecyclerView.Adapter<BindUsersAdapter.MyViewHolder> {
    private List<User> mUsers;
    private OnItemClickListener mListener;
    public BindUsersAdapter(){
        mUsers=new ArrayList<>();
    }
    public interface OnItemClickListener {
        void onItemClick(User user, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void loadData(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_bind_users_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.tvUser.setText(mUsers.get(position).getAccount());
        holder.flItem.setTag(position);
        holder.flItem.setOnClickListener(null);
        holder.flItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int pos = (int) holder.flItem.getTag();
                    mListener.onItemClick(mUsers.get(pos), pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser;
        FrameLayout flItem;

        MyViewHolder(View itemView) {
            super(itemView);
            tvUser = (TextView) itemView.findViewById(R.id.tv_bind_users);
            flItem = (FrameLayout) itemView.findViewById(R.id.fl_item);
        }
    }
}