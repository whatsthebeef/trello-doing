package com.zode64.trellodoing.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Card;

import java.util.ArrayList;

import static com.zode64.trellodoing.models.Card.ListType.CLOCKED_OFF;
import static com.zode64.trellodoing.models.Card.ListType.DOING;
import static com.zode64.trellodoing.models.Card.ListType.DONE;
import static com.zode64.trellodoing.models.Card.ListType.TODAY;
import static com.zode64.trellodoing.models.Card.ListType.TODO;


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
    public final static String DEADLINE = "deadline";
    public final static String IN_LIST_TYPE = "inListType";
    public final static String DOING_LIST = "doingList";
    public final static String DONE_LIST = "doneList";
    public final static String TODAY_LIST = "todayList";
    public final static String TODO_LIST = "todoList";
    public final static String SHORT_LINK = "shortLink";

    private String[] cols = new String[]{ ID, NAME, BOARD_SHORTLINK, BOARD_NAME, BOARD_ID,
            DEADLINE, IN_LIST_TYPE, CLOCKED_OFF_LIST, DOING_LIST,
            DONE_LIST, TODAY_LIST, TODO_LIST, SHORT_LINK };

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
            DEADLINE + " REAL, " +
            IN_LIST_TYPE + " INTEGER NOT NULL, " +
            CLOCKED_OFF_LIST + " TEXT, " +
            DOING_LIST + " TEXT, " +
            DONE_LIST + " TEXT, " +
            TODAY_LIST + " TEXT, " +
            TODO_LIST + " TEXT, " +
            SHORT_LINK + " TEXT" +
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
        values.put( DEADLINE, card.getDeadline() );
        values.put( IN_LIST_TYPE, card.getInListTypeOrdinal() );
        values.put( CLOCKED_OFF_LIST, card.getListId( CLOCKED_OFF ) );
        values.put( DOING_LIST, card.getListId( DOING ) );
        values.put( DONE_LIST, card.getListId( DONE ) );
        values.put( TODAY_LIST, card.getListId( TODAY ) );
        values.put( TODO_LIST, card.getListId( TODO ) );
        values.put( SHORT_LINK, card.getShortLink() );
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
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void resetDeadline( String id ) {
        ContentValues values = new ContentValues();
        values.put( DEADLINE, -1 );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void updataName( String id, String name ) {
        ContentValues values = new ContentValues();
        values.put( NAME, name );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public Card find( String cardId ) {
        Cursor cursor = database.query( true, TABLE_NAME, cols, ID + "=?", new String[]{ cardId }, null, null, null, null );
        ArrayList<Card> card = cursorToCards( cursor );
        return card.isEmpty() ? null : card.get( 0 );
    }

    public void setClockedOff( String  id ) {
        ContentValues values = new ContentValues();
        values.put( IN_LIST_TYPE, CLOCKED_OFF.ordinal() );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void setClockedOn( String id ) {
        ContentValues values = new ContentValues();
        values.put( IN_LIST_TYPE, DOING.ordinal() );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void setDone( String id ) {
        ContentValues values = new ContentValues();
        values.put( IN_LIST_TYPE, DONE.ordinal() );
        values.put( DEADLINE, 0 );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void setToday( String id ) {
        ContentValues values = new ContentValues();
        values.put( IN_LIST_TYPE, TODAY.ordinal() );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void setTodo( String id ) {
        ContentValues values = new ContentValues();
        values.put( IN_LIST_TYPE, TODO.ordinal() );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
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
                card.setDeadline( cursor.getLong( 5 ) );
                card.setInListType( cursor.getInt( 6 ) );
                card.setListId( CLOCKED_OFF, cursor.getString( 7 ) );
                card.setListId( DOING, cursor.getString( 8 ) );
                card.setListId( DONE, cursor.getString( 9 ) );
                card.setListId( TODAY, cursor.getString( 10 ) );
                card.setListId( TODO, cursor.getString( 11 ) );
                card.setShortLink( cursor.getString( 12 ) );
                cards.add( card );
            }
            cursor.close();
        }
        return cards;
    }
}
