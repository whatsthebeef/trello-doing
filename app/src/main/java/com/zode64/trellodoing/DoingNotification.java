package com.zode64.trellodoing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import java.util.Arrays;
import java.util.Calendar;

import static com.zode64.trellodoing.TimeUtils.between;

/**
 * Created by john on 2/12/15.
 */
public class DoingNotification {

    private static final int CLOCK_ON_ID = 676767;
    private static final int CLOCK_OFF_ID = 767676;

    public static void manage(boolean isDoing, String lastBoard, Context context) {
        Calendar now = Calendar.getInstance();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(between(7, 13, now) && !isDoing) {
            generate("Clock on!", CLOCK_ON_ID, context, lastBoard, notificationManager);
        }
        else if(between(16, 22, now) && isDoing) {
            generate("Clock off!", CLOCK_OFF_ID, context, lastBoard, notificationManager);
        }
        else {
            remove(context, notificationManager);
        }
    }

    private static void generate(String content, int id, Context context, String lastBoard, NotificationManager notificationManager) {

        long[] vibratePattern = {0, 1000, 0, 1000, 0};
        Notification.Builder builder =
                new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setContentTitle("Trello Doing Reminder")
                        .setLights(Color.argb(0, 0, 255, 255), 100, 1000)
                        .setVibrate(vibratePattern)
                        .setContentText(content);

        if(lastBoard != null) {
            Intent getBoard = new Intent(Intent.ACTION_VIEW, Uri.parse(lastBoard));
            PendingIntent pendingGetBoard = PendingIntent.getActivity(context, 0, getBoard, 0);
            builder.setContentIntent(pendingGetBoard);
        }

        notificationManager.notify(id, builder.build());
    }

    private static void remove(Context context, NotificationManager notificationManager) {
        notificationManager.cancel(CLOCK_ON_ID);
        notificationManager.cancel(CLOCK_OFF_ID);
    }
}
