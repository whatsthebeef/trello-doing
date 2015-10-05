package com.zode64.trellodoing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;

public class CardActionActivity extends Activity {

    private TextView cardText;

    private ImageButton clockOff;
    private ImageButton setDeadline;
    private ImageButton cancel;

    private TrelloManager trello;
    private CardDAO cardDAO;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.card_action );

        cardText = ( TextView ) findViewById( R.id.card_name );

        clockOff = ( ImageButton ) findViewById( R.id.clock_out );
        setDeadline = ( ImageButton ) findViewById( R.id.set_deadline );
        cancel = ( ImageButton ) findViewById( R.id.cancel );

        cardDAO = new CardDAO( getApplication() );

        final String cardId = getIntent().getStringExtra( DoingWidget.UpdateService.EXTRA_CARD_ID );
        final Card card = cardDAO.find( cardId );

        final DoingPreferences preferences = new DoingPreferences( this );
        trello = new TrelloManager( preferences.getSharedPreferences() );

        cardText.setText( card.getName() );

        clockOff.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ClockOffTask().execute( card );
            }
        } );

        setDeadline.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                cardDAO.setDeadline( cardId, preferences.getDelay() );
                startService( new Intent( getApplication(), DoingWidget.UpdateService.class ) );
                finish();
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                finish();
            }
        } );
    }

    @Override
    protected void onStart() {
        mProgress = new ProgressDialog( this );
        mProgress.setTitle( getString( R.string.loading ) );
        mProgress.setMessage( getString( R.string.wait_while_loading ) );
        super.onStart();
    }

    private class ClockOffTask extends AsyncTask<Card, Void, String> {

        @Override
        protected String doInBackground( Card... card ) {
            if ( trello.clockOff( card[ 0 ] ) ) {
                return null;
            } else {
                return card[ 0 ].getId();
            }
        }

        @Override
        protected void onPostExecute( String cardId ) {
            mProgress.dismiss();
            if ( cardId != null ) {
                cardDAO.setIsClockedOff( cardId );
            }
            startService( new Intent( getApplication(), DoingWidget.UpdateService.class ) );
            finish();
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }
    }
}
