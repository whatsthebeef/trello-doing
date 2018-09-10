package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.R;
import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;

import java.util.ArrayList;

public abstract class ListProvider implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = ListProvider.class.getName();

    protected ArrayList<Card> cards;

    protected Context context;

    CardDAO cardDAO;

    ListProvider( Context context ) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        BoardDAO boardDAO = new BoardDAO( context );
        this.cardDAO = new CardDAO( context, boardDAO.boardMap() );
        boardDAO.closeDB();
        loadCards();
    }

    @Override
    public void onDataSetChanged() {
        loadCards();
    }

    @Override
    public void onDestroy() {
        cardDAO.closeDB();
    }

    @Override
    public int getCount() {
        return cards.size();
    }

    @Override
    public long getItemId( int position ) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public RemoteViews getViewAt( int position ) {
        if ( cards.size() < position ) {
            return null;
        }
        Card card = cards.get( position );
        Log.i( TAG, "View with card :" + card.getName() );
        RemoteViews view = new RemoteViews( context.getPackageName(), R.layout.doing_card );
        view.setTextViewText( R.id.card_name, card.getBoardName() + ": " + card.getName() );
        setClickListener( view, card );
        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    protected abstract void loadCards();

    void setClickListener( RemoteViews view, Card card ) {
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra( DoingWidget.EXTRA_CARD_ID, card.getId() );
        view.setOnClickFillInIntent( R.id.doing_card, fillInIntent );
    }
}