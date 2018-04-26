package cn.jcyh.peephole.control;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import cn.jcyh.peephole.MainActivity;
import timber.log.Timber;

/**
 * Created by it on 2017/3/6.
 * 窗体管理工具
 */

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity :
                activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }


    public static void finishActivity(Class cls) {
        for (Activity activity :
                activities) {
            if (activity.getClass() == cls) {
                activity.finish();
                break;
            }
        }
    }

    public static void finishAllOnlyMain() {
        for (Activity act :
                activities) {
            if (!act.isFinishing() && !(act instanceof MainActivity)) {
                act.finish();
                Timber.i("--->" + act.getLocalClassName() + "isfinished");
            }
        }
    }
}
