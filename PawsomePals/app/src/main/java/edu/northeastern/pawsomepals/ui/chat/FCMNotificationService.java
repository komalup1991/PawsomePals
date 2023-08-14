package edu.northeastern.pawsomepals.ui.chat;

import com.google.firebase.messaging.FirebaseMessagingService;

public class FCMNotificationService extends FirebaseMessagingService {
//    private static final String TAG = "FCMNotificationService";
//    private static final String CHANNEL_ID = "default_channel";
//
//
//    @Override
//    public void handleIntent(Intent intent) {
//        super.handleIntent(intent);
//        Log.d("FCM", "Message received");
//    }
//
//    @Override
//    public void onMessageReceived(RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
//        Log.e("tag","received");
//        if (remoteMessage.getNotification() != null) {
//            handleNotificationMessage(remoteMessage);
//        }
//    }
//
//    @Override
//    public void onNewToken(String token) {
//        super.onNewToken(token);
//        Log.d(TAG, "Refreshed token: " + token);
//    }
//
//
//    private void handleNotificationMessage(RemoteMessage remoteMessage) {
//
//        String title = remoteMessage.getNotification().getTitle();
//        String body = remoteMessage.getNotification().getBody();
//        String summaryText = "";
//        if (remoteMessage.getData().size() > 0) {
//            summaryText = remoteMessage.getData().get("summaryText");
//        }
//        showNotificationWithTextOnly(title,body);
//    }
//
//    private void showNotificationWithTextOnly(String title, String body) {
//        Intent intent = new Intent(this, PawsomePalsApplication.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, PawsomePalsApplication.MESSAGE_CHANNEL)
//                .setSmallIcon(R.drawable.dogpawheart)
//                .setContentTitle(title)
//                .setContentText(body)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        if (notificationManager != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                // Create notification channel for Android Oreo and above
//                NotificationChannel channel = new NotificationChannel(PawsomePalsApplication.MESSAGE_CHANNEL, "incoming_message", NotificationManager.IMPORTANCE_HIGH);
//                notificationManager.createNotificationChannel(channel);
//            }
//            notificationManager.notify(0, notificationBuilder.build());
//        }
//    }
}
