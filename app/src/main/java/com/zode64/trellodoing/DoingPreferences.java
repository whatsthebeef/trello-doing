package com.zode64.trellodoing;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zode64.trellodoing.utils.TimeUtils;

import java.util.Calendar;

public class DoingPreferences {

    private static final String KEEP_DOING_ALARM = "keepDoingAlarm";
    private static final String START_HOUR = "start_hour";
    private static final String END_HOUR = "end_hour";
    private static final String TODAY_OR_THIS_WEEK = "todayOrThisWeek";
    private static final String TOKEN = "token";
    private static final String APP_KEY = "app_key";
    private static final String HOURS_IN_DAY = "hoursInDay";

    private static final String TODAY = "isToday";
    private static final String TODO = "isTodo";
    private static final String THIS_WEEK = "thisWeek";

    private static final String LAST_DOING_BOARD = "lastDoingBoard";
    static final String LAST_ADDED_BOARD = "lastAddedBoard";

    private static final int DEFAULT_HOURS_IN_DAY = 9;
    private static final int DEFAULT_START_HOUR = 8;
    private static final int DEFAULT_END_HOUR = 18;

    private SharedPreferences mPreferences;

    public DoingPreferences( Context context ) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences( context );
    }

    public void saveLastDoingBoard( String url ) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( LAST_DOING_BOARD, url );
        editor.apply();
    }

    public String getLastDoingBoard() {
        return mPreferences.getString( LAST_DOING_BOARD, null );
    }

    void saveLastAddedBoard( String boardId ) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( LAST_ADDED_BOARD, boardId );
        editor.apply();
    }

    String getLastAddedBoard() {
        return mPreferences.getString( LAST_ADDED_BOARD, null );
    }

    void setKeepDoing( Calendar alarm ) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong( KEEP_DOING_ALARM, alarm.getTimeInMillis() );
        editor.apply();
    }

    void resetKeepDoing() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong( KEEP_DOING_ALARM, -1 );
        editor.apply();
    }

    Long getKeepDoing() {
        return mPreferences.getLong( KEEP_DOING_ALARM, 0 );
    }

    public boolean keepDoing() {
        return getKeepDoing() > Calendar.getInstance().getTimeInMillis();
    }

    public void setThisWeek() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( TODAY_OR_THIS_WEEK, THIS_WEEK );
        editor.apply();
    }

    public boolean isThisWeek() {
        return THIS_WEEK.equals( mPreferences.getString( TODAY_OR_THIS_WEEK, TODAY ) );
    }

    public void setToday() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( TODAY_OR_THIS_WEEK, TODAY );
        editor.apply();
    }

    public void setTodo() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( TODAY_OR_THIS_WEEK, TODO );
        editor.apply();
    }

    public Integer getStartHour() {
        return Integer.parseInt( mPreferences.getString( START_HOUR, String.valueOf( DEFAULT_START_HOUR ) ) );
    }

    public Integer getEndHour() {
        return Integer.parseInt( mPreferences.getString( END_HOUR, String.valueOf( DEFAULT_END_HOUR ) ) );
    }

    public Integer getHoursInDay() {
        return Integer.parseInt( mPreferences.getString( HOURS_IN_DAY, String.valueOf( DEFAULT_HOURS_IN_DAY ) ) );
    }

    public String getAppKey() {
        return mPreferences.getString( APP_KEY, null );
    }

    public String getToken() {
        return mPreferences.getString( TOKEN, null );
    }

    public boolean hasToken() {
        String token = mPreferences.getString( TOKEN, null );
        return token != null && !token.equals( "" );
    }

}
