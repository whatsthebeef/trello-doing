package com.zode64.trellodoing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.models.Board;

import java.util.List;

public class BoardSelectActivity extends Activity {

    private BoardDAO boardDAO;
    private List<Board> boards;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );

        // Called before set view so the preference is in the right state
        boardDAO = new BoardDAO( this );
        boards = boardDAO.all();

        setContentView( R.layout.select_board );

        Button cancel = ( Button ) findViewById( R.id.cancel_board_select );
        cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                BoardSelectActivity.this.finish();
            }
        } );

        ListView boardList = ( ListView ) this.findViewById( R.id.board_list );
        // make something for List adapter
        boardList.setAdapter( new BoardListAdapter( this, android.R.layout.simple_list_item_1, boardDAO.all() ) );
        boardList.setTextFilterEnabled( true );

        boardList.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
                Intent getBoard = new Intent( Intent.ACTION_VIEW, Uri.parse( boards.get( position ).getShortUrl() ) );
                startActivity( getBoard );
            }
        } );
    }

    @Override
    public void onDestroy() {
        boardDAO.closeDB();
        super.onDestroy();
    }

    public class BoardListAdapter extends ArrayAdapter<Board> {

        public BoardListAdapter( Context context, int resource, List<Board> items ) {
            super( context, resource, items );
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {

            View view = convertView;

            if ( view == null ) {
                LayoutInflater vi = LayoutInflater.from( getContext() );
                view = vi.inflate( android.R.layout.simple_list_item_1, null );
            }

            Board board = getItem( position );

            if ( board != null ) {
                TextView name = ( TextView ) view.findViewById( android.R.id.text1 );
                name.setText( board.getName() );
            }

            return view;
        }

    }

}
