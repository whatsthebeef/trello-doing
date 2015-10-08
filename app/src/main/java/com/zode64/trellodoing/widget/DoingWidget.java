package com.zode64.trellodoing.widget;

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

import com.zode64.trellodoing.ActionActivity;
import com.zode64.trellodoing.AdderActivity;
import com.zode64.trellodoing.DoingNotification;
import com.zode64.trellodoing.DoingPreferences;
import com.zode64.trellodoing.KeepDoingActivity;
import com.zode64.trellodoing.MainActivity;
import com.zode64.trellodoing.R;
import com.zode64.trellodoing.TrelloManager;
import com.zode64.trellodoing.WidgetAlarm;
import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Member;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.zode64.trellodoing.TimeUtils.between;

public class DoingWidget extends AppWidgetProvider {

    public final static String ACTION_REFRESH = "com.zode64.trellodoing.intent.action.REFRESH";
    public final static String ACTION_DEADLINE_ALARM = "com.zode64.trellodoing.intent.action.DEADLINE_ALARM";
    public final static String ACTION_STANDARD_ALARM = "com.zode64.trellodoing.intent.action.STANDARD_ALARM";
    public final static String ACTION_SET_ALARM = "com.zode64.trellodoing.intent.action.SET_ALARM";
    public final static String ACTION_STOP_ALARM = "com.zode64.trellodoing.intent.action.STOP_ALARM";
    public final static String ACTION_NETWORK_CHANGE = "com.zode64.trellodoing.intent.action.NETWORK_CHANGE";
    public final static String ACTION_ADD_PERSONAL_CARD = "com.zode64.trellodoing.intent.action.ADD_PERSONAL_CARD";
    public final static String ACTION_TODAY_BOARDS_SWITCH = "com.zode64.trellodoing.intent.action.TODAY_BOARDS_SWITCH";

    public static final String EXTRA_CARD_ID = "com.zode64.trellodoing.cardsproivder.EXTRA_CARD_ID";
    public static final String EXTRA_BOARD_ID = "com.zode64.trellodoing.cardsproivder.EXTRA_BOARD_ID";

    private final static String TAG = DoingWidget.class.getName();

