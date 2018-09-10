package com.zode64.trellodoing.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;

import java.util.ArrayList;
import java.util.HashMap;

public class CardDAO {

    private static final String TAG = CardDAO.class.getName();

    private final static String TABLE_NAME = "cards";
    private final static String ID = "id";
    private final static String SERVER_ID = "serverId";
    private final static String NAME = "name";
    private final static String BOARD_ID = "boardId";
    private final static String SHORT_LINK = "shortLink";
    private final static String LIST_TYPE = "listType";
    private final static String MARKED_FOR_DELETE = "markedForDelete";
    private final static String START_TIME_FOR_CURRENT_LIST_TYPE = "startTimeForCurrentListType";

    private String[] cols = new String[]{ ID, SERVER_ID, NAME, BOARD_ID,
            SHORT_LINK, LIST_TYPE, START_TIME_FOR_CURRENT_LIST_TYPE, MARKED_FOR_DELETE };

    private SQLiteDatabase database;

    private HashMap<String, Board> boardReg;

    public CardDAO( Context context, HashMap<String, Board> boardReg ) {
        DoingDatabaseHelper dbHelper = new DoingDatabaseHelper( context );
        database = dbHelper.getWritableDatabase();
        this.boardReg = boardReg;
    }

    static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SERVER_ID + " TEXT, " +
            NAME + " TEXT NOT NULL, " +
            BOARD_ID + " TEXT, " +
            SHORT_LINK + " TEXT, " +
            LIST_TYPE + " INTEGER NOT NULL, " +
            START_TIME_FOR_CURRENT_LIST_TYPE + " REAL NOT NULL, " +
            MARKED_FOR_DELETE + " INTEGER NOT NULL" +
            ");";

    static final String DELETE_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXIST";

    public void create( Card card ) {
        Log.i( TAG, "Creating card : " + card.getName() );
        ContentValues values = new ContentValues();
        values.put( SERVER_ID, card.getServerId() );
        values.put( NAME, card.getName() );
        values.put( BOARD_ID, card.getBoardId() );
        values.put( SHORT_LINK, card.getShortLink() );
        values.put( LIST_TYPE, card.getListType().ordinal() );
        values.put( START_TIME_FOR_CURRENT_LIST_TYPE, card.getStartTimeForCurrentListType() );
        values.put( MARKED_FOR_DELETE, 0 );
        card.setId( ( int ) database.insert( TABLE_NAME, null, values ) );
    }

    public ArrayList<Card> all() {
        Log.i( TAG, "Fetching all cards" );
        Cursor cursor = database.query( true, TABLE_NAME, cols, MARKED_FOR_DELETE + "=0", null, null, null, null, null );
        return cursorToCards( cursor );
    }

    public ArrayList<Card> where( Board.ListType listType ) {
        Log.i( TAG, "Fetching all cards with list type: " + listType.ordinal() );
        Cursor cursor = database.query( true, TABLE_NAME, cols, LIST_TYPE + "=? AND " + MARKED_FOR_DELETE + "=0",
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

    public void setListType(String cardId, Board.ListType listType ) {
        ContentValues values = new ContentValues();
        values.put( LIST_TYPE, listType.ordinal() );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ cardId } );
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
                card.setBoard( boardReg.get( cursor.getString( 3 ) ) );
                card.setShortLink( cursor.getString( 4 ) );
                card.setListType( Board.ListType.values()[ cursor.getInt( 5 ) ] );
                card.setStartTimeOfCurrentListType( cursor.getLong( 6 ) );
                cards.add( card );
            }
            cursor.close();
        }
        return cards;
    }

}
