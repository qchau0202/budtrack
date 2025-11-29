package vn.edu.tdtu.lhqc.budtrack.utils;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import vn.edu.tdtu.lhqc.budtrack.R;

/**
 * Utility class for customizing ProgressBar appearance.
 * Provides methods to set custom colors and styles for progress bars.
 */
public final class ProgressBarUtils {

    private ProgressBarUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Sets a custom progress bar color with rounded corners.
     * Creates a custom drawable with the specified progress color and default background.
     * 
     * @param context The context to get resources
     * @param progressBar The ProgressBar to customize
     * @param progressColorResId The color resource ID for the progress bar
     */
    public static void setProgressBarColor(Context context, ProgressBar progressBar, int progressColorResId) {
        setProgressBarColor(context, progressBar, progressColorResId, R.color.secondary_grey);
    }

    /**
     * Sets a custom progress bar color with rounded corners.
     * Creates a custom drawable with the specified progress color and background color.
     * 
     * @param context The context to get resources
     * @param progressBar The ProgressBar to customize
     * @param progressColorResId The color resource ID for the progress bar
     * @param backgroundColorResId The color resource ID for the background
     */
    public static void setProgressBarColor(Context context, ProgressBar progressBar, 
                                          int progressColorResId, int backgroundColorResId) {
        setProgressBarColor(context, progressBar, progressColorResId, backgroundColorResId, 4.0f);
    }

    /**
     * Sets a custom progress bar color with rounded corners.
     * Creates a custom drawable with the specified progress color, background color, and corner radius.
     * 
     * @param context The context to get resources
     * @param progressBar The ProgressBar to customize
     * @param progressColorResId The color resource ID for the progress bar
     * @param backgroundColorResId The color resource ID for the background
     * @param cornerRadiusDp The corner radius in dp
     */
    public static void setProgressBarColor(Context context, ProgressBar progressBar, 
                                          int progressColorResId, int backgroundColorResId, 
                                          float cornerRadiusDp) {
        if (context == null || progressBar == null) {
            return;
        }

        // Convert dp to pixels for corner radius
        float density = context.getResources().getDisplayMetrics().density;
        float cornerRadius = cornerRadiusDp * density;

        // Create background shape
        ShapeDrawable backgroundShape = new ShapeDrawable();
        backgroundShape.setShape(new RoundRectShape(
            new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                       cornerRadius, cornerRadius, cornerRadius, cornerRadius}, null, null));
        backgroundShape.getPaint().setColor(ContextCompat.getColor(context, backgroundColorResId));

        // Create progress shape with the specified color
        ShapeDrawable progressShape = new ShapeDrawable();
        progressShape.setShape(new RoundRectShape(
            new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                       cornerRadius, cornerRadius, cornerRadius, cornerRadius}, null, null));
        progressShape.getPaint().setColor(ContextCompat.getColor(context, progressColorResId));

        // Create clip drawable for progress
        ClipDrawable clipDrawable = new ClipDrawable(progressShape,
            android.view.Gravity.START, ClipDrawable.HORIZONTAL);

        // Create layer drawable
        LayerDrawable layerDrawable = new LayerDrawable(
            new android.graphics.drawable.Drawable[]{backgroundShape, clipDrawable});
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);

        progressBar.setProgressDrawable(layerDrawable);
    }
}

