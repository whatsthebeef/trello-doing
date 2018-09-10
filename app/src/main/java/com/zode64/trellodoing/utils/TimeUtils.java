package com.zode64.trellodoing.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

    private static final String TAG = TimeUtils.class.getName();

    private static Calendar hour( int hour ) {
        Calendar now = Calendar.getInstance();
        now.set( Calendar.HOUR_OF_DAY, hour );
        now.set( Calendar.MINUTE, 0 );
        return now;
    }

    public static boolean betweenHours( int hourOne, int hourTwo, Calendar now ) {
        return now.after( hour( hourOne ) ) && now.before( hour( hourTwo ) );
    }

    public static boolean betweenHours( int hourOne, int hourTwo ) {
        return betweenHours( hourOne, hourTwo, Calendar.getInstance() );
    }

    public static boolean mondayToThursday( Calendar now ) {
        return now.after( startOfDayOfThisWeek( Calendar.MONDAY ) ) && now.before( startOfDayOfThisWeek( Calendar.FRIDAY ) );
    }

    public static boolean isThisWeek( Calendar cal, int endHour ) {
        Calendar sunday = startOfDayOfThisWeek( Calendar.SUNDAY );
        sunday.set( Calendar.HOUR_OF_DAY, endHour );
        return cal.after( startOfDayOfThisWeek( Calendar.MONDAY ) ) && cal.before( sunday );
    }

    public static boolean isToday( Calendar now, int endHour ) {
        Calendar endOfToday = startOfToday();
        endOfToday.set( Calendar.HOUR_OF_DAY, endHour );
        return now.after( startOfToday() ) && now.before( endOfToday );
    }

    public static boolean yesterday( long time ) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get( Calendar.DAY_OF_WEEK );
        calendar.setTimeInMillis( time );
        int dayDifference = today - calendar.get( Calendar.DAY_OF_WEEK );
        return dayDifference == 1 || dayDifference == -6;
    }

    private static boolean compareTime( long time, int unit ) {
        Calendar calendar = Calendar.getInstance();
        int thisWeek = calendar.get( unit );
        calendar.setTimeInMillis( time );
        int unitDifference = thisWeek - calendar.get( unit );
        return unitDifference == 0;
    }

    private static Calendar startOfDayOfThisWeek( int dayOfWeek ) {
        Calendar calendar = startOfToday();
        calendar.set( Calendar.DAY_OF_WEEK, dayOfWeek );
        return calendar;
    }

    private static Calendar startOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.HOUR_OF_DAY, 0 );
        calendar.set( Calendar.MINUTE, 0 );
        calendar.set( Calendar.SECOND, 0 );
        calendar.set( Calendar.MILLISECOND, 0 );
        return calendar;
    }

    public static boolean after( int hour ) {
        return Calendar.getInstance().after( hour( hour ) );
    }

    private static String format( Date date ) {
        DateFormat df = new SimpleDateFormat( "HH:mm dd/MM" );
        return df.format( date );
    }

    public static String format( Calendar cal ) {
        DateFormat df = new SimpleDateFormat( "HH:mm dd/MM" );
        return df.format( cal.getTime() );
    }

    public static boolean pastDeadline( Long deadline, long now ) {
        return deadline != null && deadline < now;
    }

    public static String format( long millis ) {
        return format( new Date( millis ) );
    }
}
