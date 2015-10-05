package com.zode64.trellodoing;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.zode64.trellodoing.models.Card;

import java.util.ArrayList;

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory( Intent intent ) {
        return ( new CardsProvider( this.getApplicationContext(), intent ) );
    }

}