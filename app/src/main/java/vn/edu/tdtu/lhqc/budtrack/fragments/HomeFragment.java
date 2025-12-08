package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.analytics.HomePieChartController;
import vn.edu.tdtu.lhqc.budtrack.controllers.analytics.HomeWeeklyAnalyticsController;
import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.BalanceController;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.TabStyleUtils;

public class HomeFragment extends Fragment {

    // Track current analytics tab selection
    private boolean isIncomeSelectedForAnalytics = false;

    // Flag to track if we need to refresh when fragment becomes visible
    private boolean needsRefresh = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Listen for transaction creation to refresh data immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED,
            this,
            (requestKey, result) -> {
                if (TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED.equals(requestKey)) {
                    // Immediately refresh data - data is already committed synchronously
                    // Use post to ensure we're on the UI thread and view is ready
                    if (getView() != null) {
                        getView().post(this::refreshDataInternal);
                    } else {
                        needsRefresh = true;
                    }
                }
            }
        );
        
        // Set up SharedPreferences listener for currency changes (more reliable than FragmentResult)
        currencyPreferenceListener = (sharedPrefs, key) -> {
            if (SettingsHandler.KEY_CURRENCY.equals(key)) {
                // Currency changed - refresh UI immediately
                if (getView() != null && isAdded() && !isDetached()) {
                    getView().post(this::refreshData);
                } else {
                    needsRefresh = true;
                }
            }
        };
    }
    
    private android.content.SharedPreferences.OnSharedPreferenceChangeListener currencyPreferenceListener;

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
        // Register currency preference change listener
        if (currencyPreferenceListener != null) {
            SettingsHandler.getPrefs(requireContext()).registerOnSharedPreferenceChangeListener(currencyPreferenceListener);
        }
        // Always refresh when fragment becomes visible
        // This ensures data is up-to-date, especially if we missed an update while fragment was hidden
        refreshData();
        needsRefresh = false; // Clear the flag after refresh
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Unregister currency preference change listener
        if (currencyPreferenceListener != null) {
            SettingsHandler.getPrefs(requireContext()).unregisterOnSharedPreferenceChangeListener(currencyPreferenceListener);
        }
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
        // Unregister currency preference change listener (safety check)
        if (currencyPreferenceListener != null && getContext() != null) {
            SettingsHandler.getPrefs(getContext()).unregisterOnSharedPreferenceChangeListener(currencyPreferenceListener);
        }
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
            final String originalBalanceText = CurrencyUtils.formatCurrency(requireContext(), totalBalance);
            
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
        // Delegate pie chart logic to controller to keep fragment lean
        HomePieChartController.updatePieChartAndTabs(requireContext(), root, 
            (categoryName, categoryIconResId) -> {
                // Navigate to category transactions fragment
                CategoryTransactionsFragment fragment = CategoryTransactionsFragment.newInstance(
                    categoryName, categoryIconResId
                );
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, "CATEGORY_TRANSACTIONS_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
            });
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
        HomeWeeklyAnalyticsController.updateWeeklyAnalytics(
                requireContext(),
                root,
                showIncome,
                this::showBarTooltip
        );
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
            String amountText = CurrencyUtils.formatCurrency(requireContext(), amount);
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
    
    private void showWalletFragment() {
        WalletFragment walletFragment = WalletFragment.newInstance();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, walletFragment, "WALLET_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

}