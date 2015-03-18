package com.meedamian.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

public class AutoResizeTextView extends TextView {

    private static final int NO_LINE_LIMIT = -1;
    private static final int MIN_FONT_SIZE = 12;


    private final RectF availableSpaceRect = new RectF();
    private final SizeTester sizeTester = new SizeTester();


    private float minTextSize;
    private float maxTextSize;

    private float spacingMultiplier = 1.0f;
    private float spacingAdd = 0.0f;


    private int maxWidth;
    private int maxLines;

    private boolean initiated = false;
    private TextPaint paint;

    private boolean autoResizeEnabled = true;


    public AutoResizeTextView(final Context context) {
        this(context, null, 0);
    }
    public AutoResizeTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public AutoResizeTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        // using the minimal recommended font size
        minTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, MIN_FONT_SIZE, getResources().getDisplayMetrics());
        maxTextSize = getTextSize();

        // no value was assigned during construction
        if (maxLines == 0)
            maxLines = NO_LINE_LIMIT;

        // prepare size tester:
        initiated = true;
    }


    /**
    * LINES STUFF
    **/
    @Override
    public int getMaxLines() {
        return maxLines;
    }
    @Override
    public void setMaxLines(final int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;

        adjustTextSize();
    }

    @Override
    public void setSingleLine() {
        super.setSingleLine();
        maxLines = 1;

        adjustTextSize();
    }
    @Override
    public void setSingleLine(final boolean singleLine) {
        super.setSingleLine(singleLine);
        maxLines = singleLine ? 1 : -1;

        adjustTextSize();
    }
    @Override
    public void setLines(final int lines) {
        super.setLines(lines);
        maxLines = lines;

        adjustTextSize();
    }

    @Override
    public void setLineSpacing(final float add, final float mult) {
        super.setLineSpacing(add, mult);
        spacingMultiplier = mult;
        spacingAdd = add;
    }


    /**
    * TEXT STUFF
    **/
    @Override
    public void setTextSize(final float size) {
        maxTextSize = size;

        adjustTextSize();
    }

    @Override
    public void setTextSize(final int unit, final float size) {
        final Context c = getContext();
        Resources r = c==null ? Resources.getSystem() : c.getResources();

        setMinTextSize(TypedValue.applyDimension(unit, size, r.getDisplayMetrics()));
    }

    public void setMinTextSize(final float minTextSize) {
        this.minTextSize = minTextSize;

        adjustTextSize();
    }

    @Override
    public void setTypeface(final Typeface tf) {
        if (paint == null)
            paint = new TextPaint(getPaint());

        paint.setTypeface(tf);
        super.setTypeface(tf);
    }

    private void adjustTextSize() {
        if (!initiated || !autoResizeEnabled)
            return;

        final int startSize = (int) minTextSize;
        final int heightLimit = getMeasuredHeight() - getCompoundPaddingBottom() - getCompoundPaddingTop();
        maxWidth = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
        if (maxWidth <= 0) {
            return;
        }

        availableSpaceRect.right = maxWidth;
        availableSpaceRect.bottom = heightLimit;

        // new approach
        String text = getText().toString();
        int b = betterSearch(startSize, (int) maxTextSize, getTheLongestWord(text), true);
        int c = betterSearch(startSize, b, text);

        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, c);
    }

    private int betterSearch(int smallestSize, int biggestSize, String text, boolean forceOneLine) {
        int lastBest = smallestSize + (biggestSize + smallestSize) / 2;

        while (true) {
            if (lastBest == smallestSize || lastBest == biggestSize)
                return lastBest;

            if (Math.abs(biggestSize - smallestSize) <= 3)
                return lastBest;

            boolean tooSmall = sizeTester.testSize(text, lastBest, forceOneLine);

            if (tooSmall) {
                smallestSize = lastBest;
                int oneThirdOfDiff = (biggestSize - lastBest) / 3;
                lastBest += oneThirdOfDiff;

            } else {
                biggestSize = lastBest;
                int oneThirdOfDiff = (lastBest - smallestSize) / 3;
                lastBest -= oneThirdOfDiff;

            }
        }
    }
    private int betterSearch(int smallestSize, int biggestSize, String text) {
        return betterSearch(smallestSize, biggestSize, text, false);
    }


    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        super.onTextChanged(text, start, before, after);
        adjustTextSize();
    }

    @Override
    protected void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width != oldWidth || height != oldHeight)
            adjustTextSize();
    }
    private String getTheLongestWord(String text) {
        String[] words = text.split("((?<=-)|\\s+)");
        Arrays.sort(words, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
            return rhs.length() - lhs.length();
            }
        });

        return words[0];
    }

    public void setAutoResize(boolean enable) {
        this.autoResizeEnabled = enable;
    }

    private class SizeTester {
        private final RectF textRect = new RectF();

        public boolean testSize(String text, int size, boolean forceOneLine) {
            paint.setTextSize(size);

            if (getMaxLines() == 1 || forceOneLine) {
                textRect.bottom = paint.getFontSpacing();
                textRect.right = paint.measureText(text);

            } else {
                StaticLayout sl = new StaticLayout(text, paint, maxWidth, Layout.Alignment.ALIGN_NORMAL, spacingMultiplier, spacingAdd, true);

                if (maxLines != NO_LINE_LIMIT && sl.getLineCount() > getMaxLines())
                    return false;

                textRect.bottom = sl.getHeight();

                int maxWidth = -1;
                for (int i = 0; i < sl.getLineCount(); i++) {
                    if (maxWidth < sl.getLineWidth(i))
                        maxWidth = (int) sl.getLineWidth(i);
                }
                textRect.right = maxWidth;
            }

            textRect.offsetTo(0, 0);

            return availableSpaceRect.contains(textRect);
        }
    }
}
