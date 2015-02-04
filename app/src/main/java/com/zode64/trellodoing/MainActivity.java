package com.zode64.trellodoing;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zode64.trellodoing.models.Board;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


public class MainActivity extends Activity {

    private TrelloManager mTrelloManager;

    private SharedPreferences mPreferences;

    private final static String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mTrelloManager = new TrelloManager(mPreferences);

        TextView appKeyLink = (TextView) findViewById(R.id.app_key_link);
        appKeyLink.setText(mTrelloManager.appKeyUrl());

        initToken();
        initAppKey();
    }

    private void initToken() {
        final EditText token = (EditText) findViewById(R.id.token);
        token.setText(mPreferences.getString("token", ""));
        Button submitToken = (Button) findViewById(R.id.token_submit);
        submitToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("token", token.getText().toString());
                editor.commit();
                retrieveBoards();
            }
        });
    }

    private void initAppKey() {
        final EditText appKey = (EditText) findViewById(R.id.app_key);
        appKey.setText(mPreferences.getString("app_key", ""));
        Button submitAppKey = (Button) findViewById(R.id.app_key_submit);
        submitAppKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("app_key", appKey.getText().toString());
                editor.commit();
                TextView link = (TextView) findViewById(R.id.token_link);
                link.setText(mTrelloManager.tokenUrl());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void retrieveBoards() {
        // TextView boards = (TextView) findViewById(R.id.boards);
    }


}
