package com.zode64.trellodoing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;

/**
 * Created by john on 9/22/15.
 */
public class PersonalTodoAdder extends Activity {

    private Button mSubmit;
    private Button mCancel;
    private EditText mEditText;
    private CardDAO cardDAO;
    private TrelloManager trelloManager;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.new_card );

        cardDAO = new CardDAO( getApplication() );

        mSubmit = ( Button ) findViewById( R.id.submit_new_card );
        mCancel = ( Button ) findViewById( R.id.cancel_new_card );
        mEditText = ( EditText ) findViewById( R.id.new_card_name );

        Card personalCard = cardDAO.getPersonalCard();
        if ( personalCard != null ) {
            mEditText.setText( personalCard.getName() );
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

                    trelloManager = new TrelloManager( PreferenceManager.getDefaultSharedPreferences( getApplication() ) );
                    if ( !trelloManager.newPersonalCard( text ) ) {
                        cardDAO.createPersonalCard( text );
                    }
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
