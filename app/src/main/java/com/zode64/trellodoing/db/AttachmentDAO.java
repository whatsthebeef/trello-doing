package com.zode64.trellodoing.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zode64.trellodoing.models.Attachment;

import java.util.ArrayList;

public class AttachmentDAO {

    private static final String TAG = AttachmentDAO.class.getName();

    private DoingDatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String TABLE_NAME = "attachments";

    public final static String ID = "id";
    public final static String FILENAME = "name";
    public final static String CARD_SERVER_ID = "cardServerId";
    public final static String TYPE = "type";
    public final static String IS_UPLOADED = "isUploaded";

    private String[] cols = new String[]{ ID, FILENAME, CARD_SERVER_ID, TYPE, IS_UPLOADED };

    /**
     * @param context
     */
    public AttachmentDAO( Context context ) {
        dbHelper = new DoingDatabaseHelper( context );
        database = dbHelper.getWritableDatabase();
    }

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FILENAME + " TEXT NOT NULL, " +
            CARD_SERVER_ID + " TEXT NOT NULL, " +
            TYPE + " INTEGER NOT NULL, " +
            IS_UPLOADED + " INTEGER NOT NULL " +
            ");";

    public static final String DELETE_TABLE = "DROP TABLE " + TABLE_NAME + " IF EXIST";

    public void create( Attachment attachment ) {
        Log.i( TAG, "Adding attachment with name: " + attachment.getFilename() );
        ContentValues values = new ContentValues();
        values.put( FILENAME, attachment.getFilename() );
        values.put( CARD_SERVER_ID, attachment.getCardServerId() );
        values.put( TYPE, attachment.getTypeOrdinal() );
        values.put( IS_UPLOADED, attachment.isUploaded() ? 1 : 0 );
        database.insert( TABLE_NAME, null, values );
    }

    public ArrayList<Attachment> allPending() {
        Log.i( TAG, "Fetching all attachments not uploaded" );
        Cursor cursor = database.query( true, TABLE_NAME, cols, IS_UPLOADED + "=0", null, null, null, null, null );
        return cursorToAttachments( cursor );
    }

    public ArrayList<Attachment> all() {
        Log.i( TAG, "Fetching all attachments" );
        Cursor cursor = database.query( true, TABLE_NAME, cols, null, null, null, null, null, null );
        return cursorToAttachments( cursor );
    }

    public void delete(int id) {
        Log.i( TAG, "Deleting id: " + id );
        database.delete( TABLE_NAME, ID + "=?", new String[]{ String.valueOf( id ) } );
    }

    public void deleteAll() {
        database.delete( TABLE_NAME, null, null );
    }

    public void setUploaded(int id) {
        Log.i( TAG, "Setting attachment with id : " + id + " to 'Uploaded'");
        ContentValues values = new ContentValues();
        values.put( IS_UPLOADED, 1 );
        database.update( TABLE_NAME, values, ID + "=?", new String[]{ String.valueOf( id ) } );
    }

    public void updateServerId( String cardServerId, String newCardServerId ) {
        ContentValues values = new ContentValues();
        values.put( CARD_SERVER_ID, newCardServerId );
        database.update( TABLE_NAME, values, CARD_SERVER_ID + "=?", new String[]{ cardServerId } );
    }

    public void closeDB() {
        database.close();
    }

    private ArrayList<Attachment> cursorToAttachments( Cursor cursor ) {
        ArrayList<Attachment> attachments = new ArrayList<>();
        if ( cursor != null ) {
            while ( cursor.moveToNext() ) {
                Attachment attachment = new Attachment();
                attachment.setId( cursor.getInt( 0 ) );
                attachment.setFilename( cursor.getString( 1 ) );
                attachment.setCardServerId( cursor.getString( 2 ) );
                attachment.setType( cursor.getInt( 3 ) );
                attachment.setUploaded( cursor.getInt( 4 ) == 0 ? false : true );
                attachments.add( attachment );
            }
            cursor.close();
        }
        return attachments;
    }
}
