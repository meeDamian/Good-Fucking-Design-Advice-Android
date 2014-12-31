package com.meeDamian.designAdvice;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.meedamian.common.Advice;
import com.meedamian.common.MyDatabase;

public class NotificationHelper extends BroadcastReceiver {

    public static void la(Context context) {
        Advice a = new MyDatabase(context).getNewAdvice();

        String title = "Good Fucking Design Advice";

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_refresh_grey600_48dp)
                .setContentText("#" + a.getId())
                .setContentTitle(a.getBody())
                .setSubText(title)
                .setNumber(a.getIntegerId())
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigTextStyle()
                    //.setBigContentTitle(a.getBody())
                    .bigText(a.getBody()))
                .setAutoCancel(true);

        Intent actionIntent = new Intent(context, NotificationHelper.class);
        PendingIntent next = PendingIntent.getBroadcast(
            context,
            0,
            actionIntent,
            0
        );
        mBuilder.addAction(R.drawable.ic_refresh_grey600_36dp, "another", next);


        Intent clickIntent = new Intent(context, AdviceActivity.class);
        clickIntent.putExtra("id", a.getId());

        PendingIntent pi = PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);

        NotificationManagerCompat
            .from(context)
            .notify(0, mBuilder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        la(context);
    }
}
