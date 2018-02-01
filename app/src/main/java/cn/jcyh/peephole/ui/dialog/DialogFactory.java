package cn.jcyh.peephole.ui.dialog;

import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.base.BaseFragment;


/**
 * Created by jogger on 2017/3/21.
 * 对话框创建工厂
 */

public class DialogFactory {
    private static DialogFactory mDialogFactory;
    public static final int DIALOG_EDIT = 0X001;
    public static final int DIALOG_COMMON_PROGRESS = 0X002;
    public static final int DIALOG_COMMON_HINT = 0X003;//普通提示框
    public static final int DIALOG_CHANGE_PWD = 0X004;
    private FragmentTransaction mTransaction;
    private BaseDialogFragment mDialogFragment;
    private OnDialogListener mOnDialogListener;

    private DialogFactory() {
    }

    public static DialogFactory getDialogFactory() {
        if (mDialogFactory == null) {
            synchronized (DialogFactory.class) {
                if (mDialogFactory == null) {
                    mDialogFactory = new DialogFactory();
                }
            }
        }
        return mDialogFactory;
    }

    public DialogFactory create(BaseActivity activity, int dialogId, String content, String hintContent, int inputMaxLen) {
        mTransaction = activity.getSupportFragmentManager()
                .beginTransaction();
        if (createDialog(dialogId, content, hintContent, inputMaxLen)) return mDialogFactory;
        return null;
    }

    public DialogFactory create(BaseFragment fragment, int dialogId, String content, String hintContent, int inputMaxLen) {
        mTransaction = fragment.getChildFragmentManager()
                .beginTransaction();
        if (createDialog(dialogId, content, hintContent, inputMaxLen)) return mDialogFactory;
        return null;
    }

    private boolean createDialog(int dialogId, String content, String hintContent, int inputMaxLen) {
        switch (dialogId) {
            case DIALOG_COMMON_HINT:
                HintDialogFragmemt hintDialogFragmemt = new HintDialogFragmemt();
                if (!TextUtils.isEmpty(content))
                    hintDialogFragmemt.setHintContent(content);
                mDialogFragment = hintDialogFragmemt;
                mTransaction.add(mDialogFragment, "HintDialogFragmemt");
                return true;
        }
        return false;
    }

    public DialogFactory create(BaseActivity activity, int dialogId, String hintContent) {
        return create(activity, dialogId, "", hintContent, -1);
    }

    public DialogFactory create(BaseActivity activity, int dialogId) {
        return create(activity, dialogId, "", null, -1);
    }


    public DialogFactory create(BaseActivity activity, String content, int dialogId) {
        return create(activity, dialogId, content, null, -1);
    }

    public DialogFactory create(BaseFragment fragment, String content, int dialogId) {
        return create(fragment, dialogId, content, null, -1);
    }

    public DialogFactory create(BaseFragment fragment, int dialogId) {
        return create(fragment, dialogId, "", null, -1);
    }

    public DialogFactory create(BaseFragment fragment, int dialogId, String hintContent) {
        return create(fragment, dialogId, "", hintContent, -1);
    }

    public DialogFactory create(BaseFragment fragment, int dialogId, String hintContent, int inputMaxLen) {
        return create(fragment, dialogId, "", hintContent, inputMaxLen);
    }

    public DialogFactory setOnDialogListener(OnDialogListener onDialogListener) {
        mOnDialogListener = onDialogListener;
        return mDialogFactory;
    }

    public DialogFactory setCancelable(boolean cancelable) {
        mDialogFragment.setCancelable(cancelable);
        return mDialogFactory;
    }

    public BaseDialogFragment getDialog() {
        return mDialogFragment;
    }

    public void dismiss() {
        if (mDialogFragment != null && mDialogFragment.getDialog() != null
                && mDialogFragment.getDialog().isShowing()) {
            mDialogFragment.dismiss();
        }
    }

    public boolean isShowing() {
        return mDialogFragment != null && mDialogFragment.getDialog() != null &&
                mDialogFragment.getDialog().isShowing();
    }


    public void commit() {
        if (isShowing())
            return;
        mDialogFragment.setOnDialogListener(mOnDialogListener);
        mTransaction.commitAllowingStateLoss();
    }

}
