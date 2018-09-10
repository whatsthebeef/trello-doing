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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.zode64.trellodoing.BoardSelectActivity;
import com.zode64.trellodoing.CardActionActivity;
import com.zode64.trellodoing.CardAdderActivity;
import com.zode64.trellodoing.DoingPreferences;
import com.zode64.trellodoing.KeepDoingActivity;
import com.zode64.trellodoing.MainActivity;
import com.zode64.trellodoing.R;
import com.zode64.trellodoing.db.ActionDAO;
import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Member;
import com.zode64.trellodoing.utils.DoingNotification;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.utils.WidgetAlarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static com.zode64.trellodoing.utils.TimeUtils.betweenHours;
import static com.zode64.trellodoing.utils.TimeUtils.mondayToThursday;

public class DoingWidget extends AppWidgetProvider {

    public final static String ACTION_SYNC = "com.zode64.trellodoing.intent.action.SYNC";
    public final static String ACTION_STANDARD_ALARM = "com.zode64.trellodoing.intent.action.STANDARD_ALARM";
    public final static String ACTION_SET_ALARM = "com.zode64.trellodoing.intent.action.SET_ALARM";
    public final static String ACTION_STOP_ALARM = "com.zode64.trellodoing.intent.action.STOP_ALARM";
    public final static String ACTION_NETWORK_CHANGE = "com.zode64.trellodoing.intent.action.NETWORK_CHANGE";
    public final static String ACTION_ADD_CARD = "com.zode64.trellodoing.intent.action.ADD_CARD";
    public final static String ACTION_TODAY_LIST_SWITCH = "com.zode64.trellodoing.intent.action.TODAY_SWITCH";
    public final static String ACTION_THIS_WEEK_LIST_SWITCH = "com.zode64.trellodoing.intent.action.THIS_WEEK_SWITCH";
    public final static String ACTION_SHOW_BOARDS = "com.zode64.trellodoing.intent.action.SHOW_BOARDS";
    public final static String ACTION_LIST_SWITCH = "com.zode64.trellodoing.intent.action.LIST_SWITCH";
    public final static String ACTION_ACTION_PERFORMED = "com.zode64.trellodoing.intent.action.ACTION_PERFORMED";

    public static final String EXTRA_CARD_ID = "com.zode64.trellodoing.cardsproivder.EXTRA_CARD_ID";
    public static final String EXTRA_BOARD_ID = "com.zode64.trellodoing.cardsproivder.EXTRA_BOARD_ID";

    public static final int NO_CONNECTION = -1;

    private final static String TAG = DoingWidget.class.getName();

    /**
     * Prevents high frequency polling because of constant network changes
     */
    private volatile static Boolean isAlreadyConnected = false;

