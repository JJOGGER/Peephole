package cn.jcyh.peephole.ui.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.youth.banner.loader.ImageLoader;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.BcManager;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.activity.DoorbellLookActivity;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.L;
import cn.jcyh.peephole.utils.LunarCalendar;
import cn.jcyh.peephole.utils.T;

import static cn.jcyh.peephole.R.id.tv_time;


public class MainFragment extends BaseFragment {
    @BindView(tv_time)
    TextView tvTime;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.tv_date2)
    TextView tvDate2;
    @BindView(R.id.iv_home)
    ImageView ivHome;
    //    @BindView(R.id.banner)
//    Banner banner;
    private static MainFragment sInstance;
    private ProgressDialog mProgressDialog;
    private DoorbellConfig mDoorbellConfig;
    private MyReceiver mReceiver;

    public static MainFragment getInstance(Bundle bundle) {
        if (sInstance == null) {
            synchronized (MainFragment.class) {
                if (sInstance == null) {
                    sInstance = new MainFragment();
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
        new TimeThread(this).start();
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage(getString(R.string.waitting));
        mDoorbellConfig = DoorBellControlCenter.getInstance().getDoorbellConfig();
        mReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        mActivity.registerReceiver(mReceiver, intentFilter);
//        banner.setImageLoader(new GlideImageLoader());
//        banner.start();
//        banner.setVisibility(View.GONE);
        ivHome.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.rl_media_record, R.id.rl_leave_message, R.id.rl_monitor_switch, R.id.rl_sos,
            R.id.iv_home})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_media_record:
                openAlbum();
                break;
            case R.id.rl_leave_message:
                mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + FileUtil.getInstance().getDoorbellImgPath())));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.putExtra("type", 1);
                intent.setType("vnd.android.cursor.dir/video");
                startActivity(intent);
                break;
            case R.id.rl_monitor_switch:
                if (mProgressDialog.isShowing()) return;
                switchMonitor();
                break;
            case R.id.rl_sos:
                calSOS();
                break;
            case R.id.iv_home:
