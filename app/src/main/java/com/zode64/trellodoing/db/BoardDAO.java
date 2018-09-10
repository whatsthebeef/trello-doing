package com.zode64.trellodoing.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zode64.trellodoing.models.Board;

import java.util.ArrayList;
import java.util.HashMap;


public class BoardDAO {

    private static final String TAG = BoardDAO.class.getName();

    private SQLiteDatabase database;

    private final static String TABLE_NAME = "boards";
    private final static String ID = "id";
    private final static String NAME = "name";
    private final static String SHORTLINK = "shortLink";
    private final static String TODO_LIST_ID = "todoListId";
    private final static String TODAY_LIST_ID = "todayListId";
    private final static String DONE_LIST_ID = "doneListId";
    private final static String DOING_LIST_ID = "doingListId";
    private final static String CLOCKED_OFF_LIST_ID = "clockedOffListId";
    private final static String THIS_WEEK_LIST_ID = "thisWeekListId";
    private final static String ORGANIZATION_ID = "idOrganization";

    private String[] cols = new String[]{ ID, NAME, SHORTLINK, TODO_LIST_ID, TODAY_LIST_ID, DONE_LIST_ID,
            DOING_LIST_ID, CLOCKED_OFF_LIST_ID, THIS_WEEK_LIST_ID, ORGANIZATION_ID };

    public BoardDAO( Context context ) {
        DoingDatabaseHelper dbHelper = new DoingDatabaseHelper( context );
        database = dbHelper.getWritableDatabase();
    }

    static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
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

    static final String DELETE_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXIST";

    public void create( Board board ) {
        Log.i( TAG, "Creating board : " + board.getName() );
        ContentValues values = new ContentValues();
        values.put( ID, board.getId() );
        values.put( NAME, board.getName() );
        values.put( SHORTLINK, board.getShortLink() );
        values.put( TODO_LIST_ID, board.getListId( Board.ListType.TODO ) );
        values.put( TODAY_LIST_ID, board.getListId( Board.ListType.TODAY ) );
        values.put( DONE_LIST_ID, board.getListId( Board.ListType.DONE ) );
        values.put( DOING_LIST_ID, board.getListId( Board.ListType.DOING ) );
        values.put( CLOCKED_OFF_LIST_ID, board.getListId( Board.ListType.CLOCKED_OFF ) );
        values.put( THIS_WEEK_LIST_ID, board.getListId( Board.ListType.THIS_WEEK ) );
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

    public void deleteAll() {
        database.delete( TABLE_NAME, null, null );
    }

    public void closeDB() {
        database.close();
    }

    public HashMap<String, Board> boardMap() {
        Cursor cursor = database.query( true, TABLE_NAME, cols, null, null, null, null, null, null );
        HashMap<String, Board> boards = new HashMap<>();
        if ( cursor != null ) {
            while ( cursor.moveToNext() ) {
                boards.put( cursor.getString( 0 ), cursorToBoard( cursor ) );
            }
            cursor.close();
        }
        return boards;
    }

    private ArrayList<Board> cursorToBoards( Cursor cursor ) {
        ArrayList<Board> boards = new ArrayList<>();
        if ( cursor != null ) {
            while ( cursor.moveToNext() ) {
                boards.add( cursorToBoard( cursor ) );
            }
            cursor.close();
        }
        return boards;
    }

    private Board cursorToBoard( Cursor cursor ) {
        Board board = new Board();
        board.setId( cursor.getString( 0 ) );
        board.setName( cursor.getString( 1 ) );
        board.setShortLink( cursor.getString( 2 ) );
        board.addList( cursor.getString( 3 ), Board.ListType.TODO );
        board.addList( cursor.getString( 4 ), Board.ListType.TODAY );
        board.addList( cursor.getString( 5 ), Board.ListType.DONE );
        board.addList( cursor.getString( 6 ), Board.ListType.DOING );
        board.addList( cursor.getString( 7 ), Board.ListType.CLOCKED_OFF );
        board.addList( cursor.getString( 8 ), Board.ListType.THIS_WEEK );
        board.setIdOrganization( cursor.getString( 9 ) );
        return board;
    }


}
