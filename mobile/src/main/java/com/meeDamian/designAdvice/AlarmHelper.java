package com.meeDamian.designAdvice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmHelper extends BroadcastReceiver {

    public static final Integer ALARM_DAYS = 1;
    public static final Integer ALARM_HOUR = 4;

    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                setAlarm(context);
                break;
        }
    }

    private static void setAlarm(Context context, int hour) {
        Intent intent = new Intent(context, NotificationHelper.class);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DATE, ALARM_DAYS);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmMgr.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            alarmIntent
        );
    }

    public static void setAlarm(Context context) {
        setAlarm(context, ALARM_HOUR);
    }
}
