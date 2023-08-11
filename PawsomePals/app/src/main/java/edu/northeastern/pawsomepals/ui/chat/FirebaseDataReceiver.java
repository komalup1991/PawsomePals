package edu.northeastern.pawsomepals.ui.chat;


import static android.app.Notification.EXTRA_NOTIFICATION_ID;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.application.PawsomePalsApplication;

public class FirebaseDataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BroadcastReceiver::", "intent.getExtras().getString()");

//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PawsomePalsApplication.MESSAGE_CHANNEL)
//                .setSmallIcon(R.drawable.dogpawheart)
//                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .setContentTitle("Chat")
//                .setContentText("You receive a new message.")
//                .setContentIntent(pendingIntent)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setAutoCancel(true);
//        Intent actionIntent = new Intent(context, PawsomePalsApplication.class);
//        actionIntent.putExtra("fragment", "ChatFragment");
//
//        actionIntent.setAction("OPEN_CHAT_FRAGMENT");
//
//        PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
//        builder.addAction(R.drawable.chat_image_icon, "View in Chat", actionPendingIntent);
//        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
//
//// notificationId is a unique int for each notification that you must define
//
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        manager.notify(0, builder.build());
    }
}