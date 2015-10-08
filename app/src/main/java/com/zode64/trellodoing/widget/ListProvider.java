package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.R;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;

import java.util.ArrayList;

public abstract class ListProvider implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = ListProvider.class.getName();

    protected ArrayList<Card> cards;

    protected Context context;

    protected CardDAO cardDAO;

    public ListProvider( Context context, Intent intent ) {
        this.context = context;
        this.cardDAO = new CardDAO( context );
    }

    @Override
    public void onCreate() {
        loadCards();
    }

    @Override
    public void onDataSetChanged() {
        loadCards();
    }

    @Override
    public void onDestroy() {
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
        Card card = cards.get( position );
        Log.i( TAG, "View with card :" + card.getName() );
        RemoteViews view = null;
        if ( card.pastDeadline() ) {
            view = new RemoteViews( context.getPackageName(), R.layout.doing_card_past_deadline );
        } else {
            view = new RemoteViews( context.getPackageName(), R.layout.doing_card );
            view.setTextViewText( R.id.card_name, card.getName() );
            if ( card.hasDeadline() ) {
                view.setViewVisibility( R.id.deadline_set, View.VISIBLE );
                view.setViewVisibility( R.id.deadline_not_set, View.GONE );
            } else {
                view.setViewVisibility( R.id.deadline_set, View.GONE );
                view.setViewVisibility( R.id.deadline_not_set, View.VISIBLE );
            }
        }
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

    protected void setClickListener( RemoteViews view, Card card ) {
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra( DoingWidget.EXTRA_CARD_ID, card.getId() );
        view.setOnClickFillInIntent( R.id.doing_card, fillInIntent );
    }
}