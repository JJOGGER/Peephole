package cn.jcyh.peephole.ui.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.ui.fragment.MainSetFragment;

public class SetActivity extends BaseActivity {

    private FragmentManager mFragmentManager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    public void init() {
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        MainSetFragment mainSetFragment = new MainSetFragment();
        transaction.replace(R.id.fl_container, mainSetFragment, MainSetFragment.class.getName());
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = mFragmentManager.findFragmentByTag(MainSetFragment.class.getName());
        if (fragment.isAdded() && !fragment.isVisible()) {
            mFragmentManager.beginTransaction().replace(R.id.fl_container,fragment).show(fragment).commit();
        } else {
            finish();
        }
    }
}
