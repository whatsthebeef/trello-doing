package com.zode64.trellodoing.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zode64.trellodoing.DoingPreferences;
import com.zode64.trellodoing.models.Attachment;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Card.ListType;
import com.zode64.trellodoing.models.CardDeserializer;
import com.zode64.trellodoing.models.Member;
import com.zode64.trellodoing.models.MemberDeserializer;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class TrelloManager {

    private final static String TAG = TrelloManager.class.getName();

    public final static String TRELLO_URL = "https://trello.com";
    public final static String TRELLO_URL_API = TRELLO_URL + "/1";

    public final static int SUCCESS = 0;
    public final static int FILE_NOT_FOUND = 1;
    public final static int FAILED = 2;

    private DoingPreferences mPreferences;

    public TrelloManager( DoingPreferences preferences ) {
        mPreferences = preferences;
    }

    public String appKeyUrl() {
        return TRELLO_URL_API + appKeyPath();
    }

    public String appKeyPath() {
        return "/appKey/generate";
    }

    public String tokenUrl() {
        return TRELLO_URL_API + tokenPath();
    }

    public String tokenPath() {
        return "/authorize?key=" + mPreferences.getAppKey()
                + "&name=Trello+Doing&expiration=never&response_type=token&scope=read,write";
    }

    public Member member() {
        try {
            return get( "/members/me?actions=updateCard:idList&action_fields=data,date&board_lists=open"
                            + "&fields=initials&boards=starred&board_fields=lists,name,shortLink&actions_limit=200",
                    Member.class );
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean clockOff( Card card ) {
        return moveCard( card.getServerId(), card.getListId( ListType.CLOCKED_OFF ) );
    }

    public boolean clockOn( Card card ) {
        return moveCard( card.getServerId(), card.getListId( ListType.DOING ) );
    }

    public boolean done( Card card ) {
        return moveCard( card.getServerId(), card.getListId( ListType.DONE ) );
    }

    public boolean today( Card card ) {
        return moveCard( card.getServerId(), card.getListId( ListType.TODAY ) );
    }

    public boolean todo( Card card ) {
        return moveCard( card.getServerId(), card.getListId( ListType.TODO ) );
    }

    public boolean thisWeek( Card card ) {
        return moveCard( card.getServerId(), card.getListId( ListType.THIS_WEEK ) );
    }

    public boolean moveCard( String cardId, String toListId ) {
        try {
            put( "/cards/" + cardId + "/idList", "&value=" + toListId );
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }
    }

    public Card createCard( Card card ) {
        try {
            Card persisted = post( "/cards", "&name=" + card.getName() + "&idList="
                    + card.getListId( ListType.TODO ) );
            card.setServerId( persisted.getServerId() );
            return card;
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateCardName( String cardId, String newName ) {
        try {
            put( "/cards/" + cardId + "/name", "&value=" + newName );
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCard( String cardId ) {
        try {
            delete( "/cards/" + cardId );
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }
    }

    private String constructTrelloPutURL( String baseURL ) {
        return TRELLO_URL_API + baseURL;
    }

    private String constructTrelloURL( String baseURL ) {
        if ( baseURL.contains( "?" ) ) {
            return TRELLO_URL_API + baseURL + "&key=" + mPreferences.getAppKey()
                    + "&token=" + mPreferences.getToken();

        } else {
            return TRELLO_URL_API + baseURL + "?key=" + mPreferences.getAppKey()
                    + "&token=" + mPreferences.getToken();
        }
    }

    public boolean get( String url ) {
        URL to = null;
        HttpURLConnection urlConnection = null;
        try {
            to = new URL( url );
            Log.i( TAG, to.toString() );
            urlConnection = ( HttpURLConnection ) to.openConnection();
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
            Log.w( TAG, "IOException from GET request" );
            Log.w( TAG, "Problem with URL : " + to + " or server" );
            return false;
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
    }

    public int postAttachment( Attachment attachment ) {
        ContentBody contentPart = new FileBody( new File( attachment.getPath() ) );
        /*
        if(attachment.getType() == Attachment.Type.PHOTO) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = Bitmap.createBitmap( BitmapFactory.decodeFile( attachment.getPath(), bmOptions ) );
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress( Bitmap.CompressFormat.JPEG, 10, bos );
            contentPart = new ByteArrayBody( bos.toByteArray(), attachment.getFilename() );
        }
        else {
            contentPart = new FileBody( new File( attachment.getPath() ) );
        }
        */
        MultipartEntity reqEntity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
        reqEntity.addPart( "file", contentPart );
        return multipost( "/cards/" + attachment.getCardServerId() + "/attachments", reqEntity );
    }

    /**
     * Must be called in AyncTask or from a service
     *
     * @param path
     * @return
     */
    private <T> T get( String path, Class<T> type ) throws IOException {
        Log.v( TAG, path + " - " + type.toString() );
        URL to = null;
        HttpURLConnection urlConnection = null;
        try {
            to = new URL( constructTrelloURL( path ) );
            Log.i( TAG, to.toString() );
            urlConnection = ( HttpURLConnection ) to.openConnection();
            InputStream stream = new BufferedInputStream( urlConnection.getInputStream() );
            Gson gson = new GsonBuilder().registerTypeAdapter( Member.class, new MemberDeserializer() ).create();
            T member = gson.fromJson( new InputStreamReader( stream ), type );
            stream.close();
            return member;
        } catch ( IOException e ) {
            Log.e( TAG, "IOException from GET request" );
            e.printStackTrace();
            throw new IOException( "Problem with URL : " + to + " or server" );
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
    }

    private Card put( String path, String value ) throws IOException {
        return push( path, value, "PUT", Card.class );
    }

    private Card post( String path, String value ) throws IOException {
        return push( path, value, "POST", Card.class );
    }

    private void delete( String path ) throws IOException {
        URL to = null;
        HttpURLConnection urlConnection = null;
        Log.i( TAG, "Push to " + path );
        try {
            to = new URL( constructTrelloURL( path ) );
            urlConnection = ( HttpURLConnection ) to.openConnection();
            urlConnection.setRequestMethod( "DELETE" );
            urlConnection.getResponseCode();
        } catch ( IOException e ) {
            Log.e( TAG, "IOException from DELETE request" );
            e.printStackTrace();
            throw new IOException( "Problem with URL : " + to + " or server" );
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Must be called in AyncTask or from a service
     *
     * @param path
     * @return
     */
    private <T> T push( String path, String value, String method, Class<T> type )
            throws IOException {
        Log.v( TAG, path + " - " + value );
        URL to = null;
        HttpURLConnection urlConnection = null;
        Log.i( TAG, "Push to " + path + " with value " + value );
        try {
            to = new URL( constructTrelloPutURL( path ) );
            urlConnection = ( HttpURLConnection ) to.openConnection();
            urlConnection.setDoOutput( true );
            urlConnection.setRequestMethod( method );
            OutputStreamWriter out = new OutputStreamWriter( urlConnection.getOutputStream() );
            out.write( "token=" + mPreferences.getToken() );
            out.write( "&key=" + mPreferences.getAppKey() );
            out.write( value );
            out.close();
            InputStream inputStream = urlConnection.getInputStream();
            Gson gson = new GsonBuilder().registerTypeAdapter( type, new CardDeserializer() ).create();
            T model = gson.fromJson( new InputStreamReader( inputStream ), type );
            Log.v( TAG, "Output from " + method + " request : " + model.toString() );
            inputStream.close();
            return model;
        } catch ( IOException e ) {
            Log.e( TAG, "IOException from " + method + " request" );
            e.printStackTrace();
            throw new IOException( "Problem with URL : " + to + " or server" );
        } finally {
            if ( urlConnection != null ) {
                urlConnection.disconnect();
            }
        }
    }

    private int multipost( String path, MultipartEntity reqEntity ) {
        try {
            URL url = new URL( constructTrelloURL( path ) );
            Log.i( TAG, "Uploading file to: " + url.toString() );
            HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
            conn.setReadTimeout( 10000 );
            conn.setConnectTimeout( 15000 );
            conn.setRequestMethod( "POST" );
            conn.setUseCaches( false );
            conn.setDoInput( true );
            conn.setDoOutput( true );

            conn.setFixedLengthStreamingMode( ( int ) reqEntity.getContentLength() );
            conn.setRequestProperty( "Connection", "Keep-Alive" );
            conn.addRequestProperty( reqEntity.getContentType().getName(), reqEntity.getContentType().getValue() );

            OutputStream os = conn.getOutputStream();
            reqEntity.writeTo( conn.getOutputStream() );
            os.close();
            conn.connect();

            if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                return SUCCESS;
            } else {
                Log.w( TAG, "Response code: " + conn.getResponseCode() );
            }

        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
            return FILE_NOT_FOUND;
        } catch ( Exception e ) {
            e.printStackTrace();
            Log.w( TAG, "multipart post error " + e + "(" + path + ")" );
        }
        return FAILED;
    }

    private String readStream( InputStream in ) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader( new InputStreamReader( in ) );
            String line = "";
            while ( ( line = reader.readLine() ) != null ) {
                builder.append( line );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( reader != null ) {
                try {
                    reader.close();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    private String convertStreamToString( InputStream is ) {
        java.util.Scanner s = new java.util.Scanner( is ).useDelimiter( "\\A" );
        return s.hasNext() ? s.next() : "";
    }

}
