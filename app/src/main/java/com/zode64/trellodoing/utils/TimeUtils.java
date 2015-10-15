package com.zode64.trellodoing.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

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

    public static boolean pastDeadline(Long deadline, long now){
        return deadline != null && deadline < now;
    }

    public static String format( long millis ) {
        return format( new Date( millis ) );
    }
}