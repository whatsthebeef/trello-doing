package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.R;
import com.zode64.trellodoing.utils.TimeUtils;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.db.DeadlineDAO;
import com.zode64.trellodoing.models.Card;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public abstract class ListProvider implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = ListProvider.class.getName();

    protected ArrayList<Card> cards;

    protected Context context;

    protected CardDAO cardDAO;

    protected DeadlineDAO deadlineDAO;

    protected HashMap<String, Long> deadlines;

    protected long now;

    public ListProvider( Context context, Intent intent ) {
        this.context = context;
        this.cardDAO = new CardDAO( context );
        deadlineDAO = new DeadlineDAO( context );
    }

    @Override
    public void onCreate() {
        deadlines = deadlineDAO.all();
        now = Calendar.getInstance().getTimeInMillis();
        loadCards();
    }

    @Override
    public void onDataSetChanged() {
        deadlines = deadlineDAO.all();
        now = Calendar.getInstance().getTimeInMillis();
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
        Long deadline = deadlines.get( card.getServerId() );
        if ( TimeUtils.pastDeadline( deadline, now ) ) {
            view = new RemoteViews( context.getPackageName(), R.layout.doing_card_past_deadline );
        } else {
            view = new RemoteViews( context.getPackageName(), R.layout.doing_card );
            if ( deadline != null ) {
                view.setViewVisibility( R.id.deadline_set, View.VISIBLE );
                view.setViewVisibility( R.id.deadline_not_set, View.GONE );
            } else {
                view.setViewVisibility( R.id.deadline_set, View.GONE );
                view.setViewVisibility( R.id.deadline_not_set, View.VISIBLE );
            }
        }
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

    protected void setClickListener( RemoteViews view, Card card ) {
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra( DoingWidget.EXTRA_CARD_ID, card.getId() );
        view.setOnClickFillInIntent( R.id.doing_card, fillInIntent );
    }
}