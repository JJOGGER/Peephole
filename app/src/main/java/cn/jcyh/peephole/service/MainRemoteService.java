package cn.jcyh.peephole.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.szjcyh.mysmart.IMyAidlInterface;

import cn.jcyh.peephole.constant.Constant;


/**
 * Created by jogger on 2017/12/4.
 */

public class MainRemoteService extends Service {
    private MyBinder mBinder;
    private MyServiceConnection mConnection;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mBinder == null) mBinder = new MyBinder();
        if (mConnection == null) mConnection = new MyServiceConnection();
        if (intent != null && intent.getIntExtra(Constant.FLAG, 0) != 1) {
            //表示该服务正常被调用了startService,此时KeepBackLocalService没运行
            Intent mIntent = new Intent(this, MainService.class);
            bindService(mIntent, mConnection, Context.BIND_IMPORTANT);
        }
        return START_STICKY;
    }

    private class MyBinder extends IMyAidlInterface.Stub {

        @Override
        public void dealThings() {
        }
    }


    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                iMyAidlInterface.dealThings();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //重新启动
            Intent intent = new Intent(MainRemoteService.this, MainService.class);
            intent.putExtra(Constant.FLAG, 1);
            startService(intent);
            bindService(intent, mConnection, Context.BIND_IMPORTANT);
        }
    }
}
