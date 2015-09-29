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

import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Member;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DoingWidget extends AppWidgetProvider {

    private final static String TAG = DoingWidget.class.getName();

    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds ) {
        // super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d( TAG, "onUpdate()" );
        context.startService( new Intent( context, UpdateService.class ) );
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
        public final static String ACTION_CLOCK_OFF_PERSONAL = "com.zode64.trellodoing.intent.action.CLOCK_OFF_PERSONAL";
        public final static String ACTION_REFRESH = "com.zode64.trellodoing.intent.action.REFRESH";
        public final static String ACTION_AUTO_UPDATE = "com.zode64.trellodoing.intent.action.AUTO_UPDATE";
        public final static String ACTION_STOP_ALARM = "com.zode64.trellodoing.intent.action.STOP_ALARM";
        public final static String ACTION_KEEP_DOING = "com.zode64.trellodoing.intent.action.KEEP_DOING";
        public final static String ACTION_SET_DEADLINE = "com.zode64.trellodoing.intent.action.SET_DEADLINE";
        public final static String ACTION_NETWORK_CHANGE = "com.zode64.trellodoing.intent.action.NETWORK_CHANGE";
        public final static String ACTION_ADD_PERSONAL_CARD = "com.zode64.trellodoing.intent.action.ADD_PERSONAL_CARD";
        public final static String ACTION_SET_STATUS_DOING = "com.zode64.trellodoing.intent.action.SET_STATUS_DOING";

        public UpdateService() {
            super( "Trello service" );
        }

        @Override
        protected void onHandleIntent( Intent intent ) {
            Log.d( TAG, "onHandleIntent()" );
            Log.d( TAG, "Intent action : " + intent.getAction() );

            DoingPreferences preferences = new DoingPreferences( this );
            WidgetAlarm appWidgetAlarm = new WidgetAlarm( this.getApplicationContext(), preferences );
            if ( ACTION_STOP_ALARM.equals( intent.getAction() ) ) {
                appWidgetAlarm.stopStandardAlarm();
                return;
            }

            if ( ACTION_REFRESH.equals( intent.getAction() ) ) {
                preferences.handleKeepDoingCardComplete();
            }

            // If from a network change or something like that don't reset the deadline
            if ( ACTION_NETWORK_CHANGE.equals( intent.getAction() ) ) {
                if ( preferences.hasKeepDoing() && !preferences.hasDeadline() ) {
                    return;
                }
            }

            RemoteViews views = new RemoteViews( this.getPackageName(), R.layout.widget_doing );
            ComponentName thisWidget = new ComponentName( this, DoingWidget.class );
            AppWidgetManager manager = AppWidgetManager.getInstance( this );
            DoingNotification notifications = new DoingNotification( this );
            notifications.removeAll();

            if ( ACTION_ADD_PERSONAL_CARD.equals( intent.getAction() ) ) {
                setButtonDown( R.id.add_personal_card, views );
                Intent cardAdderIntent = new Intent( this, PersonalTodoAdder.class );
                cardAdderIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( cardAdderIntent );
                return;
            }

            if ( ACTION_KEEP_DOING.equals( intent.getAction() ) ) {
                Calendar alarm = appWidgetAlarm.delayAlarm();
                preferences.handleSetKeepDoingCall( alarm );
                setKeepDoingDown( views );
                Log.d( TAG, "Setting keep doing" );
                manager.updateAppWidget( thisWidget, views );
                return;
            }

            if ( ACTION_SET_DEADLINE.equals( intent.getAction() ) ) {
                String cardId = intent.getStringExtra( "deadlineCardId" );
                if ( cardId != null ) {
                    Calendar alarm = appWidgetAlarm.deadlineAlarm();
                    preferences.handleSetDeadlineCall( cardId, alarm );
                    setSetDeadlineDown( views );
                    Log.d( TAG, "Setting deadline" );
                    manager.updateAppWidget( thisWidget, views );
                }
                return;
            }

            setButtonUp( R.id.keep_doing, views );
            setCardUp( views );
            TrelloManager trelloManager = new TrelloManager( preferences.getSharedPreferences() );
            appWidgetAlarm.setAlarm();

            if ( ACTION_CLOCK_OFF.equals( intent.getAction() ) || preferences.isCommitPending() ) {
                if ( ACTION_CLOCK_OFF.equals( intent.getAction() ) ) {
                    preferences.handleDeadlineCardComplete();
                    preferences.handleKeepDoingCardComplete();
                    String cardId = intent.getStringExtra( "cardId" );
                    String clockedOffListId = intent.getStringExtra( "clockedOffListId" );
                    Log.d( TAG, "cardId : " + cardId );
                    setClockOffDown( views );
                    manager.updateAppWidget( thisWidget, views );
                    preferences.handleClockOffCall( cardId, clockedOffListId );
                }
                // So we don't set the clocked off button down twice
                if ( trelloManager.clockOff() ) {
                    preferences.handleClockOffSuccess();
                } else {
                    manager.updateAppWidget( thisWidget, views );
                    return;
                }
            }

            if ( ACTION_CLOCK_OFF_PERSONAL.equals( intent.getAction() ) ) {
                preferences.handleDeadlineCardComplete();
                preferences.handleKeepDoingCardComplete();
                preferences.handleAddPersonalCardSuccess();
                setClockOffDown( views );
                clearCard( views );
                manager.updateAppWidget( thisWidget, views );
            }

            if( ACTION_SET_STATUS_DOING.equals( intent.getAction() )) {
                preferences.handleSetStatusDoing( intent.getIntExtra( "statusDoing" , 0 ) );
            }

            setSettingsListener( views );
            setRefreshClickListener( views );
            setKeepDoingClickListener( views );
            setAddPersonalCardListener( views );

            Member member = trelloManager.member();
            if ( member != null ) {

                if ( preferences.hasPersonalCardName() ) {
                    Log.i(TAG, "Uploading personal card : " + preferences.getPersonalCardName());
                    if ( trelloManager.newPersonalCard( member.getPersonalTodoList().getId(),
                            member.getPersonalDoingList().getId() ) ) {
                        preferences.handleAddPersonalCardSuccess();
                        setButtonUp( R.id.add_personal_card, views );
                        member = trelloManager.member();
                    }
                }

                Map<Action.Status, List<Action>> actions = member.findDoingActions();
                List<Action> workActions = actions.get( Action.Status.WORK );
                if ( !workActions.isEmpty() ) {
                    views.setTextViewText( R.id.widget_name, getString( R.string.work ) );
                    notifications.removeMultipleDoings();
                    Action action = workActions.get( 0 );
                    preferences.saveBoard( action.getBoardShortUrl() );
                    views.setTextViewText( R.id.card_name, action.getCardName() );

                    // Check if deadline has been set and act accordingly
                    deadlineCheck( action, preferences, notifications, appWidgetAlarm, views );

                    notifications.standard( true, action.getBoardShortUrl() );
                    setCardClickListener( action, views );
                    Log.d( TAG, "cardId : " + action.getCardId() );
                    setClockOffListener( action, member, views );
                    setButtonUp( R.id.clock_out, views );
                    if ( workActions.size() > 1 ) {
                        notifications.multiDoings( workActions.get( 1 ).getBoardShortUrl() );
                    }
                } else {
                    notifications.removeMultipleDoings();
                    notifications.standard( false, preferences.getLastDoingBoard() );
                    List<Action> personalActions = actions.get( Action.Status.PERSONAL );

                    if ( personalActions.isEmpty() ) {
                        preferences.handleDeadlineCardComplete();
                        preferences.handleKeepDoingCardComplete();
                        appWidgetAlarm.stopDeadlineAlarm();
                        Log.i( TAG, "No work cards or personal card" );
                        setButtonDown( R.id.set_deadline, views );
                        setButtonDown( R.id.clock_out, views );
                        clearCard( views );
                    } else {
                        views.setTextViewText( R.id.widget_name, getString( R.string.personal ) );
                        Action todo = personalActions.get( 0 );
                        Log.i( TAG, "No work cards in doing so selecting the personal card : " + todo.getCardName() );

                        preferences.saveBoard( todo.getBoardShortUrl() );
                        setCardClickListener( todo, views );
                        setClockOffListener( todo, member, views );

                        setButtonUp( R.id.clock_out, views );
                        views.setTextViewText( R.id.card_name, todo.getCardName() );

                        // Check if deadline has been set and act accordingly
                        deadlineCheck( todo, preferences, notifications, appWidgetAlarm, views );
                    }
                }
                Log.d( TAG, "Update built" );
                setLastChecked( views );
            } else {
                if ( preferences.hasPersonalCardName() ) {
                    Action todo = new Action();
                    todo.setCardName( preferences.getPersonalCardName() );
                    todo.setCardId( "temp" );
                    Log.i( TAG, "No connections, presenting pending personal card : " + preferences.getPersonalCardName() );
                    views.setTextViewText( R.id.card_name, todo.getCardName() );
                    setOfflinePersonalClockOffListener( views );

                    // Check if deadline has been set and act accordingly
                    deadlineCheck( todo, preferences, notifications, appWidgetAlarm, views );
                }
                else {
                   clearCard( views );
                }
                setLastCheckedOffline( views );
            }
            manager.updateAppWidget( thisWidget, views );
            Log.d( TAG, "Widget updated" );
        }

        private void setSetDeadlineDown( RemoteViews views ) {
            setButtonDown( R.id.set_deadline, views );
            setButtonDown( R.id.keep_doing, views );
        }

        private void setKeepDoingDown( RemoteViews views ) {
            setButtonDown( R.id.keep_doing, views );
        }

        private void setClockOffDown( RemoteViews views ) {
            setButtonDown( R.id.set_deadline, views );
            setButtonDown( R.id.clock_out, views );
        }

        private void setCardDown( RemoteViews views ) {
            views.setTextColor( R.id.card_name, getResources().getColor( R.color.black ) );
            setButtonDown( R.id.card_name, views );
        }

        private void setCardUp( RemoteViews views ) {
            views.setTextColor( R.id.card_name, getResources().getColor( R.color.black ) );
            setButtonUp( R.id.card_name, views );
        }

        private void setCardReachedDeadline( RemoteViews views ) {
            views.setInt( R.id.card_name, "setBackgroundResource", R.drawable.layout_card_red );
            views.setTextColor( R.id.card_name, getResources().getColor( R.color.white ) );
            views.setViewPadding( R.id.card_name, 50, 50, 50, 50 );
            setSetDeadlineDown( views );
        }

        private void setButtonDown( int viewId, RemoteViews views) {
            views.setInt( viewId, "setBackgroundResource", R.drawable.layout_card );
            views.setViewPadding( viewId, 50, 50, 50, 50 );
        }

        private void setButtonUp( int viewId, RemoteViews views) {
            views.setInt( viewId, "setBackgroundResource", R.drawable.layout_card_up );
            views.setViewPadding( viewId, 50, 50, 50, 50 );
        }

        private void setLastChecked( RemoteViews views ) {
            DateFormat df = new SimpleDateFormat( "HH:mm dd/MM" );
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format( today );
            views.setTextViewText( R.id.last_checked, getString( R.string.last_updated ) + " " + reportDate );
        }

        private void setLastCheckedOffline( RemoteViews views ) {
            views.setTextViewText( R.id.last_checked, getString( R.string.last_updated ) + getString( R.string.offline ) );
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

        private void setCardClickListener( Action action, RemoteViews views ) {
            Intent getBoard = new Intent( Intent.ACTION_VIEW, Uri.parse( action.getBoardShortUrl() ) );
            PendingIntent pendingGetBoard = PendingIntent.getActivity( this, 0, getBoard, 0 );
            views.setOnClickPendingIntent( R.id.card_name, pendingGetBoard );
        }

        private void setSettingsListener( RemoteViews views ) {
            Intent settings = new Intent( this, MainActivity.class );
            PendingIntent pendingSettings = PendingIntent.getActivity( this, 0, settings, 0 );
            views.setOnClickPendingIntent( R.id.settings, pendingSettings );
        }

        private void setClockOffListener( Action action, Member member, RemoteViews views ) {
            Intent clockOff = new Intent( ACTION_CLOCK_OFF );
            clockOff.putExtra( "clockedOffListId", member.getClockedOffList( action.getBoardId() ).getId() );
            clockOff.putExtra( "cardId", action.getCardId() );
            clockOff.setDataAndType( Uri.parse( clockOff.toUri( Intent.URI_INTENT_SCHEME ) ), "text/plain" );
            PendingIntent pendingClockOff = PendingIntent.getService( this, 0, clockOff, 0 );
            views.setOnClickPendingIntent( R.id.clock_out, pendingClockOff );
        }

        private void setOfflinePersonalClockOffListener( RemoteViews views ) {
            Intent offlinePersonalClockOff = new Intent( ACTION_CLOCK_OFF_PERSONAL );
            PendingIntent pendingClockOff = PendingIntent.getService( this, 0, offlinePersonalClockOff, 0 );
            views.setOnClickPendingIntent( R.id.clock_out, pendingClockOff );
        }

        private void setSetDeadlineListener( Action action, RemoteViews views ) {
            setButtonUp( R.id.set_deadline, views );
            Intent setDeadline = new Intent( UpdateService.ACTION_SET_DEADLINE );
            setDeadline.putExtra( "deadlineCardId", action.getCardId() );
            setDeadline.setDataAndType( Uri.parse( setDeadline.toUri( Intent.URI_INTENT_SCHEME ) ), "text/plain" );
            PendingIntent pendingSetDeadline = PendingIntent.getService( this, 0, setDeadline, 0 );
            views.setOnClickPendingIntent( R.id.set_deadline, pendingSetDeadline );
        }

        private void setAddPersonalCardListener( RemoteViews views ) {
            setButtonUp( R.id.add_personal_card, views );
            Intent addPersonalCard = new Intent( UpdateService.ACTION_ADD_PERSONAL_CARD );
            PendingIntent pendingAddPersonalCard = PendingIntent.getService( this, 0, addPersonalCard, 0 );
            views.setOnClickPendingIntent( R.id.add_personal_card, pendingAddPersonalCard );
        }

        private void clearCard( RemoteViews views ) {
            setCardDown( views );
            views.setTextViewText( R.id.card_name, getString( R.string.no_card ) );
        }

        private void deadlineCheck( Action action, DoingPreferences preferences, DoingNotification notifications,
                                    WidgetAlarm appWidgetAlarm, RemoteViews views ) {
            String deadlineCardId = preferences.getDeadlineCardId();
            if ( deadlineCardId != null ) {
                if ( action.getCardId().equals( deadlineCardId ) ) {
                    if ( preferences.pastDeadline() ) {
                        Log.d( TAG, "Card not complete after deadline" );
                        setCardReachedDeadline( views );
                        notifications.deadline( action.getBoardShortUrl() );
                    }
                } else {
                    preferences.handleDeadlineCardComplete();
                    appWidgetAlarm.stopDeadlineAlarm();
                }
            }
            if ( !preferences.hasDeadline() ) {
                setSetDeadlineListener( action, views );
            }
        }

    }
}