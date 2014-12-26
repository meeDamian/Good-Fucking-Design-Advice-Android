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

    public Advice getNewAdvice() {
        Cursor c = db.query("advices",
            new String[] {
                "id",
                "advice",
                "shown"
            },
            null,
            null,
            null,
            null,
            "shown ASC",
            "1"
        );
        c.moveToFirst();

        Advice a = new Advice(c);

        ContentValues updatedCount = new ContentValues();
        updatedCount.put(Advice.SHOWN_COUNT, a.incShownCount());
        db.update("advices",
            updatedCount,
            "id=?",
            new String[] {
                "" + a.getId()
            }
        );
        c.close();

        return a;
    }
}
