package cn.jcyh.peephole.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import cn.jcyh.peephole.ui.fragment.MainFragment;
import cn.jcyh.peephole.ui.fragment.MenuFragment;

public class MainPageAdapter extends FragmentPagerAdapter {
    public MainPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return position == 0 ? new MainFragment() : new MenuFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }
}
