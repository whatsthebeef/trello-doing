package com.zode64.trellodoing.models;

import com.zode64.trellodoing.utils.TrelloManager;

public class CreateAction extends Action {

    public CreateAction( int id, Action.Type type, Card card, TrelloManager trello  ) {
        super( id, type, card, trello );
    }

    @Override
    public boolean perform() {
        Card persisted = trello.createCard( card );
        return persisted != null;
    }
}
