package com.zode64.trellodoing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zode64.trellodoing.models.Member;

import java.util.Calendar;

import static com.zode64.trellodoing.TimeUtils.between;

public class WidgetAlarm {

    private static final String TAG = WidgetAlarm.class.getName();

    public static final int DEFAULT_DELAY_HOURS = 2;
    public static final int DEFAULT_START_HOUR = 8;
    public static final int DEFAULT_END_HOUR = 18;

    private static final int WORKING_DELAY_MINUTES = 30;
    private static final int NON_WORKING_DELAY_HOURS = 2;

    private static final int ALARM_ID = 0;
    private static final int DEADLINE_ALARM_ID = 1;

    private Context mContext;
    private DoingPreferences mPreferences;

    public WidgetAlarm(Context context, DoingPreferences preferences) {
        mContext = context;
        mPreferences = preferences;
    }

    public void setAlarm() {
        stopStandardAlarm();
        Calendar alarm = Calendar.getInstance();
        Log.d(TAG, "Hour in the day : " + String.valueOf(alarm.get(Calendar.HOUR_OF_DAY)));
        if(between(mPreferences.getStartHour(), mPreferences.getEndHour(), alarm))  {
            alarm.add(Calendar.MINUTE, WORKING_DELAY_MINUTES);
            Log.d(TAG, "Inside working hours");
            setAlarmWithCalendar(alarm, DoingWidget.UpdateService.ACTION_AUTO_UPDATE, ALARM_ID, AlarmManager.RTC_WAKEUP);
        }
        else {
            Log.d(TAG, "Outside working hours");
            alarm.add(Calendar.HOUR, NON_WORKING_DELAY_HOURS);
            setAlarmWithCalendar(alarm, DoingWidget.UpdateService.ACTION_AUTO_UPDATE, ALARM_ID, AlarmManager.RTC);
        }
    }

    public Calendar delayAlarm() {
        stopStandardAlarm();
        Calendar alarm = Calendar.getInstance();
        alarm.add(Calendar.HOUR, mPreferences.getDelay());
        setAlarmWithCalendar(alarm, DoingWidget.UpdateService.ACTION_AUTO_UPDATE, DEADLINE_ALARM_ID, AlarmManager.RTC);
        return alarm;
    }

    public Calendar deadlineAlarm() {
        stopDeadlineAlarm();
        Calendar alarm = Calendar.getInstance();
        // alarm.add(Calendar.SECOND, 5);
        alarm.add(Calendar.HOUR, mPreferences.getDelay());
        setAlarmWithCalendar(alarm, DoingWidget.UpdateService.ACTION_AUTO_UPDATE, DEADLINE_ALARM_ID, AlarmManager.RTC_WAKEUP);
        return alarm;
    }

    private void setAlarmWithCalendar(Calendar calendar, String action, int alarmId, int type) {
        Intent alarm = new Intent(action);
        PendingIntent pendingAlarm = PendingIntent.getService(mContext, alarmId, alarm, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Log.d(TAG, "Setting alarm in hour : " + calendar.get(Calendar.HOUR_OF_DAY));
        alarmManager.set(type, calendar.getTimeInMillis(), pendingAlarm);
    }

    public void stopStandardAlarm() {
        stopAlarm(DoingWidget.UpdateService.ACTION_AUTO_UPDATE, ALARM_ID);
    }

    public void stopDeadlineAlarm() {
        stopAlarm(DoingWidget.UpdateService.ACTION_AUTO_UPDATE, DEADLINE_ALARM_ID);
    }

    private void stopAlarm(String action, int alarmId) {
        Intent alarm = new Intent(action);
        PendingIntent pendingAlarm = PendingIntent.getService(mContext, alarmId, alarm, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingAlarm);
    }

}