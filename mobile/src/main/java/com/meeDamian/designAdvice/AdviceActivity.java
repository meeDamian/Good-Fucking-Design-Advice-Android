package com.meeDamian.designAdvice;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.meedamian.common.Advice;
import com.meedamian.common.MyDatabase;

public class AdviceActivity extends Activity implements ImageButton.OnClickListener {

    private MyDatabase db;
    private TextView advice;
    private TextView adviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice);

        db = new MyDatabase(this);

        advice = (TextView) findViewById(R.id.advice);
        adviceId = (TextView) findViewById(R.id.adviceId);

        ImageButton nextAdvice = (ImageButton) findViewById(R.id.nextAdvice);
        nextAdvice.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        String id = extras != null
            ? extras.getString("id")
            : null;

        setNewAdvice(id);

        NotificationHelper.la(this);

        AlarmHelper.setAlarm(this, AlarmHelper.ALARM_HOUR);
    }

    private void setNewAdvice(String id) {
        Advice a = id != null
            ? db.getNewAdvice(id)
            : db.getNewAdvice();

        adviceId.setText(a.getId() + ".");
        advice.setText(a.getBody());
    }

    @Override
    public void onClick(View v) {
        setNewAdvice(null);
    }
}
