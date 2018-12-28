package cn.jcyh.peephole.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.jcyh.peephole.R;

public class ChooseSetAdapter extends RecyclerView.Adapter<ChooseSetAdapter.MyViewHolder> {
    private List<String> mDatas;
    private int mCurrentPos;
    private OnItemClickListener mListener;

    public ChooseSetAdapter(List<String> datas) {
        mDatas = datas;
    }

    public interface OnItemClickListener {
        void onItemClick(String data, int pos);
        void onLongItemClick(String data, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public List<String> getData() {
        return mDatas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_dialog_choose_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        if (mDatas == null) return;
        holder.tvContent.setText(mDatas.get(position));
        holder.rbChecked.setChecked(false);
        if (mCurrentPos == position) {
            holder.rbChecked.setChecked(true);
        }
        holder.rlItem.setTag(position);
        holder.rlItem.setOnClickListener(null);
        holder.rlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int pos = (int) holder.rlItem.getTag();
                    mListener.onItemClick(mDatas.get(pos), pos);
                    setCheckedItem(pos);
                }
            }
        });
        holder.rlItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mListener != null) {
                    int pos = (int) holder.rlItem.getTag();
                    mListener.onLongItemClick(mDatas.get(pos), pos);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mDatas==null)return 0;
        return mDatas.size();
    }

    public void setCheckedItem(int position) {
        mCurrentPos = position;
        notifyDataSetChanged();
    }

    public int getPosition(String data) {
        if (mDatas == null) return -1;
        for (int i = 0; i < mDatas.size(); i++) {
            if (data.equals(mDatas.get(i)))
                return i;
        }
        return -1;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        RadioButton rbChecked;
        RelativeLayout rlItem;

        MyViewHolder(View itemView) {
            super(itemView);
            tvContent = (TextView) itemView.findViewById(R.id.tv_content);
            rbChecked = (RadioButton) itemView.findViewById(R.id.rb_checked);
            rlItem = (RelativeLayout) itemView.findViewById(R.id.rl_item);
        }
    }
}
