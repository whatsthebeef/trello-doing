package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.DoingPreferences;
import com.zode64.trellodoing.R;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;

public class TodayWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {
        return new TodayListProvider( getApplicationContext() );
    }

    static class TodayListProvider extends ListProvider {

        private DoingPreferences preferences;

        TodayListProvider( Context context ) {
            super( context );
            preferences = new DoingPreferences( context );
        }

        @Override
        public RemoteViews getViewAt( int position ) {
            if ( cards.size() < position ) {
                return null;
            }
            return super.getViewAt( position );
        }

        @Override
        protected void loadCards() {
            if ( preferences.isThisWeek() ) {
                cards = cardDAO.where( Board.ListType.THIS_WEEK );
            } else {
                cards = cardDAO.where( Board.ListType.TODAY );
            }
        }
    }
}