package com.zode64.trellodoing;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.zode64.trellodoing.widget.DoingWidget;

public class AdderActivity extends Activity {

    private final static String TAG = AdderActivity.class.getName();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.card_action );
        Log.i(TAG, "Board id: " + getIntent().getStringExtra( DoingWidget.EXTRA_BOARD_ID ));
        getFragmentManager().beginTransaction().replace( R.id.card_actions,
                new CardAdderFragment() ).commit();
    }
}
