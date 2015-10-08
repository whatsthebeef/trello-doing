package com.zode64.trellodoing;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

    public static String format( Date date ) {
        DateFormat df = new SimpleDateFormat( "HH:mm dd/MM" );
        return df.format( date );
    }

    public static String format( long millis ) {
        return format( new Date( millis ) );
    }
}
