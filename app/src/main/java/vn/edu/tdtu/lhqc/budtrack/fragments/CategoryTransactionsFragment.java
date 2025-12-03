package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.adapters.TransactionHistoryAdapter;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Category;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockCategoryData;
import vn.edu.tdtu.lhqc.budtrack.mockdata.TransactionAdapterHelper;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.TabStyleUtils;

/**
 * Fragment that displays all transactions for a specific category.
 * Accessed from the pie chart category tabs.
 */
public class CategoryTransactionsFragment extends Fragment {

    private static final String ARG_CATEGORY_NAME = "category_name";
    private static final String ARG_CATEGORY_ICON_RES_ID = "category_icon_res_id";

    private String categoryName;
    private int categoryIconResId;

    public static CategoryTransactionsFragment newInstance(String categoryName, int categoryIconResId) {
        CategoryTransactionsFragment fragment = new CategoryTransactionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_NAME, categoryName);
        args.putInt(ARG_CATEGORY_ICON_RES_ID, categoryIconResId);
        fragment.setArguments(args);
        return fragment;
    }

    public CategoryTransactionsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_category_transactions, container, false);

        GeneralHeaderController.setup(root, this);
        setupCategoryTransactions(root);

        // Listen for currency changes to refresh UI immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            "currency_changed",
            this,
            (requestKey, result) -> {
                if ("currency_changed".equals(requestKey)) {
                    // Refresh UI when currency changes
                    if (getView() != null) {
                        setupCategoryTransactions(getView());
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
                    // Refresh transaction list when a new transaction is created
                    if (getView() != null) {
                        setupCategoryTransactions(getView());
                    }
                }
            }
        );

        return root;
    }

    private void setupCategoryTransactions(View root) {
        Bundle args = getArguments();
        if (args == null) return;

        categoryName = args.getString(ARG_CATEGORY_NAME, "");
        categoryIconResId = args.getInt(ARG_CATEGORY_ICON_RES_ID, 0);

        if (categoryName.isEmpty() || categoryIconResId == 0) {
            return;
        }

        // Header: back button
        ImageButton btnBack = root.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager().popBackStack()
            );
        }

        // Category icon and name
        ImageView ivCategoryIcon = root.findViewById(R.id.iv_category_icon);
        TextView tvCategoryTitle = root.findViewById(R.id.tv_category_title);
        if (ivCategoryIcon != null) {
            ivCategoryIcon.setImageResource(categoryIconResId);
            ivCategoryIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_green));
        }
        if (tvCategoryTitle != null) {
            tvCategoryTitle.setText(categoryName);
        }

        // Get all transactions filtered by category
        List<Transaction> categoryTransactions = getCategoryTransactions();

        // Calculate total amount
        long totalAmount = 0;
        for (Transaction transaction : categoryTransactions) {
            totalAmount += transaction.getAmount();
        }

        // Display total amount
        TextView tvTotalAmount = root.findViewById(R.id.tv_total_amount);
        TextView tvTransactionCount = root.findViewById(R.id.tv_transaction_count);
        if (tvTotalAmount != null) {
            tvTotalAmount.setText(CurrencyUtils.formatCurrency(requireContext(), totalAmount));
        }
        if (tvTransactionCount != null) {
            String countText = categoryTransactions.size() + " " + getString(R.string.transactions);
            tvTransactionCount.setText(countText);
        }

        // Setup transaction history using RecyclerView
        setupTransactionHistory(root, categoryTransactions);
    }

    private List<Transaction> getCategoryTransactions() {
        List<Transaction> allTransactions = TransactionManager.getTransactions(requireContext());
        List<Transaction> categoryTransactions = new ArrayList<>();

        for (Transaction transaction : allTransactions) {
            if (transaction == null || transaction.getType() != TransactionType.EXPENSE) {
                continue;
            }

            // Match by category name and icon
            boolean matches = false;
            if (transaction.getCategoryName() != null && transaction.getCategoryIconResId() != null) {
                matches = transaction.getCategoryName().equals(categoryName) &&
                         transaction.getCategoryIconResId() == categoryIconResId;
            } else if (transaction.getCategoryId() != null) {
                // Legacy: try to match by categoryId
                Category category = findCategoryById(transaction.getCategoryId());
                if (category != null) {
                    matches = category.getName().equals(categoryName) &&
                             category.getIconResId() == categoryIconResId;
                }
            }

            if (matches) {
                categoryTransactions.add(transaction);
            }
        }

        // Sort by date descending (newest first)
        Collections.sort(categoryTransactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                Date d1 = t1.getDate() != null ? t1.getDate() : new Date(0);
                Date d2 = t2.getDate() != null ? t2.getDate() : new Date(0);
                return d2.compareTo(d1);
            }
        });

        return categoryTransactions;
    }

    private void setupTransactionHistory(View root, List<Transaction> expenseTransactions) {
        // Find views from the included transaction history view
        MaterialButton tabIncome = root.findViewById(R.id.tabIncome);
        MaterialButton tabExpense = root.findViewById(R.id.tabExpense);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_transactions);
        TextView tvEmptyState = root.findViewById(R.id.tv_empty_state);

        if (recyclerView == null) {
            return;
        }

        // Convert transactions to adapter format using helper
        List<TransactionHistoryAdapter.DailyTransactionGroup> expenseGroups = 
            TransactionAdapterHelper.convertToDailyGroups(requireContext(), expenseTransactions, false);
        
        // Create empty income groups (categories only have expenses)
        List<TransactionHistoryAdapter.DailyTransactionGroup> incomeGroups = new ArrayList<>();

        // Create adapters
        TransactionHistoryAdapter expenseAdapter = new TransactionHistoryAdapter(expenseGroups);
        TransactionHistoryAdapter incomeAdapter = new TransactionHistoryAdapter(incomeGroups);
        
        // Setup click listener for transaction details
        expenseAdapter.setOnTransactionClickListener(transaction -> {
            TransactionDetailFragment detailFragment = TransactionDetailFragment.newInstance(
                transaction.getTransactionId()
            );
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment, "TRANSACTION_DETAIL_FRAGMENT")
                .addToBackStack(null)
                .commit();
        });
        
        incomeAdapter.setOnTransactionClickListener(transaction -> {
            TransactionDetailFragment detailFragment = TransactionDetailFragment.newInstance(
                transaction.getTransactionId()
            );
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment, "TRANSACTION_DETAIL_FRAGMENT")
                .addToBackStack(null)
                .commit();
        });

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(expenseAdapter); // Default to expenses

        // Helper method to update empty state visibility
        Runnable updateEmptyState = () -> {
            if (tvEmptyState != null && recyclerView != null) {
                TransactionHistoryAdapter currentAdapter = (TransactionHistoryAdapter) recyclerView.getAdapter();
                boolean isEmpty = currentAdapter == null || currentAdapter.getItemCount() == 0;
                if (isEmpty) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        };

        // Setup tab listeners
        if (tabIncome != null && tabExpense != null) {
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
        }

        // Show/hide empty state initially
        updateEmptyState.run();
    }

    private void selectTab(boolean incomeSelected, MaterialButton tabIncome, MaterialButton tabExpense) {
        if (tabIncome == null || tabExpense == null) {
            return;
        }
        if (incomeSelected) {
            TabStyleUtils.selectTab(requireContext(), tabIncome, tabExpense);
        } else {
            TabStyleUtils.selectTab(requireContext(), tabExpense, tabIncome);
        }
    }

    private Category findCategoryById(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        for (Category category : MockCategoryData.getSampleCategories()) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listeners
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            "currency_changed");
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED);
    }
}

