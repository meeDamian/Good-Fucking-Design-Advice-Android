package com.meeDamian.designAdvice;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationHelper extends BroadcastReceiver {

    public static void showNotification(@NonNull Context context) {
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setContentText(context.getString(R.string.app_name))
                .setContentTitle(context.getString(R.string.notification_action))
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setShowWhen(false)
                .setAutoCancel(true);

        Intent clickIntent = new Intent(context, AdviceActivity.class);

        PendingIntent pi = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

        NotificationManagerCompat
            .from(context)
            .notify(0, mBuilder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        showNotification(context);
    }
}
