package com.zode64.trellodoing.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
    public final static String SERVER_ID = "serverId";
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
    public final static String MARKED_FOR_DELETE = "markedForDelete";

    private String[] cols = new String[]{ ID, SERVER_ID, NAME, BOARD_SHORTLINK, BOARD_NAME, BOARD_ID,
            DEADLINE, IN_LIST_TYPE, CLOCKED_OFF_LIST, DOING_LIST,
            DONE_LIST, TODAY_LIST, TODO_LIST, SHORT_LINK, MARKED_FOR_DELETE };

    /**
     * @param context
     */
    public CardDAO( Context context ) {
        dbHelper = new DoingDatabaseHelper( context );
        database = dbHelper.getWritableDatabase();
    }

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SERVER_ID + " TEXT, " +
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
            SHORT_LINK + " TEXT, " +
            MARKED_FOR_DELETE + " INTEGER NOT NULL" +
            ");";

    public static final String DELETE_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXIST";

    public void create( Card card ) {
        Log.i( TAG, "Creating card : " + card.getName() );
        ContentValues values = new ContentValues();
        values.put( SERVER_ID, card.getServerId() );
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
        values.put( MARKED_FOR_DELETE, 0 );
        card.setId( (int) database.insert( TABLE_NAME, null, values ) );
    }

    public ArrayList<Card> all() {
        Log.i( TAG, "Fetching all cards" );
        Cursor cursor = database.query( true, TABLE_NAME, cols, MARKED_FOR_DELETE + "=0", null, null, null, null, null );
        return cursorToCards( cursor );
    }

    public ArrayList<Card> where( Card.ListType listType ) {
        Log.i( TAG, "Fetching all cards with list type: " + listType.ordinal() );
        Cursor cursor = database.query( true, TABLE_NAME, cols, IN_LIST_TYPE + "=? AND " + MARKED_FOR_DELETE + "=0",
                new String[]{ "" + listType.ordinal() }, null, null, null, null );
        return cursorToCards( cursor );
    }

    public void delete( String id ) {
        database.delete( TABLE_NAME, ID + "=?", new String[]{ id } );
    }

    public void markForDelete( String id ) {
        ContentValues values = new ContentValues();
        values.put( MARKED_FOR_DELETE, 1 );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
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

    public void updateName( String id, String name ) {
        ContentValues values = new ContentValues();
        values.put( NAME, name );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public void updateServerId( String id, String serverId ) {
        ContentValues values = new ContentValues();
        values.put( SERVER_ID, serverId );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ id } );
    }

    public Card findByServerId( String cardId ) {
        Cursor cursor = database.query( true, TABLE_NAME, cols, SERVER_ID + "=?", new String[]{ cardId }, null, null, null, null );
        ArrayList<Card> card = cursorToCards( cursor );
        return card.isEmpty() ? null : card.get( 0 );
    }

    public Card findById( String id ) {
        Cursor cursor = database.query( true, TABLE_NAME, cols, ID + "=?", new String[]{ id }, null, null, null, null );
        ArrayList<Card> card = cursorToCards( cursor );
        return card.isEmpty() ? null : card.get( 0 );
    }

    public void setClockedOff( String id ) {
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
                card.setId( cursor.getInt( 0 ) );
                card.setServerId( cursor.getString( 1 ) );
                card.setName( cursor.getString( 2 ) );
                card.setBoardShortLink( cursor.getString( 3 ) );
                card.setBoardName( cursor.getString( 4 ) );
                card.setBoardId( cursor.getString( 5 ) );
                card.setDeadline( cursor.getLong( 6 ) );
                card.setInListType( cursor.getInt( 7 ) );
                card.setListId( CLOCKED_OFF, cursor.getString( 8 ) );
                card.setListId( DOING, cursor.getString( 9 ) );
                card.setListId( DONE, cursor.getString( 10 ) );
                card.setListId( TODAY, cursor.getString( 11 ) );
                card.setListId( TODO, cursor.getString( 12 ) );
                card.setShortLink( cursor.getString( 13 ) );
                cards.add( card );
            }
            cursor.close();
        }
        return cards;
    }
}
