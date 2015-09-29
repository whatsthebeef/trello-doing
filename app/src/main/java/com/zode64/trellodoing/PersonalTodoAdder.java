package com.zode64.trellodoing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by john on 9/22/15.
 */
public class PersonalTodoAdder extends Activity {

    private Button mSubmit;
    private Button mCancel;
    private EditText mEditText;
    private DoingPreferences preferences;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.new_card );

        preferences = new DoingPreferences( this );

        mSubmit = ( Button ) findViewById( R.id.submit_new_card );
        mCancel = ( Button ) findViewById( R.id.cancel_new_card );
        mEditText = ( EditText ) findViewById( R.id.new_card_name );

        if ( preferences.hasPersonalCardName() ) {
            mEditText.setText( preferences.getPersonalCardName() );
        } else {
            mSubmit.setEnabled( false );
        }

        mEditText.addTextChangedListener( new TextWatcher() {
                                              @Override
                                              public void beforeTextChanged( CharSequence s, int start, int count, int after ) {
                                              }

                                              @Override
                                              public void onTextChanged( CharSequence s, int start, int before, int count ) {
                                              }

                                              @Override
                                              public void afterTextChanged( Editable s ) {
                                                  if ( s.length() > 0 ) {
                                                      mSubmit.setEnabled( true );
                                                  } else {
                                                      mSubmit.setEnabled( false );
                                                  }

                                              }
                                          }
        );


        mSubmit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String text = mEditText.getText().toString();
                if ( text != null && !text.equals( "" ) ) {
                    preferences.handleSetAddPersonalCard( mEditText.getText().toString() );
                    startService( new Intent( getApplication(), DoingWidget.UpdateService.class ) );
                    finish();
                }
            }
        } );

        mCancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                finish();
            }
        } );
    }
}
