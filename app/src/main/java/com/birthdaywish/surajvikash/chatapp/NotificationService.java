package com.birthdaywish.surajvikash.chatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.birthdaywish.surajvikash.chatapp.Activities.EntryPage;
import com.birthdaywish.surajvikash.chatapp.Activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by surajvikash on 28/05/18.
 */

public class NotificationService extends FirebaseMessagingService {

    String TAG = "NotificationUtils ";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String notificationTitle = "", notificationBody = "";
        String dataTitle = "", dataMessage = "";
        String click_action = "", fromUserId = "", fromUsername = "", type = "";

//        NotificationUtils utils  = new NotificationUtils(this);
//        utils.showNotificationMessage("hello", "hi", "dh", new Intent());

        Log.e(TAG + "from ", remoteMessage.getFrom());

        if(remoteMessage.getData() != null){
//            Log.e(TAG, "Message NotificationUtils Body: " + remoteMessage.getNotification().getBody());
            /*notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
            click_action = remoteMessage.getNotification().getClickAction();
            fromUserId = remoteMessage.getData().get("from_user_id");
            fromUsername = remoteMessage.getData().get("from_user_name");
            type = remoteMessage.getData().get("notification_type");*/
            //Log.e("From-->", fromUserId);
            sendNotification("chat", remoteMessage.getData().get("message"), click_action, fromUserId, fromUsername);
            NotificationUtils utils1  = new NotificationUtils();
            utils1.showNotificationMessage(dataTitle, notificationBody, "dh", new Intent());
        }

        //super.onMessageReceived(remoteMessage);
    }

    private void sendNotification(String notificationTitle, String notificationBody, String click_action, String fromUserId, String fromUsername) {
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        resultIntent.putExtra("message", "hello");
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setContentTitle(notificationTitle)
//                .setContentText(notificationBody)
//                .setVibrate(new long[]{100, 200 ,250, 150, 50})
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0, notificationBuilder.build());


        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext(), "notify_001");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("hello");
        bigText.setBigContentTitle("Today's Bible Verse");
        bigText.setSummaryText("Text in detail");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Your HanTitle");
        mBuilder.setContentText("Your text");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
       // mBuilder.setStyle(bigText);

        mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify_001",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }

        if (mNotificationManager != null) {
            mNotificationManager.notify(0, mBuilder.build());
        }


//        final int icon = R.mipmap.ic_launcher;
//        Notification notification;
//        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                this);
//        notification = mBuilder.setSmallIcon(icon).setTicker(notificationTitle).setWhen(0)
//                //.setAutoCancel(true)
//                .setContentTitle(notificationTitle)
//                //.setContentIntent(pendingIntent)
//                .setSound(defaultSoundUri)
//                //.setStyle(inboxStyle)
//                //.setWhen(NotificationUtils.getTimeMilliSec("3776312834"))
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), icon))
//                .setContentText(notificationBody)
//                .build();
//
//        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(100, notification);
    }
}
