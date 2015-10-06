package com.zode64.trellodoing;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class ConfigureDelayFragment extends Fragment {

    private ImageButton done;
    private ImageButton cancel;

    private EditText delayInput;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.configure_delay_action, container, false );
        done = ( ImageButton ) view.findViewById( R.id.done );
        cancel = ( ImageButton ) view.findViewById( R.id.cancel );
        delayInput = ( EditText ) view.findViewById( R.id.delay_input );

        final String cardId = getActivity().getIntent().getStringExtra( DoingWidget.EXTRA_CARD_ID );

        final DoingPreferences preferences = new DoingPreferences( getActivity() );

        String existingDelay = preferences.getDelay().toString();
        if ( existingDelay != null && !"".equals( existingDelay ) ) {
            delayInput.setText( existingDelay );
        } else {
            done.setEnabled( false );
        }

        done.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if ( delayInput.getText() != null && !"".equals( delayInput.getText() ) ) {
                    String delay = delayInput.getText().toString();
                    preferences.setDelay( delay );
                    ( ( DelayChangeListener ) getActivity() ).onChange( Double.parseDouble( delay ), cardId );
                }
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                getActivity().onBackPressed();
            }
        } );

        delayInput.addTextChangedListener( new TextWatcher() {
                                               @Override
                                               public void beforeTextChanged( CharSequence s, int start, int count, int after ) {
                                               }

                                               @Override
                                               public void onTextChanged( CharSequence s, int start, int before, int count ) {
                                               }

                                               @Override
                                               public void afterTextChanged( Editable s ) {
                                                   if ( s.length() > 0 ) {
                                                       done.setEnabled( true );
                                                   } else {
                                                       done.setEnabled( false );
                                                   }

                                               }
                                           }
        );

        return view;
    }

    interface DelayChangeListener {
        void onChange( Double delay, String cardId );
    }
}

