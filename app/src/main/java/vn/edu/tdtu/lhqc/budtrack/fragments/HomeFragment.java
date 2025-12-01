package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCalculator;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.mockdata.BudgetDisplayData;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockBudgetData;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockBudgetHelper;
import vn.edu.tdtu.lhqc.budtrack.models.Budget;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.BalanceController;
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
        // Load budgets from mockdata
        List<Budget> budgets = MockBudgetData.getSampleBudgets();
        
        // Create BudgetDisplayData with spent amounts
        LinkedHashMap<String, BudgetDisplayData> budgetData = new LinkedHashMap<>();
        long totalSpent = 0;
        
        for (vn.edu.tdtu.lhqc.budtrack.models.Budget budget : budgets) {
            long spentAmount = MockBudgetHelper.getMockSpentAmount(budget);
            BudgetDisplayData displayData = new BudgetDisplayData(budget, spentAmount);
            budgetData.put(budget.getName(), displayData);
            totalSpent += spentAmount;
        }

        // Initialize pie chart
        PieChartView pieChart = root.findViewById(R.id.pieChart);
        if (pieChart != null && totalSpent > 0) {
            LinkedHashMap<String, Float> pieData = new LinkedHashMap<>();
            for (String key : budgetData.keySet()) {
                BudgetDisplayData budget = budgetData.get(key);
                float percentage = totalSpent > 0 ? (float) ((budget.getSpentAmount() / (double) totalSpent) * 100) : 0;
                pieData.put(key, percentage);
            }

            pieChart.setData(pieData, Arrays.asList(
                    ContextCompat.getColor(requireContext(), R.color.primary_green),
                    ContextCompat.getColor(requireContext(), R.color.primary_yellow),
                    ContextCompat.getColor(requireContext(), R.color.primary_red)
            ));
            float density = getResources().getDisplayMetrics().density;
            pieChart.setRingThicknessPx(12f * density);
            pieChart.setSegmentGapDegrees(14f);
            pieChart.setCenterTexts(getString(R.string.expense), CurrencyUtils.formatCurrency(totalSpent));
        } else if (pieChart != null) {
            // If no spending, show empty chart
            pieChart.setData(new LinkedHashMap<>(), Arrays.asList());
            pieChart.setCenterTexts(getString(R.string.expense), CurrencyUtils.formatCurrency(0));
        }

        // Update budget tabs with amounts and percentages
        updateBudgetTabs(root, budgetData, totalSpent);
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
                // Set up long-press listener for tooltip
                final int dayIndex = i;
                final Calendar dayCalendar = dayCalendars[i];
                bar.setOnLongClickListener(v -> {
                    showBarTooltip(v, dayCalendar, finalDailyAmounts[dayIndex], showIncome);
                    return true;
                });
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
    
    // Update budget tabs with dynamic data. This method can be called whenever data changes.
    private void updateBudgetTabs(View root, LinkedHashMap<String, BudgetDisplayData> budgetData, long totalSpent) {
        // Daily Budget (maps to Transport tab in layout)
        TextView tvCategoryTransport = root.findViewById(R.id.tv_category_transport);
        TextView tvAmountTransport = root.findViewById(R.id.tv_amount_transport);
        TextView tvPercentTransport = root.findViewById(R.id.tv_percent_transport);
        if (budgetData.containsKey("Daily")) {
            BudgetDisplayData daily = budgetData.get("Daily");
            if (tvCategoryTransport != null) {
                tvCategoryTransport.setText(daily.getName());
            }
            if (tvAmountTransport != null) {
                tvAmountTransport.setText(CurrencyUtils.formatCurrency(daily.getSpentAmount()));
            }
            if (tvPercentTransport != null && totalSpent > 0) {
                float percentage = (float) ((daily.getSpentAmount() / (double) totalSpent) * 100);
                tvPercentTransport.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
            }
        }

        // Personal Budget (maps to Food tab in layout)
        TextView tvCategoryFood = root.findViewById(R.id.tv_category_food);
        TextView tvAmountFood = root.findViewById(R.id.tv_amount_food);
        TextView tvPercentFood = root.findViewById(R.id.tv_percent_food);
        if (budgetData.containsKey("Personal")) {
            BudgetDisplayData personal = budgetData.get("Personal");
            if (tvCategoryFood != null) {
                tvCategoryFood.setText(personal.getName());
            }
            if (tvAmountFood != null) {
                tvAmountFood.setText(CurrencyUtils.formatCurrency(personal.getSpentAmount()));
            }
            if (tvPercentFood != null && totalSpent > 0) {
                float percentage = (float) ((personal.getSpentAmount() / (double) totalSpent) * 100);
                tvPercentFood.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
            }
        }

        // Others Budget (maps to Shopping tab in layout)
        TextView tvCategoryShopping = root.findViewById(R.id.tv_category_shopping);
        TextView tvAmountShopping = root.findViewById(R.id.tv_amount_shopping);
        TextView tvPercentShopping = root.findViewById(R.id.tv_percent_shopping);
        if (budgetData.containsKey("Others")) {
            BudgetDisplayData others = budgetData.get("Others");
            if (tvCategoryShopping != null) {
                tvCategoryShopping.setText(others.getName());
            }
            if (tvAmountShopping != null) {
                tvAmountShopping.setText(CurrencyUtils.formatCurrency(others.getSpentAmount()));
            }
            if (tvPercentShopping != null && totalSpent > 0) {
                float percentage = (float) ((others.getSpentAmount() / (double) totalSpent) * 100);
                tvPercentShopping.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
            }
        }
    }


    
    private void showWalletFragment() {
        WalletFragment walletFragment = WalletFragment.newInstance();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, walletFragment, "WALLET_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}