package com.zode64.trellodoing.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DoingDatabaseHelper extends SQLiteOpenHelper {

    private final static String TAG = DoingDatabaseHelper.class.getName();

    private static final String DATABASE_NAME = "doing";

    private static final int DATABASE_VERSION = 2;

    DoingDatabaseHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onCreate( SQLiteDatabase database ) {
        database.execSQL( CardDAO.CREATE_TABLE );
        database.execSQL( BoardDAO.CREATE_TABLE );
        database.execSQL( ActionDAO.CREATE_TABLE );
    }

    @Override
    public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {
        Log.w( TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data" );
        database.execSQL( CardDAO.DELETE_TABLE );
        database.execSQL( BoardDAO.DELETE_TABLE );
        database.execSQL( ActionDAO.DELETE_TABLE );
        onCreate( database );
    }

}
