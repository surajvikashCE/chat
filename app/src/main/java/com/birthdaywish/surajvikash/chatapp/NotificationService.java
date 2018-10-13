package com.birthdaywish.surajvikash.chatapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.birthdaywish.surajvikash.chatapp.Activities.EntryPage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by surajvikash on 28/05/18.
 */

public class NotificationService extends FirebaseMessagingService {

    String TAG = "Notification ";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String notificationTitle = "", notificationBody = "";
        String dataTitle = "", dataMessage = "";
        String click_action = "", fromUserId = "", fromUsername = "", type = "";


        Log.e(TAG + "from ", remoteMessage.getFrom());

        if(remoteMessage.getNotification() != null){
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
            click_action = remoteMessage.getNotification().getClickAction();
            fromUserId = remoteMessage.getData().get("from_user_id");
            fromUsername = remoteMessage.getData().get("from_user_name");
            type = remoteMessage.getData().get("notification_type");
            Log.e("From-->", fromUserId);
            sendNotification(notificationTitle, notificationBody, click_action, fromUserId, fromUsername);
        }

        //super.onMessageReceived(remoteMessage);
    }

    private void sendNotification(String notificationTitle, String notificationBody, String click_action, String fromUserId, String fromUsername) {
        Intent intent = new Intent(click_action);
        intent.putExtra("user_id", fromUserId);
        intent.putExtra("name", fromUsername);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setVibrate(new long[]{100, 200 ,250, 150, 50})
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
