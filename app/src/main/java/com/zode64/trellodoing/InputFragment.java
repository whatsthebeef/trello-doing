package com.zode64.trellodoing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class InputFragment extends DoingFragment {

    private EditText newCardName;

    private TextChangeListener listener;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.input, container, false );

        listener = ( TextChangeListener ) getActivity();

        Button submit = ( Button ) view.findViewById( R.id.submit );
        Button cancel = ( Button ) view.findViewById( R.id.cancel );

        newCardName = ( EditText ) view.findViewById( R.id.new_card_name );

        newCardName.setText( listener.getCard().getName() );

        submit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                listener.onTextChange( newCardName.getText().toString() );
                getActivity().onBackPressed();
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                getActivity().onBackPressed();
            }
        } );

        return view;
    }

    interface TextChangeListener extends CardGetter {
        void onTextChange( String text );
    }
}

