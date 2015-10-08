package com.zode64.trellodoing;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.zode64.trellodoing.widget.DoingWidget;

public class ActionActivity extends Activity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.card_action );
        String cardId = getIntent().getStringExtra( DoingWidget.EXTRA_CARD_ID );
        if(cardId != null) {
            getFragmentManager().beginTransaction().replace( R.id.card_actions,
                    new CardActionFragment() ).commit();
        }
        else {
            getFragmentManager().beginTransaction().replace( R.id.card_actions,
                    new BoardActionFragment() ).commit();
        }
    }
}
