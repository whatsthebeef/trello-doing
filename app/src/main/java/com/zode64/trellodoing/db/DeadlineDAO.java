package com.zode64.trellodoing.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;

public class DeadlineDAO {

    private static final String TAG = DeadlineDAO.class.getName();

    private DoingDatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String TABLE_NAME = "deadlines";

    public final static String ID = "id";
    public final static String CARD_SERVER_ID = "cardServerId";
    public final static String TIME = "time";

    private String[] cols = new String[]{ ID, CARD_SERVER_ID, TIME };

    /**
     * @param context
     */
    public DeadlineDAO( Context context ) {
        dbHelper = new DoingDatabaseHelper( context );
        database = dbHelper.getWritableDatabase();
    }

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CARD_SERVER_ID + " TEXT NOT NULL, " +
            TIME + " REAL NOT NULL" +
            " );";

    public static final String DELETE_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXIST";

    public void create( String cardServerId, long time ) {
        Log.i( TAG, "Adding deadline for card id: " + cardServerId );
        ContentValues values = new ContentValues();
        values.put( CARD_SERVER_ID, cardServerId );
        values.put( TIME, time );
        database.insert( TABLE_NAME, null, values );
    }

    public HashMap<String, Long> all() {
        Log.i( TAG, "Fetching all deadlines" );
        Cursor cursor = database.query( true, TABLE_NAME, cols, null, null, null, null, null, null );
        return cursorToDeadlines( cursor );
    }

    public void updateServerId( String cardServerId, String newCardServerId ) {
        ContentValues values = new ContentValues();
        values.put( CARD_SERVER_ID, newCardServerId );
        database.update( TABLE_NAME, values, CARD_SERVER_ID + "=?", new String[]{ cardServerId } );
    }

    public Long find( String cardServerId ) {
        Log.i( TAG, "Fetching all deadlines" );
        Cursor cursor = database.query( true, TABLE_NAME, cols, CARD_SERVER_ID + "=?", new String[]{ cardServerId },
                null, null, null, null );
        return cursorToDeadlines( cursor ).get( cardServerId );
    }

    public void delete( String cardServerId ) {
        database.delete( TABLE_NAME, CARD_SERVER_ID + "=?", new String[]{ cardServerId } );
    }

    public void closeDB() {
        database.close();
    }

    private HashMap<String, Long> cursorToDeadlines( Cursor cursor ) {
        HashMap<String, Long> deadlines = new HashMap<>();
        if ( cursor != null ) {
            while ( cursor.moveToNext() ) {
                deadlines.put( cursor.getString( 1 ), cursor.getLong( 2 ) );
            }
            cursor.close();
        }
        return deadlines;
    }
}
