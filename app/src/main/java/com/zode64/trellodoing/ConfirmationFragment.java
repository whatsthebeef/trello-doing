package com.zode64.trellodoing;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class ConfirmationFragment extends DoingFragment {

    private ImageButton confirmed;
    private ImageButton cancel;

    private TextView instruction;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.confirmation, container, false );

        confirmed = ( ImageButton ) view.findViewById( R.id.confirmed );
        cancel = ( ImageButton ) view.findViewById( R.id.cancel );

        instruction = ( TextView ) view.findViewById( R.id.instruction );
        instruction.setText( ( ( ConfirmationListener ) getActivity() ).getConfirmationInstruction() );

        confirmed.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                ( ( ConfirmationListener ) getActivity() ).onConfirmation();
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

    interface ConfirmationListener {
        void onConfirmation();
        String getConfirmationInstruction();
    }
}

