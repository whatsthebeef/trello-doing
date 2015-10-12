package com.zode64.trellodoing;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class InputFragment extends Fragment {

    private ImageButton submit;
    private ImageButton cancel;

    private EditText newCardName;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.input, container, false );

        submit = ( ImageButton ) view.findViewById( R.id.submit );
        cancel = ( ImageButton ) view.findViewById( R.id.cancel );

        newCardName = ( EditText ) view.findViewById( R.id.new_card_name );

        newCardName.setText( ( ( InputChangeListener ) getActivity() ).getPlaceholder() );

        submit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                ( ( InputChangeListener ) getActivity() ).onChange( newCardName.getText().toString() );
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

    interface InputChangeListener {
        void onChange( String text );
        String getPlaceholder();
    }
}

