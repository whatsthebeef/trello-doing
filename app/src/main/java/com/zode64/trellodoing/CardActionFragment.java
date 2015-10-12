package com.zode64.trellodoing;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Card.ListType;

import java.util.ArrayList;
import java.util.Date;

public class CardActionFragment extends Fragment {

    private TextView cardText;

    private TextView deadlineText;
    private TextView noTodayListText;
    private TextView noDoingListText;
    private TextView noDoneListText;
    private TextView noClockedOffListText;
    private TextView noTodoListText;

    private ImageButton clockOff;
    private ImageButton setDeadline;
    private ImageButton clockOn;
    private ImageButton todo;
    private ImageButton done;
    private ImageButton today;
    private ImageButton delete;
    private ImageButton open;

    private TrelloManager trello;

    private Card card;

    private ActionActivity activity;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.card_action, container, false );

        cardText = ( TextView ) view.findViewById( R.id.card_name );

        clockOff = ( ImageButton ) view.findViewById( R.id.clock_out );
        setDeadline = ( ImageButton ) view.findViewById( R.id.set_deadline );
        clockOn = ( ImageButton ) view.findViewById( R.id.clock_on );
        done = ( ImageButton ) view.findViewById( R.id.done );
        today = ( ImageButton ) view.findViewById( R.id.today );
        todo = ( ImageButton ) view.findViewById( R.id.to_todo );
        delete = ( ImageButton ) view.findViewById( R.id.delete );
        open = ( ImageButton ) view.findViewById( R.id.open );

        deadlineText = ( TextView ) view.findViewById( R.id.deadline_text );
        noDoneListText = ( TextView ) view.findViewById( R.id.no_done_list );
        noDoingListText = ( TextView ) view.findViewById( R.id.no_doing_list );
        noClockedOffListText = ( TextView ) view.findViewById( R.id.no_clocked_off_list );
        noTodayListText = ( TextView ) view.findViewById( R.id.no_today_list );
        noTodoListText = ( TextView ) view.findViewById( R.id.no_todo_list );

        activity = ( ActionActivity ) getActivity();

        card = activity.getCard();

        if ( card.getInListType() == ListType.DOING ) {
            clockOff.setVisibility( View.VISIBLE );
            done.setVisibility( View.VISIBLE );
        } else if ( card.getInListType() == ListType.TODAY ) {
            clockOn.setVisibility( View.VISIBLE );
            todo.setVisibility( View.VISIBLE );
        } else if ( card.getInListType() == ListType.CLOCKED_OFF ) {
            clockOn.setVisibility( View.VISIBLE );
            today.setVisibility( View.VISIBLE );
        }

        if ( card.hasDeadline() ) {
            deadlineText.setVisibility( View.VISIBLE );
            deadlineText.setText( getResources().getString( R.string.deadline_set_for )
                    + " " + TimeUtils.format( new Date( card.getDeadline() ) ) );
        }

        if ( card.getListId( ListType.DOING ) == null ) {
            noDoingListText.setVisibility( View.VISIBLE );
            clockOn.setEnabled( false );
        }
        if ( card.getListId( ListType.DONE ) == null ) {
            noDoneListText.setVisibility( View.VISIBLE );
            done.setEnabled( false );
        }
        if ( card.getListId( ListType.TODAY ) == null ) {
            noTodayListText.setVisibility( View.VISIBLE );
            today.setEnabled( false );
        }
        if ( card.getListId( ListType.CLOCKED_OFF ) == null ) {
            noClockedOffListText.setVisibility( View.VISIBLE );
            clockOff.setEnabled( false );
        }
        if ( card.getListId( ListType.TODO ) == null ) {
            noTodoListText.setVisibility( View.VISIBLE );
            todo.setEnabled( false );
        }

        final DoingPreferences preferences = new DoingPreferences( activity );
        trello = new TrelloManager( preferences.getSharedPreferences() );

        cardText.setText( card.getName() );

        cardText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                getFragmentManager().beginTransaction().replace( R.id.card_actions,
                        new InputFragment() ).commit();
            }
        } );

        clockOff.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ClockOffTask( activity, trello ).execute( card );
            }
        } );

        clockOn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ClockOnTask( activity, trello ).execute( card );
            }
        } );

        done.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new DoneTask( activity, trello ).execute( card );
            }
        } );

        todo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new ToTodoTask( activity, trello ).execute( card );
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
                new TodayTask( activity, trello ).execute( card );
            }
        } );

        open.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Intent getBoard = new Intent( Intent.ACTION_VIEW, Uri.parse( card.getShortUrl() ) );
                startActivity( getBoard );
            }
        } );

        delete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                getFragmentManager().beginTransaction().replace( R.id.card_actions,
                        new ConfirmationFragment() ).commit();
            }
        } );
        return view;
    }

    public void confirmDelete() {
        new DeleteTask( activity, trello ).execute( card );
    }

    public void changeCardName() {
        new UpdateCardNameTask( activity, trello ).execute( card );
    }

    private class ClockOffTask extends TrelloTask {
        public ClockOffTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().clockOff( card[ 0 ] ) ) {
                getActionDAO().setClockedOff( card[ 0 ] );
            }
            return null;
        }
    }

    private class ClockOnTask extends TrelloTask {
        public ClockOnTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().clockOn( card[ 0 ] ) ) {
                getActionDAO().setClockedOn( card[ 0 ] );
            }
            return null;
        }
    }

    private class DoneTask extends TrelloTask {

        public DoneTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().done( card[ 0 ] ) ) {
                getActionDAO().setDone( card[ 0 ] );
            }
            return null;
        }
    }

    private class TodayTask extends TrelloTask {

        public TodayTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().today( card[ 0 ] ) ) {
                getActionDAO().setToday( card[ 0 ] );
            }
            return null;
        }
    }

    private class ToTodoTask extends TrelloTask {

        public ToTodoTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !getTrello().todo( card[ 0 ] ) ) {
                getActionDAO().setTodo( card[ 0 ] );
            }
            return null;
        }
    }

    private class DeleteTask extends TrelloTask {

        public DeleteTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... cards ) {
            Card card = cards[ 0 ];
            if ( !getTrello().deleteCard( card.getId() ) ) {
                ArrayList<Action> createActions = getActionDAO().find( card.getId(), Action.Type.CREATE );
                if ( createActions.isEmpty() ) {
                    getActionDAO().createDelete( card );
                } else {
                    getActionDAO().delete( card.getId() );
                }
            }
            getActionDAO().getCardDAO().delete( card.getId() );
            return null;
        }
    }

    private class UpdateCardNameTask extends TrelloTask {

        public UpdateCardNameTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... cards ) {
            Card card = cards[ 0 ];
            if ( !getTrello().updateCardName( card.getId(), card.getName() ) ) {
                getActionDAO().createUpdate( card );
            }
            return null;
        }
    }
}
