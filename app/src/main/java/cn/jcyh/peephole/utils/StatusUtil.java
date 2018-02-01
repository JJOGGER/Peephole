package cn.jcyh.peephole.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cn.jcyh.peephole.R;


/**
 * Created by it on 2017/2/21.
 */

public class StatusUtil {
    public void immersive(Activity activity, int immersiveColor) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(immersiveColor);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        View view = activity.findViewById(R.id.immersive);
        if (view != null) {
            int statusHeight = ScreenUtil.getStatusHeight(activity);
            if (statusHeight != -1) {
                view.setPadding(0, statusHeight, 0, 0);
            }
        }
    }

    public void setActivityFullScreen(Activity activity) {
        Window window = activity.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams
                .FLAG_FULLSCREEN);
    }
}
