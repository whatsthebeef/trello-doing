package com.zode64.trellodoing;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Board.ListType;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.widget.DoingWidget;

import java.util.Calendar;

public class CardAdderActivity extends Activity implements Preference.OnPreferenceChangeListener {

    private Button submit;
    private EditText nameInput;

    private BoardDAO boardDAO;

    private Board board;

    private TrelloManager trelloManager;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );

        // Called before set view so the preference is in the right state
        boardDAO = new BoardDAO( this );
        final DoingPreferences preferences = new DoingPreferences( this );
        String boardId = getIntent().getStringExtra( DoingWidget.EXTRA_BOARD_ID );
        if ( boardId == null ) {
            boardId = preferences.getLastAddedBoard();
        } else {
            preferences.saveLastAddedBoard( boardId );
        }
        if ( boardId != null ) {
            board = boardDAO.find( boardId );
        }

        setContentView( R.layout.add_card );

        submit = ( Button ) findViewById( R.id.submit_new_card );
        Button cancel = ( Button ) findViewById( R.id.cancel_new_card );
        nameInput = ( EditText ) findViewById( R.id.new_card_name );

        trelloManager = new TrelloManager( preferences );

        nameInput.setHint( getString( R.string.add_card ) );
        nameInput.requestFocus();

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
                if ( !text.equals( "" ) ) {
                    Card card = new Card();
                    card.setName( text );
                    card.setServerId( "" + Calendar.getInstance().getTimeInMillis() );
                    card.setListType( preferences.isThisWeek() ? ListType.THIS_WEEK : ListType.TODAY );
                    card.setBoard( board );
                    new AddCardTask( CardAdderActivity.this, trelloManager ).execute( card );
                }
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                CardAdderActivity.this.finish();
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
        if ( board != null && !text.equals( "" ) ) {
            submit.setEnabled( true );
        } else {
            submit.setEnabled( false );
        }
    }

    private static class AddCardTask extends TrelloTask {

        AddCardTask( Activity activity, TrelloManager trello ) {
            super( activity, trello );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            Card newCard = card[ 0 ];
            Card persistedCard = trello.createCard( newCard );
            if ( persistedCard == null ) {
                getCardDAO().create( newCard );
                getActionDAO().createCreate( newCard );
                getActionDAO().createMove( newCard );
            } else {
                if ( !trello.moveCard( newCard.getServerId(), newCard.getBoardListId( ListType.TODO ) ) ) {
                    getActionDAO().createMove( newCard );
                }
            }
            return null;
        }
    }
}
