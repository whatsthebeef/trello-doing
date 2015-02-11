package com.zode64.trellodoing;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.zode64.trellodoing.models.Action;
import com.zode64.trellodoing.models.Member;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DoingWidget extends AppWidgetProvider {

    private final static String TAG = DoingWidget.class.getName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate()");
        context.startService(new Intent(context, UpdateService.class));
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled()");
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class NetworkChangeReceiver extends BroadcastReceiver {

        public NetworkChangeReceiver(){
            super();
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.d(TAG, "onReceive()");
            final ConnectivityManager connMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            //should check null because in air plan mode it will be null
            if(netInfo != null && netInfo.isConnectedOrConnecting()) {
                Log.d(TAG, "Connected to network");
                context.startService(new Intent(context, UpdateService.class));
            }
        }
    }

    public static class UpdateService extends IntentService {

        public final static String ACTION_CLOCK_OFF = "com.zode64.trellodoing.intent.action.CLOCK_OFF";
        public final static String ACTION_REFRESH = "com.zode64.trellodoing.intent.action.REFRESH";

        public UpdateService() {
            super("Trello service");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            Log.d(TAG, "onHandleIntent()");

            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_doing);
            ComponentName thisWidget = new ComponentName(this, DoingWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);

            // Set refresh button
            Intent refresh = new Intent(UpdateService.ACTION_REFRESH);
            PendingIntent pendingRefresh = PendingIntent.getService(this, 0, refresh, 0);
            views.setOnClickPendingIntent(R.id.refresh, pendingRefresh);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if(ACTION_CLOCK_OFF.equals(intent.getAction()) || preferences.getBoolean("commitPending", false)) {
                if(ACTION_CLOCK_OFF.equals(intent.getAction())) {
                    String cardId = intent.getStringExtra("cardId");
                    String clockedOffListId = intent.getStringExtra("clockedOffListId");
                    setButtonDown(R.id.clock_out, views);
                    handleClockOffCall(preferences, cardId, clockedOffListId);
                }
                TrelloManager trelloManager = new TrelloManager(preferences);
                if(trelloManager.clockOff()) {
                    handleClockOffSuccess(preferences);
                    setButtonUp(R.id.clock_out, views);
                }
                else {
                    manager.updateAppWidget(thisWidget, views);
                    return;
                }
            }

            // Build the widget update for today
            buildUpdate(this, views);
            Log.d(TAG, "Update built");

            // Push update for this widget to the home screen
            manager.updateAppWidget(thisWidget, views);
            Log.d(TAG, "Widget updated");
        }


        public RemoteViews buildUpdate(Context context, RemoteViews views) {

            TrelloManager trelloManager = new TrelloManager(PreferenceManager.getDefaultSharedPreferences(context));

            Member member = trelloManager.member();
            if(member == null) {
                return views;
            }

            Map<String, Boolean> shifts = new HashMap<String, Boolean>();
            // Cycles from most recent to least recent
            boolean isDoing = false;
            for (Action action : member.getActions()) {
                if (action.isStoppedDoingAction()) {
                    shifts.put(action.getCard().getId(), true);
                } else if (action.isDoingAction()) {
                    if (shifts.containsKey(action.getCard().getId())) {
                        shifts.remove(action.getCard().getId());
                    } else {
                        isDoing = true;
                        views.setTextViewText(R.id.card_name, action.getCard().getName());

                        Intent getBoard = new Intent(Intent.ACTION_VIEW, Uri.parse(action.getBoard().getShortUrl()));
                        PendingIntent pendingGetBoard = PendingIntent.getActivity(context, 0, getBoard, 0);
                        views.setOnClickPendingIntent(R.id.card_name, pendingGetBoard);

                        Intent clockOff = new Intent(ACTION_CLOCK_OFF);
                        clockOff.putExtra("clockedOffListId", member.getClockedOffList(action.getBoard().getId()).getId());
                        clockOff.putExtra("cardId", action.getCard().getId());
                        PendingIntent pendingClockOff = PendingIntent.getService(context, 0, clockOff, 0);
                        views.setOnClickPendingIntent(R.id.clock_out, pendingClockOff);

                        setButtonUp(R.id.card_name, views);
                        setButtonUp(R.id.clock_out, views);

                        break;
                    }
                }
            }

            if(!isDoing) {
                setButtonDown(R.id.card_name, views);
                setButtonDown(R.id.clock_out, views);
                views.setTextViewText(R.id.card_name, getString(R.string.no_card));
            }

            DateFormat df = new SimpleDateFormat("HH:mm dd/MM");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            views.setTextViewText(R.id.last_checked, context.getString(R.string.last_checked) + " " + reportDate);

            return views;
        }

        private void handleClockOffCall(SharedPreferences preferences, String cardId, String clockedOffListId) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("commitPending", true);
            editor.putString("cardId", cardId);
            editor.putString("clockedOffListId", clockedOffListId);
            editor.commit();
        }

        private void handleClockOffSuccess(SharedPreferences preferences) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("commitPending", false);
            editor.remove("cardId");
            editor.remove("clockedOffListId");
            editor.commit();
        }

        private void setButtonDown(int viewId, RemoteViews views) {
            views.setInt(viewId, "setBackgroundResource", R.drawable.layout_card);
            views.setViewPadding(viewId, 50, 50, 50, 50);
        }

        private void setButtonUp(int viewId, RemoteViews views) {
            views.setInt(viewId, "setBackgroundResource", R.drawable.layout_card_up);
            views.setViewPadding(viewId, 50, 50, 50, 50);
        }
    }
}
