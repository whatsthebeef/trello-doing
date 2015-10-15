package com.zode64.trellodoing;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class InputFragment extends DoingFragment {

    private ImageButton submit;
    private ImageButton cancel;

    private EditText newCardName;

    private TextChangeListener listener;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.input, container, false );

        listener = ( TextChangeListener ) getActivity();

        submit = ( ImageButton ) view.findViewById( R.id.submit );
        cancel = ( ImageButton ) view.findViewById( R.id.cancel );

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

