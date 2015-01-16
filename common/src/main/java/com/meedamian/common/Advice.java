package com.meedamian.common;

import android.database.Cursor;
import android.support.annotation.NonNull;

public class Advice {
    public static final String ID = "id";
    public static final String BODY = "advice";
    public static final String SHOWN_COUNT = "shown";

    private Integer id;
    private String body;
    private int shownCount;

    public Advice(@NonNull Cursor c) {
        body = c.getString(c.getColumnIndex(BODY));
        id = c.getInt(c.getColumnIndex(ID));
        shownCount = c.getInt(c.getColumnIndex(SHOWN_COUNT));
    }

    public String getId() {
        return id.toString();
    }
    public Integer getIntegerId() {
        return id;
    }

    public String getBody() {
        return body;
    }
    public String getUrl() {
        return "http://goodfuckingdesignadvice.com/advice/" + getId() + "/";
    }

    public int incShownCount() {
        shownCount++;
        return getShownCount();
    }
    public int getShownCount() {
        return shownCount;
    }
}
