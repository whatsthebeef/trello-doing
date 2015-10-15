package com.zode64.trellodoing.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import com.zode64.trellodoing.R;

public class DoingNotification {

    private static final int CLOCK_ON_ID = 676767;
    private static final int CLOCK_OFF_ID = 767676;
    private static final int DEADLINE_ID = 666666;
    private static final int MULTIPLE_DOINGS_ID = 777777;

    private NotificationManager mNotificationManager;

    private Context mContext;

    public DoingNotification( Context context ) {
        mNotificationManager = ( NotificationManager ) context.getSystemService( Context.NOTIFICATION_SERVICE );
        mContext = context;
    }

    public void clockOn( String lastBoard ) {
        generate( mContext.getString( R.string.clock_on ), CLOCK_ON_ID, lastBoard, false );
    }

    public void clockOff( String lastBoard ) {
        generate( mContext.getString( R.string.clock_out ), CLOCK_OFF_ID, lastBoard, false );
    }

    public void deadline( String lastBoard ) {
        generate( mContext.getString( R.string.change_what_you_are_doing ), DEADLINE_ID, lastBoard, true );
    }

    public void removeAll() {
        mNotificationManager.cancel( DEADLINE_ID );
        mNotificationManager.cancel( CLOCK_ON_ID );
        mNotificationManager.cancel( CLOCK_OFF_ID );
        mNotificationManager.cancel( MULTIPLE_DOINGS_ID );
    }

    public void multiDoings( String lastBoard ) {
        generate( mContext.getString( R.string.two_things_at_once ), MULTIPLE_DOINGS_ID, lastBoard, true );
    }

    private void generate( String content, int id, String lastBoard, boolean warning ) {

        long[] vibratePattern = { 0, 500, 0, 1000, 0, 500 };
        Notification.Builder builder =
                new Notification.Builder( mContext )
                        .setSmallIcon( android.R.drawable.stat_notify_error )
                        .setContentTitle( mContext.getString( R.string.doing_reminder ) )
                        .setLights( Color.argb( 0, 0, 255, 255 ), 100, 500 )
                        .setDefaults( Notification.DEFAULT_SOUND )
                                // .setSound( Uri.parse( "android.resource://" + mContext.getPackageName() + "/" + R.raw.sotp ) )
                        .setVibrate( vibratePattern )
                        .setContentText( content );

        if ( warning ) {
            builder.setLights( Color.argb( 0, 255, 255, 0 ), 100, 500 )
                    .setSound( Uri.parse( "android.resource://" + mContext.getPackageName() + "/" + R.raw.sotp ) );
        }

        if ( lastBoard != null ) {
            Intent getBoard = new Intent( Intent.ACTION_VIEW, Uri.parse( lastBoard ) );
            PendingIntent pendingGetBoard = PendingIntent.getActivity( mContext, 0, getBoard, 0 );
            builder.setContentIntent( pendingGetBoard );
        }

        mNotificationManager.notify( id, builder.build() );
    }

}