package com.zode64.trellodoing;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Member;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Scanner;

public class TrelloManager {

    private final static String TAG = TrelloManager.class.getName();

    private final static String TRELLO_URL = "https://trello.com/1";

    private SharedPreferences mPreferences;

    public TrelloManager(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    public String appKeyUrl() {
        return TRELLO_URL + "/appKey/generate";
    }

    public String tokenUrl() {
        return TRELLO_URL + "/authorize?key=" + mPreferences.getString("app_key", "")
                + "&name=Trello+Doing&expiration=never&response_type=token&scope=read,write";
    }

    public List<Board> boards() {
        try {
            return get("/members/me/boards?filter=open&lists=open&fields=name,shortUrl", new TypeToken<List<Board>>() {}.getType());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Action> actions() {
        try {
            return get("/members/me/actions?filter=updateCard:idList&memberCreator=false&fields=data",
                            new TypeToken<List<Action>>() {}.getType());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Member member() {
        try {
            return get("/members/me?actions=updateCard:idList&action_fields=data&board_lists=all"
                            + "&fields=initials&boards=open&board_fields=lists,name",
                    Member.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Card> doingListCards(String listId) {
        try {
            return get("/lists/" + listId + "/cards", new TypeToken<List<Card>>() {}.getType());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean clockOff() {
        String cardId = mPreferences.getString("cardId", null);
        String clockedOffListId = mPreferences.getString("clockedOffListId", null);
        if(cardId == null || clockedOffListId == null) {
            throw new RuntimeException("Unexpected preference state : no cardId or clockedOffListId");
        }
        try {
            put("/cards/" + cardId + "/idList", clockedOffListId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String constructTrelloPutURL(String baseURL) {
        return TRELLO_URL + baseURL;
    }

    private String constructTrelloURL(String baseURL) {
        if (baseURL.contains("?")) {
            return TRELLO_URL + baseURL + "&key=" + mPreferences.getString("app_key", "")
                    + "&token=" + mPreferences.getString("token", "");

        } else {
            return TRELLO_URL + baseURL + "?key=" + mPreferences.getString("app_key", "")
                    + "&token=" + mPreferences.getString("token", "");
        }
    }

    /**
     * Must be called in AyncTask or from a service
     * @param path
     * @return
     */
    private <T> T get(String path, Class<T> type) throws IOException {
        Log.v(TAG, path + " - " + type.toString());
        URL to = null;
        HttpURLConnection urlConnection = null;
        try {
            to = new URL(constructTrelloURL(path));
            urlConnection = (HttpURLConnection) to.openConnection();
            InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
            Gson gson = new GsonBuilder().create();
            T member = gson.fromJson(new InputStreamReader(stream), type);
            stream.close();
            return member;
        }
        catch (IOException e) {
            Log.e(TAG, "IOException from GET request");
            e.printStackTrace();
            throw new IOException("Problem with URL : " + to + " or server");
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Must be called in AyncTask or from a service
     * @param path
     * @return
     */
    private List get(String path, Type type) throws IOException {
        Log.v(TAG, path + " - " + type.toString());
        URL to = null;
        HttpURLConnection urlConnection = null;
        try {
            to = new URL(constructTrelloURL(path));
            urlConnection = (HttpURLConnection) to.openConnection();
            InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
            Gson gson = new GsonBuilder().create();
            List boards = gson.fromJson(new InputStreamReader(stream), type);
            stream.close();
            return boards;
        }
        catch (IOException e) {
            Log.e(TAG, "IOException from GET request");
            e.printStackTrace();
            throw new IOException("Problem with URL : " + to + " or server");
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Must be called in AyncTask or from a service
     * @param path
     * @return
     */
    private void put(String path, String value) throws IOException {
        Log.v(TAG, path + " - " + value);
        URL to = null;
        HttpURLConnection urlConnection = null;
        try {
            to = new URL(constructTrelloPutURL(path));
            urlConnection = (HttpURLConnection) to.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write("value="+value);
            out.write("&token="+mPreferences.getString("token", ""));
            out.write("&key="+mPreferences.getString("app_key", ""));
            out.close();
            InputStream response = urlConnection.getInputStream();
            Log.v(TAG, "Output from PUT request : " + convertStreamToString(response));
            response.close();
        }
        catch (IOException e) {
            Log.e(TAG, "IOException from PUT request");
            e.printStackTrace();
            throw new IOException("Problem with URL : " + to + " or server");
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
