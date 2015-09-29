package com.zode64.trellodoing;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zode64.trellodoing.models.Action;

import static com.zode64.trellodoing.WidgetAlarm.*;

import java.util.Calendar;

/**
 * Created by john on 2/21/15.
 */
public class DoingPreferences {

    private static final String LAST_DOING_BOARD = "lastDoingBoard";
    private static final String COMMIT_PENDING = "commitPending";
    private static final String DEADLINE_ALARM = "deadlineAlarm";
    private static final String KEEP_DOING_ALARM = "keepDoingAlarm";
    private static final String DEADLINE_CARD_ID = "deadlineCardId";
    private static final String PERSONAL_CARD_NAME = "personalCardName";
    private static final String CARD_ID = "cardId";
    private static final String CLOCKED_OFF_LIST_ID = "clockedOffListId";
    private static final String DELAY = "delay";
    private static final String START_HOUR = "start_hour";
    private static final String END_HOUR = "end_hour";
    private static final String STATUS_DOING = "status_doing";

    private static final int BOTH = 0;
    private static final int WORK = 1;
    private static final int PERSONAL = 2;

    private SharedPreferences mPreferences;

    public DoingPreferences(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveBoard(String url) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LAST_DOING_BOARD, url);
        editor.commit();
    }

    public void handleClockOffCall(String cardId, String clockedOffListId) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(COMMIT_PENDING, true);
        editor.putString(CARD_ID, cardId);
        editor.putString(CLOCKED_OFF_LIST_ID, clockedOffListId);
        editor.commit();
    }

    public void handleClockOffSuccess() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(COMMIT_PENDING, false);
        editor.remove( CARD_ID );
        editor.remove( CLOCKED_OFF_LIST_ID );
        editor.commit();
    }

    public void handleSetDeadlineCall(String cardId, Calendar alarm) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( DEADLINE_CARD_ID, cardId );
        editor.putLong( DEADLINE_ALARM, alarm.getTimeInMillis() );
        editor.commit();
    }

    public void handleDeadlineCardComplete() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove( DEADLINE_CARD_ID );
        editor.remove( DEADLINE_ALARM );
        editor.commit();
    }

    public void handleSetKeepDoingCall(Calendar alarm) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong( KEEP_DOING_ALARM, alarm.getTimeInMillis() );
        editor.commit();
    }

    public void handleKeepDoingCardComplete() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove( KEEP_DOING_ALARM );
        editor.commit();
    }

    public void handleAddPersonalCardSuccess() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove( PERSONAL_CARD_NAME );
        editor.commit();
    }

    public void handleSetAddPersonalCard(String name) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( PERSONAL_CARD_NAME, name );
        editor.commit();
    }

    public void handleSetStatusDoing(Action.Status status) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt( STATUS_DOING, status.getInt() );
        editor.commit();
    }

    public void handleSetStatusDoing(int status) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt( STATUS_DOING, status );
        editor.commit();
    }
    public Action.Status getStatusDoing() {
        return Action.Status.getStatus( mPreferences.getInt( STATUS_DOING, 0 ) );
    }

    public String getPersonalCardName() {
        return mPreferences.getString(PERSONAL_CARD_NAME, null);
    }

    public boolean hasPersonalCardName() {
        return mPreferences.getString( PERSONAL_CARD_NAME, null ) != null;
    }

    public boolean isCommitPending() {
        return mPreferences.getBoolean(COMMIT_PENDING, false);
    }

    public String getDeadlineCardId() {
        return mPreferences.getString( DEADLINE_CARD_ID, null );
    }

    public boolean pastDeadline() {
        long alarm = mPreferences.getLong(DEADLINE_ALARM, 0);
        return Calendar.getInstance().getTimeInMillis() > alarm;
    }

    public boolean hasDeadline() {
        return mPreferences.getLong( DEADLINE_ALARM, 0 ) != 0;
    }

    public boolean hasKeepDoing() {
        return mPreferences.getLong( KEEP_DOING_ALARM, 0 ) != 0;
    }

    public String getLastDoingBoard() {
        return mPreferences.getString(LAST_DOING_BOARD, null);
    }

    public Integer getDelay() {
        return Integer.parseInt( mPreferences.getString( DELAY, String.valueOf( DEFAULT_DELAY_HOURS ) ) );
    }

    public Integer getStartHour() {
        return Integer.parseInt( mPreferences.getString( START_HOUR, String.valueOf( DEFAULT_START_HOUR ) ) );
    }

    public Integer getEndHour() {
        return Integer.parseInt( mPreferences.getString(END_HOUR, String.valueOf( DEFAULT_END_HOUR )) );
    }

    public SharedPreferences getSharedPreferences() {
       return mPreferences;
    }
}
