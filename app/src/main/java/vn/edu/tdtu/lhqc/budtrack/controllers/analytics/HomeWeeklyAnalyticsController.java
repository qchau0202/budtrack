package vn.edu.tdtu.lhqc.budtrack.controllers.analytics;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

/**
 * Controller for weekly analytics (bars + total amount) on the Home screen.
 * Extracts data and bar rendering logic from HomeFragment.
 */
public final class HomeWeeklyAnalyticsController {

    private HomeWeeklyAnalyticsController() {
    }

    /**
     * Callback used to show a tooltip when a bar is tapped.
     */
    public interface TooltipCallback {
        void onShowTooltip(View anchor, Calendar dayCalendar, long amount, boolean isIncome);
    }

    /**
     * Bind weekly analytics (total + day bars) into the given root view.
     *
     * @param context        Context for resources and data access
     * @param root           Root view containing the weekly analytics card
     * @param showIncome     true to show income, false to show expenses
     * @param tooltipCallback callback to display tooltip when a bar is tapped
     */
    public static void updateWeeklyAnalytics(Context context,
                                             View root,
                                             boolean showIncome,
                                             TooltipCallback tooltipCallback) {
        if (context == null || root == null) {
            return;
        }

        View analyticsCard = root.findViewById(R.id.card_weekly_expenses);
        if (analyticsCard == null) {
            return;
        }

        TextView tvWeeklyAmount = analyticsCard.findViewById(R.id.tv_weekly_amount);

        // Get current week's transactions (Monday to Sunday)
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Calculate days to subtract to get to Monday
        // Calendar.MONDAY = 2, Calendar.SUNDAY = 1
        int daysFromMonday = (currentDayOfWeek == Calendar.SUNDAY) ? 6 : currentDayOfWeek - Calendar.MONDAY;

        // Go back to Monday of current week
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date weekStart = calendar.getTime();

        // Go to Sunday of current week (6 days after Monday)
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date weekEnd = calendar.getTime();

        java.util.List<Transaction> weekTransactions = TransactionManager.getTransactionsInRange(
                context, weekStart, weekEnd);

        // Filter by type
        long[] dailyAmounts = new long[7]; // Mon-Sun
        long totalAmount = 0;

        for (Transaction transaction : weekTransactions) {
            boolean isIncome = transaction.getType() == TransactionType.INCOME;
            if ((showIncome && isIncome) || (!showIncome && !isIncome)) {
                Date transDate = transaction.getDate();
                if (transDate != null) {
                    Calendar transCal = Calendar.getInstance();
                    transCal.setTime(transDate);
                    int dayOfWeek = transCal.get(Calendar.DAY_OF_WEEK);
                    // Convert to 0-6 (Monday=0, Sunday=6)
                    int index = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
                    if (index >= 0 && index < 7) {
                        dailyAmounts[index] += transaction.getAmount();
                        totalAmount += transaction.getAmount();
                    }
                }
            }
        }

        // Update total amount
        if (tvWeeklyAmount != null) {
            tvWeeklyAmount.setText(CurrencyUtils.formatCurrency(context, totalAmount));
        }

        // Find max amount for scaling
        long maxAmount = 0;
        for (long amount : dailyAmounts) {
            if (amount > maxAmount) {
                maxAmount = amount;
            }
        }

        // Update bars and dates
        int[] barIds = {R.id.bar_mon, R.id.bar_tue, R.id.bar_wed, R.id.bar_thu, R.id.bar_fri, R.id.bar_sat, R.id.bar_sun};
        int[] dateIds = {R.id.tv_date_mon, R.id.tv_date_tue, R.id.tv_date_wed, R.id.tv_date_thu, R.id.tv_date_fri, R.id.tv_date_sat, R.id.tv_date_sun};

        Calendar today = Calendar.getInstance();
        int todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        int todayIndex = (todayDayOfWeek == Calendar.SUNDAY) ? 6 : todayDayOfWeek - Calendar.MONDAY;

        // Get date labels for current week
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(weekStart);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault());

        // Store daily amounts and dates for tooltip
        final long[] finalDailyAmounts = dailyAmounts;
        final Calendar[] dayCalendars = new Calendar[7];
        for (int i = 0; i < 7; i++) {
            dayCalendars[i] = Calendar.getInstance();
            dayCalendars[i].setTime(dateCal.getTime());
            dateCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        float density = context.getResources().getDisplayMetrics().density;
        int maxBarHeight = (int) (80 * density); // 80dp

        for (int i = 0; i < 7; i++) {
            View bar = analyticsCard.findViewById(barIds[i]);
            TextView dateText = analyticsCard.findViewById(dateIds[i]);

            long amount = finalDailyAmounts[i];
            int barHeight = (maxAmount > 0)
                    ? (int) Math.max((amount * maxBarHeight) / (double) maxAmount, 4 * density)
                    : (int) (4 * density);

            if (bar != null) {
                ViewGroup.LayoutParams params = bar.getLayoutParams();
                params.height = barHeight;
                bar.setLayoutParams(params);

                boolean isToday = (i == todayIndex);
                if (isToday) {
                    bar.setBackgroundResource(R.drawable.bg_bar_active);
                    if (bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) bar.getLayoutParams();
                        marginParams.width = (int) (24 * density);
                        bar.setLayoutParams(marginParams);
                    }
                } else {
                    bar.setBackgroundResource(R.drawable.bg_bar_default);
                    if (bar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) bar.getLayoutParams();
                        marginParams.width = (int) (20 * density);
                        bar.setLayoutParams(marginParams);
                    }
                }

                // Attach click handler to show tooltip via callback
                final int index = i;
                bar.setOnClickListener(v -> {
                    if (tooltipCallback != null) {
                        tooltipCallback.onShowTooltip(
                                v,
                                dayCalendars[index],
                                finalDailyAmounts[index],
                                showIncome
                        );
                    }
                });
            }

            if (dateText != null) {
                // Set date number
                dateText.setText(dateFormat.format(dayCalendars[i].getTime()));
                // Update style for today
                if (i == todayIndex) {
                    dateText.setTextColor(ContextCompat.getColor(context, R.color.primary_black));
                    dateText.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    dateText.setTextColor(ContextCompat.getColor(context, R.color.primary_grey));
                    dateText.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
            }
        }
    }
}


