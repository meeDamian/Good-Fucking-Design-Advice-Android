package com.meeDamian.designAdvice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.meeDamian.common.Advice;
import com.meeDamian.common.MyDatabase;
import com.meeDamian.designAdvice.util.SystemUiHider;
import com.meeDamian.lib.AutoResizeTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AdviceActivity extends Activity {

    private static final String CURSE_WORD_RAW      = "fucking";
    private static final String CURSE_WORD_CENSORED = "f******";

    private static final boolean DEFAULT_CENSORSHIP = false;

    private Tracker t;

    private MyDatabase db;
    private AutoResizeTextView advice;
    private ImageButton share;
//    private TextView adviceId;

    private Advice a;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        t = analytics.newTracker(R.xml.global_tracker);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
            .setDefaultFontPath("fonts/Roboto-Bold.ttf")
            .setFontAttrId(R.attr.fontPath)
            .build()
        );

        setContentView(R.layout.activity_advice);

        db = new MyDatabase(this);

        advice = (AutoResizeTextView) findViewById(R.id.advice);
        advice.setMovementMethod(LinkMovementMethod.getInstance());
        advice.setHighlightColor(Color.TRANSPARENT);
//        adviceId = (TextView) findViewById(R.id.adviceId);

        share = (ImageButton) findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            String fileName = "GFDA_" + a.getId() + ".png";

            savePic(takeScreenShot(), fileName);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://com.meeDamian.designAdvice.fileprovider/screen/" + fileName));
            share.setType("image/png");
            share.putExtra(Intent.EXTRA_TEXT, a.getBody() + "\n\n" + a.getUrl());
            startActivity(Intent.createChooser(share, "Share Advice #" + a.getId()));
            }
        });

        // After each start make sure alarm is set
        AlarmHelper.setAlarm(this);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT && hasNavBar())
            setupLegacyUiHider();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void fixRecentsAppearance(String id) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        String name = getString(R.string.app_name);
        setTaskDescription(new ActivityManager.TaskDescription(name + " #" + id, icon, Color.WHITE));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        if (!getCensorship()) {
            if (isAFibonacciNumber(getNumberOfLaunches()))
                Toast.makeText(this, R.string.censorship_notice,Toast.LENGTH_LONG).show();

            incNumberOfLaunches();
        }

        Bundle extras = getIntent().getExtras();
        String id = extras != null
            ? extras.getString("id")
            : null;

        setNewAdvice(id);

        t.setScreenName("Advice #" + a.getId());
        t.send(new HitBuilders.ScreenViewBuilder().build());

        int visibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            visibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fixRecentsAppearance(a.getId());
        }

        SpannableString spannedString = new SpannableString(a.getBody());
        int red = Color.rgb(248, 68, 68);

        int curseStart = a.getBody().indexOf(CURSE_WORD_RAW);

        if (getCensorship()) {
            String censoredQuote = a.getBody().replace(CURSE_WORD_RAW, CURSE_WORD_CENSORED);
            spannedString = new SpannableString(censoredQuote);
            spannedString.setSpan(new ForegroundColorSpan(red), curseStart + 1, curseStart + CURSE_WORD_RAW.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // color dot red (last char)
        spannedString.setSpan(new ForegroundColorSpan(red), spannedString.length() - 1, spannedString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // NOTE uncomment once line-height fix is found
        // increase dot size
        //spannedString.setSpan(new RelativeSizeSpan(1.5f), spannedString.length()-1, spannedString.length(), 0);

        spannedString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                toggleCensorship();
                advice.setAutoResize(false);
                setNewAdvice(null);
                advice.setAutoResize(true);
            }

            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        }, curseStart, curseStart + CURSE_WORD_RAW.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        advice.setText(spannedString);
    }

    private Bitmap takeScreenShot() {
        View view = getWindow().getDecorView();

        share.setVisibility(View.GONE);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

        int statusBarHeight = frame.top;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        share.setVisibility(View.VISIBLE);
        return b;
    }
    private void savePic(Bitmap b, String fileName) {
        FileOutputStream fos;
        try {
            File f = new File(getCacheDir(), fileName);
            fos = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean getCensorship() {
        return sp.getBoolean("censorship", DEFAULT_CENSORSHIP);
    }
    private void setCensorship(boolean censor) {
        sp.edit()
            .putBoolean("censorship", censor)
            .apply();
    }
    private void toggleCensorship() {
        setCensorship(!getCensorship());
    }

    private int getNumberOfLaunches() {
        return sp.getInt("launchNo", 1);
    }
    private void incNumberOfLaunches() {
        sp.edit()
            .putInt("launchNo", getNumberOfLaunches() + 1)
            .apply();
    }

    private boolean isAFibonacciNumber(int n) {
        return n == 1 || n == 2 || n == 3 ||
            n == 5 || n == 8 || n == 13 ||
            n == 21 || n == 34;
    }

    // old AND softkey phones UI hider
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final boolean TOGGLE_ON_CLICK = true;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    private SystemUiHider mSystemUiHider;
    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT && hasNavBar())
            delayedHide(100);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void setupLegacyUiHider() {
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, advice, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
            .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {

                int mShortAnimTime;

                @Override
                public void onVisibilityChange(boolean visible) {
                    if (mShortAnimTime == 0)
                        mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                    // Schedule a hide().
                    if (visible) delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
            });

        advice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (TOGGLE_ON_CLICK) {
                mSystemUiHider.toggle();
            } else {
                mSystemUiHider.show();
            }
            }
        });
    }

    private boolean hasNavBar() {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        return !(hasBackKey && hasHomeKey);
    }
}
