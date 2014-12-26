package com.meeDamian.designAdvice;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.meedamian.common.Advice;
import com.meedamian.common.MyDatabase;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class AdviceActivity extends Activity {

    private SQLiteAssetHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice);
        mDbHelper = new MyDatabase(this);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
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

        Log.d("lalala", "" + a.getId() + ") " + a.getBody());

        ContentValues updatedCount = new ContentValues();
        updatedCount.put(Advice.SHOWN_COUNT, a.getShownCount() + 1);

        db.update("advices",
            updatedCount,
            "id=?",
            new String[] {
                "" + a.getId()
            }
        );
        c.close();
    }

}
