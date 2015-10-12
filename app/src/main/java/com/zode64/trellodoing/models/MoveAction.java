package com.zode64.trellodoing.models;

import com.zode64.trellodoing.TrelloManager;

public class MoveAction extends Action {

    public MoveAction( int id, Action.Type type, Card card, TrelloManager trello ) {
        super( id, type, card, trello );
    }

    @Override
    public boolean perform() {
        return trello.moveCard( card.getId(), card.getCurrentListId() );
    }
}
