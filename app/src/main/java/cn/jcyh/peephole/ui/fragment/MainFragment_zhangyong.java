package cn.jcyh.peephole.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding2.view.RxView;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.AdvertData;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.NetworkAction;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.activity.BannerDescActivity;
import cn.jcyh.peephole.ui.activity.DoorbellLookActivity;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.T;
import cn.jcyh.peephole.widget.MsgCircleView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class MainFragment_zhangyong extends BaseFragment {
    private static final String IMAGE_DIR = "vnd.android.cursor.dir/image";
    private static final String VIDEO_DIR = "vnd.android.cursor.dir/video";
    //    @BindView(tv_time)
//    TextView tvTime;
//    @BindView(R.id.tv_date)
//    TextView tvDate;
//    @BindView(R.id.tv_date2)
//    TextView tvDate2;
//    @BindView(R.id.iv_home)
//    ImageView ivHome;
    @BindView(R.id.tv_monitor_switch)
    TextView tvMonitorSwitch;
    @BindView(R.id.banner)
    Banner mBanner;
    @BindView(R.id.tv_media_record_msg)
    MsgCircleView tvMediaRecordMsg;
    @BindView(R.id.tv_leave_message_msg)
    MsgCircleView tvLeaveMessageMsg;
    @SuppressLint("StaticFieldLeak")
    private static MainFragment_zhangyong sInstance;
    private ProgressDialog mProgressDialog;
    private DoorbellConfig mDoorbellConfig;
    private MyReceiver mReceiver;
    private Disposable mSubscribe;

    public static MainFragment_zhangyong getInstance(Bundle bundle) {
        if (sInstance == null) {
            synchronized (MainFragment_zhangyong.class) {
                if (sInstance == null) {
                    sInstance = new MainFragment_zhangyong();
                    if (bundle != null)
                        sInstance.setArguments(bundle);
                }
            }
        }
        return sInstance;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void init() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage(getString(R.string.waitting));
        mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        mReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        mActivity.registerReceiver(mReceiver, intentFilter);
        mBanner.setImageLoader(new GlideImageLoader());
//        banner.start();
//        banner.setVisibility(View.GONE);
//        ivHome.setVisibility(View.VISIBLE);
        //特殊处理的点击按钮
        mSubscribe = RxView.clicks(tvMonitorSwitch)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        switchMonitor();
                    }
                });
        initBanners();
    }

    private void initBanners() {
        HttpAction.getHttpAction().getBanners(new IDataListener<AdvertData>() {
            @Override
            public void onSuccess(AdvertData advertData) {
                L.e("-------------advertData:" + advertData);
                List<AdvertData.Advert> adverts = advertData.getAdverts();
                mBanner.setImages(adverts);
                mBanner.setDelayTime(advertData.getAdvertConfig().getDisplayTime() * 1000);
                mBanner.start();
            }

            @Override
            public void onFailure(int errorCode, String desc) {
                L.e("-------------onFailure");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        int doorbellMediaCount = ControlCenter.getDoorbellManager().getDoorbellMediaCount();
        int doorbellLeaveMsgCount = ControlCenter.getDoorbellManager().getDoorbellLeaveMsgCount();
        tvMediaRecordMsg.setVisibility(doorbellMediaCount > 0 ? View.VISIBLE : View.GONE);
        tvLeaveMessageMsg.setVisibility(doorbellLeaveMsgCount > 0 ? View.VISIBLE : View.GONE);
        tvMediaRecordMsg.setText(String.valueOf(doorbellMediaCount));
        tvLeaveMessageMsg.setText(String.valueOf(doorbellLeaveMsgCount));
    }

    @OnClick({R.id.fl_media_record, R.id.fl_leave_message,
//            R.id.tv_sos_number,
            R.id.tv_doorbell_look})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_media_record:
                ControlCenter.getDoorbellManager().setDoorbellMediaCount(0);
                openAlbum();
                break;
            case R.id.fl_leave_message:
                //清空本地未查看记录
                ControlCenter.getDoorbellManager().setDoorbellLeaveMsgCount(0);
                mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + FileUtil.getDoorbellImgPath())));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.putExtra(Constant.TYPE, 2);
                intent.setType(VIDEO_DIR);
                startActivity(intent);
                break;
