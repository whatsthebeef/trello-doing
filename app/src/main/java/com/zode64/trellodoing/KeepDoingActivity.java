package com.zode64.trellodoing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

import com.zode64.trellodoing.utils.WidgetAlarm;
import com.zode64.trellodoing.widget.DoingWidget;

import java.util.Calendar;

public class KeepDoingActivity extends Activity implements ConfigureDelayFragment.DelayChangeListener {

    private WidgetAlarm alarm;
    private DoingPreferences preferences;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
        setContentView( R.layout.keep_doing );

        preferences = new DoingPreferences( this );
        alarm = new WidgetAlarm( this, preferences );
        getFragmentManager().beginTransaction().replace( R.id.keep_doing,
                new ConfigureDelayFragment() ).commit();
    }

    @Override
    public void onDelayChange( Double delay ) {
        Calendar until = alarm.delayAlarm( delay );
        preferences.setKeepDoing( until );
        startService( new Intent( DoingWidget.ACTION_SET_ALARM ) );
    }

    @Override
    public void resetDelay() {
        preferences.resetKeepDoing();
        startService( new Intent( DoingWidget.ACTION_SET_ALARM ) );
    }

    @Override
    public Long getExistingDelay() {
        long keepDoing = preferences.getKeepDoing();
        return keepDoing > Calendar.getInstance().getTimeInMillis() ? keepDoing : -1;
    }
}
