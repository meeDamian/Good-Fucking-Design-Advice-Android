package com.meeDamian.designAdvice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.meedamian.common.Advice;
import com.meedamian.common.MyDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AdviceActivity extends Activity implements ImageButton.OnClickListener {

    private static final String CURSE_WORD_RAW      = "fucking";
    private static final String CURSE_WORD_CENSORED = "f******";

    private MyDatabase db;
    private TextView advice;
    private TextView adviceId;

    private Advice a;

    private boolean censored = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            fixRecentsAppearance();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
            .setDefaultFontPath("fonts/Roboto-Bold.ttf")
            .setFontAttrId(R.attr.fontPath)
            .build()
        );

        setContentView(R.layout.activity_advice);

        db = new MyDatabase(this);

        advice = (TextView) findViewById(R.id.advice);
        adviceId = (TextView) findViewById(R.id.adviceId);

//        ImageButton nextAdvice = (ImageButton) findViewById(R.id.nextAdvice);
//        nextAdvice.setOnClickListener(this);

        ImageButton share = (ImageButton) findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareThat();
            }
        });

        Bundle extras = getIntent().getExtras();
        String id = extras != null
            ? extras.getString("id")
            : null;

        //id = "";

        setNewAdvice(id);

        NotificationHelper.la(this);

        AlarmHelper.setAlarm(this, AlarmHelper.ALARM_HOUR);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void fixRecentsAppearance() {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        String name = getString(R.string.app_name);
        setTaskDescription(new ActivityManager.TaskDescription(name, icon, Color.WHITE));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        int visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            visibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(visibility);

        super.onResume();
    }

    private void setNewAdvice(@Nullable String id) {
        a = id != null
            ? db.getNewAdvice(id)
            : db.getNewAdvice();

        adviceId.setText("#" + a.getId());

        SpannableString aoe = new SpannableString(a.getBody());
        int red = Color.rgb(248, 68, 68);

        if (censored) {
            int curseStart = a.getBody().indexOf(CURSE_WORD_RAW);
            String censoredQuote = a.getBody().replace(CURSE_WORD_RAW, CURSE_WORD_CENSORED);
            aoe = new SpannableString(censoredQuote);
            aoe.setSpan(new ForegroundColorSpan(red), curseStart + 1, curseStart + CURSE_WORD_RAW.length(), 0);
        }

        // color dot red (last char)
        aoe.setSpan(new ForegroundColorSpan(red), aoe.length()-1, aoe.length(), 0);

        // NOTE uncomment once line height fix is found
        // increase dot size
        //aoe.setSpan(new RelativeSizeSpan(1.5f), aoe.length()-1, aoe.length(), 0);

        advice.setText(aoe);
    }

    private void shareThat() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
//        share.putExtra(Intent.EXTRA_SUBJECT, a.getBody());
//        share.putExtra(Intent.EXTRA_TITLE, a.getBody());
        share.putExtra(Intent.EXTRA_TEXT, a.getBody() + "\n\n" + a.getUrl());
        startActivity(Intent.createChooser(share, "Share Advice"));
    }

    @Override
    public void onClick(View v) {
        setNewAdvice(null);
    }
}
