package com.example.myspecialstalker;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsManager;

public class OutgoingCallsBroadcastReceiver extends BroadcastReceiver {

    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";
    private static final String NOTIFICATION_TITLE = "mySpecialStalker";
    private static final String NOTIFICATION_CHANNEL_NAME = "Notification Channel";
    private static final String SENDING_MESSAGE = "sending message...";
    private static final String MESSAGE_SENT = "message sent successfully!";
    private static final String MESSAGE_DELIVERED = "message received successfully!";
    private static final String CHANNEL_ID = "1";
    NotificationCompat.Builder builder;

    private void handleNotifications(String title, Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            Notification notification =
                    new Notification.Builder(context.getApplicationContext())
                            .setSmallIcon(R.drawable.icons8bellfilled50)
                            .setContentTitle(NOTIFICATION_TITLE)
                            .setContentText(title)
                            .setChannelId(CHANNEL_ID).build();
            NotificationManager notificationManager = context.getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(1, notification);
        }
        else
        {
            builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.icons8bellfilled50)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setContentText(title)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
            notificationManager.notify(1, builder.build());
        }
    }

    private void sendSMS(String phoneNumber, String message, final Context context) {

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(
                SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                new Intent(DELIVERED), 0);
        BroadcastReceiver sendSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        handleNotifications(MESSAGE_SENT, context);
                        break;
                }
            }
        };
        BroadcastReceiver deliverSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        handleNotifications(MESSAGE_DELIVERED, context);
                        break;
                }
            }
        };
        context.getApplicationContext().registerReceiver(sendSMS, new IntentFilter(SENT));
        context.getApplicationContext().registerReceiver(deliverSMS, new IntentFilter(DELIVERED));
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction()))
        {
            String calledNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (MainActivity.isReadyToSend())
            {
                String messageToSend = MainActivity.getCurrentTextMessage() + calledNumber;
                handleNotifications(SENDING_MESSAGE, context);
                sendSMS(MainActivity.getCurrentPhoneNumber(), messageToSend, context);
            }
        }
    }
}
