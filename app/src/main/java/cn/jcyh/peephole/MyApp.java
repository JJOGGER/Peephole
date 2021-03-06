package cn.jcyh.peephole;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.util.NIMUtil;

import cn.jcyh.peephole.event.online.OnlineStateEventManager;
import cn.jcyh.peephole.utils.Util;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NIMClient.init(this, null, null);
        if (NIMUtil.isMainProcess(this)) {
            Util.init(this);
            // 初始化在线状态事件
            OnlineStateEventManager.init();
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
