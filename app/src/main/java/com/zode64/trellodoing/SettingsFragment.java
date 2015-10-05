package com.zode64.trellodoing;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by john on 9/18/15.
 */
public class SettingsFragment extends PreferenceFragment {

    private TrelloManager mTrelloManager;

    private EditTextPreference mAppKey;
    private EditTextPreference mToken;
    private Preference mGetToken;
    private Preference mGetAppKey;

    private ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setHasOptionsMenu( true );
        addPreferencesFromResource( R.xml.settings );

        mGetAppKey = findPreference( "get_app_key" );
        mAppKey = (EditTextPreference) findPreference( "app_key" );
        mGetToken = findPreference( "get_token" );
        mToken = (EditTextPreference) findPreference( "token" );

        mTrelloManager = new TrelloManager( PreferenceManager.getDefaultSharedPreferences( getActivity() ));

        mGetAppKey.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick( Preference preference ) {
                Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( mTrelloManager.appKeyUrl() ) );
                startActivity( browserIntent );
                return true;
            }
        } );

        mAppKey.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                checkAppKey();
                return true;
            }
        });

        mGetToken.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick( Preference preference ) {
                Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( mTrelloManager.tokenUrl() ) );
                startActivity( browserIntent );
                return true;
            }
        } );

        mToken.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                checkToken();
                return true;
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        mProgress = new ProgressDialog(getActivity());
        mProgress.setTitle(getString( R.string.loading ));
        mProgress.setMessage( getString( R.string.wait_while_loading ) );

        // checkAppKey();
        checkToken();
    }

    private void checkAppKey() {
        new AppKeyChecker().execute();
    }

    private void checkToken() {
        new TokenChecker().execute();
    }

    private class AppKeyChecker extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... v) {
            return mTrelloManager.get(mTrelloManager.tokenUrl());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgress.dismiss();
            if(result) {
                mToken.setEnabled( true );
                mGetToken.setEnabled( true );
            }
            else {
                mToken.setEnabled( false );
                mGetToken.setEnabled( false );
            }
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }
    }


    private class TokenChecker extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... v) {
            return mTrelloManager.member() != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgress.dismiss();
            if(result && !isDetached() ) {
                mToken.setEnabled( true );
                mGetToken.setEnabled( true );
                Intent refresh = new Intent( DoingWidget.UpdateService.ACTION_REFRESH );
                getActivity().startService( refresh );
            }
            else {
                mToken.setText( "" );
            }
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }
    }
}
