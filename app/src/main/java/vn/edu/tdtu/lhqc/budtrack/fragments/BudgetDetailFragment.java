package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.adapters.TransactionHistoryAdapter;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCalculator;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCategoryManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Budget;
import vn.edu.tdtu.lhqc.budtrack.mockdata.TransactionAdapterHelper;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.ProgressBarUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.TabStyleUtils;

/**
 * Budget Detail Fragment - Shows detailed breakdown of a budget with income and expenses
 */
public class BudgetDetailFragment extends Fragment {

    private static final String ARG_BUDGET_ID = "budget_id";
    private static final String ARG_BUDGET_NAME = "budget_name";
    private static final String ARG_BUDGET_AMOUNT = "budget_amount";
    private static final String ARG_SPENT_AMOUNT = "spent_amount";
    private static final String ARG_COLOR_RES_ID = "color_res_id";
    private static final String ARG_CUSTOM_COLOR = "custom_color";

    private long budgetId = 0;

    public BudgetDetailFragment() {
        // Required empty public constructor
    }

    public static BudgetDetailFragment newInstance(long budgetId, String budgetName, long budgetAmount, 
                                                   long spentAmount, int colorResId, Integer customColor) {
        BudgetDetailFragment fragment = new BudgetDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_BUDGET_ID, budgetId);
        args.putString(ARG_BUDGET_NAME, budgetName);
        args.putLong(ARG_BUDGET_AMOUNT, budgetAmount);
        args.putLong(ARG_SPENT_AMOUNT, spentAmount);
        args.putInt(ARG_COLOR_RES_ID, colorResId);
        if (customColor != null) {
            args.putInt(ARG_CUSTOM_COLOR, customColor);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_budget_detail, container, false);

        GeneralHeaderController.setup(root, this);
        setupBudgetDetail(root);

