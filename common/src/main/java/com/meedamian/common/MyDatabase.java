package com.meedamian.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.MutableDateTime;

public class MyDatabase extends SQLiteAssetHelper {

    private SQLiteDatabase db;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "advices.db";

    public MyDatabase(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        db = getWritableDatabase();
    }

    private Cursor getTodaysCursor() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);

        Days days = Days.daysBetween(epoch, new DateTime());

        return getCursor(String.valueOf((days.getDays()%224) + 1));
    }

    private Cursor getCursor(String adviceId) {
        return db.query(
            "advices",
            null,
            " semiRandId = ? ",
            new String[] { adviceId },
            null,
            null,
            null
        );
    }

    private Advice getNewAdvice(Cursor c) {
        c.moveToFirst();

        Advice a = new Advice(
            c.getInt(c.getColumnIndex(Advice.ID)),
            c.getString(c.getColumnIndex(Advice.BODY)),
            c.getInt(c.getColumnIndex(Advice.SHOWN_COUNT))
        );

        ContentValues updatedCount = new ContentValues();
        updatedCount.put(Advice.SHOWN_COUNT, a.incShownCount());
        db.update("advices",
            updatedCount,
            "id=?",
            new String[] {
                a.getId()
            }
        );
        c.close();

        return a;
    }

    public Advice getNewAdvice() {
        return getNewAdvice(getTodaysCursor());
    }

    public Advice getNewAdvice(@NonNull String adviceId) {
        return getNewAdvice(getCursor(adviceId));
    }
}
