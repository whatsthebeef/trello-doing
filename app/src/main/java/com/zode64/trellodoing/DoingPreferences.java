package com.zode64.trellodoing;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import static com.zode64.trellodoing.WidgetAlarm.DEFAULT_DELAY_HOURS;
import static com.zode64.trellodoing.WidgetAlarm.DEFAULT_END_HOUR;
import static com.zode64.trellodoing.WidgetAlarm.DEFAULT_START_HOUR;

/**
 * Created by john on 2/21/15.
 */
public class DoingPreferences {

    private static final String LAST_DOING_BOARD = "lastDoingBoard";
    private static final String KEEP_DOING_ALARM = "keepDoingAlarm";
    private static final String DELAY = "delay";
    private static final String START_HOUR = "start_hour";
    private static final String END_HOUR = "end_hour";

    private SharedPreferences mPreferences;

    public DoingPreferences( Context context ) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences( context );
    }

    public void saveBoard( String url ) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( LAST_DOING_BOARD, url );
        editor.commit();
    }

    public void handleSetKeepDoingCall( Calendar alarm ) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong( KEEP_DOING_ALARM, alarm.getTimeInMillis() );
        editor.commit();
    }

    public void handleKeepDoingCardComplete() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove( KEEP_DOING_ALARM );
        editor.commit();
    }

    public boolean hasKeepDoing() {
        return mPreferences.getLong( KEEP_DOING_ALARM, 0 ) != 0;
    }

    public String getLastDoingBoard() {
        return mPreferences.getString( LAST_DOING_BOARD, null );
    }

    public Double getDelay() {
        return Double.parseDouble( mPreferences.getString( DELAY, String.valueOf( DEFAULT_DELAY_HOURS ) ) );
    }

    public void setDelay(String delay) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( DELAY, delay );
        editor.commit();
    }

    public Integer getStartHour() {
        return Integer.parseInt( mPreferences.getString( START_HOUR, String.valueOf( DEFAULT_START_HOUR ) ) );
    }

    public Integer getEndHour() {
        return Integer.parseInt( mPreferences.getString( END_HOUR, String.valueOf( DEFAULT_END_HOUR ) ) );
    }

    public SharedPreferences getSharedPreferences() {
        return mPreferences;
    }
}
