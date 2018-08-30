package cn.jcyh.peephole.thread;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.event.TimerUpdateAction;
import cn.jcyh.peephole.utils.LunarCalendar;
import cn.jcyh.peephole.utils.Util;

/**
 * Created by jogger on 2018/7/16.
 */
public class MainTimeThread extends Thread {
    private Date mHourMinDate;
    private Date mSimpleDate;
    private SpannableStringBuilder mSpannableString;
    private AbsoluteSizeSpan mSizeSpanTime;
    private AbsoluteSizeSpan mSizeSpanAmPm;
    private SimpleDateFormat mHMformat;//时分转换
    private SimpleDateFormat mYMDFormat;//年月日转换
    private final Calendar mCalendar;
    private final String[] mLunarMonth;
    private final String[] mLunarDay;
    private TimerUpdateAction mUpdateAction;
    private Context mContext;

    public MainTimeThread() {
        mUpdateAction = new TimerUpdateAction();
        mHourMinDate = new Date();
        mSimpleDate = new Date();
        mContext = Util.getApp();
        Locale locale = mContext.getResources().getConfiguration().locale;
        mHMformat = new SimpleDateFormat("hh:mm", locale);
        mYMDFormat = new SimpleDateFormat("yyyy" + mContext.getString(R.string.year)
                + "MM" + mContext.getString(R.string.month) + "dd" + mContext.getString(R.string.day), locale);
        mCalendar = Calendar.getInstance();
        // 农历月数组
        TypedArray a = mContext.getResources().obtainTypedArray(R.array.setting_month);
        int len0 = a.length();
        mLunarMonth = new String[len0];
        for (int i = 0; i < len0; i++) {
            mLunarMonth[i] = mContext.getString(a.getResourceId(i, 0));
        }
        a.recycle();
        // 农历日数组
        TypedArray ar1 = mContext.getResources().obtainTypedArray(
                R.array.setting_ChinaDate);
        int len1 = ar1.length();
        mLunarDay = new String[len1];
        for (int i = 0; i < len1; i++) {
            mLunarDay[i] = mContext.getString(ar1.getResourceId(i, 0));
        }
        ar1.recycle();
    }

    @Override
    public void run() {
        super.run();
        sendDate();
    }

    private void sendDate() {
        long time = System.currentTimeMillis();
        mCalendar.setTimeInMillis(time);
        int apm = mCalendar.get(Calendar.AM_PM);
        // apm=0 表示上午，apm=1表示下午。
        String amPm = apm == 0 ? mContext.getString(R.string.am) : mContext.getString(R.string.pm);
        if (!TextUtils.isEmpty(amPm)) {
            mHourMinDate.setTime(System.currentTimeMillis());
            String hmTime = mHMformat.format(mHourMinDate);//获取时分
            if (mSpannableString == null) {
                mSpannableString = new SpannableStringBuilder(hmTime + " " + amPm);
                mSizeSpanTime = new AbsoluteSizeSpan(40);
                mSizeSpanAmPm = new AbsoluteSizeSpan(20);
            }
            mSpannableString.clear();
            mSpannableString.append(hmTime).append(" ").append(amPm);
            mSpannableString.setSpan(mSizeSpanTime, 0, hmTime.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            mSpannableString.setSpan(mSizeSpanAmPm, hmTime.length(), hmTime.length() + " ".length() + amPm.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            mUpdateAction.putExtra(Constant.TIME_AM_PM, mSpannableString);
        }

        //设置年月日 星期
        mSimpleDate.setTime(time);
        String date = mYMDFormat.format(mSimpleDate);
        int week = mCalendar.get(Calendar.DAY_OF_WEEK);
        // 星期换算
        switch (week) {
            case Calendar.SUNDAY:
                date += " " + mContext.getString(R.string.sunday);
                break;
            case Calendar.MONDAY:
                date += " " + mContext.getString(R.string.monday);
                break;
            case Calendar.TUESDAY:
                date += " " + mContext.getString(R.string.tuesday);
                break;
            case Calendar.WEDNESDAY:
                date += " " + mContext.getString(R.string.wednesday);
                break;
            case Calendar.THURSDAY:
                date += " " + mContext.getString(R.string.thursday);
                break;
            case Calendar.FRIDAY:
                date += " " + mContext.getString(R.string.friday);
                break;
            case Calendar.SATURDAY:
                date += " " + mContext.getString(R.string.saturday);
                break;
            default:
                break;
        }
        mUpdateAction.putExtra(Constant.DATE, date);
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
        mUpdateAction.putExtra(Constant.LUNAR_DATE, ChinaYear + mContext.getString(R.string.year) + ChinaMonth + ChinaDay);
        EventBus.getDefault().post(mUpdateAction);
        sendDate();//重复获取
    }
}
