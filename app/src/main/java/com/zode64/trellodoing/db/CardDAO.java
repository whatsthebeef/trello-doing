package com.zode64.trellodoing.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zode64.trellodoing.models.Card;

import static com.zode64.trellodoing.models.Card.*;

import java.util.ArrayList;

public class CardDAO {

    private static final String TAG = CardDAO.class.getName();

    private DoingDatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String TABLE_NAME = "cards";
    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String BOARD_SHORTLINK = "boardShortLink";
    public final static String BOARD_NAME = "boardName";
    public final static String BOARD_ID = "boardId";
    public final static String CLOCKED_OFF_LIST = "clockedOffList";
    public final static String IS_CLOCKED_OFF = "isClockedOff";
    public final static String IS_PENDING_PUSH = "isPendingPush";
    public final static String DEADLINE = "deadline";
    public final static String IN_LIST_TYPE = "inListType";
    public final static String DOING_LIST = "doingList";
    public final static String IS_CLOCKED_ON = "isClockedOn";
    public final static String DONE_LIST = "doneList";
    public final static String IS_DONE = "isDone";
    public final static String TODAY_LIST = "todayList";
    public final static String IS_TODAY = "isToday";

    private String[] cols = new String[]{ ID, NAME, BOARD_SHORTLINK, BOARD_NAME, BOARD_ID,
            CLOCKED_OFF_LIST, IS_CLOCKED_OFF, IS_PENDING_PUSH, DEADLINE, IN_LIST_TYPE, DOING_LIST, IS_CLOCKED_ON,
            DONE_LIST, IS_DONE, TODAY_LIST, IS_TODAY  };

    /**
     * @param context
     */
    public CardDAO( Context context ) {
        dbHelper = new DoingDatabaseHelper( context );
        database = dbHelper.getWritableDatabase();
    }

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
            ID + " TEXT PRIMARY KEY, " +
            NAME + " TEXT NOT NULL, " +
            BOARD_SHORTLINK + " TEXT, " +
            BOARD_NAME + " TEXT, " +
            BOARD_ID + " TEXT, " +
            CLOCKED_OFF_LIST + " TEXT, " +
            IS_CLOCKED_OFF + " INTEGER NOT NULL, " +
            IS_PENDING_PUSH + " INTEGER NOT NULL, " +
            DEADLINE + " REAL, " +
            IN_LIST_TYPE + " INTEGER NOT NULL, " +
            DOING_LIST + " TEXT, " +
            IS_CLOCKED_ON + " INTEGER NOT NULL, " +
            DONE_LIST + " TEXT, " +
            IS_DONE + " INTEGER NOT NULL, " +
            TODAY_LIST + " TEXT, " +
            IS_TODAY + " INTEGER NOT NULL" +
            ");";

    public static final String DELETE_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXIST";

    public void create( Card card ) {
        Log.i( TAG, "Creating card : " + card.getName() );
        ContentValues values = new ContentValues();
        values.put( ID, card.getId() );
        values.put( NAME, card.getName() );
        values.put( BOARD_SHORTLINK, card.getBoardShortLink() );
        values.put( BOARD_NAME, card.getBoardName() );
        values.put( BOARD_ID, card.getBoardId() );
        values.put( CLOCKED_OFF_LIST, card.getClockedOffList() );
        values.put( IS_CLOCKED_OFF, card.getIsClockedOff() );
        values.put( IS_PENDING_PUSH, card.getDeadline() );
        values.put( DEADLINE, card.getDeadline() );
        values.put( IN_LIST_TYPE, card.getInListTypeOrdinal() );
        values.put( DOING_LIST, card.getDoingList() );
        values.put( IS_CLOCKED_ON, card.getIsClockedOn() );
        values.put( DONE_LIST, card.getDoneList() );
        values.put( IS_DONE, card.getIsDone() );
        values.put( TODAY_LIST, card.getTodayList() );
        values.put( IS_TODAY, card.getIsToday() );
        database.insert( TABLE_NAME, null, values );
    }

    public ArrayList<Card> all() {
        Log.i( TAG, "Fetching all cards" );
        Cursor cursor = database.query( true, TABLE_NAME, cols, null, null, null, null, null, null );
        return cursorToCards( cursor );
    }

    public ArrayList<Card> where( Card.ListType listType ) {
        Log.i( TAG, "Fetching all cards with list type: " + listType.ordinal() );
        Cursor cursor = database.query( true, TABLE_NAME, cols, IN_LIST_TYPE + "=?", new String[]{ "" + listType.ordinal() },
                null, null, null, null );
        return cursorToCards( cursor );
    }

