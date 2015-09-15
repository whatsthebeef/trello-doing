package com.zode64.trellodoing;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Created by john on 2/21/15.
 */
public class DoingPreferences {

    private SharedPreferences mPreferences;

    public DoingPreferences(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveBoard(String url) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("lastDoingBoard", url);
        editor.commit();
    }

    public void handleClockOffCall(String cardId, String clockedOffListId) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("commitPending", true);
        editor.putString("cardId", cardId);
        editor.putString("clockedOffListId", clockedOffListId);
        editor.commit();
    }

    public void handleClockOffSuccess() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("commitPending", false);
        editor.remove("cardId");
        editor.remove("clockedOffListId");
        editor.commit();
    }

    public void handleSetDeadlineCall(String cardId, Calendar alarm) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("deadlineCardId", cardId);
        editor.putLong("deadlineAlarm", alarm.getTimeInMillis());
        editor.commit();
    }

    public void handleDeadlineCardComplete() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove("deadlineCardId");
        editor.remove("deadlineAlarm");
        editor.commit();
    }

    public boolean isCommitPending() {
        return mPreferences.getBoolean("commitPending", false);
    }

    public String getDeadlineCardId() {
        return mPreferences.getString("deadlineCardId", null);
    }

    public boolean pastDeadline() {
        long alarm = mPreferences.getLong("deadlineAlarm", 0);
        return Calendar.getInstance().getTimeInMillis() > alarm;
    }

    public boolean hasDeadline() {
        return mPreferences.getLong("deadlineAlarm", 0) != 0;
    }

    public String getLastDoingBoard() {
        return mPreferences.getString("lastDoingBoard", null);
    }

    public SharedPreferences getSharedPreferences() {
       return mPreferences;
    }
}
