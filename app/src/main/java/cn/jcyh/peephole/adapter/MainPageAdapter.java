package cn.jcyh.peephole.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import cn.jcyh.peephole.ui.fragment.MainFragment;
import cn.jcyh.peephole.ui.fragment.MenuFragment;

/**
 * Created by jogger on 2018/1/17.
 */

public class MainPageAdapter extends FragmentPagerAdapter {
    public MainPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return position == 0 ? MainFragment.getInstance(null) : MenuFragment.getInstance(null);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
