package cn.jcyh.peephole.utils;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by it on 2017/2/21.
 */

public class ScreenUtil {
    /**
     * 获取屏幕宽度
     */
    public static int getSrceenWidth(Context context) {
        return context.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getSrceenHeight(Context context) {
        return context.getApplicationContext().getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取状态栏的高度
     * int resid = context.getResources().getIdentifier("icon_logo", "drawable", context
     * .getPackageName())//基于资源名称的字符串找到资源所对应的ID
     * iv.setImageResourece(resid);
     */
    public static int getStatusHeight(Context context) {
        int resid = context.getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resid > 0) {
            return context.getApplicationContext().getResources().getDimensionPixelSize(resid);//通过资源id获得资源所对应的值
        }
        return -1;
    }

    /**
     * 获取底部 navigation bar 高度
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getApplicationContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }
    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return
     */
    public static int sp2px(Context context, float spVal) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spVal * fontScale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2dp(Context context, float pxVal) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    /**
     * px转sp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2sp(Context context, float pxVal) {
        return (pxVal / context.getApplicationContext().getResources().getDisplayMetrics().scaledDensity);
    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, int dpValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
