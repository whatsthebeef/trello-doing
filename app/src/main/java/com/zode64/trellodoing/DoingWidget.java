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
        context.startService(new Intent(context, UpdateService.class));
    }

    @Override
    public void onEnabled(Context context) {
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends IntentService {

        public final static String ACTION_CLOCK_OFF = "com.zode64.trellodoing.intent.action.CLOCK_OFF";

        public UpdateService() {
            super("Trello service");
        }

        @Override
        protected void onHandleIntent(Intent intent) {

            if(ACTION_CLOCK_OFF.equals(intent.getAction())) {
                TrelloManager trelloManager = new TrelloManager(PreferenceManager.getDefaultSharedPreferences(this));
                trelloManager.clockOff(intent.getStringExtra("cardId"), intent.getStringExtra("clockedOffListId"));
            }

            Log.d(TAG, "onHandleIntent()");

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

                        views.setTextViewText(R.id.card_name, action.getCard().getName()
                                    + "\n\n" + context.getString(R.string.last_checked) + " " + reportDate);

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(action.getBoard().getShortUrl()));
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                        views.setOnClickPendingIntent(R.id.card_name, pendingIntent);

                        intent = new Intent(ACTION_CLOCK_OFF);
                        intent.putExtra("clockedOffListId", member.getClockedOffList(action.getBoard().getId()).getId());
                        intent.putExtra("cardId", action.getCard().getId());
                        pendingIntent = PendingIntent.getService(context, 0, intent, 0);
                        views.setOnClickPendingIntent(R.id.clock_out, pendingIntent);

                        break;
                    }
                }
            }
            return views;
        }
    }
}
