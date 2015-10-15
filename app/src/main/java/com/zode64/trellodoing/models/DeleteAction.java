package com.zode64.trellodoing.models;

import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.db.CardDAO;

public class DeleteAction extends Action {

    private CardDAO cardDAO;

    public DeleteAction( int id, Type type, Card card, TrelloManager trello, CardDAO cardDAO ) {
        super( id, type, card, trello );
        this.cardDAO = cardDAO;
    }

    @Override
    public boolean perform() {
        if(trello.deleteCard( card.getServerId() )){
            cardDAO.delete( card.getId() );
            return true;
        }
        return false;
    }
}
