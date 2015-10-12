package com.zode64.trellodoing.models;

import com.zode64.trellodoing.TrelloManager;

public class DeleteAction extends Action {

    private String cardId;

    public DeleteAction( int id, Type type, String cardId, TrelloManager trello ) {
        super( id, type, null, trello );
        this.cardId = cardId;
    }

    @Override
    public boolean perform() {
        return trello.deleteCard( cardId );
    }
}
