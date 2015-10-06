package com.zode64.trellodoing;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;

import java.util.ArrayList;

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {
        return ( new CardsProvider( this.getApplicationContext(), intent ) );
    }

    static class CardsProvider implements RemoteViewsService.RemoteViewsFactory {

        private static final String TAG = CardsProvider.class.getName();

        private ArrayList<Card> cards;

        private Context context;

        private CardDAO cardDAO;

        private Card.ListType listType;

        public CardsProvider( Context context, Intent intent ) {
            this.context = context;
            this.cardDAO = new CardDAO( context );
            this.listType = Card.ListType.values()[ intent.getIntExtra( DoingWidget.EXTRA_LIST_TYPE_ORDINAL, 0 ) ];
        }

        @Override
        public void onCreate() {
            this.cards = cardDAO.where( listType );
        }

        @Override
        public void onDataSetChanged() {
            this.cards = cardDAO.where( listType );
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
            Log.i( TAG, "Number of cards: " + cards.size() );

            Card card = cards.get( position );
            RemoteViews view = new RemoteViews( context.getPackageName(), R.layout.doing_card );
            if(card.pastDeadline()) {
                view.setInt( R.layout.doing_card, "setBackgroundResource", R.drawable.layout_list_item_past_deadline );
            }
            if(card.hasDeadline()) {
                view.setViewVisibility( R.id.deadline_set, View.VISIBLE );
                view.setViewVisibility( R.id.deadline_not_set, View.GONE );
            }
            view.setTextViewText( R.id.card_name, card.getName() );
            Intent fillInIntent = new Intent( DoingWidget.ACTION_LIST_ITEM_CLICKED );
            fillInIntent.putExtra( DoingWidget.EXTRA_CARD_ID, card.getId() );
            fillInIntent.putExtra( DoingWidget.EXTRA_LIST_TYPE_ORDINAL, card.getInListTypeOrdinal() );
            view.setOnClickFillInIntent( R.id.doing_card, fillInIntent );

            return view;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }
    }
}