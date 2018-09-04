package cn.jcyh.peephole.adapter;

import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.adapter.callback.OnSystemSettingListener;
import cn.jcyh.peephole.entity.SystemData;
import cn.jcyh.peephole.widget.UnScrollGridLayoutManager;

/**
 * Created by jogger on 2018/8/17.
 */
public class SystemSettingAdapter extends BaseQuickAdapter<String, BaseViewHolder> implements BaseQuickAdapter.OnItemClickListener {
    private OnSystemSettingListener mListener;

    public SystemSettingAdapter(@Nullable List<String> data, OnSystemSettingListener listener) {
        super(R.layout.rv_system_item, data);
        mListener = listener;
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tv_title, item);
        RecyclerView rvContent = helper.getView(R.id.rv_content);
        initContent(rvContent, item);
    }

    /**
     * 内容
     */
    private void initContent(RecyclerView recyclerView, String content) {
        List<SystemData> data = new ArrayList<>();
        if (content.equals(mContext.getString(R.string.wifi_network))) {
            String[] wifi_network = mContext.getResources().getStringArray(R.array.SETTING_WIFI_NETWORK);
            TypedArray array = mContext.getResources().obtainTypedArray(R.array.SETTING_WIFI_NETWORK_RES);
            int len = array.length();
            int[] wifi_network_res = new int[len];
            for (int i = 0; i < len; i++) {
                wifi_network_res[i] = array.getResourceId(i, 0);
            }
            array.recycle();
            for (int i = 0; i < wifi_network.length; i++) {
                SystemData systemData = new SystemData();
                systemData.setResID(wifi_network_res[i]);
                systemData.setName(wifi_network[i]);
                data.add(systemData);
            }
        } else if (content.equals(mContext.getString(R.string.device))) {
            String[] device = mContext.getResources().getStringArray(R.array.SETTING_DEVICE);
            TypedArray array = mContext.getResources().obtainTypedArray(R.array.SETTING_DEVICE_RES);
            int len = array.length();
            int[] device_res = new int[len];
            for (int i = 0; i < len; i++) {
                device_res[i] = array.getResourceId(i, 0);
            }
            array.recycle();

            for (int i = 0; i < device.length; i++) {
                SystemData systemData = new SystemData();
                systemData.setResID(device_res[i]);
                systemData.setName(device[i]);
                data.add(systemData);
            }
        } else if (content.equals(mContext.getString(R.string.personal))) {
            String[] personal = mContext.getResources().getStringArray(R.array.SETTING_PERSONAL);
            TypedArray array = mContext.getResources().obtainTypedArray(R.array.SETTING_PERSONAL_RES);
            int len = array.length();
            int[] personal_res = new int[len];
            for (int i = 0; i < len; i++) {
                personal_res[i] = array.getResourceId(i, 0);
            }
            array.recycle();
            for (int i = 0; i < personal.length; i++) {
                SystemData systemData = new SystemData();
                systemData.setResID(personal_res[i]);
                systemData.setName(personal[i]);
                data.add(systemData);
            }
        } else if (content.equals(mContext.getString(R.string.system))) {
            String[] system = mContext.getResources().getStringArray(R.array.SETTING_SYSTEM);
            TypedArray array = mContext.getResources().obtainTypedArray(R.array.SETTING_SYSTEM_RES);
            int len = array.length();
            int[] system_res = new int[len];
            for (int i = 0; i < len; i++) {
                system_res[i] = array.getResourceId(i, 0);
            }
            array.recycle();
            for (int i = 0; i < system.length; i++) {
                SystemData systemData = new SystemData();
                systemData.setResID(system_res[i]);
                systemData.setName(system[i]);
                data.add(systemData);
            }
        }

        SystemSettingItemAdapter systemSettingItemAdapter = new SystemSettingItemAdapter(data);
        recyclerView.setLayoutManager(new UnScrollGridLayoutManager(mContext, 2));
        recyclerView.setAdapter(systemSettingItemAdapter);
        systemSettingItemAdapter.notifyDataSetChanged();
        systemSettingItemAdapter.setOnItemClickListener(SystemSettingAdapter.this);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        SystemData systemData = (SystemData) adapter.getItem(position);
        if (systemData == null) return;
        String name = systemData.getName();
        if (TextUtils.isEmpty(name)) return;
        if (mListener == null) return;
        if (name.equals(mContext.getString(R.string.wlan))) {
            mListener.onWLANClick();
        } else if (name.equals(mContext.getString(R.string.bluetooth))) {
            mListener.onBluetoothClick();
        } else if (name.equals(mContext.getString(R.string.flow_use_desc))) {
            mListener.onFlowUseClick();
        } else if (name.equals(mContext.getString(R.string.more))) {
            mListener.onMoreClick();
        } else if (name.equals(mContext.getString(R.string.show))) {
            mListener.onShowClick();
        } else if (name.equals(mContext.getString(R.string.battery))) {
            mListener.onBatteryClick();
        } else if (name.equals(mContext.getString(R.string.storage))) {
            mListener.onStorageClick();
        } else if (name.equals(mContext.getString(R.string.app))) {
            mListener.onAppClick();
        } else if (name.equals(mContext.getString(R.string.language))) {
            mListener.onLanguageClick();
        }  else if (name.equals(mContext.getString(R.string.date_time))) {
            mListener.onDateTimeClick();
        }else if (name.equals(mContext.getString(R.string.system_update))){
            mListener.onSystemUpdateClick();
        }
    }
}
