package com.meeDamian.designAdvice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmHelper extends BroadcastReceiver {

    public static final Integer ALARM_HOUR = 4;

    @Override
    public void onReceive(Context context, Intent intent) {

        switch(intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                setAlarm(context, ALARM_HOUR);
                break;
        }
    }

    public static void setAlarm(Context context, int hour) {
        Intent intent = new Intent(context, NotificationHelper.class);

        // TODO: make sure already-running-alarm-detection works
        boolean alarmUp = (
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) != null
        );

        if(!alarmUp) {
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);

            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            alarmMgr.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            );
        }
    }
}
