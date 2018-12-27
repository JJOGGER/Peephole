package cn.jcyh.peephole.ui.dialog;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import cn.jcyh.peephole.base.BaseActivity;


/**
 * Created by jogger on 2017/3/21.
 * 对话框创建
 */

public class DialogHelper {
    public static final int DIALOG_EDIT = 0X001;
    public static final int DIALOG_COMMON_PROGRESS = 0X002;
    public static final int DIALOG_COMMON_HINT = 0X003;//普通提示框
    public static final int DIALOG_CHANGE_PWD = 0X004;
    private BaseDialogFragment mDialogFragment;
    private FragmentManager mFragmentManager;

    private DialogHelper() {
    }

    public DialogHelper(BaseActivity activity, BaseDialogFragment dialogFragment) {
        mFragmentManager = activity.getSupportFragmentManager();
        mDialogFragment=dialogFragment;
//        switch (dialogId) {
//            case DIALOG_EDIT:
//                break;
//            case DIALOG_COMMON_PROGRESS:
//                mDialogFragment = new CommonProgressDialog();
//                break;
//            case DIALOG_COMMON_HINT:
//                mDialogFragment = new HintDialogFragmemt();
//                break;
//            case DIALOG_CHANGE_PWD:
//                break;
//        }
    }

    public BaseDialogFragment getDialogFragment() {
        return mDialogFragment;
    }

    public void dismiss() {
        if (mDialogFragment != null && mDialogFragment.getDialog() != null
                && mDialogFragment.getDialog().isShowing()) {
            mDialogFragment.dismissAllowingStateLoss();
        }
    }

    public boolean isShowing() {
        return mDialogFragment != null && mDialogFragment.getDialog() != null &&
                mDialogFragment.getDialog().isShowing();
    }


    public void commit() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (isShowing())
            return;
        Fragment fragmentByTag = mFragmentManager.findFragmentByTag(mDialogFragment.getClass().getSimpleName());
        if (!mDialogFragment.isAdded() && !mDialogFragment.isVisible() && fragmentByTag == null)
            transaction.add(mDialogFragment, mDialogFragment.getClass().getSimpleName());
        transaction.commitAllowingStateLoss();
    }

}
