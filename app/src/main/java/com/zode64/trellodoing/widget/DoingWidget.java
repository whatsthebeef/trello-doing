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

import static com.zode64.trellodoing.utils.TimeUtils.between;

public class DoingWidget extends AppWidgetProvider {

    public final static String ACTION_SYNC = "com.zode64.trellodoing.intent.action.SYNC";
    public final static String ACTION_DEADLINE_ALARM = "com.zode64.trellodoing.intent.action.DEADLINE_ALARM";
    public final static String ACTION_STANDARD_ALARM = "com.zode64.trellodoing.intent.action.STANDARD_ALARM";
    public final static String ACTION_SET_ALARM = "com.zode64.trellodoing.intent.action.SET_ALARM";
    public final static String ACTION_STOP_ALARM = "com.zode64.trellodoing.intent.action.STOP_ALARM";
    public final static String ACTION_NETWORK_CHANGE = "com.zode64.trellodoing.intent.action.NETWORK_CHANGE";
    public final static String ACTION_ADD_CARD = "com.zode64.trellodoing.intent.action.ADD_CARD";
    public final static String ACTION_TODAY_BOARDS_SWITCH = "com.zode64.trellodoing.intent.action.TODAY_BOARDS_SWITCH";
    public final static String ACTION_UPLOAD_ATTACHMENTS = "com.zode64.trellodoing.intent.action.UPLOAD_ATTACHMENTS";

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
                    intent.putExtra( EXTRA_WIFI_CHECKED, true );
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
            if ( ACTION_STOP_ALARM.equals( intent.getAction() ) ) {
                appWidgetAlarm.stopStandardAlarm();
                return;
            }
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
            if ( ACTION_ADD_CARD.equals( intent.getAction() ) ) {
                Intent cardAdderIntent = new Intent( this, CardAdderActivity.class );
                BoardDAO boardDAO = new BoardDAO( this );
                cardAdderIntent.putExtra( EXTRA_BOARD_ID, boardDAO.findPersonalBoard().getId() );
                cardAdderIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( cardAdderIntent );
                return;
            }
            if ( ACTION_TODAY_BOARDS_SWITCH.equals( intent.getAction() ) ) {
                if ( preferences.isBoards() ) {
                    preferences.setToday();
                } else {
                    preferences.setBoards();
                }
            }
            if ( !ACTION_SET_ALARM.equals( intent.getAction() ) ) {
                appWidgetAlarm.setAlarm();
            }

            if ( !preferences.hasToken() ) {
                mHandler.post( new DisplayTokenMissingToast( this ) );
                return;
            }

            TrelloManager trelloManager = new TrelloManager( preferences );
            CardDAO cardDAO = new CardDAO( this );
            ActionDAO actionDAO = new ActionDAO( this, trelloManager, cardDAO );
            ArrayList<Card> cards = null;

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

                if ( ACTION_UPLOAD_ATTACHMENTS.equals( intent.getAction() ) ) {
                    if ( !intent.getBooleanExtra( EXTRA_WIFI_CHECKED, false ) ) {
                        if ( connectionType == ConnectivityManager.TYPE_WIFI ) {
                            return;
                        }
                    }
                    postAttachments( trelloManager );
                }
                // Try update the cache
                Member member = trelloManager.member();
                if ( member != null ) {
                    cards = member.getCards();
                    if ( ACTION_SYNC.equals( intent.getAction() ) ) {
                        sync( member );
                    }
                    // Clear cache
                    cardDAO.deleteAll();
                    for ( Card card : cards ) {
                        cardDAO.create( card );
                        Log.d( TAG, "Card id: " + card.getId() );
                    }
                }
            }

            cards = cardDAO.all();
            // Check to see if we should create any notifications
            DeadlineDAO deadlineDAO = new DeadlineDAO( this );
            HashMap<String, Long> deadlines = deadlineDAO.all();
            DoingNotification notifications = new DoingNotification( this );
            notifications.removeAll();
            int numDoingCards = 0;
            String doingBoardUrl = preferences.getLastDoingBoard();
            Calendar now = Calendar.getInstance();
            for ( Card card : cards ) {
                if ( card.isClockedOn() ) {
                    doingBoardUrl = card.getBoardShortUrl();
                    preferences.saveLastDoingBoard( doingBoardUrl );
                    numDoingCards++;
                    if ( !between( preferences.getStartHour(), preferences.getEndHour(), now ) ) {
                        notifications.clockOff( doingBoardUrl );
                    }
                }
                if ( card.isClockedOn() || card.getInListType() == Card.ListType.TODAY ) {
                    if ( TimeUtils.pastDeadline( deadlines.get( card.getServerId() ), now.getTimeInMillis() ) ) {
                        notifications.deadline( card.getBoardShortUrl() );
                    }
                }
            }
            if ( numDoingCards == 0 ) {
                if ( between( preferences.getStartHour(), preferences.getEndHour(), now ) ) {
                    notifications.clockOn( doingBoardUrl );
                }
            } else if ( numDoingCards > 1 ) {
                notifications.multiDoings( doingBoardUrl );
            }

            // Clean up
            actionDAO.closeDB();
            cardDAO.closeDB();
            deadlineDAO.closeDB();

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

        private void postAttachments( TrelloManager trelloManager ) {
            AttachmentDAO attachmentDAO = new AttachmentDAO( this );
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

        private void sync( Member member ) {
            Log.i( TAG, "Syncing..." );
            ArrayList<Card> cards = member.getCards();
            HashMap<String, Card> cardReg = new HashMap<>();
            for ( Card card : cards ) {
                cardReg.put( card.getServerId(), card );
            }
            AttachmentDAO attachmentDAO = new AttachmentDAO( this );
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
            attachmentDAO.closeDB();
            DeadlineDAO deadlineDAO = new DeadlineDAO( this );
            HashMap<String, Long> deadlines = deadlineDAO.all();
            for ( Map.Entry<String, Long> deadline : deadlines.entrySet() ) {
                if ( !cardReg.containsKey( deadline.getKey() ) ) {
                    Log.i( TAG, "Deleting deadline for " + deadline.getKey() );
                    deadlineDAO.delete( deadline.getKey() );
                }
            }
            deadlineDAO.closeDB();
            BoardDAO boardDAO = new BoardDAO( this );
            boardDAO.deleteAll();
            for ( Board board : member.getBoards() ) {
                boardDAO.create( board );
            }
            boardDAO.closeDB();
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