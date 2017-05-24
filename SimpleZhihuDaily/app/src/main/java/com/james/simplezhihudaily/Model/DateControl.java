package com.james.simplezhihudaily.Model;

import android.util.Log;

/**
 * 采用单例模式来维护app主页当前显示的日期
 */
public class DateControl {
    private int cursor;
    private int today;
    private static DateControl dateControl;
    private int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private DateControl(int today) {
        this.cursor = today;
        this.today = today;
    }

    public static synchronized DateControl getInstance(int today) {
        if (dateControl == null)
        {
            Log.d("dateControlCreate",String.valueOf(today));
            dateControl = new DateControl(today);
        }
        return dateControl;
    }

    public static synchronized DateControl getInstance() {
        return dateControl;
    }

    public synchronized void subOneDay() {
        processDate();
        int year = dateControl.cursor / 10000;
        int month = (dateControl.cursor % 10000 - dateControl.cursor % 100) / 100;
        int day = dateControl.cursor % 100;
        if ((day - 1) % days[month - 1] == 0)
        {//根据月份得到每个月的天数 注意减一
            day = days[month - 2];
            if ((month - 1) % 12 == 0)
            {
                month = 12;
                year -= 1;
            } else
            {
                month -= 1;
            }
        } else
        {
            day -= 1;
        }
        int combined = year * 10000 + month * 100 + day;
        dateControl.cursor = combined;
        Log.d("dateControlSub",String.valueOf(combined));
    }

    /**
     * 加一天得判断是否已经到达了当前日期，若已到达，则直接返回false
     *
     * @return whether succeed
     */
    public synchronized boolean addOneDay() {
        if (dateControl.cursor + 1 > dateControl.today)
        {
            Log.d("dateControl","AlreadyLatestDay");
            return false;
        } else
        {
            processDate();
            int year = dateControl.cursor / 10000;
            int month = (dateControl.cursor % 10000 - dateControl.cursor % 100) / 100;
            int day = dateControl.cursor % 100;
            if ((day + 1) % days[month - 1] == 1)
            {//根据月份得到每个月的天数 注意减一
                day = 1;
                if ((month + 1) % 12 == 0)
                {
                    month = 1;
                    year += 1;
                } else
                {
                    month += 1;
                }
            } else
            {
                day += 1;
            }
            int combined = year * 10000 + month * 100 + day;
            dateControl.cursor = combined;
            Log.d("dateControlAdd",String.valueOf(combined));
            return true;
        }
    }

    public synchronized void backToToday() {
        dateControl.cursor = dateControl.today;
    }

    public int getCursor() {
        return dateControl.cursor;
    }

    public int getToday() {
        return dateControl.today;
    }

    /**
     * 自己解析日期 这里主要考虑闰年的情况
     */
    private void processDate() {
        int year = dateControl.cursor / 10000;
        if ((year % 100 != 0) && (year % 4 == 0) || (year % 400 == 0))
        {
            days[1] = 29;
        }
    }
}

