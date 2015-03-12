package com.meedamian.common;

public class Advice {
    public static final String ID = "id";
    public static final String BODY = "advice";
    public static final String SHOWN_COUNT = "shown";

    private Integer id;
    private String body;
    private int shownCount;

    public Advice(int id, String body, int count) {
        this.id = id;
        this.body = body;
        this.shownCount = count;
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
        return getUrl(getIntegerId());
    }

    public int incShownCount() {
        shownCount++;
        return getShownCount();
    }
    public int getShownCount() {
        return shownCount;
    }


    public static String getUrl(int id) {
        return "http://goodfuckingdesignadvice.com/advice/" + id + "/";
    }
}
