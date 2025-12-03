package vn.edu.tdtu.lhqc.budtrack.controllers.analytics;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockCategoryData;
import vn.edu.tdtu.lhqc.budtrack.models.Category;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.widgets.PieChartView;

/**
 * Controller responsible for building and rendering the Home pie chart and its category list.
 * Extracted from HomeFragment to keep the fragment lean and focused on UI wiring.
 */
public final class HomePieChartController {

    private HomePieChartController() {
    }

    /**
     * Compute pie chart data from transactions and render both the chart and the
     * category legend below it.
     *
     * @param context Context for data access and resources
     * @param root    Root view containing the pie chart and category container
     */
    public static void updatePieChartAndTabs(Context context, View root) {
        updatePieChartAndTabs(context, root, null);
    }

    /**
     * Compute pie chart data from transactions and render both the chart and the
     * category legend below it, with optional click listener for category tabs.
     *
     * @param context Context for data access and resources
     * @param root    Root view containing the pie chart and category container
     * @param categoryClickListener Optional click listener for category tabs
     */
    public static void updatePieChartAndTabs(Context context, View root, CategoryTabClickListener categoryClickListener) {
        if (context == null || root == null) {
            return;
        }

        // Get all transactions and filter to expenses only
        List<Transaction> allTransactions = TransactionManager.getTransactions(context);
        List<Transaction> expenseTransactions = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            if (transaction != null && transaction.getType() == TransactionType.EXPENSE) {
                expenseTransactions.add(transaction);
            }
        }

        // Aggregate spent amount by category (using name + icon as unique key)
        // Use a composite key: "name|iconResId" to uniquely identify categories
        Map<String, Long> categorySums = new LinkedHashMap<>();
        Map<String, Integer> categoryIcons = new LinkedHashMap<>(); // Store icon for each category key
        long totalSpent = 0;

        for (Transaction transaction : expenseTransactions) {
            long amount = transaction.getAmount();
            totalSpent += amount;

            // Get category from transaction (prefer name+icon, fallback to categoryId for legacy data)
            String categoryKey = null;
            int iconResId = 0;

            if (transaction.getCategoryName() != null && transaction.getCategoryIconResId() != null) {
                // Use user-defined category (name + icon)
                categoryKey = transaction.getCategoryName() + "|" + transaction.getCategoryIconResId();
                iconResId = transaction.getCategoryIconResId();
            } else if (transaction.getCategoryId() != null) {
                // Legacy: try to match categoryId to MockCategoryData (for backward compatibility)
                for (Category category : MockCategoryData.getSampleCategories()) {
                    if (category.getId() == transaction.getCategoryId()) {
                        categoryKey = category.getName() + "|" + category.getIconResId();
                        iconResId = category.getIconResId();
                        break;
                    }
                }
            }

            if (categoryKey == null) {
                // Skip if no category set
                continue;
            }

            Long current = categorySums.get(categoryKey);
            if (current == null) {
                current = 0L;
                categoryIcons.put(categoryKey, iconResId);
            }
            categorySums.put(categoryKey, current + amount);
        }

        // Build category summaries with icon + title
        List<CategorySummary> summaries = new ArrayList<>();

        for (Map.Entry<String, Long> entry : categorySums.entrySet()) {
            String categoryKey = entry.getKey();
            long amount = entry.getValue();

            // Parse category key: "name|iconResId"
            String[] parts = categoryKey.split("\\|");
            if (parts.length == 2) {
                String categoryName = parts[0];
                int categoryIconResId = Integer.parseInt(parts[1]);

                summaries.add(new CategorySummary(
                        categoryName,
                        categoryIconResId,
                        amount
                ));
            }
        }

        // Sort by spent amount descending
        Collections.sort(summaries, new Comparator<CategorySummary>() {
            @Override
            public int compare(CategorySummary o1, CategorySummary o2) {
                return Long.compare(o2.amount, o1.amount);
            }
        });

        // Limit to top 5 categories for clarity
        if (summaries.size() > 5) {
            summaries = new ArrayList<>(summaries.subList(0, 5));
        }

