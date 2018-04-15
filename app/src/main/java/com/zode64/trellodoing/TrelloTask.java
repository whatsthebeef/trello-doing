package com.zode64.trellodoing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import com.zode64.trellodoing.db.ActionDAO;
import com.zode64.trellodoing.db.AttachmentDAO;
import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.db.DeadlineDAO;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.widget.DoingWidget;

public abstract class TrelloTask extends AsyncTask<Card, Void, Void> {

    private ProgressDialog progress;
    private Activity activity;
    private TrelloManager trello;
    private CardDAO cardDAO;
    private DeadlineDAO deadlineDAO;
    private AttachmentDAO attachmentDAO;
    private ActionDAO actionDAO;
    private BoardDAO boardDAO;

    public TrelloTask( Activity activity, TrelloManager trello ) {
        this.activity = activity;
        progress = new ProgressDialog( activity );
        progress.setTitle( activity.getString( R.string.loading ) );
        progress.setMessage( activity.getString( R.string.wait_while_loading ) );
        boardDAO = new BoardDAO( activity );
        cardDAO = new CardDAO( activity, boardDAO.boardMap() );
        deadlineDAO = new DeadlineDAO( activity );
        attachmentDAO = new AttachmentDAO( activity );
        actionDAO = new ActionDAO( activity, trello, cardDAO, deadlineDAO, attachmentDAO );
        this.trello = trello;
    }

    @Override
    protected void onPostExecute( Void success ) {
        actionDAO.closeDB();
        attachmentDAO.closeDB();
        cardDAO.closeDB();
        deadlineDAO.closeDB();
        boardDAO.closeDB();
        if ( ( progress != null ) && progress.isShowing() && !activity.isDestroyed() ) {
            progress.dismiss();
        }
        Intent intent = new Intent( activity, DoingWidget.UpdateService.class );
        intent.setAction( DoingWidget.ACTION_ACTION_PERFORMED );
        activity.startService( intent );
        activity.finish();
    }

    @Override
    protected void onPreExecute() {
        progress.show();
    }

    protected ActionDAO getActionDAO() {
        return actionDAO;
    }

    protected CardDAO getCardDAO() {
        return cardDAO;
    }

    protected TrelloManager getTrello() {
        return trello;
    }
}
