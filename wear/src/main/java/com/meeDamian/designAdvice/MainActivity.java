package com.meeDamian.designAdvice;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import com.meeDamian.common.Advice;
import com.meeDamian.common.MyDatabase;

public class MainActivity extends Activity {

    private MyDatabase db;
    private TextView advice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new MyDatabase(this);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
            advice = (TextView) stub.findViewById(R.id.advice);

            Advice a = db.getNewAdvice();
            advice.setText(a.getBody());
            }
        });
    }
}
