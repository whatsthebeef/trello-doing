package com.zode64.trellodoing.models;

import com.zode64.trellodoing.TrelloManager;
import com.zode64.trellodoing.db.CardDAO;

public class UpdateNameAction extends Action {

    public UpdateNameAction( int id, Type type, Card card, TrelloManager trello ) {
        super( id, type, card, trello );
    }

    @Override
    public boolean perform() {
        return trello.updateCardName( card.getId(), card.getName() );
    }
}
