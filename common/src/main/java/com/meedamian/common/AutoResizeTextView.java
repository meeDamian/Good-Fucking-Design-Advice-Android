package com.meedamian.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

/**
 * a textView that is able to self-adjust its font size depending on the min and max size of the font, and its own size.<br/>
 * code is heavily based on this StackOverflow thread:
 * http://stackoverflow.com/questions/16017165/auto-fit-textview-for-android/21851239#21851239 <br/>
 * It should work fine with most Android versions, but might have some issues on Android 3.1 - 4.04, as setTextSize will only work for the first time. <br/>
 * More info here: https://code.google.com/p/android/issues/detail?id=22493 and here in case you wish to fix it: http://stackoverflow.com/a/21851239/878126
 */
public class AutoResizeTextView extends TextView {
    private static final int NO_LINE_LIMIT = -1;


    private final RectF availableSpaceRect = new RectF();
    private final SizeTester sizeTester;


    private float minTextSize;
    private float maxTextSize;

    private float spacingMultiplier = 1.0f;
    private float spacingAdd = 0.0f;


    private int maxWidth;
    private int maxLines;


    private Cache cache = null;

    private boolean initiated = false;
    private TextPaint paint;


    public AutoResizeTextView(final Context context) {
        this(context, null, 0);
    }
    public AutoResizeTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public AutoResizeTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        // using the minimal recommended font size
        minTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        maxTextSize = getTextSize();

        // no value was assigned during construction
        if (maxLines == 0)
            maxLines = NO_LINE_LIMIT;

        // prepare size tester:
        sizeTester = new SizeTester();
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
        if (!initiated)
            return;

        final int startSize = (int) minTextSize;
        final int heightLimit = getMeasuredHeight() - getCompoundPaddingBottom() - getCompoundPaddingTop();
        maxWidth = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
        if (maxWidth <= 0)
            return;

        availableSpaceRect.right = maxWidth;
        availableSpaceRect.bottom = heightLimit;
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, efficientTextSizeSearch(startSize, (int) maxTextSize, availableSpaceRect));
    }

    private int efficientTextSizeSearch(final int start, final int end, final RectF availableSpace) {
        return binarySearch(start, end, availableSpace);
    }

    private int binarySearch(final int start, final int end, final RectF availableSpace) {
        int lastBest = start;
        int lo = start;
        int hi = end - 1;
        int mid;
        while (lo <= hi) {
            mid = lo + hi >>> 1;
            final int midValCmp = sizeTester.testSize(mid, availableSpace);
            if (midValCmp < 0) {
                lastBest = lo;
                lo = mid + 1;

            } else if (midValCmp > 0) {
                hi = mid - 1;
                lastBest = hi;

            } else return mid;
        }
        // make sure to return last best
        // this is what should always be returned
        return lastBest;
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

    private class Cache {
        String prevText = null;
        int minTextSize;
        int maxTextSize;
    }

    private class SizeTester {

        private final RectF textRect = new RectF();

        public int testSize(int suggestedSize, RectF availableSpace) {
            paint.setTextSize(suggestedSize);

            String originalText = getText().toString();
            String[] words = getWordsArray(originalText);

            final String text = TextUtils.join(" ", words);

            final boolean singleLine = getMaxLines() == 1;

            if (singleLine) {
                textRect.bottom = paint.getFontSpacing();
                textRect.right = paint.measureText(text);

            } else {
                final StaticLayout layout = new StaticLayout(text, paint, maxWidth, Layout.Alignment.ALIGN_NORMAL, spacingMultiplier, spacingAdd, true);

                // return early if we have more lines
                if (getMaxLines() != NO_LINE_LIMIT && layout.getLineCount() > getMaxLines())
                    return 1;

                textRect.bottom = layout.getHeight();
                int maxWidth = -1;
                for (int i = 0; i < layout.getLineCount(); i++) {
                    if (maxWidth < layout.getLineWidth(i))
                        maxWidth = (int) layout.getLineWidth(i);
                }

                textRect.right = maxWidth;
            }

            textRect.offsetTo(0, 0);

            // may be too small, don't worry we will find the best match
            if (availableSpace.contains(textRect))
                return -1;

            // else, too big
            return 1;
        }

        private String[] getWordsArray(String text) {
            String[] words = text.split("\\s+");
            Arrays.sort(words, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                return rhs.length() - lhs.length();
                }
            });

            return words;
        }

        private String getLongestWord(String text) {
            return getWordsArray(text)[0];
        }

    }
}
