package com.zode64.trellodoing;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.models.Board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BoardSelector extends PreferenceFragment {

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        BoardDAO boardDAO = new BoardDAO( getActivity() );

        addPreferencesFromResource( R.xml.board_selector );

        ArrayList<Board> boards = boardDAO.all();
        String[] boardNames = new String[ boards.size() ];
        String[] boardIds = new String[ boards.size() ];

        final ListPreference listPreference = ( ListPreference ) getPreferenceManager().findPreference( DoingPreferences.LAST_BOARD );

        final Map<String, String> boardReg = new HashMap<>();
        for ( int i = 0; i < boards.size(); i++ ) {
            Board board = boards.get( i );
            boardNames[ i ] = board.getName();
            boardIds[ i ] = board.getId();
            boardReg.put( board.getId(), board.getName() );
        }

        listPreference.setTitle( boardReg.get( listPreference.getValue() ) );
        listPreference.setEntries( boardNames );
        listPreference.setEntryValues( boardIds );
        listPreference.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                OnPreferenceChangeListener listener = ( OnPreferenceChangeListener ) getActivity();
                listener.onPreferenceChange( preference, newValue );
                listPreference.setTitle( boardReg.get( newValue ) );
                return true;
            }
        } );
    }

}
