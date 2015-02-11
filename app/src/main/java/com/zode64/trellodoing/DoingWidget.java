package com.zode64.trellodoing;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

        Intent refresh = new Intent(UpdateService.ACTION_REFRESH);
        PendingIntent pendingRefresh = PendingIntent.getService(context, 0, refresh, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_doing);
        views.setOnClickPendingIntent(R.id.refresh, pendingRefresh);
        ComponentName thisWidget = new ComponentName(context, DoingWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, views);

        context.startService(new Intent(context, UpdateService.class));
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled()");
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends IntentService {

        public final static String ACTION_CLOCK_OFF = "com.zode64.trellodoing.intent.action.CLOCK_OFF";
        public final static String ACTION_REFRESH = "com.zode64.trellodoing.intent.action.REFRESH";

        public UpdateService() {
            super("Trello service");
        }

        @Override
        protected void onHandleIntent(Intent intent) {

            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_doing);

            Log.d(TAG, "onHandleIntent()");
            if(ACTION_CLOCK_OFF.equals(intent.getAction())) {
                views.setInt(R.id.clock_out, "setBackgroundResource", android.R.drawable.editbox_background );
                TrelloManager trelloManager = new TrelloManager(PreferenceManager.getDefaultSharedPreferences(this));
                trelloManager.clockOff(intent.getStringExtra("cardId"), intent.getStringExtra("clockedOffListId"));
                // Push update for this widget to the home screen
                ComponentName thisWidget = new ComponentName(this, DoingWidget.class);
                AppWidgetManager manager = AppWidgetManager.getInstance(this);
                manager.updateAppWidget(thisWidget, views);
                return;
            }

            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);
            Log.d(TAG, "Update built");

            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, DoingWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
            Log.d(TAG, "Widget updated");
        }

        public RemoteViews buildUpdate(Context context) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_doing);
            TrelloManager trelloManager = new TrelloManager(PreferenceManager.getDefaultSharedPreferences(context));

            Member member = trelloManager.member();
            Map<String, Boolean> shifts = new HashMap<String, Boolean>();
            // Cycles from most recent to least recent
            for (Action action : member.getActions()) {
                if (action.isStoppedDoingAction()) {
                    shifts.put(action.getCard().getId(), true);
                } else if (action.isDoingAction()) {
                    if (shifts.containsKey(action.getCard().getId())) {
                        shifts.remove(action.getCard().getId());
                    } else {
                        DateFormat df = new SimpleDateFormat("HH:mm dd/MM");
                        Date today = Calendar.getInstance().getTime();
                        String reportDate = df.format(today);

                        views.setTextViewText(R.id.card_name, action.getCard().getName());
                        views.setTextViewText(R.id.last_checked, context.getString(R.string.last_checked) + " " + reportDate);

                        Intent getBoard = new Intent(Intent.ACTION_VIEW, Uri.parse(action.getBoard().getShortUrl()));
                        PendingIntent pendingGetBoard = PendingIntent.getActivity(context, 0, getBoard, 0);
                        views.setOnClickPendingIntent(R.id.card_name, pendingGetBoard);

                        Intent clockOff = new Intent(ACTION_CLOCK_OFF);
                        clockOff.putExtra("clockedOffListId", member.getClockedOffList(action.getBoard().getId()).getId());
                        clockOff.putExtra("cardId", action.getCard().getId());
                        PendingIntent pendingClockOff = PendingIntent.getService(context, 0, clockOff, 0);
                        views.setOnClickPendingIntent(R.id.clock_out, pendingClockOff);

                        break;
                    }
                }
            }
            return views;
        }
    }
}
