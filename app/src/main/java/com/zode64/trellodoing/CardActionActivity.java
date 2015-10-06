package com.zode64.trellodoing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Card.ListType;

import java.util.Calendar;

public class CardActionActivity extends Activity implements ConfigureDelayFragment.DelayChangeListener {

    private TextView cardText;

    private ImageButton clockOff;
    private ImageButton setDeadline;
    private ImageButton cancel;
    private ImageButton clockOn;
    private ImageButton done;
    private ImageButton today;

    private TrelloManager trello;
    private CardDAO cardDAO;
    private Card card;

    private ProgressDialog mProgress;
    private WidgetAlarm alarm;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.card_action );

        cardText = ( TextView ) findViewById( R.id.card_name );

        clockOff = ( ImageButton ) findViewById( R.id.clock_out );
        setDeadline = ( ImageButton ) findViewById( R.id.set_deadline );
        cancel = ( ImageButton ) findViewById( R.id.cancel );
        clockOn = ( ImageButton ) findViewById( R.id.clock_on );
        done = ( ImageButton ) findViewById( R.id.done );
        today = ( ImageButton ) findViewById( R.id.today );

        ListType listType = ListType.values()[
                getIntent().getIntExtra( DoingWidget.EXTRA_LIST_TYPE_ORDINAL, 0 ) ];
        if ( listType == ListType.DOING ) {
            clockOff.setVisibility( View.VISIBLE );
            done.setVisibility( View.VISIBLE );
        } else if ( listType == ListType.TODAY ) {
            clockOn.setVisibility( View.VISIBLE );
            done.setVisibility( View.VISIBLE );
        } else if ( listType == ListType.CLOCKED_OFF ) {
            clockOn.setVisibility( View.VISIBLE );
            today.setVisibility( View.VISIBLE );
        }

        cardDAO = new CardDAO( getApplication() );

        final String cardId = getIntent().getStringExtra( DoingWidget.EXTRA_CARD_ID );
        card = cardDAO.find( cardId );

        final DoingPreferences preferences = new DoingPreferences( this );
        alarm = new WidgetAlarm( this, preferences );
        trello = new TrelloManager( preferences.getSharedPreferences() );

        cardText.setText( card.getName() );

        cardText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Intent getBoard = new Intent( Intent.ACTION_VIEW, Uri.parse( card.getBoardShortUrl() ) );
                startActivity( getBoard );
            }
        } );

        clockOff.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ClockOffTask().execute( card );
            }
        } );

        clockOn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ClockOnTask().execute( card );
            }
        } );

        done.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new DoneTask().execute( card );
            }
        } );

        setDeadline.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                getFragmentManager().beginTransaction().replace( R.id.card_actions,
                        new ConfigureDelayFragment() ).commit();
            }
        } );

        today.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new TodayTask().execute( card );
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

    @Override
    public void onChange(Double delay, String cardId) {
        Calendar deadline = alarm.deadlineAlarm( delay );
        cardDAO.setDeadline( cardId, deadline.getTimeInMillis() );
        startService( new Intent( getApplication(), DoingWidget.UpdateService.class ) );
        finish();
    }

    public Card getCard() {
        return card;
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
                cardDAO.setClockedOff( cardId );
            }
            startService( new Intent( getApplication(), DoingWidget.UpdateService.class ) );
            finish();
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }
    }

    private class ClockOnTask extends AsyncTask<Card, Void, String> {

        @Override
        protected String doInBackground( Card... card ) {
            if ( trello.clockOn( card[ 0 ] ) ) {
                return null;
            } else {
                return card[ 0 ].getId();
            }
        }

        @Override
        protected void onPostExecute( String cardId ) {
            mProgress.dismiss();
            if ( cardId != null ) {
                cardDAO.setClockedOn( cardId );
            }
            startService( new Intent( getApplication(), DoingWidget.UpdateService.class ) );
            finish();
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }
    }

    private class DoneTask extends AsyncTask<Card, Void, String> {

        @Override
        protected String doInBackground( Card... card ) {
            if ( trello.done( card[ 0 ] ) ) {
                return null;
            } else {
                return card[ 0 ].getId();
            }
        }

        @Override
        protected void onPostExecute( String cardId ) {
            mProgress.dismiss();
            if ( cardId != null ) {
                cardDAO.setDone( cardId );
            }
            startService( new Intent( getApplication(), DoingWidget.UpdateService.class ) );
            finish();
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }
    }

    private class TodayTask extends AsyncTask<Card, Void, String> {

        @Override
        protected String doInBackground( Card... card ) {
            if ( trello.today( card[ 0 ] ) ) {
                return null;
            } else {
                return card[ 0 ].getId();
            }
        }

        @Override
        protected void onPostExecute( String cardId ) {
            mProgress.dismiss();
            if ( cardId != null ) {
                cardDAO.setToday( cardId );
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
