package com.meedamian.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class MyDatabase extends SQLiteAssetHelper {

    private SQLiteDatabase db;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "advices.db";

    public MyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        db = getWritableDatabase();
    }

    private Cursor getCursor() {
        return db.rawQuery("SELECT * " +
                "FROM advices " +
                "WHERE shown=(SELECT MIN(shown) FROM advices) " +
                "ORDER BY RANDOM() " +
                "LIMIT 1",
            null
        );
    }

    private Cursor getCursor(String adviceId) {
        return db.query(
            "advices",
            null,
            "id=?",
            new String[] {
                adviceId
            },
            null,
            null,
            null
        );
    }

    private Advice getNewAdvice(Cursor c) {
        c.moveToFirst();

        Advice a = new Advice(c);

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
        return getNewAdvice(getCursor());
    }

    public Advice getNewAdvice(String adviceId) {
        return getNewAdvice(getCursor(adviceId));
    }
}
