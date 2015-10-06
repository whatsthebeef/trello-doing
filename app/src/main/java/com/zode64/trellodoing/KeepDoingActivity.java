package com.zode64.trellodoing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import java.util.Calendar;

public class KeepDoingActivity extends Activity implements ConfigureDelayFragment.DelayChangeListener {

    private WidgetAlarm alarm;
    private DoingPreferences preferences;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.keep_doing );
        preferences = new DoingPreferences( this );
        alarm = new WidgetAlarm( this, preferences );
        getFragmentManager().beginTransaction().replace( R.id.keep_doing,
                new ConfigureDelayFragment() ).commit();
    }

    @Override
    public void onChange( Double delay, String cardId ) {
        Calendar until = alarm.delayAlarm( delay );
        preferences.handleSetKeepDoingCall( until );
        startService( new Intent( getApplication(), DoingWidget.UpdateService.class ) );
        finish();
    }
}
