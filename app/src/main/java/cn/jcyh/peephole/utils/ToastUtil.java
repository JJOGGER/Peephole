package cn.jcyh.peephole.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

/**
 * Created by it on 2017/2/22.
 * 土司工具
 */

public class ToastUtil {
    private static Toast toast;
    private static Toast customToast;

    public static void showToast(Context context, String str) {
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), str, Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }

    public static void showToast(Context context, int str) {
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(),
                    str, Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }

    public static void showFullToast(Context context, int layoutId) {
        View view = LayoutInflater.from(context.getApplicationContext()).inflate(layoutId, null);
        if (customToast == null) {
            customToast = Toast.makeText(context.getApplicationContext(),
                    null, Toast.LENGTH_SHORT);
        }
        customToast.setGravity(Gravity.FILL, 0, 0);
        customToast.setView(view);
        customToast.show();
    }
}
