package com.zode64.trellodoing;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zode64.trellodoing.utils.TimeUtils;

public class ConfigureDelayFragment extends DoingFragment {

    private ImageButton done;
    private ImageButton cancel;
    private ImageButton delete;

    private EditText delayInput;

    private TextView existingDelayView;

    private DelayChangeListener listener;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.configure_delay_action, container, false );

        listener = ( DelayChangeListener ) getActivity();

        done = ( ImageButton ) view.findViewById( R.id.done );
        cancel = ( ImageButton ) view.findViewById( R.id.cancel );
        delete = ( ImageButton ) view.findViewById( R.id.delete );
        delayInput = ( EditText ) view.findViewById( R.id.delay_input );

        existingDelayView = ( TextView ) view.findViewById( R.id.existing_delay_text );

        Long delay = listener.getExistingDelay();

        if ( delay != null && delay > 0 ) {
            delayInput.setVisibility( View.GONE );
            done.setVisibility( View.GONE );
            delete.setVisibility( View.VISIBLE );
            existingDelayView.setText( getResources().getString( R.string.existing_delay ) + " " + TimeUtils.format( delay ) );
            existingDelayView.setVisibility( View.VISIBLE );
        }
        done.setEnabled( false );

        done.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String delay = delayInput.getText().toString();
                listener.onDelayChange( Double.parseDouble( delay ) );
                getActivity().onBackPressed();
            }
        } );

        delete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                listener.resetDelay();
                delayInput.setVisibility( View.VISIBLE );
                done.setVisibility( View.VISIBLE );
                delete.setVisibility( View.GONE );
                existingDelayView.setVisibility( View.GONE );
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
        void onDelayChange( Double delay );

        Long getExistingDelay();

        void resetDelay();
    }
}

