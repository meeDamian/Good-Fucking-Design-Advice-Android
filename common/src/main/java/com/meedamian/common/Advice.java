package com.meedamian.common;

import android.database.Cursor;

public class Advice {
    public static final String ID = "id";
    public static final String BODY = "advice";
    public static final String SHOWN_COUNT = "shown";

    private int id;
    private String body;
    private int shownCount;

    public Advice(Cursor c) {
        body = c.getString(c.getColumnIndex(BODY));
        id = c.getInt(c.getColumnIndex(ID));
        shownCount = c.getInt(c.getColumnIndex(SHOWN_COUNT));
    }

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public int getShownCount() {
        return shownCount;
    }
}
