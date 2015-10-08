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

import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.widget.DoingWidget;

/**
 * Created by john on 9/22/15.
 */
public class CardAdderFragment extends Fragment {

    private ImageButton mSubmit;
    private ImageButton mCancel;
    private EditText mEditText;

    private CardDAO cardDAO;
    private BoardDAO boardDAO;

    private Board board;

    private TrelloManager trelloManager;

    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.new_card, container, false );

        cardDAO = new CardDAO( getActivity() );
        boardDAO = new BoardDAO( getActivity() );

        mSubmit = ( ImageButton ) view.findViewById( R.id.submit_new_card );
        mCancel = ( ImageButton ) view.findViewById( R.id.cancel_new_card );
        mEditText = ( EditText ) view.findViewById( R.id.new_card_name );

        String boardId = getActivity().getIntent().getStringExtra( DoingWidget.EXTRA_BOARD_ID );
        board = boardDAO.find( boardId );

        trelloManager = new TrelloManager( PreferenceManager.getDefaultSharedPreferences( getActivity() ) );

        mSubmit.setEnabled( false );

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
                    Card card = new Card();
                    card.setName( text );
                    card.setBoardId( board.getId() );
                    new AddCardTask( getActivity() ).execute( card );
                }
            }
        } );

        mCancel.setOnClickListener( new View.OnClickListener() {
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
        super.onDestroy();
    }

    private class AddCardTask extends TrelloTask {

        public AddCardTask( Activity activity ) {
            super( activity );
        }

        @Override
        protected Void doInBackground( Card... card ) {
            if ( !trelloManager.newCard( card[ 0 ], board ) ) {
                dao.create( card[ 0 ] );
            }
            return null;
        }
    }
}
