package com.zode64.trellodoing.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Action.Type;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.CreateAction;
import com.zode64.trellodoing.models.DeleteAction;
import com.zode64.trellodoing.models.MoveAction;
import com.zode64.trellodoing.models.UpdateNameAction;
import com.zode64.trellodoing.utils.TrelloManager;

import java.util.ArrayList;
import java.util.HashMap;

public class ActionDAO {

    private static final String TAG = CardDAO.class.getName();

    private DoingDatabaseHelper dbHelper;

    private SQLiteDatabase database;

    private CardDAO cardDAO;
    private DeadlineDAO deadlineDAO;
    private AttachmentDAO attachmentDAO;

    private TrelloManager trello;

    public final static String TABLE_NAME = "actions";

    public final static String ID = "id";
    public final static String TYPE = "type";
    public final static String CARD_ID = "cardId";
    public final static String CREATED_AT = "created_at";

    private String[] cols = new String[]{ ID, TYPE, CARD_ID, CREATED_AT };

    /**
     * @param context
     */
    public ActionDAO( Context context, TrelloManager trello ) {
        this( context, trello, new CardDAO( context ) );
    }

    public ActionDAO( Context context, TrelloManager trello, CardDAO cardDAO ) {
        dbHelper = new DoingDatabaseHelper( context );
        database = dbHelper.getWritableDatabase();
        this.cardDAO = cardDAO;
        this.trello = trello;
        this.deadlineDAO = new DeadlineDAO( context );
        this.attachmentDAO = new AttachmentDAO( context );
    }

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TYPE + " INTEGER NOT NULL, " +
            CARD_ID + " INTEGER NOT NULL, " +
            CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP " +
            ");";

    public static final String DELETE_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXIST";

    public ArrayList<Action> all() {
        Log.i( TAG, "Fetching all actions" );
        Cursor cursor = database.query( true, TABLE_NAME, cols, null, null, null, null,
                CREATED_AT + " ASC", null );
        return cursorToActions( cursor );
    }

    public void delete( int id ) {
        database.delete( TABLE_NAME, ID + "=?", new String[]{ String.valueOf( id ) } );
    }

    public void delete( String cardId, Action.Type type ) {
        database.delete( TABLE_NAME, CARD_ID + "=? AND " + TYPE + "=?", new String[]{
                cardId, String.valueOf( type.ordinal() ) } );
    }

    public void delete( String cardId ) {
        database.delete( TABLE_NAME, CARD_ID + "=?", new String[]{ String.valueOf( cardId ) } );
    }

    public void deleteAll() {
        database.delete( TABLE_NAME, null, null );
    }

    public void closeDB() {
        database.close();
        cardDAO.closeDB();
        deadlineDAO.closeDB();
    }

    public ArrayList<Action> find( String cardId, Type type ) {
        Cursor cursor = database.query( true, TABLE_NAME, cols, CARD_ID + "=? AND " + TYPE + "=?",
                new String[]{ cardId, String.valueOf( type.ordinal() ) }, null, null, null, null );
        return cursorToActions( cursor );
    }

    public void createDelete( Card card ) {
        ContentValues values = new ContentValues();
        values.put( TYPE, Type.DELETE.ordinal() );
        values.put( CARD_ID, card.getId() );
        database.insert( TABLE_NAME, null, values );
    }

    public void createDelete( String cardId ) {
        ContentValues values = new ContentValues();
        values.put( TYPE, Type.DELETE.ordinal() );
        values.put( CARD_ID, cardId );
        database.insert( TABLE_NAME, null, values );
    }

    public void createCreate( Card card ) {
        ContentValues values = new ContentValues();
        values.put( TYPE, Type.CREATE.ordinal() );
        values.put( CARD_ID, card.getId() );
        database.insert( TABLE_NAME, null, values );
    }

    public void createMove( Card card ) {
        delete( card.getId(), Type.MOVE );
        ContentValues values = new ContentValues();
        values.put( TYPE, Type.MOVE.ordinal() );
        values.put( CARD_ID, card.getId() );
        database.insert( TABLE_NAME, null, values );
    }

    public void createUpdate( Card card ) {
        delete( card.getId(), Type.UPDATE_NAME );
        ContentValues values = new ContentValues();
        values.put( TYPE, Type.UPDATE_NAME.ordinal() );
        values.put( CARD_ID, card.getId() );
        database.insert( TABLE_NAME, null, values );
    }

    public void setClockedOff( Card card ) {
        cardDAO.setClockedOff( card.getId() );
        createMove( card );
    }

    public void setClockedOn( Card card ) {
        cardDAO.setClockedOn( card.getId() );
        createMove( card );
    }

    public void setDone( Card card ) {
        cardDAO.setDone( card.getId() );
        createMove( card );
    }

    public void setToday( Card card ) {
        cardDAO.setToday( card.getId() );
        createMove( card );
    }

    public void setTodo( Card card ) {
        cardDAO.setTodo( card.getId() );
        createMove( card );
    }

    private ArrayList<Action> cursorToActions( Cursor cursor ) {
        ArrayList<Action> actions = new ArrayList<>();
        if ( cursor != null ) {
            HashMap<String, Card> cardReg = new HashMap<>();
            while ( cursor.moveToNext() ) {
                Action.Type type = Action.Type.values()[ cursor.getInt( 1 ) ];
                int id = cursor.getInt( 0 );
                String cardId = String.valueOf( cursor.getInt( 2 ) );
                Card card = cardReg.get( cardId );
                if ( card == null ) {
                    card = cardDAO.findById( cardId );
                    cardReg.put( cardId, card );
                }
                switch ( type ) {
                    case CREATE:
                        actions.add( new CreateAction( id, type, card, trello, deadlineDAO, attachmentDAO ) );
                        break;
                    case MOVE:
                        actions.add( new MoveAction( id, type, card, trello ) );
                        break;
                    case UPDATE_NAME:
                        actions.add( new UpdateNameAction( id, type, card, trello ) );
                        break;
                    case DELETE:
                        actions.add( new DeleteAction( id, type, card, trello, cardDAO ) );
                        break;
                }
            }
            cursor.close();
        }
        return actions;
    }

    public CardDAO getCardDAO() {
        return cardDAO;
    }
}