        // Initialize pie chart
        PieChartView pieChart = root.findViewById(R.id.pieChart);
        if (pieChart != null) {
            if (totalSpent > 0 && !summaries.isEmpty()) {
                LinkedHashMap<String, Float> pieData = new LinkedHashMap<>();
                List<Integer> colors = new ArrayList<>();

                // Generate distinct HSV colors using only Android's Color utilities
                java.util.Random random = new java.util.Random();
                float baseHue = random.nextFloat() * 360f;
                float hueStep = summaries.size() > 0 ? 360f / summaries.size() : 360f;

                for (int i = 0; i < summaries.size(); i++) {
                    CategorySummary summary = summaries.get(i);
                    float percentage = (float) ((summary.amount / (double) totalSpent) * 100f);
                    pieData.put(summary.name, percentage);

                    // Generate a pleasant color solely via Android's Color utilities
                    float hue = (baseHue + i * hueStep) % 360f;
                    float saturation = 0.65f;
                    float value = 0.9f;
                    int colorInt = Color.HSVToColor(new float[]{hue, saturation, value});

                    // Store color on summary so the category list can match pie segment colors
                    summary.colorInt = colorInt;

                    colors.add(colorInt);
                }

                pieChart.setData(pieData, colors);
                float density = context.getResources().getDisplayMetrics().density;
                pieChart.setRingThicknessPx(12f * density);
                pieChart.setSegmentGapDegrees(14f);
                pieChart.setCenterTexts(context.getString(R.string.expense),
                        CurrencyUtils.formatCurrency(context, totalSpent));
            } else {
                // No categories or no transactions: clear chart and show simple text
                pieChart.setData(new LinkedHashMap<String, Float>(), new ArrayList<Integer>());
                pieChart.setCenterTexts(null, context.getString(R.string.pie_no_data));
            }
        }

        // Update category list below the pie chart
        if (categoryClickListener != null) {
            updateCategoryTabs(context, root, summaries, totalSpent, categoryClickListener);
        } else {
            updateCategoryTabs(context, root, summaries, totalSpent);
        }
    }

    /**
     * Interface for handling category tab clicks
     */
    public interface CategoryTabClickListener {
        void onCategoryTabClick(String categoryName, int categoryIconResId);
    }

    // Update category tabs with dynamic data (icon + title + amount + percentage)
    private static void updateCategoryTabs(Context context,
                                           View root,
                                           List<CategorySummary> summaries,
                                           long totalSpent) {
        updateCategoryTabs(context, root, summaries, totalSpent, null);
    }

    // Update category tabs with click listener support
    private static void updateCategoryTabs(Context context,
                                           View root,
                                           List<CategorySummary> summaries,
                                           long totalSpent,
                                           CategoryTabClickListener clickListener) {
        LinearLayout container = root.findViewById(R.id.container_category_tabs);
        if (container == null) {
            return;
        }

        container.removeAllViews();

        if (summaries == null || summaries.isEmpty() || totalSpent <= 0) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        float density = context.getResources().getDisplayMetrics().density;

        for (CategorySummary summary : summaries) {
            View itemView = inflater.inflate(R.layout.item_pie_category_tab, container, false);

            ImageView ivIcon = itemView.findViewById(R.id.iv_category_icon);
            TextView tvName = itemView.findViewById(R.id.tv_category_name);
            TextView tvAmount = itemView.findViewById(R.id.tv_category_amount);
            TextView tvPercent = itemView.findViewById(R.id.tv_category_percent);

            if (ivIcon != null) {
                ivIcon.setImageResource(summary.iconResId);
                // Match icon tint to the pie segment color (if available)
                if (summary.colorInt != 0) {
                    ivIcon.setColorFilter(summary.colorInt);
                }
            }
            if (tvName != null) {
                tvName.setText(summary.name);
            }
            if (tvAmount != null) {
                tvAmount.setText(CurrencyUtils.formatCurrency(context, summary.amount));
                // Match amount text color to the pie segment color for visual consistency
                if (summary.colorInt != 0) {
                    tvAmount.setTextColor(summary.colorInt);
                }
            }
            if (tvPercent != null) {
                float percentage = (float) ((summary.amount / (double) totalSpent) * 100f);
                tvPercent.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
                // Optionally tint percentage text as well
                if (summary.colorInt != 0) {
                    tvPercent.setTextColor(summary.colorInt);
                }
            }

            // Add right margin between items
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.rightMargin = (int) (8 * density);
            itemView.setLayoutParams(params);

            // Make tab clickable
            if (clickListener != null) {
                final CategorySummary finalSummary = summary;
                itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onCategoryTabClick(finalSummary.name, finalSummary.iconResId);
                    }
                });
                itemView.setClickable(true);
                itemView.setFocusable(true);
            }

            container.addView(itemView);
        }
    }

    // Simple data holder for category summary in pie chart
    private static class CategorySummary {
        final String name;
        final int iconResId;
        final long amount;
        int colorInt;

        CategorySummary(String name, int iconResId, long amount) {
            this.name = name;
            this.iconResId = iconResId;
            this.amount = amount;
        }
    }
}


