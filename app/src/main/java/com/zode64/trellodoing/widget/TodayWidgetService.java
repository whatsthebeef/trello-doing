package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.DoingPreferences;
import com.zode64.trellodoing.models.Card;

public class TodayWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {
        return new TodayListProvider( getApplicationContext(), intent );
    }

    static class TodayListProvider extends ListProvider {

        private DoingPreferences preferences;

        public TodayListProvider( Context context, Intent intent ) {
            super( context, intent );
            preferences = new DoingPreferences( context );
        }

        @Override
        protected void loadCards() {
            if ( preferences.isThisWeek() ) {
                cards = cardDAO.where( Card.ListType.THIS_WEEK );
            } else {
                cards = cardDAO.where( Card.ListType.TODAY );
            }
        }
    }
}