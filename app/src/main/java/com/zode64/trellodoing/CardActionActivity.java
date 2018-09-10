package com.zode64.trellodoing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.widget.DoingWidget;

import java.util.ArrayList;
import java.util.List;

public class CardActionActivity extends Activity implements ConfirmationFragment.ConfirmationListener, InputFragment.TextChangeListener {

    private static final String TAG = CardActionActivity.class.getSimpleName();

    private TrelloManager trello;

    private CardDAO cardDAO;

    private Card card;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );

        String cardId = getIntent().getStringExtra( DoingWidget.EXTRA_CARD_ID );

        BoardDAO boardDAO = new BoardDAO( this );
        cardDAO = new CardDAO( this, boardDAO.boardMap() );

        setContentView( R.layout.card_action );

        Log.i( TAG, "Opening card with id: " + cardId );
        card = cardDAO.findById( cardId );
        Log.i( TAG, "Card name: " + card.getName() );

        TextView cardText = ( TextView ) findViewById( R.id.card_name );

        Button clockOff = ( Button ) findViewById( R.id.clock_out );
        Button clockOn = ( Button ) findViewById( R.id.clock_on );
        Button done = ( Button ) findViewById( R.id.done );
        Button today = ( Button ) findViewById( R.id.today );
        Button todo = ( Button ) findViewById( R.id.to_todo );
        Button delete = ( Button ) findViewById( R.id.delete );
        Button open = ( Button ) findViewById( R.id.open );
        Button cancel = ( Button ) findViewById( R.id.cancel );
        Button thisWeek = ( Button ) findViewById( R.id.to_this_week );

        TextView noDoneListText = ( TextView ) findViewById( R.id.no_done_list );
        TextView noDoingListText = ( TextView ) findViewById( R.id.no_doing_list );
        TextView noClockedOffListText = ( TextView ) findViewById( R.id.no_clocked_off_list );
        TextView noTodayListText = ( TextView ) findViewById( R.id.no_today_list );
        TextView noTodoListText = ( TextView ) findViewById( R.id.no_todo_list );
        TextView noThisWeekListText = ( TextView ) findViewById( R.id.no_this_week_list );

        switch ( card.getListType() ) {
            case DOING:
                today.setVisibility( View.VISIBLE );
                clockOff.setVisibility( View.VISIBLE );
                done.setVisibility( View.VISIBLE );
                break;
            case TODAY:
                clockOn.setVisibility( View.VISIBLE );
                thisWeek.setVisibility( View.VISIBLE );
                done.setVisibility( View.VISIBLE );
                break;
            case CLOCKED_OFF:
                clockOn.setVisibility( View.VISIBLE );
                today.setVisibility( View.VISIBLE );
                done.setVisibility( View.VISIBLE );
                break;
            case THIS_WEEK:
                todo.setVisibility( View.VISIBLE );
                today.setVisibility( View.VISIBLE );
                clockOn.setVisibility( View.VISIBLE );
                break;
            default:
                throw new RuntimeException( "Weird card list type in card action view" );
        }

        if ( card.getBoardListId( Board.ListType.DOING ) == null ) {
            noDoingListText.setVisibility( View.VISIBLE );
            clockOn.setEnabled( false );
        }

        if ( card.getBoardListId( Board.ListType.DONE ) == null ) {
            noDoneListText.setVisibility( View.VISIBLE );
            done.setEnabled( false );
        }

        if ( card.getBoardListId( Board.ListType.TODAY ) == null ) {
            noTodayListText.setVisibility( View.VISIBLE );
            today.setEnabled( false );
        }

        if ( card.getBoardListId( Board.ListType.CLOCKED_OFF ) == null ) {
            noClockedOffListText.setVisibility( View.VISIBLE );
            clockOff.setEnabled( false );
        }

        if ( card.getBoardListId( Board.ListType.TODO ) == null ) {
            noTodoListText.setVisibility( View.VISIBLE );
            todo.setEnabled( false );
        }

        if ( card.getBoardListId( Board.ListType.THIS_WEEK ) == null ) {
            noThisWeekListText.setVisibility( View.VISIBLE );
            thisWeek.setEnabled( false );
        }

        DoingPreferences preferences = new DoingPreferences( this );
        trello = new TrelloManager( preferences );

        cardText.setText( card.getName() );

        cardText.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new InputFragment().show( getFragmentManager(), null );
            }
        } );

        clockOff.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new MoveTask( CardActionActivity.this, trello, Board.ListType.CLOCKED_OFF ).execute( card );
            }
        } );

        clockOn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new MoveTask( CardActionActivity.this, trello, Board.ListType.DOING ).execute( card );
            }
        } );

        done.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new MoveTask( CardActionActivity.this, trello, Board.ListType.DONE ).execute( card );
            }
        } );

        todo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new MoveTask( CardActionActivity.this, trello, Board.ListType.TODO ).execute( card );
            }
        } );

        today.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new MoveTask( CardActionActivity.this, trello, Board.ListType.TODAY ).execute( card );
            }
        } );

        thisWeek.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                new MoveTask( CardActionActivity.this, trello, Board.ListType.THIS_WEEK ).execute( card );
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

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                CardActionActivity.this.onBackPressed();
            }
        } );

    }

    @Override
    public void onDestroy() {
        cardDAO.closeDB();
        super.onDestroy();
    }

    @Override
    public Card getCard() {
        return card;
    }

    @Override
    public void onConfirmation() {
        new DeleteTask( this, trello ).execute( card );
    }

    @Override
    public String getConfirmationInstruction() {
        return getString( R.string.confirm ) + " '" + card.getName() + "' " + getString( R.string.delete );
    }

    @Override
    public void onTextChange( String text ) {
        new UpdateCardNameTask( this, trello ).execute( card );
        card.setName( text );
    }

    private static class MoveTask extends TrelloTask {
        private Board.ListType listType;

        MoveTask( Activity activity, TrelloManager trello, Board.ListType listType ) {
            super( activity, trello );
            this.listType = listType;
        }

        @Override
        protected Void doInBackground( Card... cards ) {
            Card card = cards[ 0 ];
            if ( !getTrello().moveCard( card.getServerId(), card.getBoardListId( listType ) ) ) {
                getCardDAO().setListType( card.getId(), listType );
                getActionDAO().createMove( card );
            }
            return null;
        }
    }

    private static class DeleteTask extends TrelloTask {

        DeleteTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... cards ) {
            Card card = cards[ 0 ];
            ArrayList<Action> createActions = getActionDAO().find( card.getId(), Action.Type.CREATE );
            if ( !createActions.isEmpty() ) {
                getActionDAO().delete( card.getId() );
                getCardDAO().delete( card.getId() );
                return null;
            } else {
                if ( !getTrello().deleteCard( card.getServerId() ) ) {
                    getActionDAO().createDelete( card );
                    getCardDAO().markForDelete( card.getId() );
                }
            }
            return null;
        }
    }

    private static class UpdateCardNameTask extends TrelloTask {

        UpdateCardNameTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... cards ) {
            Card card = cards[ 0 ];
            List<Action> createActions = getActionDAO().find( card.getId(), Action.Type.CREATE );
            if ( createActions.isEmpty() ) {
                if ( !getTrello().updateCardName( card.getServerId(), card.getName() ) ) {
                    getCardDAO().updateName( card.getId(), card.getName() );
                    getActionDAO().createUpdate( card );
                }
            } else {
                getCardDAO().updateName( card.getId(), card.getName() );
            }
            return null;
        }
    }
}
