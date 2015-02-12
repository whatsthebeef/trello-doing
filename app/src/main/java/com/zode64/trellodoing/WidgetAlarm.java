package com.zode64.trellodoing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import static com.zode64.trellodoing.TimeUtils.between;

public class WidgetAlarm {

    private static final String TAG = WidgetAlarm.class.getName();

    private final int ALARM_ID = 0;
    private final int INTERVAL_MILLIS = 1800000;

    private Context mContext;

    public WidgetAlarm(Context context) {
        mContext = context;
    }

    public void setAlarm() {
        stopAlarm();
        Intent alarm = new Intent(DoingWidget.UpdateService.ACTION_AUTO_UPDATE);
        PendingIntent pendingAlarm = PendingIntent.getService(mContext, ALARM_ID, alarm, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Calendar now = Calendar.getInstance();
        Log.d(TAG, "Hour in the day : " + String.valueOf(now.get(Calendar.HOUR_OF_DAY)));
        if(between(7, 22, now))  {
            now.add(Calendar.MILLISECOND, INTERVAL_MILLIS);
            Log.d(TAG, "Inside working hours");
            alarmManager.set(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), pendingAlarm);
        }
        else {
            Log.d(TAG, "Outside working hours");
            now.add(Calendar.MILLISECOND, INTERVAL_MILLIS*4);
            alarmManager.set(AlarmManager.RTC, now.getTimeInMillis(), pendingAlarm);
        }
    }


    public void stopAlarm() {
        Intent alarm = new Intent(DoingWidget.UpdateService.ACTION_AUTO_UPDATE);
        PendingIntent pendingAlarm = PendingIntent.getService(mContext, ALARM_ID, alarm, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingAlarm);
    }

}