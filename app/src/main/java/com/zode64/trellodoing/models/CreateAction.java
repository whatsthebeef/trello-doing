package com.zode64.trellodoing.models;

import com.zode64.trellodoing.db.AttachmentDAO;
import com.zode64.trellodoing.db.DeadlineDAO;
import com.zode64.trellodoing.utils.FileUtils;
import com.zode64.trellodoing.utils.TrelloManager;

public class CreateAction extends Action {

    private DeadlineDAO deadlineDAO;
    private AttachmentDAO attachmentDAO;

    public CreateAction( int id, Action.Type type, Card card, TrelloManager trello, DeadlineDAO deadlineDAO,
                         AttachmentDAO attachmentDAO ) {
        super( id, type, card, trello );
        this.deadlineDAO = deadlineDAO;
        this.attachmentDAO = attachmentDAO;
    }

    @Override
    public boolean perform() {
        String currentServerId = card.getServerId();
        Card persisted = trello.createCard( card );
        if ( persisted != null ) {
            deadlineDAO.updateServerId( currentServerId, persisted.getServerId() );
            attachmentDAO.updateServerId( currentServerId, persisted.getServerId() );
            FileUtils.renameAudioDir( currentServerId, persisted.getServerId() );
            FileUtils.renameImageDir( currentServerId, persisted.getServerId() );
            return true;
        }
        return false;
    }
}
