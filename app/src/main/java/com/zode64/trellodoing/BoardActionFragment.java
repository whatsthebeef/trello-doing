package com.zode64.trellodoing;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.widget.DoingWidget;

public class BoardActionFragment extends Fragment {

    private final static String TAG = BoardActionFragment.class.getName();

    private TextView boardName;

    private ImageButton newCard;
    private ImageButton open;

    private BoardDAO boardDAO;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.board_action, container, false );

        boardName = ( TextView ) view.findViewById( R.id.board_name );

        newCard = ( ImageButton ) view.findViewById( R.id.new_card );
        open = ( ImageButton ) view.findViewById( R.id.open );

        boardDAO = new BoardDAO( getActivity() );
        String boardId = getActivity().getIntent().getStringExtra( DoingWidget.EXTRA_BOARD_ID );
        final Board board = boardDAO.find( boardId );

        boardName.setText( board.getName() );

        newCard.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                startActivity( new Intent( getActivity(), CardAdderActivity.class ) );
            }
        } );

        open.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                Log.i(TAG, "Opening board at: " + board.getShortUrl());
                Intent getBoard = new Intent( Intent.ACTION_VIEW, Uri.parse( board.getShortUrl() ) );
                startActivity( getBoard );
            }
        } );

        return view;
    }

    @Override
    public void onDestroy() {
        boardDAO.closeDB();
        super.onDestroy();
    }

}
