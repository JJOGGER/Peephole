package cn.jcyh.peephole.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.entity.SystemData;

/**
 * Created by jogger on 2018/8/17.
 */
public class SystemSettingItemAdapter extends BaseQuickAdapter<SystemData, BaseViewHolder> {
     SystemSettingItemAdapter(@Nullable List<SystemData> data) {
        super(R.layout.rv_system_item_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SystemData item) {
        helper.setText(R.id.tv_name, item.getName());
        helper.setImageResource(R.id.iv_icon, item.getResID());
    }
}
