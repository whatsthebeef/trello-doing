package com.zode64.trellodoing.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zode64.trellodoing.models.Board;

import java.util.ArrayList;


public class BoardDAO {

    private static final String TAG = BoardDAO.class.getName();

    private DoingDatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String PERSONAL_BOARD = "Personal";

    public final static String TABLE_NAME = "boards";
    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String SHORTLINK = "shortLink";
    public final static String TODO_LIST_ID = "todoListId";
    public final static String TODAY_LIST_ID = "todayListId";
    public final static String DONE_LIST_ID = "doneListId";
    public final static String DOING_LIST_ID = "doingListId";
    public final static String CLOCKED_OFF_LIST_ID = "clockedOffListId";
    public final static String THIS_WEEK_LIST_ID = "thisWeekListId";
    public final static String ORGANIZATION_ID = "idOrganization";

    private String[] cols = new String[]{ ID, NAME, SHORTLINK, TODO_LIST_ID, TODAY_LIST_ID, DONE_LIST_ID,
            DOING_LIST_ID, CLOCKED_OFF_LIST_ID, THIS_WEEK_LIST_ID, ORGANIZATION_ID };

    /**
     * @param context
     */
    public BoardDAO( Context context ) {
        dbHelper = new DoingDatabaseHelper( context );
        database = dbHelper.getWritableDatabase();
    }

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
            ID + " TEXT PRIMARY KEY, " +
            NAME + " TEXT NOT NULL, " +
            SHORTLINK + " TEXT, " +
            TODO_LIST_ID + " TEXT, " +
            TODAY_LIST_ID + " TEXT, " +
            DONE_LIST_ID + " TEXT, " +
            DOING_LIST_ID + " TEXT, " +
            CLOCKED_OFF_LIST_ID + " TEXT, " +
            THIS_WEEK_LIST_ID + " TEXT, " +
            ORGANIZATION_ID + " TEXT" +
            ");";

    public static final String DELETE_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXIST";

    public void create( Board board ) {
        Log.i( TAG, "Creating board : " + board.getName() );
        ContentValues values = new ContentValues();
        values.put( ID, board.getId() );
        values.put( NAME, board.getName() );
        values.put( SHORTLINK, board.getShortLink() );
        values.put( TODO_LIST_ID, board.getTodoListId() );
        values.put( TODAY_LIST_ID, board.getTodayListId() );
        values.put( DONE_LIST_ID, board.getDoneListId() );
        values.put( DOING_LIST_ID, board.getDoingListId() );
        values.put( CLOCKED_OFF_LIST_ID, board.getClockedOffListId() );
        values.put( THIS_WEEK_LIST_ID, board.getThisWeekListId() );
        values.put( ORGANIZATION_ID, board.getIdOrganization() );
        database.insert( TABLE_NAME, null, values );
    }

    public ArrayList<Board> all() {
        Log.i( TAG, "Fetching all boards" );
        Cursor cursor = database.query( true, TABLE_NAME, cols, null, null, null, null, null, null );
        return cursorToBoards( cursor );
    }

    public Board find( String boardId ) {
        Cursor cursor = database.query( true, TABLE_NAME, cols, ID + "=?", new String[]{ boardId }, null, null, null, null );
        ArrayList<Board> board = cursorToBoards( cursor );
        return board.isEmpty() ? null : board.get( 0 );
    }

    public Board findPersonalBoard() {
        Cursor cursor = database.query( true, TABLE_NAME, cols, NAME + "=?", new String[]{ PERSONAL_BOARD }, null, null, null, null );
        ArrayList<Board> board = cursorToBoards( cursor );
        return board.isEmpty() ? null : board.get( 0 );
    }

    public void deleteAll() {
        database.delete( TABLE_NAME, null, null );
    }

    public void closeDB() {
        database.close();
    }

    private ArrayList<Board> cursorToBoards( Cursor cursor ) {
        ArrayList<Board> boards = new ArrayList<>();
        if ( cursor != null ) {
            while ( cursor.moveToNext() ) {
                Board board = new Board();
                board.setId( cursor.getString( 0 ) );
                board.setName( cursor.getString( 1 ) );
                board.setShortLink( cursor.getString( 2 ) );
                board.setTodoListId( cursor.getString( 3 ) );
                board.setTodayListId( cursor.getString( 4 ) );
                board.setDoneListId( cursor.getString( 5 ) );
                board.setDoingListId( cursor.getString( 6 ) );
                board.setClockedOffListId( cursor.getString( 7 ) );
                board.setThisWeekListId( cursor.getString( 8 ) );
                board.setIdOrganization( cursor.getString( 9 ) );
                boards.add( board );
            }
            cursor.close();
        }
        return boards;
    }
}
