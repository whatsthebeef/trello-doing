package com.zode64.trellodoing.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.zode64.trellodoing.DoingPreferences;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.BoardsDeserializer;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.CardDeserializer;
import com.zode64.trellodoing.models.CardsDeserializer;
import com.zode64.trellodoing.models.Member;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class TrelloManager {

    private final static String TAG = TrelloManager.class.getName();

    private final static int REQUEST_TIMEOUT = 8000;

    private final static String TRELLO_URL = "https://trello.com";
    private final static String TRELLO_URL_API = TRELLO_URL + "/1";

    private DoingPreferences mPreferences;

    public TrelloManager( DoingPreferences preferences ) {
        mPreferences = preferences;
    }

    public String appKeyUrl() {
        return TRELLO_URL_API + appKeyPath();
    }

    private String appKeyPath() {
        return "/appKey/generate";
    }

    public String tokenUrl() {
        return TRELLO_URL_API + tokenPath();
    }

    private String tokenPath() {
        return "/authorize?key=" + mPreferences.getAppKey()
                + "&name=Trello+Doing&expiration=never&response_type=token&scope=read,write";
    }

    public Member cards( HashMap<String, Board> boardReg ) {
        try {
            return get( "/members/me?actions=updateCard:idList&action_fields=data,date&fields=initials&actions_limit=250",
                    Member.class, new CardsDeserializer( boardReg ) );
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }

    public Member cards2( HashMap<String, Board> boardReg ) {
        try {
            return get( "/members/me?actions=updateCard:idList&action_fields=data,date&fields=initials&actions_limit=250",
                    Member.class, new CardsDeserializer( boardReg ) );
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }

    public Member boards() {
        try {
            return get( "/members/me?board_lists=open&boards=starred&fields=initials&board_fields=lists,name,shortLink,idOrganization",
                    Member.class, new BoardsDeserializer() );
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean moveCard( String cardServerId, String toListId ) {
        try {
            put( "/cards/" + cardServerId + "/idList", "&value=" + toListId );
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }
    }

    public Card createCard( Card card ) {
        try {
            Card persisted = post( "&name=" + card.getName() + "&idList="
                    + card.getBoardListId( Board.ListType.TODO ) );
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

    /**
     * Must be called in AyncTask or from a service
     */
    private <T> T get( String path, Class<T> type, JsonDeserializer deserializer ) throws IOException {
        Log.v( TAG, path + " - " + type.toString() );
        URL to = null;
        HttpURLConnection urlConnection = null;
        try {
            to = new URL( constructTrelloURL( path ) );
            Log.i( TAG, to.toString() );
            urlConnection = ( HttpURLConnection ) to.openConnection();
            urlConnection.setReadTimeout( REQUEST_TIMEOUT );
            urlConnection.setConnectTimeout( REQUEST_TIMEOUT );
            InputStream stream = new BufferedInputStream( urlConnection.getInputStream() );
            Gson gson = new GsonBuilder().registerTypeAdapter( Member.class, deserializer ).create();
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

    private void put( String path, String value ) throws IOException {
        push( path, value, "PUT", Card.class );
    }

    private Card post( String value ) throws IOException {
        return push( "/cards", value, "POST", Card.class );
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
            urlConnection.setReadTimeout( REQUEST_TIMEOUT );
            urlConnection.setConnectTimeout( REQUEST_TIMEOUT );
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
            urlConnection.setReadTimeout( REQUEST_TIMEOUT );
            urlConnection.setConnectTimeout( REQUEST_TIMEOUT );
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
}


