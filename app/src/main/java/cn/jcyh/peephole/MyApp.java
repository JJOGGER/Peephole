package cn.jcyh.peephole;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import cn.jcyh.peephole.utils.SystemUtil;
import cn.jcyh.peephole.utils.Util;


public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Util.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static boolean inMainProcess(Context context) {
        String packageName = context.getPackageName();
        String processName = SystemUtil.getProcessName(context);
        return packageName.equals(processName);
    }

}
