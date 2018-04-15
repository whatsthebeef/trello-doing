package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;

public class ClockedOffWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {
        return ( new ClockedOffListProvider( this.getApplicationContext(), intent ) );
    }

    static class ClockedOffListProvider extends ListProvider {

        public ClockedOffListProvider( Context context, Intent intent ) {
            super( context, intent );
        }

        protected void loadCards() {
            cards = cardDAO.where( Board.ListType.CLOCKED_OFF );
        }
    }
}