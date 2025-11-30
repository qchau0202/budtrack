package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.adapters.TransactionHistoryAdapter;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockTransactionData;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;
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

    private static final String ARG_BUDGET_NAME = "budget_name";
    private static final String ARG_BUDGET_AMOUNT = "budget_amount";
    private static final String ARG_SPENT_AMOUNT = "spent_amount";
    private static final String ARG_COLOR_RES_ID = "color_res_id";


    public BudgetDetailFragment() {
        // Required empty public constructor
    }

    public static BudgetDetailFragment newInstance(String budgetName, long budgetAmount, 
                                                   long spentAmount, int colorResId) {
        BudgetDetailFragment fragment = new BudgetDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BUDGET_NAME, budgetName);
        args.putLong(ARG_BUDGET_AMOUNT, budgetAmount);
        args.putLong(ARG_SPENT_AMOUNT, spentAmount);
        args.putInt(ARG_COLOR_RES_ID, colorResId);
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

        String budgetName = args.getString(ARG_BUDGET_NAME, "Budget");
        long budgetAmount = args.getLong(ARG_BUDGET_AMOUNT, 0);
        long spentAmount = args.getLong(ARG_SPENT_AMOUNT, 0);
        int colorResId = args.getInt(ARG_COLOR_RES_ID, R.color.primary_green);

        // Set budget title
        TextView tvBudgetTitle = root.findViewById(R.id.tv_budget_title);
        if (tvBudgetTitle != null) {
            tvBudgetTitle.setText(budgetName);
        }

        // Setup balance section
        setupBalanceSection(root, budgetAmount, spentAmount, colorResId);

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
                Toast.makeText(requireContext(), "Edit budget: " + budgetName, Toast.LENGTH_SHORT).show();
                // TODO: Implement budget edit functionality
            });
        }

        // Load sample transactions from mockdata (using first wallet ID)
        // Get first available wallet ID, or default to 1
        List<Wallet> wallets = WalletManager.getWallets(requireContext());
        long walletId = wallets.isEmpty() ? 1L : wallets.get(0).getId();
        List<Transaction> allTransactions = MockTransactionData.getAllSampleTransactions(walletId);
        
        // Filter income and expense transactions
        List<Transaction> incomeTransactions = new ArrayList<>();
        List<Transaction> expenseTransactions = new ArrayList<>();
        
        for (Transaction transaction : allTransactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                incomeTransactions.add(transaction);
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                expenseTransactions.add(transaction);
            }
        }

        // Setup transaction history view with tabs and RecyclerView
        setupTransactionHistory(root, incomeTransactions, expenseTransactions);
    }

    private void setupBalanceSection(View root, long budgetAmount, long spentAmount, int colorResId) {
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
            
            // Set progress bar color
            ProgressBarUtils.setProgressBarColor(requireContext(), progressBalance, colorResId);
        }

        if (tvSpentInfo != null) {
            tvSpentInfo.setText(CurrencyUtils.formatCurrency(spentAmount) + " of " + CurrencyUtils.formatCurrency(budgetAmount) + " spent");
        }
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



}