        return root;
    }

    private void setupBudgetDetail(View root) {
        Bundle args = getArguments();
        if (args == null) return;

        budgetId = args.getLong(ARG_BUDGET_ID, 0);
        String budgetName = args.getString(ARG_BUDGET_NAME, "Budget");
        long budgetAmount = args.getLong(ARG_BUDGET_AMOUNT, 0);
        long spentAmount = args.getLong(ARG_SPENT_AMOUNT, 0);
        int colorResId = args.getInt(ARG_COLOR_RES_ID, 0);
        Integer customColor = args.containsKey(ARG_CUSTOM_COLOR) ? args.getInt(ARG_CUSTOM_COLOR) : null;

        // Set budget title
        TextView tvBudgetTitle = root.findViewById(R.id.tv_budget_title);
        if (tvBudgetTitle != null) {
            tvBudgetTitle.setText(budgetName);
        }

        // Setup balance section
        setupBalanceSection(root, budgetAmount, spentAmount, colorResId, customColor);

        // Setup back button
        ImageButton btnBack = root.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }

        // Setup edit button
        ImageButton btnEdit = root.findViewById(R.id.btn_edit);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                if (budgetId > 0) {
                    BudgetCreateFragment editFragment = BudgetCreateFragment.newInstanceForEdit(budgetId);
                    editFragment.show(requireActivity().getSupportFragmentManager(), BudgetCreateFragment.TAG);
                } else {
                    Toast.makeText(requireContext(), "Cannot edit: Budget ID not found", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Listen for budget updates
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            BudgetCreateFragment.RESULT_KEY_UPDATED,
            this,
            (requestKey, result) -> {
                if (BudgetCreateFragment.RESULT_KEY_UPDATED.equals(requestKey)) {
                    long updatedBudgetId = result.getLong("budget_id", 0);
                    if (updatedBudgetId == budgetId) {
                        // Reload budget data and refresh the detail view
                        Budget updatedBudget = BudgetManager.getBudgetById(requireContext(), budgetId);
                        if (updatedBudget != null) {
                            long updatedSpentAmount = BudgetCalculator.calculateSpentAmount(requireContext(), updatedBudget);
                            int updatedColorResId = updatedBudget.getCustomColor() != null ? 0 : updatedBudget.getColorResId();
                            Integer updatedCustomColor = updatedBudget.getCustomColor();
        
                            // Update UI
                            if (tvBudgetTitle != null) {
                                tvBudgetTitle.setText(updatedBudget.getName());
                            }
                            setupBalanceSection(root, updatedBudget.getBudgetAmount(), updatedSpentAmount, 
                                              updatedColorResId, updatedCustomColor);
                            
                            // Refresh transaction history
                            loadAndDisplayTransactions(root);
                        }
                    }
                }
            }
        );
        
        // Listen for transaction creation/updates to refresh the list
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED,
            this,
            (requestKey, result) -> {
                if (TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED.equals(requestKey)) {
                    // Refresh transaction history when a new transaction is created
                    loadAndDisplayTransactions(root);
                }
            }
        );

        // Load and display transactions dynamically
        loadAndDisplayTransactions(root);
    }

    private void setupBalanceSection(View root, long budgetAmount, long spentAmount, int colorResId, Integer customColor) {
        TextView tvBalanceAmount = root.findViewById(R.id.tv_balance_amount);
        ProgressBar progressBalance = root.findViewById(R.id.progress_balance);
        TextView tvSpentInfo = root.findViewById(R.id.tv_spent_info);

        if (tvBalanceAmount != null) {
            tvBalanceAmount.setText(CurrencyUtils.formatCurrency(budgetAmount));
        }

        if (progressBalance != null) {
            int percentage = budgetAmount > 0 ? (int) Math.round(((double) spentAmount / budgetAmount) * 100) : 0;
            progressBalance.setMax(100);
            progressBalance.setProgress(Math.min(percentage, 100));
            
            // Only apply user's color when there's spending (percentage > 0)
            // When percentage is 0, keep it grey (default)
            if (percentage > 0) {
                if (customColor != null) {
                    // For custom colors, create a proper drawable with the custom color
                    setProgressBarCustomColor(progressBalance, customColor);
                } else if (colorResId != 0) {
                    // Use color resource ID
            ProgressBarUtils.setProgressBarColor(requireContext(), progressBalance, colorResId);
                } else {
                    // Default to primary green if no color specified
                    ProgressBarUtils.setProgressBarColor(requireContext(), progressBalance, R.color.primary_green);
                }
            } else {
                // When no spending, ensure it's grey (reset to default)
                // Reset to default drawable from XML
                progressBalance.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.progress_bar_budget));
            }
        }

        if (tvSpentInfo != null) {
            tvSpentInfo.setText(CurrencyUtils.formatCurrency(spentAmount) + " of " + CurrencyUtils.formatCurrency(budgetAmount) + " spent");
        }
    }

    /**
     * Load transactions for this budget and display them.
     * Filters transactions by budget's categories and date range (based on period).
     */
    private void loadAndDisplayTransactions(View root) {
        if (budgetId <= 0) {
            // No budget ID, show empty lists
            setupTransactionHistory(root, new ArrayList<>(), new ArrayList<>());
            return;
        }

        // Get budget to access period
        Budget budget = BudgetManager.getBudgetById(requireContext(), budgetId);
        if (budget == null) {
            setupTransactionHistory(root, new ArrayList<>(), new ArrayList<>());
            return;
        }

        // Get category IDs for this budget
        List<Long> categoryIds = BudgetCategoryManager.getCategoryIdsForBudget(requireContext(), budgetId);
        if (categoryIds.isEmpty()) {
            // No categories, show empty lists
            setupTransactionHistory(root, new ArrayList<>(), new ArrayList<>());
            return;
        }

        // Get date range based on budget period
        Date[] dateRange = getDateRangeForPeriod(budget.getPeriod());
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        // Get transactions in date range
        List<Transaction> transactionsInRange = TransactionManager.getTransactionsInRange(
            requireContext(), startDate, endDate);

        // Filter transactions by category IDs
        List<Transaction> filteredTransactions = new ArrayList<>();
        for (Transaction transaction : transactionsInRange) {
            if (transaction.getCategoryId() != null && categoryIds.contains(transaction.getCategoryId())) {
                filteredTransactions.add(transaction);
            }
        }

        // Separate income and expense transactions
        List<Transaction> incomeTransactions = new ArrayList<>();
        List<Transaction> expenseTransactions = new ArrayList<>();

        for (Transaction transaction : filteredTransactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                incomeTransactions.add(transaction);
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                expenseTransactions.add(transaction);
            }
        }

        // Sort by date (newest first)
        Collections.sort(incomeTransactions, (t1, t2) -> {
            Date d1 = t1.getDate() != null ? t1.getDate() : new Date(0);
            Date d2 = t2.getDate() != null ? t2.getDate() : new Date(0);
            return d2.compareTo(d1);
        });
        Collections.sort(expenseTransactions, (t1, t2) -> {
            Date d1 = t1.getDate() != null ? t1.getDate() : new Date(0);
            Date d2 = t2.getDate() != null ? t2.getDate() : new Date(0);
            return d2.compareTo(d1);
        });

        // Setup transaction history view with tabs and RecyclerView
        setupTransactionHistory(root, incomeTransactions, expenseTransactions);
    }

    /**
     * Get date range for a budget period.
     * Returns [startDate, endDate] array.
     */
    private Date[] getDateRangeForPeriod(String period) {
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        switch (period != null ? period : "monthly") {
            case "daily":
                // Today
                break;
            case "weekly":
                // This week (start of week)
                int dayOfWeek = startCalendar.get(Calendar.DAY_OF_WEEK);
                int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
                startCalendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
                break;
            case "monthly":
                // This month
                startCalendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "yearly":
                // This year
                startCalendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
            default:
                // Default to monthly
                startCalendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
        }

        Date startDate = startCalendar.getTime();
        return new Date[]{startDate, endDate};
    }

    private void setupTransactionHistory(View root, List<Transaction> incomeTransactions, 
                                         List<Transaction> expenseTransactions) {
        // Find views from the included transaction history view
        MaterialButton tabIncome = root.findViewById(R.id.tabIncome);
        MaterialButton tabExpense = root.findViewById(R.id.tabExpense);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_transactions);

        if (recyclerView == null || tabIncome == null || tabExpense == null) {
            return;
        }

        // Convert transactions to adapter format using helper
        List<TransactionHistoryAdapter.DailyTransactionGroup> incomeGroups = 
            TransactionAdapterHelper.convertToDailyGroups(incomeTransactions, true);
        List<TransactionHistoryAdapter.DailyTransactionGroup> expenseGroups = 
            TransactionAdapterHelper.convertToDailyGroups(expenseTransactions, false);

        // Create adapters
        TransactionHistoryAdapter incomeAdapter = new TransactionHistoryAdapter(incomeGroups);
        TransactionHistoryAdapter expenseAdapter = new TransactionHistoryAdapter(expenseGroups);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(expenseAdapter); // Default to expenses

        // Setup tab listeners
        tabIncome.setOnClickListener(v -> {
            selectTab(true, tabIncome, tabExpense);
            recyclerView.setAdapter(incomeAdapter);
        });

        tabExpense.setOnClickListener(v -> {
            selectTab(false, tabIncome, tabExpense);
            recyclerView.setAdapter(expenseAdapter);
        });

        // Default to expenses tab
        selectTab(false, tabIncome, tabExpense);
    }


    private void selectTab(boolean incomeSelected, MaterialButton tabIncome, MaterialButton tabExpense) {
        if (incomeSelected) {
            TabStyleUtils.selectTab(requireContext(), tabIncome, tabExpense);
        } else {
            TabStyleUtils.selectTab(requireContext(), tabExpense, tabIncome);
        }
    }

    /**
     * Sets a custom color for the progress bar (similar to ProgressBarUtils but for custom colors).
     * Creates a proper drawable with grey background and custom color for progress.
     */
    private void setProgressBarCustomColor(ProgressBar progressBar, int customColor) {
        if (progressBar == null) {
            return;
        }

        // Convert dp to pixels for corner radius (4dp)
        float density = requireContext().getResources().getDisplayMetrics().density;
        float cornerRadius = 4.0f * density;

        // Create background shape (grey)
        ShapeDrawable backgroundShape = new ShapeDrawable();
        backgroundShape.setShape(new RoundRectShape(
            new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                       cornerRadius, cornerRadius, cornerRadius, cornerRadius}, null, null));
        backgroundShape.getPaint().setColor(ContextCompat.getColor(requireContext(), R.color.secondary_grey));

        // Create progress shape with the custom color
        ShapeDrawable progressShape = new ShapeDrawable();
        progressShape.setShape(new RoundRectShape(
            new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                       cornerRadius, cornerRadius, cornerRadius, cornerRadius}, null, null));
        progressShape.getPaint().setColor(customColor);

        // Create clip drawable for progress
        ClipDrawable clipDrawable = new ClipDrawable(progressShape,
            Gravity.START, ClipDrawable.HORIZONTAL);

        // Create layer drawable
        LayerDrawable layerDrawable = new LayerDrawable(
            new android.graphics.drawable.Drawable[]{backgroundShape, clipDrawable});
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);

        progressBar.setProgressDrawable(layerDrawable);
    }

}
