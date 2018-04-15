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

import com.zode64.trellodoing.CardActionActivity;
import com.zode64.trellodoing.CardAdderActivity;
import com.zode64.trellodoing.DoingPreferences;
import com.zode64.trellodoing.KeepDoingActivity;
import com.zode64.trellodoing.MainActivity;
import com.zode64.trellodoing.R;
import com.zode64.trellodoing.db.ActionDAO;
import com.zode64.trellodoing.db.AttachmentDAO;
import com.zode64.trellodoing.db.BoardDAO;
import com.zode64.trellodoing.db.CardDAO;
import com.zode64.trellodoing.db.DeadlineDAO;
import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Attachment;
import com.zode64.trellodoing.models.Board;
import com.zode64.trellodoing.models.Card;
import com.zode64.trellodoing.models.Member;
import com.zode64.trellodoing.utils.DoingNotification;
import com.zode64.trellodoing.utils.TimeUtils;
import com.zode64.trellodoing.utils.TrelloManager;
import com.zode64.trellodoing.utils.WidgetAlarm;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;
import static com.zode64.trellodoing.utils.TimeUtils.between;

public class DoingWidget extends AppWidgetProvider {

    public final static String ACTION_SYNC = "com.zode64.trellodoing.intent.action.SYNC";
    public final static String ACTION_DEADLINE_ALARM = "com.zode64.trellodoing.intent.action.DEADLINE_ALARM";
    public final static String ACTION_STANDARD_ALARM = "com.zode64.trellodoing.intent.action.STANDARD_ALARM";
    public final static String ACTION_SET_ALARM = "com.zode64.trellodoing.intent.action.SET_ALARM";
    public final static String ACTION_STOP_ALARM = "com.zode64.trellodoing.intent.action.STOP_ALARM";
    public final static String ACTION_NETWORK_CHANGE = "com.zode64.trellodoing.intent.action.NETWORK_CHANGE";
    public final static String ACTION_ADD_CARD = "com.zode64.trellodoing.intent.action.ADD_CARD";
    public final static String ACTION_TODAY_LIST_SWITCH = "com.zode64.trellodoing.intent.action.TODAY_SWITCH";
    public final static String ACTION_THIS_WEEK_LIST_SWITCH = "com.zode64.trellodoing.intent.action.THIS_WEEK_SWITCH";
    public final static String ACTION_LIST_SWITCH = "com.zode64.trellodoing.intent.action.LIST_SWITCH";
    public final static String ACTION_UPLOAD_ATTACHMENTS = "com.zode64.trellodoing.intent.action.UPLOAD_ATTACHMENTS";
    public final static String ACTION_ACTION_PERFORMED = "com.zode64.trellodoing.intent.action.ACTION_PERFORMED";

    public static final String EXTRA_CARD_ID = "com.zode64.trellodoing.cardsproivder.EXTRA_CARD_ID";
    public static final String EXTRA_BOARD_ID = "com.zode64.trellodoing.cardsproivder.EXTRA_BOARD_ID";
    public static final String EXTRA_WIFI_CHECKED = "com.zode64.trellodoing.cardsproivder.EXTRA_WIFI_CHECKED";

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

        setListAdapter( views, R.id.doing_cards_list, DoingWidgetService.class, context );
        setListAdapter( views, R.id.today_cards_list, TodayWidgetService.class, context );
        setListAdapter( views, R.id.clocked_off_cards_list, ClockedOffWidgetService.class, context );
        setCardListListener( views, context );