    public ArrayList<Card> wherePendingPush() {
        Cursor cursor = database.query( true, TABLE_NAME, cols, IS_PENDING_PUSH + "=1", null, null, null, null, null );
        return cursorToCards( cursor );
    }

    public boolean existsDeadlineSet() {
        return DatabaseUtils.queryNumEntries( database, TABLE_NAME, DEADLINE + ">1", null ) > 0;
    }

    public void delete( String cardId ) {
        database.delete( TABLE_NAME, ID + "=?", new String[]{ cardId } );
    }

    public void deleteAll() {
        database.delete( TABLE_NAME, null, null );
    }

    public void setDeadline( String id, long deadline ) {
        ContentValues values = new ContentValues();
        values.put( DEADLINE, deadline );
        values.put( IS_PENDING_PUSH, TRUE );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void setIsClockedOff( String id ) {
        ContentValues values = new ContentValues();
        values.put( IS_CLOCKED_OFF, TRUE );
        values.put( IS_DONE, FALSE );
        values.put( IS_TODAY, FALSE );
        values.put( IS_CLOCKED_ON, FALSE );
        values.put( IS_PENDING_PUSH, TRUE );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void setIsClockedOn( String id ) {
        ContentValues values = new ContentValues();
        values.put( IS_CLOCKED_ON, TRUE );
        values.put( IS_DONE, FALSE );
        values.put( IS_TODAY, FALSE );
        values.put( IS_CLOCKED_OFF, FALSE );
        values.put( IS_PENDING_PUSH, TRUE );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void setIsDone( String id ) {
        ContentValues values = new ContentValues();
        values.put( IS_DONE, TRUE );
        values.put( IS_CLOCKED_ON, FALSE );
        values.put( IS_TODAY, FALSE );
        values.put( IS_CLOCKED_OFF, FALSE );
        values.put( IS_PENDING_PUSH, TRUE );
        values.put( DEADLINE, 0 );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void setIsToday( String id ) {
        ContentValues values = new ContentValues();
        values.put( IS_TODAY, TRUE );
        values.put( IS_CLOCKED_ON, FALSE );
        values.put( IS_DONE, FALSE );
        values.put( IS_CLOCKED_OFF, FALSE );
        values.put( IS_PENDING_PUSH, TRUE );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void createPersonalCard( String name ) {
        delete( "temp" );
        Card card = new Card();
        card.setId( "temp" );
        card.setName( name );
        card.setIsPendingPush( 1 );
        create( card );
    }

    public Card find( String cardId ) {
        Cursor cursor = database.query( true, TABLE_NAME, cols, ID + "=?", new String[]{ cardId }, null, null, null, null );
        ArrayList<Card> card = cursorToCards( cursor );
        return card.isEmpty() ? null : card.get( 0 );
    }

    public Card getPersonalCard() {
        Cursor cursor = database.query( true, TABLE_NAME, cols, ID + "=?", new String[]{ "temp" }, null, null, null, null );
        ArrayList<Card> personalCard = cursorToCards( cursor );
        return personalCard.isEmpty() ? null : personalCard.get( 0 );
    }

    public void closeDB() {
        database.close();
    }

    private ArrayList<Card> cursorToCards( Cursor cursor ) {
        ArrayList<Card> cards = new ArrayList<>();
        if ( cursor != null ) {
            while ( cursor.moveToNext() ) {
                Card card = new Card();
                card.setId( cursor.getString( 0 ) );
                card.setName( cursor.getString( 1 ) );
                card.setBoardShortLink( cursor.getString( 2 ) );
                card.setBoardName( cursor.getString( 3 ) );
                card.setBoardId( cursor.getString( 4 ) );
                card.setClockedOffList( cursor.getString( 5 ) );
                card.setIsClockedOff( cursor.getInt( 6 ) );
                card.setIsPendingPush( cursor.getInt( 7 ) );
                card.setDeadline( cursor.getLong( 8 ) );
                card.setInListType( cursor.getInt( 9 ) );
                card.setDoingList( cursor.getString( 10 ) );
                card.setIsClockedOn( cursor.getInt( 11 ) );
                card.setDoneList( cursor.getString( 12 ) );
                card.setIsDone( cursor.getInt( 13 ) );
                card.setTodayList( cursor.getString( 14 ) );
                card.setIsToday( cursor.getInt( 15 ) );
                cards.add( card );
            }
            cursor.close();
        }
        return cards;
    }
}
