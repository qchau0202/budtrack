package vn.edu.tdtu.lhqc.budtrack.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.edu.tdtu.lhqc.budtrack.R;

/**
 * Simple pie chart view that draws wedges based on percentages.
 * No external libraries are used. Data can be updated dynamically via setData(...).
 */
public class PieChartView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    // Keep insertion order for stable segment ordering/legend mapping
    private LinkedHashMap<String, Float> labelToPercent = new LinkedHashMap<>();
    private final List<Integer> segmentColors = new ArrayList<>();

    // Appearance configuration (can be adjusted via setters)
    private float ringThicknessPx = 28f;
    private float segmentGapDegrees = 8f;
    private String centerTitle = null;
    private String centerValue = null;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Provide data and colors. Percentages do not have to sum to 100; they will be normalized.
     * Colors list size should match map size; otherwise segments reuse last color if fewer provided.
     */
    public void setData(LinkedHashMap<String, Float> labelToPercent, List<Integer> colors) {
        if (labelToPercent == null || labelToPercent.isEmpty()) {
            this.labelToPercent.clear();
            segmentColors.clear();
            invalidate();
            return;
        }
        this.labelToPercent = new LinkedHashMap<>(labelToPercent);
        segmentColors.clear();
        if (colors != null) {
            segmentColors.addAll(colors);
        }
        invalidate();
    }

    public void setRingThicknessPx(float thicknessPx) {
        this.ringThicknessPx = Math.max(4f, thicknessPx);
        invalidate();
    }

    public void setSegmentGapDegrees(float gapDegrees) {
        this.segmentGapDegrees = Math.max(0f, gapDegrees);
        invalidate();
    }

    public void setCenterTexts(@Nullable String title, @Nullable String value) {
        this.centerTitle = title;
        this.centerValue = value;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (labelToPercent.isEmpty()) return;

        float total = 0f;
        for (Float v : labelToPercent.values()) {
            if (v != null) total += v;
        }
        if (total <= 0f) return;

        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = getWidth() - getPaddingRight();
        float bottom = getHeight() - getPaddingBottom();

        float size = Math.min(right - left, bottom - top);
        float cx = (left + right) / 2f;
        float cy = (top + bottom) / 2f;
        float half = size / 2f;
        float inset = ringThicknessPx / 2f + 2f;
        arcBounds.set(cx - half + inset, cy - half + inset, cx + half - inset, cy + half - inset);
        paint.setStrokeWidth(ringThicknessPx);

        float startAngle = -90f; // start at top
        int idx = 0;
        @ColorInt int lastColor = 0xFFCCCCCC;
        for (Map.Entry<String, Float> e : labelToPercent.entrySet()) {
            float value = Math.max(0f, e.getValue() == null ? 0f : e.getValue());
            float sweep = (value / total) * 360f;
            if (sweep <= 0f) continue;

            @ColorInt int color;
            if (idx < segmentColors.size()) {
                color = segmentColors.get(idx);
                lastColor = color;
            } else {
                color = lastColor;
            }
            paint.setColor(color);
            float drawSweep = Math.max(0f, sweep - segmentGapDegrees);
            canvas.drawArc(arcBounds, startAngle + (segmentGapDegrees / 2f), drawSweep, false, paint);
            startAngle += sweep;
            idx++;
        }

        // Center text - use theme-aware colors
        if (centerTitle != null || centerValue != null) {
            float centerY = cy;
            // Get theme-aware colors
            int titleColor = ContextCompat.getColor(getContext(), R.color.primary_grey);
            int valueColor = ContextCompat.getColor(getContext(), R.color.primary_black);
            
            if (centerTitle != null && centerValue != null) {
                textPaint.setColor(titleColor);
                textPaint.setTextSize(spToPx(14f));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                canvas.drawText(centerTitle, cx, centerY - dpToPx(8f), textPaint);

                textPaint.setColor(valueColor);
                textPaint.setTextSize(spToPx(20f));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText(centerValue, cx, centerY + dpToPx(12f), textPaint);
            } else if (centerValue != null) {
                textPaint.setColor(valueColor);
                textPaint.setTextSize(spToPx(18f));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText(centerValue, cx, centerY, textPaint);
            } else {
                textPaint.setColor(titleColor);
                textPaint.setTextSize(spToPx(14f));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                canvas.drawText(centerTitle, cx, centerY, textPaint);
            }
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}