package com.zode64.trellodoing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.zode64.trellodoing.models.CardsStatus;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.widget.DoingWidget;

public class SettingsFragment extends PreferenceFragment {

    private TrelloManager mTrelloManager;

    private EditTextPreference mToken;
    private Preference mGetToken;

    private ProgressDialog mProgress;

    private DoingPreferences mPreferences;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setHasOptionsMenu( true );
        addPreferencesFromResource( R.xml.settings );

        Preference mGetAppKey = findPreference( "get_app_key" );
        EditTextPreference mAppKey = ( EditTextPreference ) findPreference( "app_key" );
        mGetToken = findPreference( "get_token" );
        mToken = ( EditTextPreference ) findPreference( "token" );
        mPreferences = new DoingPreferences( getActivity() );

        mTrelloManager = new TrelloManager( mPreferences );

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
        } );

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
        } );

    }

    @Override
    public void onResume() {
        super.onResume();

        mProgress = new ProgressDialog( getActivity() );
        mProgress.setTitle( getString( R.string.loading ) );
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
        protected Boolean doInBackground( Void... v ) {
            return mTrelloManager.get( mTrelloManager.tokenUrl() );
        }

        @Override
        protected void onPostExecute( Boolean result ) {
            mProgress.dismiss();
            if ( result ) {
                mToken.setEnabled( true );
                mGetToken.setEnabled( true );
            } else {
                mToken.setEnabled( false );
                mGetToken.setEnabled( false );
            }
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }
    }


    private class TokenChecker extends AsyncTask<Void, Void, CardsStatus> {

        @Override
        protected CardsStatus doInBackground( Void... v ) {
            return mTrelloManager.boards();
        }

        @Override
        protected void onPostExecute( CardsStatus result ) {
            mProgress.dismiss();
            if ( result != null && result.getMemberId() != null && !isDetached() ) {
                mToken.setEnabled( true );
                mGetToken.setEnabled( true );
                mPreferences.setUserId( result.getMemberId() );
                Intent refresh = new Intent( DoingWidget.ACTION_SYNC );
                if ( getActivity() != null ) {
                    getActivity().startService( refresh );
                }
            }
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }
    }
}
