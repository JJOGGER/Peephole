package cn.jcyh.peephole.ui.fragment;

import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;

/**
 * Created by jogger on 2018/1/17.
 */

public class MainFragment extends BaseFragment {
    @BindView(R.id.tv_time)
    TextView tv_time;

    private static MainFragment sInstance;

    public static MainFragment getInstance(Bundle bundle) {
        if (sInstance == null)
            sInstance = new MainFragment();
        if (bundle != null)
            sInstance.setArguments(bundle);
        return sInstance;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void init() {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm");
        simpleDateFormat.format(new Date());
    }
}
