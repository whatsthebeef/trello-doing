package com.zode64.trellodoing.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zode64.trellodoing.models.CardDeserializer;
import com.zode64.trellodoing.models.MemberDeserializer;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Card.ListType;
import com.zode64.trellodoing.models.Member;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class TrelloManager {

    private final static String TAG = TrelloManager.class.getName();

    public final static String TRELLO_URL = "https://trello.com";
    public final static String TRELLO_URL_API = TRELLO_URL + "/1";

    private SharedPreferences mPreferences;

    public TrelloManager( SharedPreferences preferences ) {
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
        return "/authorize?key=" + mPreferences.getString( "app_key", "" )
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
            return TRELLO_URL_API + baseURL + "&key=" + mPreferences.getString( "app_key", "" )
                    + "&token=" + mPreferences.getString( "token", "" );

        } else {
            return TRELLO_URL_API + baseURL + "?key=" + mPreferences.getString( "app_key", "" )
                    + "&token=" + mPreferences.getString( "token", "" );
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
        return push( path, value, "PUT" );
    }

    private Card post( String path, String value ) throws IOException {
        return push( path, value, "POST" );
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
    private Card push( String path, String value, String method ) throws IOException {
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
            out.write( "token=" + mPreferences.getString( "token", "" ) );
            out.write( "&key=" + mPreferences.getString( "app_key", "" ) );
            out.write( value );
            out.close();
            InputStream stream = urlConnection.getInputStream();
            Gson gson = new GsonBuilder().registerTypeAdapter( Card.class, new CardDeserializer() ).create();
            Card model = gson.fromJson( new InputStreamReader( stream ), Card.class );
            stream.close();
            Log.v( TAG, "Output from " + method + " request : " + model.toString() );
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

    private String convertStreamToString( InputStream is ) {
        java.util.Scanner s = new java.util.Scanner( is ).useDelimiter( "\\A" );
        return s.hasNext() ? s.next() : "";
    }

}
