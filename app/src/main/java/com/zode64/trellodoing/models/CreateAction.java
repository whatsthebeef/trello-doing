package com.zode64.trellodoing.models;

import com.zode64.trellodoing.utils.FileUtils;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.db.DeadlineDAO;

public class CreateAction extends Action {

    private DeadlineDAO deadlineDAO;

    public CreateAction( int id, Action.Type type, Card card, TrelloManager trello, DeadlineDAO deadlineDAO ) {
        super( id, type, card, trello );
        this.deadlineDAO = deadlineDAO;
    }

    @Override
    public boolean perform() {
        String currentServerId = card.getServerId();
        Card persisted = trello.createCard( card );
        if ( persisted != null ) {
            deadlineDAO.updateServerId( currentServerId, persisted.getServerId() );
            FileUtils.renameAudioDir( currentServerId, persisted.getServerId() );
            return true;
        }
        return false;
    }
}
