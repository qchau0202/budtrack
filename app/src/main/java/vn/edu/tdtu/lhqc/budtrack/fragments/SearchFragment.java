package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCalculator;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager;
import vn.edu.tdtu.lhqc.budtrack.models.Budget;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * Search Fragment - Provides search functionality across the app
 */
public class SearchFragment extends Fragment {

    private AppCompatEditText etSearch;
    private ImageButton btnClearSearch;
    private LinearLayout searchResultsContainer;
    private LinearLayout emptyState;
    private String currentQuery = "";

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        GeneralHeaderController.setup(root, this);
        setupSearchView(root);

        return root;
    }

    private void setupSearchView(View root) {
        etSearch = root.findViewById(R.id.et_search);
        btnClearSearch = root.findViewById(R.id.btn_clear_search);
        searchResultsContainer = root.findViewById(R.id.search_results_container);
        emptyState = root.findViewById(R.id.empty_state);

        // Auto-focus and show keyboard when fragment opens
        if (etSearch != null) {
            etSearch.requestFocus();
            // Show keyboard
            if (getActivity() != null) {
                android.view.inputmethod.InputMethodManager imm = 
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }

        // Search text change listener
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s.toString().trim();
                    updateClearButtonVisibility(currentQuery);
                    performSearch(currentQuery);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Handle search action from keyboard
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = etSearch.getText().toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                    }
                    hideKeyboard();
                    return true;
                }
                return false;
            });
        }

        // Clear button click listener
        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (etSearch != null) {
                    etSearch.setText("");
                    etSearch.requestFocus();
                }
            });
        }
    }

    private void updateClearButtonVisibility(String query) {
        if (btnClearSearch != null) {
            btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            // Show empty state
            showEmptyState(true);
            clearSearchResults();
        } else {
            // Hide empty state
            showEmptyState(false);
            // TODO: Implement actual search functionality
            // For now, show a placeholder
            showSearchResults(query);
        }
    }

    private void showEmptyState(boolean show) {
        if (emptyState != null) {
            emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void clearSearchResults() {
        if (searchResultsContainer != null) {
            searchResultsContainer.removeAllViews();
        }
    }

    private void showSearchResults(String query) {
        if (searchResultsContainer == null || getContext() == null) {
            return;
        }

        // Clear existing results
        clearSearchResults();

        // Perform search across all data types
        List<Transaction> matchingTransactions = searchTransactions(query);
        List<Budget> matchingBudgets = searchBudgets(query);
        List<Wallet> matchingWallets = searchWallets(query);

        // If no results, show empty state
        if (matchingTransactions.isEmpty() && matchingBudgets.isEmpty() && matchingWallets.isEmpty()) {
            TextView noResultsText = new TextView(requireContext());
            noResultsText.setText(getString(R.string.search_no_results, query));
            noResultsText.setTextColor(getResources().getColor(R.color.primary_grey, null));
            noResultsText.setTextSize(16f);
            noResultsText.setPadding(0, 24, 0, 24);
            noResultsText.setGravity(android.view.Gravity.CENTER);
            searchResultsContainer.addView(noResultsText);
            return;
        }

        // Display results grouped by type
        if (!matchingTransactions.isEmpty()) {
            addSectionHeader(getString(R.string.transactions));
            for (Transaction transaction : matchingTransactions) {
                addTransactionResult(transaction);
            }
        }

        if (!matchingBudgets.isEmpty()) {
            addSectionHeader(getString(R.string.budgets));
            for (Budget budget : matchingBudgets) {
                addBudgetResult(budget);
            }
        }

        if (!matchingWallets.isEmpty()) {
            addSectionHeader(getString(R.string.wallets));
            for (Wallet wallet : matchingWallets) {
                addWalletResult(wallet);
            }
        }
    }

    private List<Transaction> searchTransactions(String query) {
        List<Transaction> allTransactions = TransactionManager.getTransactions(requireContext());
        List<Transaction> matching = new ArrayList<>();
        String queryLower = query.toLowerCase(Locale.getDefault());

        for (Transaction transaction : allTransactions) {
            boolean matches = false;

            // Search in merchant name
            String merchantName = transaction.getMerchantName();
            if (merchantName != null && merchantName.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                matches = true;
            }

            // Search in category name
            if (!matches) {
                String categoryName = transaction.getCategoryName();
                if (categoryName != null && categoryName.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                    matches = true;
                }
            }

            // Search in note
            if (!matches) {
                String note = transaction.getNote();
                if (note != null && note.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                    matches = true;
                }
            }

            // Search in address
            if (!matches) {
                String address = transaction.getAddress();
                if (address != null && address.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                    matches = true;
                }
            }

            if (matches) {
                matching.add(transaction);
            }
        }

        // Sort newest to oldest
        matching.sort((t1, t2) -> {
            Date d1 = t1.getDate();
            Date d2 = t2.getDate();
            if (d1 == null && d2 == null) return Long.compare(t2.getId(), t1.getId());
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            int dateCompare = d2.compareTo(d1);
            return dateCompare != 0 ? dateCompare : Long.compare(t2.getId(), t1.getId());
        });

        return matching;
    }

    private List<Budget> searchBudgets(String query) {
        List<Budget> allBudgets = BudgetManager.getBudgets(requireContext());
        List<Budget> matching = new ArrayList<>();
        String queryLower = query.toLowerCase(Locale.getDefault());

        for (Budget budget : allBudgets) {
            String name = budget.getName();
            if (name != null && name.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                matching.add(budget);
            }
        }

        return matching;
    }

    private List<Wallet> searchWallets(String query) {
        List<Wallet> allWallets = WalletManager.getWallets(requireContext());
        List<Wallet> matching = new ArrayList<>();
        String queryLower = query.toLowerCase(Locale.getDefault());

        for (Wallet wallet : allWallets) {
            boolean matches = false;

            // Search in wallet name
            String name = wallet.getName();
            if (name != null && name.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                matches = true;
            }

            // Search in wallet type
            if (!matches) {
                String walletType = wallet.getWalletType();
                if (walletType != null && walletType.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                    matches = true;
                }
            }

            if (matches) {
                matching.add(wallet);
            }
        }

        return matching;
    }

    private void addSectionHeader(String title) {
        View headerView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_search_section_header, searchResultsContainer, false);
        TextView tvHeader = headerView.findViewById(R.id.tv_section_header);
        if (tvHeader != null) {
            tvHeader.setText(title);
        }
        searchResultsContainer.addView(headerView);
    }

    private void addTransactionResult(Transaction transaction) {
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_search_transaction, searchResultsContainer, false);

        ImageView ivIcon = itemView.findViewById(R.id.iv_transaction_icon);
        TextView tvTitle = itemView.findViewById(R.id.tv_transaction_title);
        TextView tvSubtitle = itemView.findViewById(R.id.tv_transaction_subtitle);
        TextView tvAmount = itemView.findViewById(R.id.tv_transaction_amount);

        // Set icon
        if (ivIcon != null) {
            int iconResId = R.drawable.ic_wallet_24dp;
            Integer categoryIconResId = transaction.getCategoryIconResId();
            if (categoryIconResId != null) {
                iconResId = categoryIconResId;
            }
            ivIcon.setImageResource(iconResId);
        }

        // Set title (merchant name or category name)
        if (tvTitle != null) {
            String title = transaction.getMerchantName();
            if (title == null || title.isEmpty()) {
                title = transaction.getCategoryName();
                if (title == null || title.isEmpty()) {
                    title = getString(R.string.unknown);
                }
            }
            tvTitle.setText(title);
        }

        // Set subtitle (date and time)
        if (tvSubtitle != null) {
            Date date = transaction.getDate();
            if (date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
                tvSubtitle.setText(dateFormat.format(date));
            } else {
                tvSubtitle.setText("");
            }
        }

        // Set amount
        if (tvAmount != null) {
            boolean isIncome = transaction.getType() == TransactionType.INCOME;
            String amountText = isIncome
                    ? "+" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount())
                    : "-" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount());
            tvAmount.setText(amountText);
            int colorRes = isIncome ? R.color.secondary_green : R.color.primary_red;
            tvAmount.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        }

        // Set click listener
        itemView.setOnClickListener(v -> {
            TransactionDetailBottomSheet detailSheet = TransactionDetailBottomSheet.newInstance(transaction.getId());
            detailSheet.show(requireActivity().getSupportFragmentManager(), TransactionDetailBottomSheet.TAG);
        });

        searchResultsContainer.addView(itemView);
    }

    private void addBudgetResult(Budget budget) {
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_search_budget, searchResultsContainer, false);

        ImageView ivIcon = itemView.findViewById(R.id.iv_budget_icon);
        TextView tvTitle = itemView.findViewById(R.id.tv_budget_title);
        TextView tvSubtitle = itemView.findViewById(R.id.tv_budget_subtitle);
        TextView tvAmount = itemView.findViewById(R.id.tv_budget_amount);

        // Set icon color based on budget color
        if (ivIcon != null) {
            int colorResId = budget.getColorResId();
            if (budget.getCustomColor() != null) {
                ivIcon.setColorFilter(budget.getCustomColor());
            } else if (colorResId != 0) {
                ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), colorResId));
            } else {
                ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_green));
            }
        }

        // Set title
        if (tvTitle != null) {
            tvTitle.setText(budget.getName());
        }

        // Set subtitle (period)
        if (tvSubtitle != null) {
            String period = budget.getPeriod();
            if (period != null && !period.isEmpty()) {
                String periodText = period.substring(0, 1).toUpperCase() + period.substring(1);
                tvSubtitle.setText(periodText);
            } else {
                tvSubtitle.setText("Monthly");
            }
        }

        // Set amount
        if (tvAmount != null) {
            tvAmount.setText(CurrencyUtils.formatCurrency(requireContext(), budget.getBudgetAmount()));
        }

        // Set click listener
        itemView.setOnClickListener(v -> {
            // Calculate spent amount for this budget
            long spentAmount = BudgetCalculator.calculateSpentAmount(requireContext(), budget);
            int colorResId = budget.getCustomColor() != null ? 0 : budget.getColorResId();
            Integer customColor = budget.getCustomColor();
            
            BudgetDetailFragment detailFragment = BudgetDetailFragment.newInstance(
                    budget.getId(), budget.getName(), budget.getBudgetAmount(),
                    spentAmount, colorResId, customColor);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        searchResultsContainer.addView(itemView);
    }

    private void addWalletResult(Wallet wallet) {
        View itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_search_wallet, searchResultsContainer, false);

        ImageView ivIcon = itemView.findViewById(R.id.iv_wallet_icon);
        TextView tvTitle = itemView.findViewById(R.id.tv_wallet_title);
        TextView tvSubtitle = itemView.findViewById(R.id.tv_wallet_subtitle);
        TextView tvBalance = itemView.findViewById(R.id.tv_wallet_balance);

        // Set icon
        if (ivIcon != null) {
            ivIcon.setImageResource(wallet.getIconResId());
        }

        // Set title
        if (tvTitle != null) {
            tvTitle.setText(wallet.getName());
        }

        // Set subtitle (wallet type)
        if (tvSubtitle != null) {
            tvSubtitle.setText(wallet.getWalletType());
        }

        // Set balance
        if (tvBalance != null) {
            tvBalance.setText(CurrencyUtils.formatCurrency(requireContext(), wallet.getBalance()));
        }

        // Set click listener - navigate to wallet fragment or show wallet details
        itemView.setOnClickListener(v -> {
            // Navigate back to home/wallet section
            requireActivity().onBackPressed();
            // TODO: Could navigate directly to wallet edit/details if needed
        });

        searchResultsContainer.addView(itemView);
    }

    private void hideKeyboard() {
        if (getActivity() != null && etSearch != null) {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        }
    }
}
