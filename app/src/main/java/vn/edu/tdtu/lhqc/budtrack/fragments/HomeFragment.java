package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.BalanceController;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockCategoryData;
import vn.edu.tdtu.lhqc.budtrack.models.Category;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.TabStyleUtils;
import vn.edu.tdtu.lhqc.budtrack.widgets.PieChartView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    
    // Track current analytics tab selection
    private boolean isIncomeSelectedForAnalytics = false;

    // Flag to track if we need to refresh when fragment becomes visible
    private boolean needsRefresh = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        
        // Listen for transaction creation to refresh data immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED,
            this,
            (requestKey, result) -> {
                if (TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED.equals(requestKey)) {
                    // Immediately refresh data - data is already committed synchronously
                    // Use post to ensure we're on the UI thread and view is ready
                    if (getView() != null) {
                        getView().post(() -> refreshDataInternal());
                    } else {
                        needsRefresh = true;
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        GeneralHeaderController.setup(root, this);

        // Setup balance view
        setupBalanceView(root);
        
        // Initialize pie chart
        setupPieChart(root);

        // View all categories button
        View btnViewAllCategories = root.findViewById(R.id.btn_view_all_categories);
        if (btnViewAllCategories != null) {
            btnViewAllCategories.setOnClickListener(v -> openCategoryManagement());
        }

        // Setup analytics tabs inside included view
        setupAnalyticsTabs(root);
        
        // Setup analytics bars
        setupAnalyticsBars(root);
        
        return root;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Always refresh when fragment becomes visible
        // This ensures data is up-to-date, especially if we missed an update while fragment was hidden
        refreshData();
        needsRefresh = false; // Clear the flag after refresh
    }
    
    /**
     * Public method to refresh data. Can be called from outside the fragment.
     * This ensures immediate UI updates when transactions are created.
     */
    public void refreshData() {
        if (getView() != null && isAdded() && !isDetached()) {
            setupBalanceView(getView());
            setupPieChart(getView());
            setupAnalyticsBars(getView(), isIncomeSelectedForAnalytics);
        } else {
            // If view is not available, mark for refresh when view becomes available
            needsRefresh = true;
        }
    }
    
    private void refreshDataInternal() {
        refreshData();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listener
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED);
        // Dismiss tooltip if showing
        if (tooltipPopup != null && tooltipPopup.isShowing()) {
            tooltipPopup.dismiss();
            tooltipPopup = null;
        }
    }
    
    private void openCategoryManagement() {
        // Open CategoryFragment on top of current tab
        CategoryFragment fragment = new CategoryFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, "CATEGORY_FRAGMENT")
                .addToBackStack(null)
                .commit();
    }
    
    private void setupBalanceView(View root) {
        TextView tvBalance = root.findViewById(R.id.tv_total_balance_amount);
        ImageButton btnVisibility = root.findViewById(R.id.btn_visibility);
        MaterialButton btnViewWallet = root.findViewById(R.id.btn_view_wallet);

        if (tvBalance != null && btnVisibility != null) {
            // Calculate total balance from actual wallet data
            long totalBalance = BalanceController.calculateTotalBalance(requireContext());
            final String originalBalanceText = CurrencyUtils.formatCurrency(totalBalance);
            
            boolean hidden = BalanceController.isHidden(requireContext());
            tvBalance.setText(BalanceController.formatDisplay(originalBalanceText, hidden));
            btnVisibility.setImageResource(hidden ? R.drawable.ic_visibility_off_24dp : R.drawable.ic_visibility_24dp);

            btnVisibility.setOnClickListener(v -> {
                boolean nowHidden = BalanceController.toggleHidden(requireContext());
                tvBalance.setText(BalanceController.formatDisplay(originalBalanceText, nowHidden));
                btnVisibility.setImageResource(nowHidden ? R.drawable.ic_visibility_off_24dp : R.drawable.ic_visibility_24dp);
            });
        }

        // Setup view wallet button click listener
        if (btnViewWallet != null) {
            btnViewWallet.setOnClickListener(v -> showWalletFragment());
        }
    }
    
    private void setupPieChart(View root) {
        // Get all transactions and filter to expenses only
        List<Transaction> allTransactions = TransactionManager.getTransactions(requireContext());
        List<Transaction> expenseTransactions = new ArrayList<>();
        for (Transaction transaction : allTransactions) {
            if (transaction != null && transaction.getType() == TransactionType.EXPENSE) {
                expenseTransactions.add(transaction);
            }
        }

        // Aggregate spent amount by category
        Map<Long, Long> categorySums = new LinkedHashMap<>();
        long totalSpent = 0;
        
        for (Transaction transaction : expenseTransactions) {
            long amount = transaction.getAmount();
            totalSpent += amount;

            // All expense transactions are expected to have a category
            Long categoryId = transaction.getCategoryId();
            if (categoryId == null) {
                // Skip if no category set (should not happen, but avoid crashing)
                continue;
            }

            Long current = categorySums.get(categoryId);
            if (current == null) current = 0L;
            categorySums.put(categoryId, current + amount);
        }

        // Build category summaries with icon + title
        List<CategorySummary> summaries = new ArrayList<>();
        List<Category> allCategories = MockCategoryData.getSampleCategories();

        for (Map.Entry<Long, Long> entry : categorySums.entrySet()) {
            Long categoryId = entry.getKey();
            long amount = entry.getValue();

            Category matched = null;
            for (Category category : allCategories) {
                if (category.getId() == categoryId) {
                    matched = category;
                    break;
                }
            }

            if (matched != null) {
                summaries.add(new CategorySummary(
                        matched.getName(),
                        matched.getIconResId(),
                        amount,
                        matched.getColor()
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

                    colors.add(colorInt);
                }

                pieChart.setData(pieData, colors);
            float density = getResources().getDisplayMetrics().density;
            pieChart.setRingThicknessPx(12f * density);
            pieChart.setSegmentGapDegrees(14f);
            pieChart.setCenterTexts(getString(R.string.expense), CurrencyUtils.formatCurrency(totalSpent));
            } else {
                // No categories or no transactions: clear chart and show simple text
                pieChart.setData(new LinkedHashMap<String, Float>(), new ArrayList<Integer>());
                pieChart.setCenterTexts(null, getString(R.string.pie_no_data));
            }
        }

        // Update category list below the pie chart
        updateCategoryTabs(root, summaries, totalSpent);
    }

    private void setupAnalyticsTabs(View root) {
        View analyticsCard = root.findViewById(R.id.card_weekly_expenses);
        if (analyticsCard == null) {
            return;
        }

        MaterialButton tabIncome = analyticsCard.findViewById(R.id.tab_income);
        MaterialButton tabExpenses = analyticsCard.findViewById(R.id.tab_expenses);
        TextView title = analyticsCard.findViewById(R.id.tv_title);

        if (tabIncome == null || tabExpenses == null) {
            return;
        }

        tabIncome.setOnClickListener(v -> selectAnalyticsTab(true, tabIncome, tabExpenses, title));
        tabExpenses.setOnClickListener(v -> selectAnalyticsTab(false, tabIncome, tabExpenses, title));
        selectAnalyticsTab(false, tabIncome, tabExpenses, title);
    }

    private void selectAnalyticsTab(boolean incomeSelected,
                                    MaterialButton tabIncome,
                                    MaterialButton tabExpenses,
                                    TextView title) {
        isIncomeSelectedForAnalytics = incomeSelected; // Store selection
        applyAnalyticsTabStyle(tabIncome, incomeSelected);
        applyAnalyticsTabStyle(tabExpenses, !incomeSelected);
        if (title != null) {
            title.setText(incomeSelected ? R.string.total_income : R.string.total_expenses);
        }
        // Update bars when tab changes
        if (getView() != null) {
            setupAnalyticsBars(getView(), incomeSelected);
        }
    }
    
    private void setupAnalyticsBars(View root) {
        setupAnalyticsBars(root, false); // Default to expenses
    }
    
    private void setupAnalyticsBars(View root, boolean showIncome) {
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
        
        List<Transaction> weekTransactions = TransactionManager.getTransactionsInRange(
            requireContext(), weekStart, weekEnd);
        
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
            tvWeeklyAmount.setText(CurrencyUtils.formatCurrency(totalAmount));
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
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        
        Calendar today = Calendar.getInstance();
        int todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        int todayIndex = (todayDayOfWeek == Calendar.SUNDAY) ? 6 : todayDayOfWeek - Calendar.MONDAY;
        
        // Get date labels for current week
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(weekStart);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        
        // Store daily amounts and dates for tooltip
        final long[] finalDailyAmounts = dailyAmounts;
        final Calendar[] dayCalendars = new Calendar[7];
        for (int i = 0; i < 7; i++) {
            dayCalendars[i] = (Calendar) dateCal.clone();
            dateCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        for (int i = 0; i < 7; i++) {
            View bar = analyticsCard.findViewById(barIds[i]);
            TextView dateText = analyticsCard.findViewById(dateIds[i]);
            
            if (bar != null) {
                // Set up tap listener for tooltip instead of long-press
                final int dayIndex = i;
                final Calendar dayCalendar = dayCalendars[i];
                bar.setOnClickListener(v -> showBarTooltip(v, dayCalendar, finalDailyAmounts[dayIndex], showIncome));
                // Calculate bar height (max 140dp, min 20dp)
                int maxHeightDp = 140;
                int minHeightDp = 20;
                float density = getResources().getDisplayMetrics().density;
                int maxHeightPx = (int) (maxHeightDp * density);
                int minHeightPx = (int) (minHeightDp * density);
                
                int barHeight;
                if (maxAmount > 0) {
                    float ratio = (float) dailyAmounts[i] / maxAmount;
                    barHeight = (int) (minHeightPx + (maxHeightPx - minHeightPx) * ratio);
                } else {
                    barHeight = minHeightPx;
                }
                
                // Update bar height
                ViewGroup.LayoutParams params = bar.getLayoutParams();
                params.height = barHeight;
                bar.setLayoutParams(params);
                
                // Update bar style (active for today)
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
            }
            
            if (dateText != null) {
                // Set date number
                dateText.setText(dateFormat.format(dayCalendars[i].getTime()));
                // Update style for today
                if (i == todayIndex) {
                    dateText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
                    dateText.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    dateText.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
                    dateText.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
            }
        }
    }

    private void applyAnalyticsTabStyle(MaterialButton button, boolean selected) {
        TabStyleUtils.applyStyle(button.getContext(), button, selected);
    }
    
    private PopupWindow tooltipPopup = null;
    
    private void showBarTooltip(View anchor, Calendar dayCalendar, long amount, boolean isIncome) {
        // Dismiss existing tooltip if any
        if (tooltipPopup != null && tooltipPopup.isShowing()) {
            tooltipPopup.dismiss();
        }
        
        // Inflate tooltip layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View tooltipView = inflater.inflate(R.layout.tooltip_analytics_bar, null);
        
        TextView tvDate = tooltipView.findViewById(R.id.tv_tooltip_date);
        TextView tvAmount = tooltipView.findViewById(R.id.tv_tooltip_amount);
        
        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        if (tvDate != null) {
            tvDate.setText(dateFormat.format(dayCalendar.getTime()));
        }
        
        // Format amount
        if (tvAmount != null) {
            String amountText = CurrencyUtils.formatCurrency(amount);
            if (amount > 0) {
                amountText = (isIncome ? "+" : "-") + amountText;
            }
            tvAmount.setText(amountText);
            // Set color based on type
            int colorRes = isIncome ? R.color.secondary_green : R.color.primary_red;
            tvAmount.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        }
        
        // Create popup window
        tooltipPopup = new PopupWindow(
            tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        );
        
        // Set background
        tooltipPopup.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_card));
        tooltipPopup.setElevation(8f);
        tooltipPopup.setOutsideTouchable(true);
        
        // Calculate position (above the bar, centered)
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int x = location[0] + (anchor.getWidth() / 2);
        int y = location[1] - anchor.getHeight();
        
        // Measure tooltip to center it properly
        tooltipView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        x -= tooltipView.getMeasuredWidth() / 2;
        y -= tooltipView.getMeasuredHeight() + (int) (8 * getResources().getDisplayMetrics().density);
        
        // Show tooltip
        tooltipPopup.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
        
        // Auto-dismiss after 2 seconds
        anchor.postDelayed(() -> {
            if (tooltipPopup != null && tooltipPopup.isShowing()) {
                tooltipPopup.dismiss();
            }
        }, 2000);
    }
    
    // Update category tabs with dynamic data (icon + title + amount + percentage)
    private void updateCategoryTabs(View root, List<CategorySummary> summaries, long totalSpent) {
        LinearLayout container = root.findViewById(R.id.container_category_tabs);
        if (container == null) {
            return;
        }

        container.removeAllViews();

        if (summaries == null || summaries.isEmpty() || totalSpent <= 0) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        float density = getResources().getDisplayMetrics().density;

        for (CategorySummary summary : summaries) {
            View itemView = inflater.inflate(R.layout.item_pie_category_tab, container, false);

            ImageView ivIcon = itemView.findViewById(R.id.iv_category_icon);
            TextView tvName = itemView.findViewById(R.id.tv_category_name);
            TextView tvAmount = itemView.findViewById(R.id.tv_category_amount);
            TextView tvPercent = itemView.findViewById(R.id.tv_category_percent);

            if (ivIcon != null) {
                ivIcon.setImageResource(summary.iconResId);
            }
            if (tvName != null) {
                tvName.setText(summary.name);
            }
            if (tvAmount != null) {
                tvAmount.setText(CurrencyUtils.formatCurrency(summary.amount));
            }
            if (tvPercent != null) {
                float percentage = (float) ((summary.amount / (double) totalSpent) * 100f);
                tvPercent.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
        }

            // Add right margin between items
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.rightMargin = (int) (8 * density);
            itemView.setLayoutParams(params);

            container.addView(itemView);
        }
    }


    
    private void showWalletFragment() {
        WalletFragment walletFragment = WalletFragment.newInstance();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, walletFragment, "WALLET_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Simple data holder for category summary in pie chart
    private static class CategorySummary {
        final String name;
        final int iconResId;
        final long amount;
        final String colorHex;

        CategorySummary(String name, int iconResId, long amount, String colorHex) {
            this.name = name;
            this.iconResId = iconResId;
            this.amount = amount;
            this.colorHex = colorHex;
        }
    }
}