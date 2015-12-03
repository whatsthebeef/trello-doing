package com.zode64.trellodoing;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;

import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.widget.DoingWidget;

import java.util.Calendar;

public class CardAdderActivity extends Activity implements Preference.OnPreferenceChangeListener {

    private final static String TAG = CardAdderActivity.class.getName();

    private ImageButton submit;
    private ImageButton cancel;
    private EditText nameInput;

    private BoardDAO boardDAO;

    private Board board;

    private TrelloManager trelloManager;

    private CardAdderActivity activity;

    private DoingPreferences preferences;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );

        // Called before set view so the preference is in the right state
        boardDAO = new BoardDAO( this );
        preferences = new DoingPreferences( this );
        String boardId = getIntent().getStringExtra( DoingWidget.EXTRA_BOARD_ID );
        if ( boardId == null ) {
            boardId = preferences.getLastAddedBoard();
        }
        else {
            preferences.saveLastAddedBoard( boardId );
        }
        if ( boardId != null ) {
            board = boardDAO.find( boardId );
        }

        setContentView( R.layout.add_card );

        activity = this;

        submit = ( ImageButton ) findViewById( R.id.submit_new_card );
        cancel = ( ImageButton ) findViewById( R.id.cancel_new_card );
        nameInput = ( EditText ) findViewById( R.id.new_card_name );

        trelloManager = new TrelloManager( preferences );

        nameInput.setHint( getString( R.string.add_card ) );

        updateSubmitState();

        nameInput.addTextChangedListener( new TextWatcher() {
                                              @Override
                                              public void beforeTextChanged( CharSequence s, int start, int count, int after ) {
                                              }

                                              @Override
                                              public void onTextChanged( CharSequence s, int start, int before, int count ) {
                                              }

                                              @Override
                                              public void afterTextChanged( Editable s ) {
                                                  updateSubmitState();
                                              }
                                          }
        );


        submit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String text = nameInput.getText().toString();
                if ( text != null && !text.equals( "" ) ) {
                    Card card = new Card();
                    card.setName( text );
                    card.setServerId( "" + Calendar.getInstance().getTimeInMillis() );
                    card.setInListType( Card.ListType.THIS_WEEK );
                    card.setBoardId( board.getId() );
                    card.setBoardName( board.getName() );
                    card.setBoardShortLink( board.getShortLink() );
                    card.setListId( Card.ListType.TODO, board.getTodoListId() );
                    card.setListId( Card.ListType.TODAY, board.getTodayListId() );
                    card.setListId( Card.ListType.DOING, board.getDoingListId() );
                    card.setListId( Card.ListType.CLOCKED_OFF, board.getClockedOffListId() );
                    card.setListId( Card.ListType.DONE, board.getDoneListId() );
                    card.setListId( Card.ListType.THIS_WEEK, board.getThisWeekListId() );
                    new AddCardTask( activity, trelloManager ).execute( card );
                }
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                activity.finish();
            }
        } );
    }

    @Override
    public void onDestroy() {
        boardDAO.closeDB();
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceChange( Preference preference, Object newValue ) {
        board = boardDAO.find( ( String ) newValue );
        updateSubmitState();
        return true;
    }

    private void updateSubmitState() {
        String text = nameInput.getText().toString();
        if ( board != null && text != null && !text.equals( "" ) ) {
            submit.setEnabled( true );
        } else {
            submit.setEnabled( false );
        }
    }

    private class AddCardTask extends TrelloTask {

        public AddCardTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            Card newCard = card[ 0 ];
            Card persistedCard = trelloManager.createCard( newCard );
            if ( persistedCard == null ) {
                getActionDAO().getCardDAO().create( newCard );
                getActionDAO().createCreate( newCard );
                getActionDAO().createMove( newCard );
            } else {
                if ( !trelloManager.thisWeek( newCard ) ) {
                    getActionDAO().createMove( newCard );
                }
            }
            return null;
        }
    }
}