        appWidgetManager.updateAppWidget( appWidgetIds, views );

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
        } else if ( ACTION_TODAY_LIST_SWITCH.equals( action ) ) {
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
        widgetAlarm.stopDeadlineAlarm();
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
                if ( connectionType == ConnectivityManager.TYPE_WIFI ) {
                    Intent attachmentsIntent = new Intent( ACTION_UPLOAD_ATTACHMENTS, null, context, UpdateService.class );
                    context.startService( attachmentsIntent );
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

            DeadlineDAO deadlineDAO = new DeadlineDAO( this );
            BoardDAO boardDAO = new BoardDAO( this );
            HashMap<String, Board> boardMap = boardDAO.boardMap();
            CardDAO cardDAO = new CardDAO( this, boardMap );
            AttachmentDAO attachmentDAO = new AttachmentDAO( this );
            ActionDAO actionDAO = new ActionDAO( this, trelloManager, cardDAO, deadlineDAO, attachmentDAO );

            if ( ACTION_SYNC.equals( intent.getAction() ) ) {
                if ( connectionType != NO_CONNECTION ) {
                    sync( trelloManager, deadlineDAO, attachmentDAO, boardDAO );
                }
            } else if ( ACTION_UPLOAD_ATTACHMENTS.equals( intent.getAction() ) ) {
                postAttachments( trelloManager, attachmentDAO );
            }

            updateCardLists( cardDAO, actionDAO, trelloManager, boardMap );

            if ( ACTION_ACTION_PERFORMED.equals( intent.getAction() ) && preferences.keepDoing() ) {
                // don't do notifications
            } else if ( updateNotifications( cardDAO, deadlineDAO, preferences, notifications ) ) {
                appWidgetAlarm.setQuickAlarm();
            } else {
                appWidgetAlarm.setAlarm();
            }

            // Clean up
            actionDAO.closeDB();
            cardDAO.closeDB();
            deadlineDAO.closeDB();
            boardDAO.closeDB();
            attachmentDAO.closeDB();

            notifyLists( ids, manager );
            //setting an empty view in case of no data
            Log.d( TAG, "Widget updated" );
        }

        private void notifyLists( int[] ids, AppWidgetManager manager ) {
            manager.notifyAppWidgetViewDataChanged( ids, R.id.doing_cards_list );
            manager.notifyAppWidgetViewDataChanged( ids, R.id.today_cards_list );
            manager.notifyAppWidgetViewDataChanged( ids, R.id.clocked_off_cards_list );
        }

        private void postAttachments( TrelloManager trelloManager, AttachmentDAO attachmentDAO ) {
            ArrayList<Attachment> pendingAttachments = attachmentDAO.allPending();
            if ( !pendingAttachments.isEmpty() ) {
                for ( Attachment attachment : pendingAttachments ) {
                    int result = trelloManager.postAttachment( attachment );
                    switch ( result ) {
                        case TrelloManager.SUCCESS:
                            attachmentDAO.setUploaded( attachment.getId() );
                            break;
                        case TrelloManager.FILE_NOT_FOUND:
                            attachmentDAO.delete( attachment.getId() );
                            break;
                        default:
                            // Do nothing
                    }
                }
            }
            attachmentDAO.closeDB();
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

        private boolean updateNotifications( CardDAO cardDAO, DeadlineDAO deadlineDAO, DoingPreferences preferences,
                                             DoingNotification notifications ) {
            ArrayList<Card> cards = cardDAO.all();
            // Check to see if we should create any notifications
            HashMap<String, Long> deadlines = deadlineDAO.all();
            notifications.removeAll();
            int numDoingCards = 0;
            int numDoingWorkCards = 0;
            int numClockedOffCards = 0;
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
                        if ( !between( preferences.getStartHour(), preferences.getEndHour(), now ) ) {
                            notifications.clockOff( doingBoardUrl );
                            notificationSet = true;
                        }
                        preferences.addTimeSpentToday( now );
                        preferences.setPeriodStartTime( now );
                    } else {
                        preferences.addTimeSpentToday( now );
                        preferences.clearPeriodStartTime();
                    }
                } else {
                    preferences.addTimeSpentToday( now );
                    preferences.clearPeriodStartTime();
                }
                if ( card.isClockedOn() || card.getListType() == Board.ListType.TODAY ) {
                    if ( TimeUtils.pastDeadline( deadlines.get( card.getServerId() ), now.getTimeInMillis() ) ) {
                        notifications.deadline( card.getBoardShortUrl() );
                        notificationSet = true;
                    }
                    if ( card.getListType() == Board.ListType.TODAY && card.tooMuchTimeSpentInCurrentList() ) {
                        notifications.todayTooLong( card.getBoardShortUrl() );
                        notificationSet = true;
                    }
                }
                if ( card.getListType() == Board.ListType.THIS_WEEK && card.tooMuchTimeSpentInCurrentList() ) {
                    notifications.thisWeekTooLong( card.getBoardShortUrl() );
                    notificationSet = true;
                }
                if ( card.getListType() == Board.ListType.CLOCKED_OFF ) {
                    numClockedOffCards++;
                    clockedOffBoardUrl = card.getBoardShortUrl();
                }
            }
            if ( numDoingWorkCards == 0 ) {
                int hoursRemainingInDay = preferences.hoursRemainingInDay();
                int hoursInDay = preferences.getHoursInDay();
                if ( hoursRemainingInDay < hoursInDay ) {
                    notifications.hoursRemainingInDay( doingBoardUrl, hoursRemainingInDay );
                    notificationSet = true;
                }
                if ( between( preferences.getStartHour(), preferences.getEndHour(), now ) ) {
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
            return notificationSet;
        }

        private void sync( TrelloManager trello, DeadlineDAO deadlineDAO, AttachmentDAO attachmentDAO, BoardDAO boardDAO ) {
            Log.i( TAG, "Syncing..." );
            Member member = trello.boards();
            ArrayList<Card> cards = member.getCards();
            HashMap<String, Card> cardReg = new HashMap<>();
            for ( Card card : cards ) {
                cardReg.put( card.getServerId(), card );
            }
            Iterator<Attachment> attachments = attachmentDAO.all().iterator();
            while ( attachments.hasNext() ) {
                Attachment attachment = attachments.next();
                if ( !cardReg.containsKey( attachment.getCardServerId() ) ) {
                    File file = attachment.getFile();
                    File dir = file.getParentFile();
                    file.delete();
                    Log.i( TAG, "Deleting file:" + file.getName() );
                    if ( dir.list().length == 0 ) {
                        dir.delete();
                        Log.i( TAG, "Deleting dir:" + dir.getName() );
                    }
                    attachmentDAO.delete( attachment.getId() );
                }
            }
            HashMap<String, Long> deadlines = deadlineDAO.all();
            for ( Map.Entry<String, Long> deadline : deadlines.entrySet() ) {
                if ( !cardReg.containsKey( deadline.getKey() ) ) {
                    Log.i( TAG, "Deleting deadline for " + deadline.getKey() );
                    deadlineDAO.delete( deadline.getKey() );
                }
            }
            boardDAO.deleteAll();
            for ( Board board : member.getBoards() ) {
                boardDAO.create( board );
            }
        }

        public class DisplayTokenMissingToast implements Runnable {
            private final Context mContext;

            public DisplayTokenMissingToast( Context mContext ) {
                this.mContext = mContext;
            }

            public void run() {
                Toast.makeText( mContext, getResources().getString( R.string.token_missing ),
                        Toast.LENGTH_LONG ).show();
            }
        }
    }
}
