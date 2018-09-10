package com.zode64.trellodoing.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.models.Board;

public class ClockedOffWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {
        return ( new ClockedOffListProvider( this.getApplicationContext() ) );
    }

    static class ClockedOffListProvider extends ListProvider {

        ClockedOffListProvider( Context context ) {
            super( context );
        }

        protected void loadCards() {
            cards = cardDAO.where( Board.ListType.CLOCKED_OFF );
        }
    }
}