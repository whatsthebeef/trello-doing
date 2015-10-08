package com.zode64.trellodoing;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Card.ListType;
import com.zode64.trellodoing.widget.DoingWidget;

import java.util.Calendar;
import java.util.Date;

public class CardActionFragment extends Fragment implements ConfigureDelayFragment.DelayChangeListener {

    private TextView cardText;
    private TextView deadlineText;

    private ImageButton clockOff;
    private ImageButton setDeadline;
    private ImageButton cancel;
    private ImageButton clockOn;
    private ImageButton done;
    private ImageButton today;

    private TrelloManager trello;
    private CardDAO cardDAO;
    private Card card;

    private WidgetAlarm alarm;

    private Activity activity;

    @Override

    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.card_action, container, false );

        cardText = ( TextView ) view.findViewById( R.id.card_name );

        clockOff = ( ImageButton ) view.findViewById( R.id.clock_out );
        setDeadline = ( ImageButton ) view.findViewById( R.id.set_deadline );
        cancel = ( ImageButton ) view.findViewById( R.id.cancel );
        clockOn = ( ImageButton ) view.findViewById( R.id.clock_on );
        done = ( ImageButton ) view.findViewById( R.id.done );
        today = ( ImageButton ) view.findViewById( R.id.today );
        deadlineText = ( TextView ) view.findViewById( R.id.deadline_text );

        activity = getActivity();

        cardDAO = new CardDAO( activity );
        String cardId = activity.getIntent().getStringExtra( DoingWidget.EXTRA_CARD_ID );
        card = cardDAO.find( cardId );

        if ( card.getInListType() == ListType.DOING ) {
            clockOff.setVisibility( View.VISIBLE );
            done.setVisibility( View.VISIBLE );
        } else if ( card.getInListType() == ListType.TODAY ) {
            clockOn.setVisibility( View.VISIBLE );
            done.setVisibility( View.VISIBLE );
        } else if ( card.getInListType() == ListType.CLOCKED_OFF ) {
            clockOn.setVisibility( View.VISIBLE );
            today.setVisibility( View.VISIBLE );
        }

        if ( card.hasDeadline() ) {
            deadlineText.setVisibility( View.VISIBLE );
            deadlineText.setText( getResources().getString( R.string.deadline_set_for )
                    + " " + TimeUtils.format( new Date( card.getDeadline() ) ) );
        }

        final DoingPreferences preferences = new DoingPreferences( activity );
        alarm = new WidgetAlarm( activity, preferences );
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
                new ClockOffTask( activity ).execute( card );
            }
        } );

        clockOn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ClockOnTask( activity ).execute( card );
            }
        } );

        done.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new DoneTask( activity ).execute( card );
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
                new TodayTask( activity ).execute( card );
                activity.startService( new Intent( activity, DoingWidget.UpdateService.class ) );
                activity.finish();
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                activity.finish();
            }
        } );
        return view;
    }

    @Override
    public void onDestroy() {
        cardDAO.closeDB();
        super.onDestroy();
    }

    @Override
    public void onChange( Double delay ) {
        Calendar deadline = alarm.deadlineAlarm( delay );
        cardDAO.setDeadline( card.getId(), deadline.getTimeInMillis() );
        activity.startService( new Intent( DoingWidget.ACTION_SET_ALARM ) );
        activity.finish();
    }

    @Override
    public void reset() {
        cardDAO.resetDeadline( card.getId() );
        activity.startService( new Intent( DoingWidget.ACTION_SET_ALARM ) );
    }

    @Override
    public long getExisting() {
        return card.getDeadline();
    }

    public Card getCard() {
        return card;
    }

    private class ClockOffTask extends TrelloTask {
        public ClockOffTask( Activity activity ) {
            super( activity );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !trello.clockOff( card[ 0 ] ) ) {
                dao.setClockedOff( card[ 0 ].getId() );
            }
            return null;
        }
    }

    private class ClockOnTask extends TrelloTask {
        public ClockOnTask( Activity activity ) {
            super( activity );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !trello.clockOn( card[ 0 ] ) ) {
                dao.setClockedOn( card[ 0 ].getId() );
            }
            return null;
        }
    }

    private class DoneTask extends TrelloTask {

        public DoneTask( Activity activity ) {
            super( activity );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !trello.done( card[ 0 ] ) ) {
                dao.setDone( card[ 0 ].getId() );
            }
            return null;
        }
    }

    private class TodayTask extends TrelloTask {

        public TodayTask( Activity activity ) {
            super( activity );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !trello.today( card[ 0 ] ) ) {
                dao.setToday( card[ 0 ].getId() );
            }
            return null;
        }

    }
}
