package com.sharan.ballichatdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brst-pc93 on 10/24/16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private static final String TAG = "FCM Service";

    String groupID;
    String groupName;
    String message;
    String senderName;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.e(TAG, "From: " + remoteMessage.getFrom());
       // Log.e(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());


        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Context context = getBaseContext();


        Log.e("Notification", notification.getBody());

        try
        {
            JSONObject object = new JSONObject(notification.getBody());

             groupID = object.getString(MyConstants.GROUP_ID);
             groupName = object.getString(MyConstants.GROUP_NAME);
             message = object.getString(MyConstants.MESSAGE);
             senderName = object.getString(MyConstants.SENDER_NAME);

            Log.e("Values",""+groupID+"  "+groupName+"  "+message+"  "+senderName);


        } catch (JSONException e)
        {
            e.printStackTrace();
        }


        if (!MySharedPreference.getInstance().getActivityState(context) && !groupID.equals(MySharedPreference.getInstance().getCurrentGroupID(context)))
        {

            createNotification(notification);
        }

    }

    private void createNotification(RemoteMessage.Notification notification12)
    {
        Context context = getBaseContext();

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        new Intent(context, com.sharan.ballichatdemo.MainActivity.class),
                        PendingIntent.FLAG_CANCEL_CURRENT
                );


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Notification notification = mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                                            .setTicker(notification12.getTitle())
                                            .setWhen(0)
                                            .setAutoCancel(true)
                                            .setContentTitle(notification12.getTitle())
                                            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                            .setContentIntent(resultPendingIntent)
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                                            .setContentText(message).build();


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

    }


}
