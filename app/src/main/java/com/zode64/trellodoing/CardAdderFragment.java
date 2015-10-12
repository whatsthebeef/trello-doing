package com.zode64.trellodoing;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.widget.DoingWidget;

import java.util.Calendar;

/**
 * Created by john on 9/22/15.
 */
public class CardAdderFragment extends Fragment {

    private ImageButton submit;
    private ImageButton cancel;
    private EditText nameInput;

    private TextView instruction;

    private CardDAO cardDAO;
    private BoardDAO boardDAO;

    private Board board;

    private TrelloManager trelloManager;

    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.new_card, container, false );

        cardDAO = new CardDAO( getActivity() );
        boardDAO = new BoardDAO( getActivity() );

        submit = ( ImageButton ) view.findViewById( R.id.submit_new_card );
        cancel = ( ImageButton ) view.findViewById( R.id.cancel_new_card );
        nameInput = ( EditText ) view.findViewById( R.id.new_card_name );

        instruction = ( TextView ) view.findViewById( R.id.instruction );

        String boardId = getActivity().getIntent().getStringExtra( DoingWidget.EXTRA_BOARD_ID );
        board = boardDAO.find( boardId );

        trelloManager = new TrelloManager( PreferenceManager.getDefaultSharedPreferences( getActivity() ) );

        submit.setEnabled( false );
        instruction.setText( getString( R.string.add_card ) + " " + board.getName() );

        nameInput.addTextChangedListener( new TextWatcher() {
                                              @Override
                                              public void beforeTextChanged( CharSequence s, int start, int count, int after ) {
                                              }

                                              @Override
                                              public void onTextChanged( CharSequence s, int start, int before, int count ) {
                                              }

                                              @Override
                                              public void afterTextChanged( Editable s ) {
                                                  if ( s.length() > 0 ) {
                                                      submit.setEnabled( true );
                                                  } else {
                                                      submit.setEnabled( false );
                                                  }

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
                    card.setId( String.valueOf( Calendar.getInstance().getTimeInMillis() ) );
                    card.setInListType( Card.ListType.TODAY );
                    card.setBoardId( board.getId() );
                    card.setBoardName( board.getName() );
                    card.setBoardShortLink( board.getShortLink() );
                    card.setListId( Card.ListType.TODO, board.getTodoListId() );
                    card.setListId( Card.ListType.TODAY, board.getTodayListId() );
                    card.setListId( Card.ListType.DOING, board.getDoingListId() );
                    card.setListId( Card.ListType.CLOCKED_OFF, board.getClockedOffListId() );
                    card.setListId( Card.ListType.DONE, board.getDoneListId() );
                    new AddCardTask( getActivity(), trelloManager ).execute( card );
                }
            }
        } );

        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                getActivity().finish();
            }
        } );
        return view;
    }

    @Override
    public void onDestroyView() {
        cardDAO.closeDB();
        boardDAO.closeDB();
        super.onDestroy();
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
            }
            return null;
        }
    }
}
