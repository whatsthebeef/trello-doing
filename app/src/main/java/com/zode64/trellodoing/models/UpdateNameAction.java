package com.zode64.trellodoing.models;

import com.zode64.trellodoing.utils.TrelloManager;

public class UpdateNameAction extends Action {

    public UpdateNameAction( int id, Type type, Card card, TrelloManager trello ) {
        super( id, type, card, trello );
    }

    @Override
    public boolean perform() {
        return trello.updateCardName( card.getServerId(), card.getName() );
    }
}
