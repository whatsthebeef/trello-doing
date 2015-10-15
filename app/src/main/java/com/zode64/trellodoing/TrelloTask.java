package com.zode64.trellodoing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import com.zode64.trellodoing.db.ActionDAO;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.widget.DoingWidget;

public abstract class TrelloTask extends AsyncTask<Card, Void, Void> {

    private ProgressDialog progress;
    private Activity activity;
    private TrelloManager trello;
    private ActionDAO actionDAO;

    public TrelloTask( Activity activity, TrelloManager trello ) {
        this.activity = activity;
        progress = new ProgressDialog( activity );
        progress.setTitle( activity.getString( R.string.loading ) );
        progress.setMessage( activity.getString( R.string.wait_while_loading ) );
        this.actionDAO = new ActionDAO( activity, trello );
        this.trello = trello;
    }

    @Override
    protected void onPostExecute( Void success ) {
        actionDAO.closeDB();
        if ( ( progress != null ) && progress.isShowing() && !activity.isDestroyed() ) {
            progress.dismiss();
        }
        activity.startService( new Intent( activity, DoingWidget.UpdateService.class ) );
        activity.finish();
    }

    @Override
    protected void onPreExecute() {
        progress.show();
    }

    protected ActionDAO getActionDAO() {
        return actionDAO;
    }

    protected TrelloManager getTrello() {
        return trello;
    }
}
