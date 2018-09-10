package com.zode64.trellodoing;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ConfirmationFragment extends DoingFragment {

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.confirmation, container, false );

        Button confirmed = ( Button ) view.findViewById( R.id.confirmed );
        Button cancel = ( Button ) view.findViewById( R.id.cancel );

        TextView instruction = ( TextView ) view.findViewById( R.id.instruction );
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

