package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.adapters.TransactionHistoryAdapter;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCalculator;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCategoryManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.category.CategoryManager;
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

        // Setup delete button
        ImageButton btnDelete = root.findViewById(R.id.btn_delete);
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(budgetId, budgetName));
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
        
        // Listen for currency changes to refresh UI immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            "currency_changed",
            this,
            (requestKey, result) -> {
                if ("currency_changed".equals(requestKey)) {
                    // Refresh budget detail UI when currency changes
                    if (root != null) {
                        Budget budget = BudgetManager.getBudgetById(requireContext(), budgetId);
                        if (budget != null) {
                            setupBalanceSection(root, budget.getBudgetAmount(), spentAmount, colorResId, customColor);
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

    private void showDeleteConfirmationDialog(long budgetId, String budgetName) {
        if (getContext() == null || budgetId <= 0) return;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_wallet_delete_confirmation, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete);

        if (dialogTitle != null) {
            dialogTitle.setText(R.string.budget_delete_title);
        }
        if (dialogMessage != null) {
            String name = (budgetName != null && !budgetName.isEmpty())
                    ? budgetName
                    : getString(R.string.budget);
            dialogMessage.setText(getString(R.string.budget_delete_message, name));
        }
        if (btnDelete != null) {
            btnDelete.setText(R.string.budget_delete_button);
        }

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                // Remove budget and its category relationships
                BudgetManager.removeBudget(requireContext(), budgetId);
                BudgetCategoryManager.removeAllForBudget(requireContext(), budgetId);

                Toast.makeText(requireContext(),
                        getString(R.string.budget_deleted), Toast.LENGTH_SHORT).show();

                dialog.dismiss();

                // Close detail screen
                if (isAdded()) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void setupBalanceSection(View root, long budgetAmount, long spentAmount, int colorResId, Integer customColor) {
        TextView tvBalanceAmount = root.findViewById(R.id.tv_balance_amount);
        ProgressBar progressBalance = root.findViewById(R.id.progress_balance);
        TextView tvSpentInfo = root.findViewById(R.id.tv_spent_info);

        if (tvBalanceAmount != null) {
            tvBalanceAmount.setText(CurrencyUtils.formatCurrency(requireContext(), budgetAmount));
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
            tvSpentInfo.setText(CurrencyUtils.formatCurrency(requireContext(), spentAmount) + " of " + CurrencyUtils.formatCurrency(requireContext(), budgetAmount) + " spent");
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

        // Filter transactions by category IDs (match by categoryId for legacy, or by name+icon for new transactions)
        List<Transaction> filteredTransactions = new ArrayList<>();
        
        // Build a map of categoryId -> (name, iconResId) for matching
        Map<Long, String> categoryIdToName = new HashMap<>();
        Map<Long, Integer> categoryIdToIcon = new HashMap<>();
        List<CategoryManager.CategoryItem> allUserCategories = CategoryManager.getCategories(requireContext());
        for (CategoryManager.CategoryItem item : allUserCategories) {
            // Generate same ID as BudgetCreateFragment uses
            long categoryId = (long) (item.name.hashCode() * 31 + item.iconResId);
            categoryIdToName.put(categoryId, item.name);
            categoryIdToIcon.put(categoryId, item.iconResId);
        }
        
        for (Transaction transaction : transactionsInRange) {
            boolean matches = false;
            
            // Check if transaction matches by categoryId (legacy or new)
            if (transaction.getCategoryId() != null && categoryIds.contains(transaction.getCategoryId())) {
                matches = true;
            } else {
                // Check if transaction matches by name+icon (for user-defined categories)
                String transactionCategoryName = transaction.getCategoryName();
                Integer transactionCategoryIconResId = transaction.getCategoryIconResId();
                
                if (transactionCategoryName != null && transactionCategoryIconResId != null) {
                    // Check if this transaction's category matches any of the budget's categories
                    for (Long budgetCategoryId : categoryIds) {
                        String budgetCategoryName = categoryIdToName.get(budgetCategoryId);
                        Integer budgetCategoryIconResId = categoryIdToIcon.get(budgetCategoryId);
                        
                        if (budgetCategoryName != null && budgetCategoryIconResId != null) {
                            // Match by BOTH name AND icon
                            if (budgetCategoryName.equals(transactionCategoryName) && 
                                budgetCategoryIconResId.equals(transactionCategoryIconResId)) {
                                matches = true;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (matches) {
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
        TextView tvEmptyState = root.findViewById(R.id.tv_empty_state);

        if (recyclerView == null || tabIncome == null || tabExpense == null) {
            return;
        }

        // Convert transactions to adapter format using helper
        List<TransactionHistoryAdapter.DailyTransactionGroup> incomeGroups = 
            TransactionAdapterHelper.convertToDailyGroups(requireContext(), incomeTransactions, true);
        List<TransactionHistoryAdapter.DailyTransactionGroup> expenseGroups = 
            TransactionAdapterHelper.convertToDailyGroups(requireContext(), expenseTransactions, false);

        // Create adapters
        TransactionHistoryAdapter incomeAdapter = new TransactionHistoryAdapter(incomeGroups);
        TransactionHistoryAdapter expenseAdapter = new TransactionHistoryAdapter(expenseGroups);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(expenseAdapter); // Default to expenses

        // Helper method to update empty state visibility
        Runnable updateEmptyState = () -> {
            TransactionHistoryAdapter currentAdapter = (TransactionHistoryAdapter) recyclerView.getAdapter();
            boolean isEmpty = currentAdapter != null && currentAdapter.getItemCount() == 0;
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        };

        // Setup tab listeners
        tabIncome.setOnClickListener(v -> {
            selectTab(true, tabIncome, tabExpense);
            recyclerView.setAdapter(incomeAdapter);
            updateEmptyState.run();
        });

        tabExpense.setOnClickListener(v -> {
            selectTab(false, tabIncome, tabExpense);
            recyclerView.setAdapter(expenseAdapter);
            updateEmptyState.run();
        });

        // Default to expenses tab
        selectTab(false, tabIncome, tabExpense);
        updateEmptyState.run();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listeners
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            BudgetCreateFragment.RESULT_KEY_UPDATED);
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED);
        requireActivity().getSupportFragmentManager().clearFragmentResultListener("currency_changed");
    }

}
