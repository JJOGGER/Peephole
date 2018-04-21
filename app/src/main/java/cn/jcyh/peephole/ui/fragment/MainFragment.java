package cn.jcyh.peephole.ui.fragment;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseFragment;
import cn.jcyh.peephole.utils.LunarCalendar;

/**
 * Created by jogger on 2018/1/17.
 */

public class MainFragment extends BaseFragment {
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_am_pm)
    TextView tvAmPm;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.tv_date2)
    TextView tvDate2;
    @BindView(R.id.iv_home)
    ImageView ivHome;
    private final String IMAGE_TYPE = "image/*";
    private String mDate;
    private static MainFragment sInstance;
    private MyHandler mHandler;
    private SimpleDateFormat mHmformat;
    private SimpleDateFormat mSimpleDateFormat;
    private Date mHourMinDate;
    private Date mSimpleDate;
    private TimeThread mTimeThread;

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
        mHmformat = new SimpleDateFormat("h:mm");
        mSimpleDateFormat = new SimpleDateFormat("yyyy" + getString(R.string.year)
                + "MM" + getString(R.string.month) + "dd" + getString(R.string.day));
        mHandler = new MyHandler(this);
        mTimeThread = new TimeThread();
        mTimeThread.start();
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
        tvAmPm.setText(apm == 0 ? R.string.am : R.string.pm);
        tvDate.setText(mDate);
        //星期
        setweek();
        // 农历月数组
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
                break;
            case R.id.rl_monitor_switch:
                break;
            case R.id.rl_sos:
                break;
            case R.id.iv_home:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机
                startActivity(intent);
                break;
        }
    }

    /**
     * 打开系统相册
     */
//    public void openAlbum() {
////        FileUtil.getInstance().getSDCardPath()
//        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setDataAndType(Uri.fromFile(file), IMAGE_TYPE);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        mActivity.startActivity(intent);
//    }
    public void openAlbum() {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(IMAGE_TYPE);
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }
        startActivity(intent);
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


    private static class MyHandler extends Handler {
        static final int WHAT = 0x001;
        private WeakReference<MainFragment> mReference;

        MyHandler(MainFragment fragment) {
            mReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainFragment mainFragment = mReference.get();
            if (mainFragment.isRemoving() || mainFragment.getActivity() == null || mainFragment.getActivity().isFinishing())
                return;
            if (msg.what == WHAT) {
                mainFragment.mHourMinDate.setTime(System.currentTimeMillis());
                mainFragment.tvDate.setText(mainFragment.mHmformat.format(mainFragment.mHourMinDate));
            }
        }
    }

    // 时间线程
    public class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    Thread.sleep(500);
                    Message msg = new Message();
                    msg.what = MyHandler.WHAT;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
