package cn.jcyh.peephole.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.config.DoorbellConfig;
import cn.jcyh.peephole.control.BcManager;
import cn.jcyh.peephole.control.DoorBellControlCenter;
import cn.jcyh.peephole.http.HttpAction;
import cn.jcyh.peephole.http.IDataListener;
import cn.jcyh.peephole.ui.activity.CameraActivity;
import cn.jcyh.peephole.utils.LunarCalendar;
import cn.jcyh.peephole.utils.ToastUtil;
import timber.log.Timber;

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
    private String mDate;
    private static MainFragment sInstance;
    private MyHandler mHandler;
    private SimpleDateFormat mHmformat;
    private SimpleDateFormat mSimpleDateFormat;
    private Date mHourMinDate;
    private Date mSimpleDate;
    private TimeThread mTimeThread;
    private String mAmPm;
    private ProgressDialog mProgressDialog;
    private DoorbellConfig mDoorbellConfig;

    public static MainFragment getInstance(Bundle bundle) {
        if (sInstance == null)
            sInstance = new MainFragment();
        if (bundle != null)
            sInstance.setArguments(bundle);
        return sInstance;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void init() {
        mHourMinDate = new Date();
        mSimpleDate = new Date();
        mHmformat = new SimpleDateFormat("hh:mm");
        mSimpleDateFormat = new SimpleDateFormat("yyyy" + getString(R.string.year)
                + "MM" + getString(R.string.month) + "dd" + getString(R.string.day));
        mHandler = new MyHandler();
        mTimeThread = new TimeThread();
        mTimeThread.start();
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage(getString(R.string.waitting));
        mDoorbellConfig = DoorBellControlCenter.getInstance(mActivity).getDoorbellConfig();
    }

    @Override
    public void onResume() {
        super.onResume();
        long time = System.currentTimeMillis();
        mSimpleDate.setTime(time);
        mDate = mSimpleDateFormat.format(mSimpleDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int apm = calendar.get(Calendar.AM_PM);
        // apm=0 表示上午，apm=1表示下午。
        mAmPm = apm == 0 ? getString(R.string.am) : getString(R.string.pm);
        //星期
        setweek();
        // 农历月数组
        TypedArray a = getResources().obtainTypedArray(R.array.setting_month);
        int len0 = a.length();
        String[] lunarMonth = new String[len0];
        for (int i = 0; i < len0; i++) {
            lunarMonth[i] = getString(a.getResourceId(i, 0));
        }
        a.recycle();
        // 农历日数组
        TypedArray ar1 = getResources().obtainTypedArray(
                R.array.setting_ChinaDate);
        int len1 = ar1.length();
        String[] lunarDay = new String[len1];
        for (int i = 0; i < len1; i++) {
            lunarDay[i] = getString(ar1.getResourceId(i, 0));
        }
        ar1.recycle();
        // 系统时间
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // 农历时间换算
        int[] lunarDate = LunarCalendar.solarToLunar(year, month, day);
        String ChinaYear = lunarDate[0] + "";//得到农历年
        String ChinaMonth = lunarMonth[lunarDate[1] - 1];//得到农历月
        String ChinaDay = lunarDay[lunarDate[2] - 1];//得到农历日
        tvDate2.setText(ChinaYear + getString(R.string.year) + ChinaMonth + ChinaDay);
    }

    @OnClick({R.id.rl_media_record, R.id.rl_leave_message, R.id.rl_monitor_switch, R.id.rl_sos,
            R.id.iv_home})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_media_record:
                openAlbum();
                break;
            case R.id.rl_leave_message:
                Intent intent = new Intent(Intent.ACTION_VIEW);
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
                startNewActivity(CameraActivity.class);
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
            ToastUtil.showToast(mActivity, getString(R.string.no_sim_msg));
            return;
        }
        if (!TextUtils.isEmpty(mDoorbellConfig.getSosNumber())) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            Uri data = Uri.parse("tel:" + mDoorbellConfig.getSosNumber());
            intent.setData(data);
            startActivity(intent);
        } else {
            ToastUtil.showToast(mActivity, getString(R.string.no_sos_number));
        }
    }

    /**
     * 查询手机内系统应用
     */
    public String getAllApps() {
        String packagename = "";
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        PackageManager pManager = mActivity.getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            //判断系统预装的应用程序
            if (!((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0)) {
                apps.add(pak);
            }
        }
        for (int i = 0; i < apps.size(); i++) {
            PackageInfo pinfo = apps.get(i);
            //set Application Name
            String a = pManager.getApplicationLabel(pinfo.applicationInfo).toString();
            Timber.e("---------" + a);
            if (a.contains("图库")) {
                packagename = pinfo.packageName;
                break;
            }
        }
        return packagename;
    }

    private void switchMonitor() {
        final DoorBellControlCenter controlCenter = DoorBellControlCenter.getInstance(mActivity);
        final DoorbellConfig doorbellConfig = controlCenter.getDoorbellConfig();
        doorbellConfig.setMonitorSwitch(1 - doorbellConfig.getMonitorSwitch());
        HttpAction.getHttpAction(mActivity).setDoorbellConfig(DoorBellControlCenter.getIMEI(mActivity),
                doorbellConfig, new IDataListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        controlCenter.saveDoorbellConfig(doorbellConfig);
                        if (doorbellConfig.getMonitorSwitch() == 1) {
                            ToastUtil.showToast(mActivity, R.string.monitor_opened);
                        } else {
                            ToastUtil.showToast(mActivity, R.string.monitor_closed);
                        }
                        BcManager.getManager(mActivity).setPIRSensorOn(doorbellConfig.getMonitorSwitch()
                                == 1);
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        ToastUtil.showToast(mActivity, getString(R.string.set_failure) + errorCode);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    /**
     * 打开系统相册
     */
    public void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("vnd.android.cursor.dir/image");
        startActivity(intent);
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media
//                .EXTERNAL_CONTENT_URI);
//        Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.addCategory(Intent.ACTION_MAIN);
//        ComponentName componentName = new ComponentName("com.android.gallery3d", "com.android" +
//                ".gallery3d.app.GalleryActivity");
//        intent.setComponent(componentName);
//        startActivity(intent);
    }

    private void setweek() {
        // 系统时间
        Calendar time = Calendar.getInstance();
        int week = time.get(Calendar.DAY_OF_WEEK);
        setWeekString(week);
    }

    private void setWeekString(int index) {
        // 星期换算
        switch (index) {
            case 1:
                mDate += " " + getString(R.string.sunday);
                break;
            case 2:
                mDate += " " + getString(R.string.monday);
                break;
            case 3:
                mDate += " " + getString(R.string.tuesday);
                break;
            case 4:
                mDate += " " + getString(R.string.wednesday);
                break;
            case 5:
                mDate += " " + getString(R.string.thursday);
                break;
            case 6:
                mDate += " " + getString(R.string.friday);
                break;
            case 7:
                mDate += " " + getString(R.string.saturday);
                break;
        }
        tvDate.setText(mDate);
    }


    private class MyHandler extends Handler {
        private static final int WHAT = 0x001;
        private SpannableStringBuilder mSpannableString;
        private AbsoluteSizeSpan mSizeSpanTime;
        private AbsoluteSizeSpan mSizeSpanAmPm;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity == null || mActivity.isFinishing() || mActivity.getFragmentManager() ==
                    null)
                return;
            if (msg.what == WHAT && !TextUtils.isEmpty(mAmPm)) {
                mHourMinDate.setTime(System.currentTimeMillis());
                String time = mHmformat.format(mHourMinDate);
                if (mSpannableString == null) {
                    mSpannableString = new SpannableStringBuilder(time + " " + mAmPm);
                    mSizeSpanTime = new AbsoluteSizeSpan(40);
                    mSizeSpanAmPm = new AbsoluteSizeSpan(20);
                }
                mSpannableString.clear();
                mSpannableString.append(time).append(" ").append(mAmPm);
                mSpannableString.setSpan(mSizeSpanTime, 0, time.length(),
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                mSpannableString.setSpan(mSizeSpanAmPm, time.length(), time.length() + " ".length
                                () + mAmPm.length(),
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                if (tvTime != null)
                    tvTime.setText(mSpannableString, TextView.BufferType.SPANNABLE);
            }
        }
    }

    // 时间线程
    private class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    Thread.sleep(500);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mHandler.sendEmptyMessage(MyHandler.WHAT);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