    private volatile static Boolean isAlreadyConnected = false;

    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds ) {
        Log.d( TAG, "onUpdate()" );

        RemoteViews views = new RemoteViews( context.getPackageName(), R.layout.widget_doing );
        setSettingsListener( views, context );
        setRefreshClickListener( views, context );
        setKeepDoingClickListener( views, context );
        setAddPersonalCardListener( views, context );
        setTodayBoardsSwitchListener( views, context );

        setListAdapter( views, R.id.doing_cards_list, DoingWidgetService.class, context );
        setListAdapter( views, R.id.today_cards_list, TodayWidgetService.class, context );
        setListAdapter( views, R.id.clocked_off_cards_list, ClockedOffWidgetService.class, context );
        setCardListListener( views, context );

        AppWidgetManager manager = AppWidgetManager.getInstance( context );
        ComponentName thisWidget = new ComponentName( context, DoingWidget.class );
        int[] ids = manager.getAppWidgetIds( thisWidget );
        manager.updateAppWidget( ids, views );

        // start alarm
        context.startService( new Intent( context, UpdateService.class ) );
        super.onUpdate( context, appWidgetManager, appWidgetIds );
    }

    @Override
    public void onEnabled( Context context ) {
        super.onEnabled( context );
        Log.d( TAG, "onEnabled()" );
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

    private void setRefreshClickListener( RemoteViews views, Context context ) {
        Intent refresh = new Intent( ACTION_REFRESH );
        PendingIntent pendingRefresh = PendingIntent.getService( context, 0, refresh, 0 );
        views.setOnClickPendingIntent( R.id.refresh, pendingRefresh );
    }

    private void setKeepDoingClickListener( RemoteViews views, Context context ) {
        Intent keepDoing = new Intent( context, KeepDoingActivity.class );
        PendingIntent pendingKeepDoing = PendingIntent.getActivity( context, 0, keepDoing, 0 );
        views.setOnClickPendingIntent( R.id.keep_doing, pendingKeepDoing );
    }

    private void setSettingsListener( RemoteViews views, Context context ) {
        Intent settings = new Intent( context, MainActivity.class );
        PendingIntent pendingSettings = PendingIntent.getActivity( context, 0, settings, 0 );
        views.setOnClickPendingIntent( R.id.settings, pendingSettings );
    }

    private void setCardListListener( RemoteViews views, Context context ) {
        Intent itemClickIntent = new Intent( context, ActionActivity.class );
        PendingIntent itemClickPendingIntent = PendingIntent.getActivity( context, 0, itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT );
        views.setPendingIntentTemplate( R.id.doing_cards_list, itemClickPendingIntent );
        views.setPendingIntentTemplate( R.id.today_cards_list, itemClickPendingIntent );
        views.setPendingIntentTemplate( R.id.clocked_off_cards_list, itemClickPendingIntent );
    }

    private void setAddPersonalCardListener( RemoteViews views, Context context ) {
        Intent addPersonalCard = new Intent( ACTION_ADD_PERSONAL_CARD );
        PendingIntent pendingAddPersonalCard = PendingIntent.getService( context, 0, addPersonalCard, 0 );
        views.setOnClickPendingIntent( R.id.add_personal_card, pendingAddPersonalCard );
    }

    private void setTodayBoardsSwitchListener( RemoteViews views, Context context ) {
        Intent switchIntent = new Intent( ACTION_TODAY_BOARDS_SWITCH );
        PendingIntent pendingSwitchIntent = PendingIntent.getService( context, 0, switchIntent, 0 );
        views.setOnClickPendingIntent( R.id.today_boards, pendingSwitchIntent );
    }

    private void setListAdapter( RemoteViews views, int resourceId, Class widgetService, Context context ) {
        Intent intent = new Intent( context, widgetService );
        intent.setData( Uri.parse( intent.toUri( Intent.URI_INTENT_SCHEME ) ) );
        views.setRemoteAdapter( resourceId, intent );
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
            if ( netInfo != null && netInfo.isConnected() ) {
                if ( !isAlreadyConnected ) {
                    // When the network returns any checks will begin as normal even if 'Keep Doing' alarm is set.
                    Log.d( TAG, "Connected to network" );
                    isAlreadyConnected = true;
                    context.startService( new Intent( ACTION_NETWORK_CHANGE, null, context, UpdateService.class ) );
                }
            } else {
                Log.d( TAG, "No network, stop checking for now" );
                isAlreadyConnected = false;
                context.startService( new Intent( ACTION_STOP_ALARM, null, context, UpdateService.class ) );
            }
        }

    }

    public static class UpdateService extends IntentService {

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
            // If from a network change or something like that don't reset the deadline
            if ( ACTION_NETWORK_CHANGE.equals( intent.getAction() ) || ACTION_STANDARD_ALARM.equals( intent.getAction() ) ) {
                if ( preferences.keepDoing() ) {
                    return;
                }
            }
            if ( ACTION_ADD_PERSONAL_CARD.equals( intent.getAction() ) ) {
                Intent cardAdderIntent = new Intent( this, AdderActivity.class );
                BoardDAO boardDAO = new BoardDAO( this );
                cardAdderIntent.putExtra( EXTRA_BOARD_ID, boardDAO.findPersonalBoard().getId() );
                cardAdderIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( cardAdderIntent );
                return;
            }
            if ( ACTION_TODAY_BOARDS_SWITCH.equals( intent.getAction() ) ) {
                if(preferences.isBoards()) {
                    preferences.setToday();
                }
                else {
                    preferences.setBoards();
                }
            }
            if ( !ACTION_SET_ALARM.equals( intent.getAction() ) ) {
                appWidgetAlarm.setAlarm();
            }

            TrelloManager trelloManager = new TrelloManager( preferences.getSharedPreferences() );
            CardDAO cardDAO = new CardDAO( this );
            boolean isOnline = true;

            // Start with out standing operations in the cache
            Map<String, Long> deadlines = new HashMap<>();
            ArrayList<Card> cards = cardDAO.all();
            for ( Card card : cards ) {
                if ( isOnline && card.isPendingPush() ) {
                    if ( card.getId().equals( "temp" ) ) {
                        BoardDAO boardDAO = new BoardDAO( this );
                        // Try push newly created cards
                        if ( !trelloManager.newCard( card, boardDAO.find( card.getBoardId() ) ) ) {
                            isOnline = false;
                        }
                    } else {
                        // Clock off, clock on, done, etc...
                        if ( !trelloManager.moveCard( card.getId(), card.getCurrentListId() ) ) {
                            // isOnline = false;
                        }
                    }
                }
                if ( card.hasDeadline() ) {
                    // Preserve deadlines in case of cache clear
                    deadlines.put( card.getId(), card.getDeadline() );
                }
            }

            // Try update the cache
            if ( isOnline ) {
                Member member = trelloManager.member();
                if ( ACTION_REFRESH.equals( intent.getAction() ) ) {
                    BoardDAO boardDAO = new BoardDAO( this );
                    boardDAO.deleteAll();
                    for ( Board board : member.getBoards() ) {
                        boardDAO.create( board );
                    }
                }
                if ( member != null ) {
                    // Clear cache
                    cardDAO.deleteAll();
                    cards = member.getCards();
                    for ( Card card : cards ) {
                        Long deadline = deadlines.get( card.getId() );
                        if ( deadline != null ) {
                            // recoup deadlines when constructing new cache
                            card.setDeadline( deadline );
                        }
                        cardDAO.create( card );
                        Log.d( TAG, "Card id: " + card.getId() );
                    }
                }
            }

            // Check to see if we should create any notifications
            DoingNotification notifications = new DoingNotification( this );
            notifications.removeAll();
            int numDoingCards = 0;
            String doingCardUrl = "https://trello.com";
            Calendar now = Calendar.getInstance();
            for ( Card card : cards ) {
                if ( card.isClockedOn() ) {
                    numDoingCards++;
                    doingCardUrl = card.getBoardShortUrl();
                    if ( !between( preferences.getStartHour(), preferences.getEndHour(), now ) ) {
                        notifications.clockOff( doingCardUrl );
                    }
                    if ( card.pastDeadline() ) {
                        notifications.deadline( card.getBoardShortUrl() );
                    }
                }
            }
            if ( numDoingCards == 0 ) {
                if ( between( preferences.getStartHour(), preferences.getEndHour(), now ) ) {
                    notifications.clockOn( doingCardUrl );
                }
            } else if ( numDoingCards > 1 ) {
                notifications.multiDoings( doingCardUrl );
            }

            // Clean up
            cardDAO.closeDB();

            AppWidgetManager manager = AppWidgetManager.getInstance( this );
            ComponentName thisWidget = new ComponentName( this, DoingWidget.class );
            int[] ids = manager.getAppWidgetIds( thisWidget );
            notifyLists( ids, manager );

            //setting an empty view in case of no data
            Log.d( TAG, "Widget updated" );
        }

        private void notifyLists( int[] ids, AppWidgetManager manager ) {
            manager.notifyAppWidgetViewDataChanged( ids, R.id.doing_cards_list );
            manager.notifyAppWidgetViewDataChanged( ids, R.id.today_cards_list );
            manager.notifyAppWidgetViewDataChanged( ids, R.id.clocked_off_cards_list );
        }
    }

}