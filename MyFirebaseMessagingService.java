package com.membership.firebase;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.membership.Activity.HomeActivity;
import com.membership.DataStorage;
import com.membership.R;

import java.util.Objects;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d("mytag", "data" + remoteMessage.getData());

        if (Boolean.parseBoolean(String.valueOf(DataStorage.read("isLogin", DataStorage.BOOLEAN)))) {

            if (remoteMessage.getData() != null && remoteMessage.getData().size() > 0) {

                if (remoteMessage.getData().get("notiType") != null) {

                    if (Objects.equals(remoteMessage.getData().get("notiType"), "2")) {
                        String Currenttoid = HomeActivity.getUser();
                        Log.d("mytag","Currentid "+Currenttoid +" from_userid "+remoteMessage.getData().get("from_user_id"));
                        if(Currenttoid.equals(remoteMessage.getData().get("from_user_id"))) {
                            HomeActivity.UpdateChat(remoteMessage.getData().get("body"),
                                    Integer.valueOf(remoteMessage.getData().get("from_user_id")),
                                    Integer.valueOf(remoteMessage.getData().get("text_status")),
                                    remoteMessage.getData().get("chat_date"),
                                    remoteMessage.getData().get("chat_time"),
                                    remoteMessage.getData().get("single_images"),
                                    remoteMessage.getData().get("thumb"),
                                    remoteMessage.getData().get("video")
                            );
                        }

                        HomeActivity.BadgeUpdate(remoteMessage.getData().get("from_user_id"),
                                remoteMessage.getData().get("badge"),
                                remoteMessage.getData().get("name"),
                                remoteMessage.getData().get("text"),
                                remoteMessage.getData().get("chat_time"),
                                remoteMessage.getData().get("text_status"));

                        showNotification(remoteMessage.getData().get("body"), remoteMessage.getData().get("name"));

                    } else {
                        showNotification(remoteMessage.getData().get("body"), "Membership");
                    }

                } else {
                    Log.d("mytag", "data" + remoteMessage.getData());
                    showNotification(remoteMessage.getData().get("body"), "Membership");
                }
            }
        } else {
            showNotification(remoteMessage.getData().get("body"), "Membership");
        }
    }

    @SuppressLint("WrongConstant")
    private void showNotification(String message, String name) {
        NotificationCompat.Builder mBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "membership";
            CharSequence c_name = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
//            mChannel.setShowBadge(false);
            mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setChannelId(CHANNEL_ID);
//            mBuilder.setNumber(0);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        } else {
            mBuilder = new NotificationCompat.Builder(this);
        }

        Intent resultIntent = new Intent(getApplicationContext(), HomeActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.setFlags(Notification.FLAG_AUTO_CANCEL);
        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, 0);
        mBuilder.setSmallIcon(R.mipmap.app_icon);
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mBuilder.setSmallIcon(R.mipmap.app_icon);
//            mBuilder.setColor(getResources().getColor(R.color.colorAccent));
//        } else {
//            mBuilder.setSmallIcon(R.mipmap.app_icon);
//        }
        mBuilder.setContentTitle(name);
        mBuilder.setContentText(message);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon);
        mBuilder.setLargeIcon(icon);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mBuilder.setCategory(Notification.CATEGORY_MESSAGE);

        }
        mBuilder.setContentIntent(intent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }


}