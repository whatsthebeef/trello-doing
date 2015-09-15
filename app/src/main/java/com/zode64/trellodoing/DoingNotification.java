package com.zode64.trellodoing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;

import com.zode64.trellodoing.models.Action;

import java.util.Calendar;
import java.util.List;

import static com.zode64.trellodoing.TimeUtils.between;

/**
 * Created by john on 2/12/15.
 */
public class DoingNotification {

    private static final int CLOCK_ON_ID = 676767;
    private static final int CLOCK_OFF_ID = 767676;
    private static final int DEADLINE_ID = 666666;
    private static final int MULTIPLE_DOINGS_ID = 777777;

    private static final int CLOCK_ON_START_HOUR = 7;
    private static final int CLOCK_ON_END_HOUR = 18;
    private static final int CLOCK_OFF_START_HOUR = 18;
    private static final int CLOCK_OFF_END_HOUR = 22;

    private NotificationManager mNotificationManager;

    private Context mContext;

    public DoingNotification(Context context) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mContext = context;
    }

    public void standard(boolean isDoing, String lastBoard) {
        Calendar now = Calendar.getInstance();
        removeStandard();
        if(between(CLOCK_ON_START_HOUR, CLOCK_ON_END_HOUR, now) && !isDoing) {
            generate("Clock on!", CLOCK_ON_ID, lastBoard);
        }
        else if(between(CLOCK_OFF_START_HOUR, CLOCK_OFF_END_HOUR, now) && isDoing) {
            generate("Clock off!", CLOCK_OFF_ID, lastBoard);
        }
    }

    public void deadline(String lastBoard) {
        generate("Change what you are doing!", DEADLINE_ID, lastBoard, true);
    }

    public void removeStandard() {
        mNotificationManager.cancel(CLOCK_ON_ID);
        mNotificationManager.cancel(CLOCK_OFF_ID);
    }

    public void removeDeadline() {
        mNotificationManager.cancel(DEADLINE_ID);
    }

    public void removeAll() {
        removeDeadline();
        removeStandard();
    }

    public void multiDoings(String lastBoard) {
        generate("You are doing two things at once!", MULTIPLE_DOINGS_ID, lastBoard, true);
    }

    public void removeMultipleDoings() {
        mNotificationManager.cancel(MULTIPLE_DOINGS_ID);
    }

    private void generate(String content, int id, String lastBoard) {
        generate(content, id, lastBoard, false);
    }

    private void generate(String content, int id, String lastBoard, boolean warning) {

        long[] vibratePattern = {0, 500, 0, 1000, 0};
        Notification.Builder builder =
                new Notification.Builder(mContext)
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setContentTitle("Trello Doing Reminder")
                        .setLights(Color.argb(0, 0, 255, 255), 100, 500)
                        .setVibrate(vibratePattern)
                        .setContentText(content);

        if(warning) {
            builder.setLights(Color.argb(0, 255, 255, 0), 100, 500)
                    .setDefaults(Notification.DEFAULT_SOUND);
        }

        if(lastBoard != null) {
            Intent getBoard = new Intent(Intent.ACTION_VIEW, Uri.parse(lastBoard));
            PendingIntent pendingGetBoard = PendingIntent.getActivity(mContext, 0, getBoard, 0);
            builder.setContentIntent(pendingGetBoard);
        }

        mNotificationManager.notify(id, builder.build());
    }

}
