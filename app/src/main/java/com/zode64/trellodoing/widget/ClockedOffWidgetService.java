package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.R;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;

public class ClockedOffWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {
        return ( new ClockedOffListProvider( this.getApplicationContext() ) );
    }

    static class ClockedOffListProvider extends ListProvider {

        ClockedOffListProvider( Context context ) {
            super( context );
        }

        @Override
        public RemoteViews getViewAt( int position ) {
            Card card = cards.get( position );
            if ( !card.isCurrentUserClockedOff() ) {
                RemoteViews view = new RemoteViews( context.getPackageName(), R.layout.doing_card_past_other );
                view.setTextViewText( R.id.card_name, card.getBoardName() + ": " + card.getName() );
                return view;
            } else {
                return super.getViewAt( position );
            }
        }

        protected void loadCards() {
            cards = cardDAO.where( Board.ListType.CLOCKED_OFF );
        }
    }
}