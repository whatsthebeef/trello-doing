package com.zode64.trellodoing;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Member;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DoingWidget extends AppWidgetProvider {

    private final static String TAG = DoingWidget.class.getName();

    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds ) {
        Log.d( TAG, "onUpdate()" );
        context.startService( new Intent( context, UpdateService.class ) );
        super.onUpdate( context, appWidgetManager, appWidgetIds );
    }

    @Override
    public void onEnabled( Context context ) {
        super.onEnabled( context );
        Log.d( TAG, "onEnabled()" );
        // start alarm
        context.startService( new Intent( context, UpdateService.class ) );
    }

    @Override
    public void onDisabled( Context context ) {
        super.onDisabled( context );
        Log.d( TAG, "onDisabled()" );
        DoingPreferences preferences = new DoingPreferences( context );
        WidgetAlarm widgetAlarm = new WidgetAlarm( context.getApplicationContext(), preferences );
        widgetAlarm.stopStandardAlarm();
        widgetAlarm.stopDeadlineAlarm();
    }

    public static class NetworkChangeReceiver extends BroadcastReceiver {

        public NetworkChangeReceiver() {
            super();
        }

        @Override
        public void onReceive( final Context context, final Intent intent ) {
            Log.d( TAG, "onReceive()" );
            final ConnectivityManager connMgr = ( ConnectivityManager ) context
                    .getSystemService( Context.CONNECTIVITY_SERVICE );

            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            if ( netInfo != null && netInfo.isConnectedOrConnecting() ) {
                // When the network returns any checks will begin as normal even if 'Keep Doing' alarm is set.
                Log.d( TAG, "Connected to network" );
                context.startService( new Intent( UpdateService.ACTION_NETWORK_CHANGE, null, context, UpdateService.class ) );
            } else {
                Log.d( TAG, "No network, stop checking for now" );
                context.startService( new Intent( UpdateService.ACTION_STOP_ALARM, null, context, UpdateService.class ) );
            }
        }
    }

    public static class UpdateService extends IntentService {

        public final static String ACTION_CLOCK_OFF = "com.zode64.trellodoing.intent.action.CLOCK_OFF";
        public final static String ACTION_REFRESH = "com.zode64.trellodoing.intent.action.REFRESH";
        public final static String ACTION_AUTO_UPDATE = "com.zode64.trellodoing.intent.action.AUTO_UPDATE";
        public final static String ACTION_STOP_ALARM = "com.zode64.trellodoing.intent.action.STOP_ALARM";
        public final static String ACTION_KEEP_DOING = "com.zode64.trellodoing.intent.action.KEEP_DOING";
        public final static String ACTION_SET_DEADLINE = "com.zode64.trellodoing.intent.action.SET_DEADLINE";
        public final static String ACTION_NETWORK_CHANGE = "com.zode64.trellodoing.intent.action.NETWORK_CHANGE";
        public final static String ACTION_ADD_PERSONAL_CARD = "com.zode64.trellodoing.intent.action.ADD_PERSONAL_CARD";
        public final static String ACTION_LIST_ITEM_CLICKED = "com.zode64.trellodoing.intent.action.LIST_ITEM_CLICKED";

        public static final String EXTRA_CARD_ID = "com.zode64.trellodoing.cardsproivder.EXTRA_CARD_ID";

        public UpdateService() {
            super( "Trello service" );
        }

        @Override
        protected void onHandleIntent( Intent intent ) {
            Log.d( TAG, "onHandleIntent()" );
            Log.d( TAG, "Intent action: " + intent.getAction() );

            DoingPreferences preferences = new DoingPreferences( this );
            WidgetAlarm appWidgetAlarm = new WidgetAlarm( this.getApplicationContext(), preferences );
            if ( ACTION_STOP_ALARM.equals( intent.getAction() ) ) {
                appWidgetAlarm.stopStandardAlarm();
                return;
            }
            if ( ACTION_REFRESH.equals( intent.getAction() ) ) {
                preferences.handleKeepDoingCardComplete();
            }

            CardDAO cardDAO = new CardDAO( this );
            // If from a network change or something like that don't reset the deadline
            if ( ACTION_NETWORK_CHANGE.equals( intent.getAction() ) ) {
                if ( preferences.hasKeepDoing() && !cardDAO.existsDeadlineSet() ) {
                    return;
                }
            }
            if ( ACTION_ADD_PERSONAL_CARD.equals( intent.getAction() ) ) {
                Intent cardAdderIntent = new Intent( this, PersonalTodoAdder.class );
                cardAdderIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( cardAdderIntent );
                return;
            }
            if ( ACTION_KEEP_DOING.equals( intent.getAction() ) ) {
                Calendar alarm = appWidgetAlarm.delayAlarm();
                preferences.handleSetKeepDoingCall( alarm );
                Log.d( TAG, "Setting keep doing" );
                return;
            }

            RemoteViews views = new RemoteViews( this.getPackageName(), R.layout.widget_doing );
            ComponentName thisWidget = new ComponentName( this, DoingWidget.class );

            DoingNotification notifications = new DoingNotification( this );
            notifications.removeAll();

            TrelloManager trelloManager = new TrelloManager( preferences.getSharedPreferences() );
            appWidgetAlarm.setAlarm();

            setSettingsListener( views );
            setRefreshClickListener( views );
            setKeepDoingClickListener( views );
            setAddPersonalCardListener( views );
            setCardListListener( views );

            ArrayList<Card> cardsPendingPush = cardDAO.wherePendingPush();
            for ( Card card : cardsPendingPush ) {
                if ( card.getId().equals( "temp" ) ) {
                    if ( trelloManager.newPersonalCard( card.getName() ) ) {
                        cardDAO.delete( card.getId() );
                    }
                } else if ( card.getIsClockedOff() == 1 ) {
                    if ( trelloManager.clockOff( card ) ) {
                        cardDAO.delete( card.getId() );
                    }
                }
            }

            Member member = trelloManager.member();
            if ( member != null ) {
                cardDAO.deleteAll();
                ArrayList<Card> cards = new ArrayList<>();
                if ( !cards.isEmpty() ) {
                    for ( Card card : cards ) {
                        cardDAO.create( card );
                        deadlineCheck( card, notifications );
                        notifications.standard( true, card.getBoardShortUrl() );
                        Log.d( TAG, "Personal card id: " + card.getId() );
                    }
                }
                if ( cards.size() > 0 ) {
                    preferences.saveBoard( doingCards.get( 0 ).getBoardShortUrl() );
                    if ( doingCards.size() > 1 ) {
                        notifications.multiDoings( doingCards.get( 1 ).getBoardShortUrl() );
                    }
                } else {
                    notifications.standard( false, preferences.getLastDoingBoard() );
                    preferences.handleKeepDoingCardComplete();
                    appWidgetAlarm.stopDeadlineAlarm();
                }
                setLastChecked( views );
            } else {
                // TODO this is an issue because it presents personal card in place of the last working card
                Card personalCard = cardDAO.getPersonalCard();
                if ( personalCard != null ) {
                    deadlineCheck( personalCard, notifications );
                }
                setLastCheckedOffline( views );
            }

            AppWidgetManager manager = AppWidgetManager.getInstance( this );
            int[] ids = manager.getAppWidgetIds( thisWidget );

            Intent doingAdapterIntent = new Intent( this, WidgetService.class );
            doingAdapterIntent.setData( Uri.parse( doingAdapterIntent.toUri( Intent.URI_INTENT_SCHEME ) ) );
            views.setRemoteAdapter( R.id.doing_cards_list, doingAdapterIntent );
            manager.notifyAppWidgetViewDataChanged( ids, R.id.doing_cards_list );

            Intent todayAdapterIntent = new Intent( this, WidgetService.class );
            todayAdapterIntent.setData( Uri.parse( todayAdapterIntent.toUri( Intent.URI_INTENT_SCHEME ) ) );
            views.setRemoteAdapter( R.id.today_cards_list, todayAdapterIntent );
            manager.notifyAppWidgetViewDataChanged( ids, R.id.today_cards_list );

            manager.updateAppWidget( thisWidget, views );
            //setting an empty view in case of no data
            Log.d( TAG, "Widget updated" );
        }


        private void setLastChecked( RemoteViews views ) {
            DateFormat df = new SimpleDateFormat( "HH:mm dd/MM" );
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format( today );
            views.setTextViewText( R.id.last_checked, reportDate );
        }

        private void setLastCheckedOffline( RemoteViews views ) {
            views.setTextViewText( R.id.last_checked, getString( R.string.offline ) );
        }

        private void setRefreshClickListener( RemoteViews views ) {
            Intent refresh = new Intent( UpdateService.ACTION_REFRESH );
            PendingIntent pendingRefresh = PendingIntent.getService( this, 0, refresh, 0 );
            views.setOnClickPendingIntent( R.id.refresh, pendingRefresh );
        }

        private void setKeepDoingClickListener( RemoteViews views ) {
            Intent keepDoing = new Intent( UpdateService.ACTION_KEEP_DOING );
            PendingIntent pendingKeepDoing = PendingIntent.getService( this, 0, keepDoing, 0 );
            views.setOnClickPendingIntent( R.id.keep_doing, pendingKeepDoing );
        }

        private void setSettingsListener( RemoteViews views ) {
            Intent settings = new Intent( this, MainActivity.class );
            PendingIntent pendingSettings = PendingIntent.getActivity( this, 0, settings, 0 );
            views.setOnClickPendingIntent( R.id.settings, pendingSettings );
        }

        private void setCardListListener( RemoteViews views ) {
            Intent itemClickIntent = new Intent( this, CardActionActivity.class );
            PendingIntent itemClickPendingIntent = PendingIntent.getActivity( this, 0, itemClickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT );
            views.setPendingIntentTemplate( R.id.doing_cards_list, itemClickPendingIntent );
        }

        private void setAddPersonalCardListener( RemoteViews views ) {
            Intent addPersonalCard = new Intent( UpdateService.ACTION_ADD_PERSONAL_CARD );
            PendingIntent pendingAddPersonalCard = PendingIntent.getService( this, 0, addPersonalCard, 0 );
            views.setOnClickPendingIntent( R.id.add_personal_card, pendingAddPersonalCard );
        }

        private void deadlineCheck( Card card, DoingNotification notifications ) {
            if ( card.pastDeadline() ) {
                Log.d( TAG, "Card not complete after deadline" );
                notifications.deadline( card.getBoardShortUrl() );
            }
        }
    }
}