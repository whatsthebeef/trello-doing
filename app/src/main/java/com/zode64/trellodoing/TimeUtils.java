package com.zode64.trellodoing;

import java.util.Calendar;

/**
 * Created by john on 2/12/15.
 */
public class TimeUtils {

    public static Calendar hour(int hour) {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, hour);
        now.set(Calendar.MINUTE, 0);
        return now;
    }

    public static boolean between(int hourOne, int hourTwo, Calendar now) {
        if(now.after(hour(hourOne)) && now.before(hour(hourTwo))) {
            return true;
        }
        return false;
    }

    public static boolean between(int hourOne, int hourTwo) {
        return between(hourOne, hourTwo, Calendar.getInstance());
    }
}
