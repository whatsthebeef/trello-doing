package com.zode64.trellodoing;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DoingPreferences {

    private static final String LAST_DOING_BOARD = "lastDoingBoard";
    private static final String KEEP_DOING_ALARM = "keepDoingAlarm";
    private static final String START_HOUR = "start_hour";
    private static final String END_HOUR = "end_hour";
    private static final String TODAY_OR_BOARDS = "todayOrBoards";

    private static final String TODAY = "today";
    private static final String BOARDS = "boards";

    public static final int DEFAULT_START_HOUR = 8;
    public static final int DEFAULT_END_HOUR = 18;

    private SharedPreferences mPreferences;

    public DoingPreferences( Context context ) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences( context );
    }

    public void saveBoard( String url ) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( LAST_DOING_BOARD, url );
        editor.commit();
    }

    public void setKeepDoing( Calendar alarm ) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong( KEEP_DOING_ALARM, alarm.getTimeInMillis() );
        editor.commit();
    }

    public Long getKeepDoing() {
        return mPreferences.getLong( KEEP_DOING_ALARM, 0 );
    }

    public void resetKeepDoing() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong( KEEP_DOING_ALARM, -1 );
        editor.commit();
    }

    public boolean keepDoing() {
        return getKeepDoing() > Calendar.getInstance().getTimeInMillis();
    }

    public String getLastDoingBoard() {
        return mPreferences.getString( LAST_DOING_BOARD, null );
    }

    public Integer getStartHour() {
        return Integer.parseInt( mPreferences.getString( START_HOUR, String.valueOf( DEFAULT_START_HOUR ) ) );
    }

    public Integer getEndHour() {
        return Integer.parseInt( mPreferences.getString( END_HOUR, String.valueOf( DEFAULT_END_HOUR ) ) );
    }

    public void setBoards() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( TODAY_OR_BOARDS, BOARDS );
        editor.commit();
    }

    public void setToday() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( TODAY_OR_BOARDS, TODAY );
        editor.commit();
    }

    public boolean isBoards() {
        return BOARDS.equals(mPreferences.getString( TODAY_OR_BOARDS, TODAY ) );
    }

    public SharedPreferences getSharedPreferences() {
        return mPreferences;
    }
}