//                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机
//                startActivity(intent);
                if (!DoorBellControlCenter.sIsVideo)
                    startNewActivity(DoorbellLookActivity.class);
                break;
        }
    }

    private void calSOS() {
        //检查是否有手机卡可用
        TelephonyManager telMgr = (TelephonyManager)
                mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        if (!result) {
            T.show(getString(R.string.no_sim_msg));
            return;
        }
        if (!TextUtils.isEmpty(mDoorbellConfig.getSosNumber())) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            Uri data = Uri.parse("tel:" + mDoorbellConfig.getSosNumber());
            intent.setData(data);
            startActivity(intent);
        } else {
            T.show(getString(R.string.no_sos_number));
        }
    }

    private void switchMonitor() {
        final DoorBellControlCenter controlCenter = DoorBellControlCenter.getInstance();
        final DoorbellConfig doorbellConfig = controlCenter.getDoorbellConfig();
        doorbellConfig.setMonitorSwitch(1 - doorbellConfig.getMonitorSwitch());
        HttpAction.getHttpAction().setDoorbellConfig(DoorBellControlCenter.getIMEI(),
                doorbellConfig, new IDataListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        controlCenter.saveDoorbellConfig(doorbellConfig);
                        if (doorbellConfig.getMonitorSwitch() == 1) {
                            T.show(R.string.monitor_opened);
                        } else {
                            T.show(R.string.monitor_closed);
                        }
                        BcManager.getManager().setPIRSensorOn(doorbellConfig.getMonitorSwitch()
                                == 1);
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        T.show(getString(R.string.set_failure) + errorCode);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        mActivity.unregisterReceiver(mReceiver);
    }

    /**
     * 打开系统相册
     */
    public void openAlbum() {
        mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + FileUtil.getInstance().getDoorbellImgPath())));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra("type", 1);
        intent.setType("vnd.android.cursor.dir/image");
        startActivity(intent);
    }

    private static class MyHandler extends Handler {
        static final int WHAT = 0x001;
        private WeakReference<MainFragment> mWeakReference;

        private MyHandler(MainFragment fragment) {
            mWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainFragment mainFragment = mWeakReference.get();
            if (mainFragment == null || mainFragment.mActivity == null || mainFragment.mActivity.isFinishing() || mainFragment.mActivity.getFragmentManager() ==
                    null)
                return;
            if (msg.what != WHAT) return;
            Bundle data = msg.getData();
            if (data == null) return;
            try {
                CharSequence spannableString = data.getCharSequence("spannableString", "");
                if (mainFragment.tvTime != null)
                    mainFragment.tvTime.setText(spannableString, TextView.BufferType.SPANNABLE);
                if (mainFragment.tvDate != null)
                    mainFragment.tvDate.setText(data.getString("date", ""));
                if (mainFragment.tvDate2 != null)
                    mainFragment.tvDate2.setText(data.getString("lunarDate", ""));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    // 时间线程
    @SuppressWarnings("InfiniteLoopStatement")
    private static class TimeThread extends Thread {
        private WeakReference<MainFragment> mWeakReference;
        private SpannableStringBuilder mSpannableString;
        private AbsoluteSizeSpan mSizeSpanTime;
        private AbsoluteSizeSpan mSizeSpanAmPm;
        private Date mHourMinDate;
        private Date mSimpleDate;
        private String mDate;
        private SimpleDateFormat mHMformat;//时分转换
        private SimpleDateFormat mYMDFormat;//年月日转换
        private String mAmPm;
        private final Calendar mCalendar;
        private final String[] mLunarMonth;
        private final String[] mLunarDay;
        private boolean mStartTime = true;
        private MyHandler mHandler;
        private Bundle mHandleData;

        TimeThread(MainFragment fragment) {
            mWeakReference = new WeakReference<>(fragment);
            mHandler = new MyHandler(fragment);
            mHandleData = new Bundle();
            mHourMinDate = new Date();
            mSimpleDate = new Date();
            mHMformat = new SimpleDateFormat("hh:mm");
            mYMDFormat = new SimpleDateFormat("yyyy" + fragment.getString(R.string.year)
                    + "MM" + fragment.getString(R.string.month) + "dd" + fragment.getString(R.string.day));
            mCalendar = Calendar.getInstance();
            // 农历月数组
            TypedArray a = fragment.getResources().obtainTypedArray(R.array.setting_month);
            int len0 = a.length();
            mLunarMonth = new String[len0];
            for (int i = 0; i < len0; i++) {
                mLunarMonth[i] = fragment.getString(a.getResourceId(i, 0));
            }
            a.recycle();
            // 农历日数组
            TypedArray ar1 = fragment.getResources().obtainTypedArray(
                    R.array.setting_ChinaDate);
            int len1 = ar1.length();
            mLunarDay = new String[len1];
            for (int i = 0; i < len1; i++) {
                mLunarDay[i] = fragment.getString(ar1.getResourceId(i, 0));
            }
            ar1.recycle();
        }

        @Override
        public void run() {
            super.run();
            while (mStartTime) {
                MainFragment mainFragment = mWeakReference.get();
                if (mainFragment == null || mainFragment.mActivity == null || mainFragment.mActivity.isFinishing() || mainFragment.mActivity.getFragmentManager() ==
                        null) {
                    mStartTime = false;
                    return;
                }
                long time = System.currentTimeMillis();
                mCalendar.setTimeInMillis(time);
                int apm = mCalendar.get(Calendar.AM_PM);
                // apm=0 表示上午，apm=1表示下午。
                mAmPm = apm == 0 ? mainFragment.getString(R.string.am) : mainFragment.getString(R.string.pm);//获取上下午
                if (!TextUtils.isEmpty(mAmPm)) {
                    mHourMinDate.setTime(System.currentTimeMillis());
                    String hmTime = mHMformat.format(mHourMinDate);//获取时分
                    if (mSpannableString == null) {
                        mSpannableString = new SpannableStringBuilder(hmTime + " " + mAmPm);
                        mSizeSpanTime = new AbsoluteSizeSpan(40);
                        mSizeSpanAmPm = new AbsoluteSizeSpan(20);
                    }
                    mSpannableString.clear();
                    mSpannableString.append(hmTime).append(" ").append(mAmPm);
                    mSpannableString.setSpan(mSizeSpanTime, 0, hmTime.length(),
                            Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    mSpannableString.setSpan(mSizeSpanAmPm, hmTime.length(), hmTime.length() + " ".length() + mAmPm.length(),
                            Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    mHandleData.putCharSequence("spannableString", mSpannableString);
                }

                //设置年月日 星期
                mSimpleDate.setTime(time);
                mDate = mYMDFormat.format(mSimpleDate);//获取年月日
                int week = mCalendar.get(Calendar.DAY_OF_WEEK);
                // 星期换算
                switch (week) {
                    case Calendar.SUNDAY:
                        mDate += " " + mainFragment.getString(R.string.sunday);
                        break;
                    case Calendar.MONDAY:
                        mDate += " " + mainFragment.getString(R.string.monday);
                        break;
                    case Calendar.TUESDAY:
                        mDate += " " + mainFragment.getString(R.string.tuesday);
                        break;
                    case Calendar.WEDNESDAY:
                        mDate += " " + mainFragment.getString(R.string.wednesday);
                        break;
                    case Calendar.THURSDAY:
                        mDate += " " + mainFragment.getString(R.string.thursday);
                        break;
                    case Calendar.FRIDAY:
                        mDate += " " + mainFragment.getString(R.string.friday);
                        break;
                    case Calendar.SATURDAY:
                        mDate += " " + mainFragment.getString(R.string.saturday);
                        break;
                    default:
                        break;
                }
                mHandleData.putString("date", mDate);
                //计算农历
                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH) + 1;
                int day = mCalendar.get(Calendar.DAY_OF_MONTH);
                // 农历时间换算
                int[] lunarDate = LunarCalendar.solarToLunar(year, month, day);
                String ChinaYear = lunarDate[0] + "";//得到农历年
                String ChinaMonth = mLunarMonth[lunarDate[1] - 1];//得到农历月
                String ChinaDay = mLunarDay[lunarDate[2] - 1];//得到农历日
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // TODO: 2018/6/20 java.lang.IllegalStateException: Fragment MainFragment{32be3539} not attached to Activity
                mHandleData.putString("lunarDate", ChinaYear + mainFragment.getString(R.string.year) + ChinaMonth + ChinaDay);
                Message message = Message.obtain();
                message.what = MyHandler.WHAT;
                message.setData(mHandleData);
                mHandler.sendMessage(message);
            }
        }
    }

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
        public void displayImage(Context context, Object path, ImageView imageView) {
            //Glide 加载图片简单用法
            Glide.with(context).load(path).into(imageView);
        }

        //提供createImageView 方法，如果不用可以不重写这个方法，主要是方便自定义ImageView的创建
        @Override
        public ImageView createImageView(Context context) {
            return new ImageView(context);
        }
    }
}