//            case R.id.tv_sos_number:
//                if (!PhoneUtil.isPhoneCallable()) {
//                    T.show(getString(R.string.no_sim_msg));
//                    return;
//                }
//                mDoorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
//                if (!TextUtils.isEmpty(mDoorbellConfig.getSosNumber())) {
//                    if (!PhoneNumberUtils.isGlobalPhoneNumber(mDoorbellConfig.getSosNumber())) {
//                        T.show(R.string.phone_no_regex);
//                        return;
//                    }
//                    PhoneUtil.callPhone(mDoorbellConfig.getSosNumber());
//                } else {
//                    T.show(getString(R.string.no_sos_number));
//                }
//                break;
            case R.id.tv_doorbell_look:
//                try{
//                    //获取相机包名
//                    Intent infoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    ResolveInfo res = mActivity.getPackageManager().
//                            resolveActivity(infoIntent, 0);
//                    if (res != null)
//                    {
//                        String packageName=res.activityInfo.packageName;
//                        if(packageName.equals("android"))
//                        {
//                            infoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
//                            res = mActivity.getPackageManager().
//                                    resolveActivity(infoIntent, 0);
//                            if (res != null)
//                                packageName=res.activityInfo.packageName;
//                        }
//                        //启动相机
//                        startApplicationByPackageName(packageName);
//                    }
//                }
//                catch(Exception e)
//                {
//                    e.printStackTrace();
//                }
                startNewActivity(DoorbellLookActivity.class);
                break;
        }
    }

    private void switchMonitor() {
        final DoorbellConfig doorbellConfig = ControlCenter.getDoorbellManager().getDoorbellConfig();
        doorbellConfig.getDoorbellSensorParam().setMonitor(1 - doorbellConfig.getDoorbellSensorParam().getMonitor());
        int monitor = doorbellConfig.getDoorbellSensorParam().getMonitor();
        if (monitor == 1) {
            T.show(R.string.monitor_opened);
        } else {
            T.show(R.string.monitor_closed);
        }
        ControlCenter.getBCManager().setPIRSensorOn(monitor== 1);
        ControlCenter.getDoorbellManager().setDoorbellConfig(doorbellConfig);
        ControlCenter.getDoorbellManager().setDoorbellConfig2Server(ControlCenter.getSN(),
                doorbellConfig, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        mActivity.unregisterReceiver(mReceiver);
        mSubscribe.dispose();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    /**
     * 打开系统相册
     */
    public void openAlbum() {
        mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + FileUtil.getDoorbellImgPath())));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Constant.TYPE, 1);
        intent.setType(IMAGE_DIR);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkAction(NetworkAction networkAction) {
        if (NetworkAction.TYPE_NETWORK_CONNECTED.equals(networkAction.getType())) {
            //
            initBanners();
        }
    }

    //通过包名启动应用
    private void startApplicationByPackageName(String packName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = mActivity.getPackageManager().getPackageInfo(packName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == packageInfo) {
            return;
        }
        Intent resolveIntent = new Intent();
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageInfo.packageName);
        List<ResolveInfo> resolveInfoList = mActivity.getPackageManager().queryIntentActivities(resolveIntent, 0);
        if (null == resolveInfoList) {
            return;
        }
        Iterator<ResolveInfo> iter = resolveInfoList.iterator();
        while (iter.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo) iter.next();
            if (null == resolveInfo) {
                return;
            }
            String packageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            startActivity(intent);
        }//while
    }//method

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                L.e("-----ACTION_SCREEN_ON");
//                HttpAction.getHttpAction().getADPictures(new IDataListener<List<Advert>>() {
//                    @Override
//                    public void onSuccess(List<Advert> adverts) {
//                        if (adverts == null || adverts.size() == 0) return;
//                        ivHome.setVisibility(View.GONE);
//                        List<String> imgUrls = new ArrayList<>();
//                        for (int i = 0; i < adverts.size(); i++) {
//                            imgUrls.add(adverts.get(i).getPicUrl());
//                        }
//                        banner.setImages(imgUrls);
//                        banner.setDelayTime(adverts.get(0).getTimer());
//                    }
//
//                    @Override
//                    public void onFailure(int errorCode) {
//
//                    }
//                });
            }
        }
    }

    public class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, final Object path, ImageView imageView) {
            //Glide 加载图片简单用法
            Glide.with(context).
                    load(((AdvertData.Advert) path).getImageUrl())
                    .error(R.mipmap.no_banner)
                    .into(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startNewActivity(BannerDescActivity.class, Constant.URL, ((AdvertData.Advert) path).getUrl());
                }
            });
        }//提供createImageView 方法，如果不用可以不重写这个方法，主要是方便自定义ImageView的创建

        @Override
        public ImageView createImageView(Context context) {
            return new ImageView(context);
        }
    }
}
