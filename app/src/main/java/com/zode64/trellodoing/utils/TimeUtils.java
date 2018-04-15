package com.zode64.trellodoing.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by john on 2/12/15.
 */
public class TimeUtils {

    public static Calendar hour( int hour ) {
        Calendar now = Calendar.getInstance();
        now.set( Calendar.HOUR_OF_DAY, hour );
        now.set( Calendar.MINUTE, 0 );
        return now;
    }

    public static boolean between( int hourOne, int hourTwo, Calendar now ) {
        if ( now.after( hour( hourOne ) ) && now.before( hour( hourTwo ) ) ) {
            return true;
        }
        return false;
    }

    public static boolean between( int hourOne, int hourTwo ) {
        return between( hourOne, hourTwo, Calendar.getInstance() );
    }

    public static boolean yesterday( long time ) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get( Calendar.DAY_OF_WEEK );
        calendar.setTimeInMillis( time );
        int dayDifference = today - calendar.get( Calendar.DAY_OF_WEEK );
        if ( dayDifference == 1 || dayDifference == -6 ) {
            return true;
        }
        return false;
    }

    public static boolean today( long time ) {
        return compareTime( time, Calendar.DAY_OF_WEEK );
    }

    public static boolean thisWeek( long time ) {
        return compareTime( time, Calendar.WEEK_OF_YEAR );
    }

    private static boolean compareTime( long time, int unit ) {
        Calendar calendar = Calendar.getInstance();
        int thisWeek = calendar.get( unit );
        calendar.setTimeInMillis( time );
        int unitDifference = thisWeek - calendar.get( unit );
        if ( unitDifference == 0 ) {
            return true;
        }
        return false;
    }

    public static long startOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.HOUR_OF_DAY, 0 );
        calendar.set( Calendar.MINUTE, 0 );
        calendar.set( Calendar.SECOND, 0 );
        calendar.set( Calendar.MILLISECOND, 0 );
        return calendar.getTimeInMillis();
    }

    public static boolean after( int hour ) {
        if ( Calendar.getInstance().after( hour( hour ) ) ) {
            return true;
        }
        return false;
    }

    public static String format( Date date ) {
        DateFormat df = new SimpleDateFormat( "HH:mm dd/MM" );
        return df.format( date );
    }

    public static boolean pastDeadline( Long deadline, long now ) {
        return deadline != null && deadline < now;
    }

    public static String format( long millis ) {
        return format( new Date( millis ) );
    }
}