    private volatile static Integer connectionType = -2;

    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds ) {
        Log.d( TAG, "onUpdate()" );

        connectionType = connectionType( context );

        RemoteViews views = new RemoteViews( context.getPackageName(), R.layout.widget_doing );
        setSettingsListener( views, context );
        setSyncClickListener( views, context );
        setKeepDoingClickListener( views, context );
        setAddCardListener( views, context );
        setTodayListBtnListener( views, context );
        setThisWeekListBtnListener( views, context );
        setShowBoardsBtnListener( views, context );

        setListAdapter( views, R.id.doing_cards_list, DoingWidgetService.class, context );
        setListAdapter( views, R.id.today_cards_list, TodayWidgetService.class, context );
        setListAdapter( views, R.id.clocked_off_cards_list, ClockedOffWidgetService.class, context );
        setCardListListener( views, context );

        appWidgetManager.updateAppWidget( appWidgetIds, views );

        DoingPreferences preferences = new DoingPreferences( context );
        if ( preferences.isThisWeek() ) {
            showThisWeek( views, context );
        } else {
            showToday( views, context );
        }

        launchService( views, context );

        super.onUpdate( context, appWidgetManager, appWidgetIds );
    }

    @Override
    public void onReceive( Context context, Intent intent ) {
        super.onReceive( context, intent );
        Log.d( TAG, "onReceive()" );
        String action = intent.getAction();
        if ( ACTION_APPWIDGET_UPDATE.equals( action ) ) {
            return;
        }
        DoingPreferences preferences = new DoingPreferences( context );
        RemoteViews views = new RemoteViews( context.getPackageName(), R.layout.widget_doing );
        if ( ACTION_THIS_WEEK_LIST_SWITCH.equals( action ) ) {
            preferences.setThisWeek();
            showThisWeek( views, context );
        } else {
            preferences.setToday();
            showToday( views, context );
        }
        launchService( views, context, ACTION_LIST_SWITCH );
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
    }

    public static int connectionType( Context context ) {
        final ConnectivityManager connMgr = ( ConnectivityManager ) context
                .getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if ( netInfo != null && netInfo.isConnected() ) {
            return netInfo.getType();
        } else {
            return NO_CONNECTION;
        }
    }

    private void setSyncClickListener( RemoteViews views, Context context ) {
        Intent sync = new Intent( ACTION_SYNC );
        PendingIntent pendingSync = PendingIntent.getService( context, 0, sync, 0 );
        views.setOnClickPendingIntent( R.id.sync, pendingSync );
    }

    private void setKeepDoingClickListener( RemoteViews views, Context context ) {
        Intent keepDoing = new Intent( context, KeepDoingActivity.class );
        PendingIntent pendingKeepDoing = PendingIntent.getActivity( context, 0, keepDoing, 0 );
        views.setOnClickPendingIntent( R.id.keep_doing, pendingKeepDoing );
    }

    private void setShowBoardsBtnListener( RemoteViews views, Context context ) {
        Intent boardsIntent = new Intent( context, BoardSelectActivity.class );
        PendingIntent pendingBroadcastBoardsIntent = PendingIntent.getActivity( context, 0, boardsIntent, 0 );
        views.setOnClickPendingIntent( R.id.show_boards_btn, pendingBroadcastBoardsIntent );
    }

    private void setSettingsListener( RemoteViews views, Context context ) {
        Intent settings = new Intent( context, MainActivity.class );
        PendingIntent pendingSettings = PendingIntent.getActivity( context, 0, settings, 0 );
        views.setOnClickPendingIntent( R.id.settings, pendingSettings );
    }

    private void setCardListListener( RemoteViews views, Context context ) {
        Intent itemClickIntent = new Intent( context, CardActionActivity.class );
        PendingIntent itemClickPendingIntent = PendingIntent.getActivity( context, 0, itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT );
        views.setPendingIntentTemplate( R.id.doing_cards_list, itemClickPendingIntent );
        views.setPendingIntentTemplate( R.id.today_cards_list, itemClickPendingIntent );
        views.setPendingIntentTemplate( R.id.clocked_off_cards_list, itemClickPendingIntent );
    }

    private void setAddCardListener( RemoteViews views, Context context ) {
        Intent addPersonalCard = new Intent( ACTION_ADD_CARD );
        PendingIntent pendingAddPersonalCard = PendingIntent.getService( context, 0, addPersonalCard, 0 );
        views.setOnClickPendingIntent( R.id.add_card, pendingAddPersonalCard );
    }

    private void setTodayListBtnListener( RemoteViews views, Context context ) {
        Intent switchIntent = new Intent( ACTION_TODAY_LIST_SWITCH );
        PendingIntent pendingBroadcastSwitchIntent = PendingIntent.getBroadcast( context, 0, switchIntent, 0 );
        views.setOnClickPendingIntent( R.id.today_btn, pendingBroadcastSwitchIntent );
    }

    private void setThisWeekListBtnListener( RemoteViews views, Context context ) {
        Intent switchIntent = new Intent( ACTION_THIS_WEEK_LIST_SWITCH );
        PendingIntent pendingBroadcastSwitchIntent = PendingIntent.getBroadcast( context, 0, switchIntent, 0 );
        views.setOnClickPendingIntent( R.id.this_week_btn, pendingBroadcastSwitchIntent );
    }

    private void setListAdapter( RemoteViews views, int resourceId, Class widgetService, Context context ) {
        Intent intent = new Intent( context, widgetService );
        intent.setData( Uri.parse( intent.toUri( Intent.URI_INTENT_SCHEME ) ) );
        views.setRemoteAdapter( resourceId, intent );
    }

    private void launchService( RemoteViews views, Context context ) {
        launchService( views, context, null );
    }

    private void launchService( RemoteViews views, Context context, String action ) {
        AppWidgetManager manager = AppWidgetManager.getInstance( context );
        ComponentName thisWidget = new ComponentName( context, DoingWidget.class );
        int[] ids = manager.getAppWidgetIds( thisWidget );
        manager.partiallyUpdateAppWidget( ids, views );
        Intent launchService = new Intent( context, UpdateService.class );
        if ( action != null ) {
            launchService.setAction( action );
        }
        context.startService( launchService );
    }

    private void showThisWeek( RemoteViews views, Context context ) {
        views.setTextViewText( R.id.today_list_title, context.getString( R.string.this_week ) );
        views.setViewVisibility( R.id.today_btn, View.VISIBLE );
        views.setViewVisibility( R.id.this_week_btn, View.GONE );
    }

    private void showToday( RemoteViews views, Context context ) {
        views.setTextViewText( R.id.today_list_title, context.getString( R.string.today ) );
        views.setViewVisibility( R.id.today_btn, View.GONE );
        views.setViewVisibility( R.id.this_week_btn, View.VISIBLE );
    }

    public static class NetworkChangeReceiver extends BroadcastReceiver {

        public NetworkChangeReceiver() {
            super();
        }

        @Override
        public void onReceive( final Context context, final Intent intent ) {
            Log.d( TAG, "onReceive()" );
            //should check null because in airplane mode it will be null
            connectionType = connectionType( context );
            if ( connectionType != NO_CONNECTION ) {
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

        Handler mHandler;

        public UpdateService() {
            super( "Trello service" );
            mHandler = new Handler();
        }

        @Override
        protected void onHandleIntent( Intent intent ) {
            Log.d( TAG, "onHandleIntent()" );
            Log.d( TAG, "Intent action: " + intent.getAction() );

            DoingPreferences preferences = new DoingPreferences( this );
            WidgetAlarm appWidgetAlarm = new WidgetAlarm( this.getApplicationContext(), preferences );
            DoingNotification notifications = new DoingNotification( this );
            TrelloManager trelloManager = new TrelloManager( preferences );
            AppWidgetManager manager = AppWidgetManager.getInstance( this );
            ComponentName thisWidget = new ComponentName( this, DoingWidget.class );
            int[] ids = manager.getAppWidgetIds( thisWidget );

            if ( ACTION_ADD_CARD.equals( intent.getAction() ) ) {
                Intent cardAdderIntent = new Intent( this, CardAdderActivity.class );
                cardAdderIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( cardAdderIntent );
                return;
            } else if ( ACTION_STOP_ALARM.equals( intent.getAction() ) ) {
                appWidgetAlarm.stopStandardAlarm();
                return;
            } else if ( ACTION_SHOW_BOARDS.equals( intent.getAction() ) ) {
                Intent boardsIntent = new Intent( this, BoardSelectActivity.class );
                boardsIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( boardsIntent );
                return;
            }

            // If from a network change or something like that don't reset the deadline
            if ( preferences.keepDoing() ) {
                if ( ACTION_SET_ALARM.equals( intent.getAction() )
                        || ACTION_LIST_SWITCH.equals( intent.getAction() ) ) {
                    notifications.removeAll();
                    notifyLists( ids, manager );
                }
                if ( !ACTION_ACTION_PERFORMED.equals( intent.getAction() ) ) {
                    return;
                }
            }

            if ( !preferences.hasToken() ) {
                mHandler.post( new DisplayTokenMissingToast( this ) );
                return;
            }

            BoardDAO boardDAO = new BoardDAO( this );
            HashMap<String, Board> boardMap = boardDAO.boardMap();
            CardDAO cardDAO = new CardDAO( this, boardMap );
            ActionDAO actionDAO = new ActionDAO( this, trelloManager, cardDAO );

            if ( ACTION_SYNC.equals( intent.getAction() ) ) {
                if ( connectionType != NO_CONNECTION ) {
                    sync( trelloManager, boardDAO );
                }
            }

            updateCardLists( cardDAO, actionDAO, trelloManager, boardMap );

            if ( ACTION_ACTION_PERFORMED.equals( intent.getAction() ) && preferences.keepDoing() ) {
                // don't do notifications
            } else if ( updateNotifications( cardDAO, preferences, notifications ) ) {
                appWidgetAlarm.setQuickAlarm();
            } else {
                appWidgetAlarm.setAlarm();
            }

            // Clean up
            actionDAO.closeDB();
            cardDAO.closeDB();
            boardDAO.closeDB();

            notifyLists( ids, manager );
            //setting an empty view in case of no data
            Log.d( TAG, "Widget updated" );
        }

        private void notifyLists( int[] ids, AppWidgetManager manager ) {
            manager.notifyAppWidgetViewDataChanged( ids, R.id.doing_cards_list );
            manager.notifyAppWidgetViewDataChanged( ids, R.id.today_cards_list );
            manager.notifyAppWidgetViewDataChanged( ids, R.id.clocked_off_cards_list );
        }

        private void updateCardLists( CardDAO cardDAO, ActionDAO actionDAO, TrelloManager trelloManager,
                                      HashMap<String, Board> boardMap ) {
            if ( connectionType != NO_CONNECTION ) {
                // Start with out standing operations in the cache
                ArrayList<Action> actions = actionDAO.all();
                for ( Action action : actions ) {
                    if ( action.perform() ) {
                        actionDAO.delete( action.getId() );
                    } else {
                        break;
                    }
                }
                // Try update the cache
                Member member = trelloManager.cards( boardMap );
                if ( member != null ) {
                    ArrayList<Card> cards = member.getCards();
                    // Clear cache
                    cardDAO.deleteAll();
                    for ( Card card : cards ) {
                        if ( boardMap.containsKey( card.getBoardId() ) ) {
                            cardDAO.create( card );
                            Log.d( TAG, "Card id: " + card.getId() );
                        }
                    }
                }
            }
        }

        private boolean updateNotifications( CardDAO cardDAO, DoingPreferences preferences,
                                             DoingNotification notifications ) {
            ArrayList<Card> cards = cardDAO.all();
            // Check to see if we should create any notifications
            notifications.removeAll();
            int numDoingCards = 0;
            int numDoingWorkCards = 0;
            int numClockedOffCards = 0;
            int numTodayCards = 0;
            int numThisWeekCards = 0;
            String doingBoardUrl = preferences.getLastDoingBoard();
            String clockedOffBoardUrl = null;
            Calendar now = Calendar.getInstance();
            boolean notificationSet = false;
            for ( Card card : cards ) {
                if ( card.isClockedOn() ) {
                    doingBoardUrl = card.getBoardShortUrl();
                    preferences.saveLastDoingBoard( doingBoardUrl );
                    numDoingCards++;
                    if ( card.isWorkCard() ) {
                        numDoingWorkCards++;
                        if ( !betweenHours( preferences.getStartHour(), preferences.getEndHour(), now ) ) {
                            notifications.clockOff( doingBoardUrl );
                            notificationSet = true;
                        }
                    }
                }
                if ( card.isClockedOn() || card.getListType() == Board.ListType.TODAY ) {
                    if ( card.getListType() == Board.ListType.TODAY ) {
                        if ( card.tooMuchTimeSpentInCurrentList( preferences.getEndHour() ) ) {
                            notifications.todayTooLong( card.getBoardShortUrl() );
                            notificationSet = true;
                        }
                        numTodayCards++;
                    }
                }
                if ( card.getListType() == Board.ListType.THIS_WEEK ) {
                    if ( card.tooMuchTimeSpentInCurrentList( preferences.getEndHour() ) ) {
                        notifications.thisWeekTooLong( card.getBoardShortUrl() );
                        notificationSet = true;
                    }
                    numThisWeekCards++;
                }
                if ( card.getListType() == Board.ListType.CLOCKED_OFF ) {
                    numClockedOffCards++;
                    clockedOffBoardUrl = card.getBoardShortUrl();
                }
            }
            if ( numDoingWorkCards == 0 ) {
                if ( betweenHours( preferences.getStartHour(), preferences.getEndHour(), now ) ) {
                    notifications.clockOn( doingBoardUrl );
                    notificationSet = true;
                }
            } else if ( numDoingCards > 1 ) {
                notifications.multiDoings( doingBoardUrl );
                notificationSet = true;
            }
            if ( numClockedOffCards > 1 ) {
                notifications.multiClockedOff( clockedOffBoardUrl );
                notificationSet = true;
            }
            if ( numThisWeekCards == 0 && mondayToThursday( now ) && betweenHours( preferences.getStartHour(), 23, now ) ) {
                notifications.thisWeekEmpty( clockedOffBoardUrl );
                notificationSet = true;
            }
            /*
            if ( numTodayCards == 0 && betweenHours(preferences.getStartHour(), 12, now ) ) {
                notifications.todayEmpty( clockedOffBoardUrl );
                notificationSet = true;
            }
            */
            return notificationSet;
        }

        private void sync( TrelloManager trello, BoardDAO boardDAO ) {
            Log.i( TAG, "Syncing..." );
            Member member = trello.boards();
            ArrayList<Card> cards = member.getCards();
            boardDAO.deleteAll();
            for ( Board board : member.getBoards() ) {
                boardDAO.create( board );
            }
        }

        public class DisplayTokenMissingToast implements Runnable {
            private final Context mContext;

            DisplayTokenMissingToast( Context mContext ) {
                this.mContext = mContext;
            }

            public void run() {
                Toast.makeText( mContext, getResources().getString( R.string.token_missing ),
                        Toast.LENGTH_LONG ).show();
            }
        }
    }
}
