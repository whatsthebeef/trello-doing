package com.zode64.trellodoing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.widget.DoingWidget;

import java.util.Calendar;

public class ActionActivity extends Activity implements ConfigureDelayFragment.DelayChangeListener,
        ConfirmationFragment.ConfirmationListener, InputFragment.InputChangeListener {

    private CardDAO cardDAO;

    private Card card;

    private WidgetAlarm alarm;

    private CardActionFragment cardActionFragment;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.card_action );

        String cardId = getIntent().getStringExtra( DoingWidget.EXTRA_CARD_ID );
        if ( cardId != null ) {
            alarm = new WidgetAlarm( this, new DoingPreferences( this ) );
            cardDAO = new CardDAO( this );
            card = cardDAO.find( cardId );
            cardActionFragment = new CardActionFragment();
            getFragmentManager().beginTransaction().replace( R.id.card_actions, cardActionFragment ).commit();
        } else {
            getFragmentManager().beginTransaction().replace( R.id.card_actions,
                    new BoardActionFragment() ).commit();
        }
    }

    @Override
    public void onChange( Double delay ) {
        Calendar deadline = alarm.deadlineAlarm( delay );
        cardDAO.setDeadline( card.getId(), deadline.getTimeInMillis() );
        startService( new Intent( DoingWidget.ACTION_SET_ALARM ) );
        finish();
    }

    @Override
    public void reset() {
        cardDAO.resetDeadline( card.getId() );
        startService( new Intent( DoingWidget.ACTION_SET_ALARM ) );
    }

    @Override
    public long getExisting() {
        return card.getDeadline();
    }

    public Card getCard() {
        return card;
    }

    @Override
    public void onConfirmation() {
        cardActionFragment.confirmDelete();
    }

    @Override
    public String getInstruction() {
        return getString( R.string.confirm ) + " '" + card.getName() + "' " + getString( R.string.delete );
    }

    @Override
    public void onChange( String text ) {
        card.setName( text );
        cardActionFragment.changeCardName();
    }

    @Override
    public String getPlaceholder() {
        return card.getName();
    }
}
