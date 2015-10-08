package com.zode64.trellodoing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.widget.DoingWidget;

public abstract class TrelloTask extends AsyncTask<Card, Void, Void> {

    protected ProgressDialog progress;
    protected Activity activity;
    protected CardDAO dao;

    public TrelloTask( Activity activity ) {
        this.activity = activity;
        progress = new ProgressDialog( activity );
        progress.setTitle( activity.getString( R.string.loading ) );
        progress.setMessage( activity.getString( R.string.wait_while_loading ) );
        dao = new CardDAO( activity );
    }

    @Override
    protected void onPostExecute( Void nada ) {
        dao.closeDB();
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
}
