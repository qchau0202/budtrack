package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.activities.TransactionHistoryActivity;
import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

public class TransactionHistoryFragment extends Fragment {

    // Selected date for filtering transactions (defaults to current date)
    private final Calendar selectedDate = Calendar.getInstance();

    // Listen for currency preference changes so we can refresh amounts immediately
    private SharedPreferences.OnSharedPreferenceChangeListener currencyPreferenceListener;
    private boolean needsRefreshAfterCurrencyChange = false;

    public TransactionHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Listen for transaction creation to refresh transaction list immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED,
            this,
            (requestKey, result) -> {
                if (TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED.equals(requestKey)) {
                    // Immediately refresh if fragment is visible
                    refreshTransactions();
                }
            }
        );
        
        // Listen for date selection from DashboardFragment calendar
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            DashboardFragment.RESULT_KEY_DATE_SELECTED,
            this,
            (requestKey, result) -> {
                if (DashboardFragment.RESULT_KEY_DATE_SELECTED.equals(requestKey)) {
                    long dateMillis = result.getLong(DashboardFragment.RESULT_SELECTED_DATE_MILLIS, -1);
                    if (dateMillis != -1) {
                        selectedDate.setTimeInMillis(dateMillis);
                        // Ensure we're on the UI thread and view is ready
                        if (getView() != null) {
                            getView().post(this::refreshTransactions);
                        } else {
                            // If view is not ready, refresh will happen in onResume
                        refreshTransactions();
                        }
                    }
                }
            }
        );
        
        // Listen for currency preference changes to refresh UI immediately
        currencyPreferenceListener = (sharedPrefs, key) -> {
            if (SettingsHandler.KEY_CURRENCY.equals(key)) {
                if (getView() != null && isAdded() && !isDetached()) {
                    getView().post(this::refreshTransactions);
                } else {
                    // Defer refresh until view becomes active again
                    needsRefreshAfterCurrencyChange = true;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        // Get the card container and make it clickable to open fullscreen view
        LinearLayout cardTransactions = root.findViewById(R.id.card_transactions);
        if (cardTransactions != null) {
            cardTransactions.setOnClickListener(v -> {
                // Only navigate if the click is not on an interactive element (tabs, buttons, etc.)
                // Child views that handle clicks will consume the event, so this will only fire
                // when clicking on non-interactive areas of the card
                Intent intent = new Intent(requireContext(), TransactionHistoryActivity.class);
                startActivity(intent);
            });
            // Make it focusable and clickable
            cardTransactions.setClickable(true);
            cardTransactions.setFocusable(true);
        }

        MaterialButton btnViewAll = root.findViewById(R.id.btn_view_all_transactions);
        LinearLayout listIncome = root.findViewById(R.id.list_income);
        LinearLayout listExpenses = root.findViewById(R.id.list_expenses);

        // Load and display transactions
        loadTransactions(listIncome, listExpenses);

        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), TransactionHistoryActivity.class);
                startActivity(intent);
            });
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register currency preference listener
        if (currencyPreferenceListener != null && getContext() != null) {
            SettingsHandler.getPrefs(getContext())
                    .registerOnSharedPreferenceChangeListener(currencyPreferenceListener);
        }

        // Always refresh when fragment becomes visible
        refreshTransactions();

        // Apply any deferred currency change
        if (needsRefreshAfterCurrencyChange) {
            refreshTransactions();
            needsRefreshAfterCurrencyChange = false;
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Unregister currency preference listener
        if (currencyPreferenceListener != null && getContext() != null) {
            SettingsHandler.getPrefs(getContext())
                    .unregisterOnSharedPreferenceChangeListener(currencyPreferenceListener);
        }
    }
    
    private void refreshTransactions() {
        if (getView() != null && isAdded() && !isDetached()) {
            LinearLayout listIncome = getView().findViewById(R.id.list_income);
            LinearLayout listExpenses = getView().findViewById(R.id.list_expenses);
            if (listIncome != null && listExpenses != null) {
                loadTransactions(listIncome, listExpenses);
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listeners
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED);
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            DashboardFragment.RESULT_KEY_DATE_SELECTED);
    }

    /**
     * Called by parent fragments (e.g., DashboardFragment) when a date is selected.
     * This provides a direct, reliable way to keep the preview list in sync with the calendar,
     * in addition to the FragmentResult mechanism.
     */
    public void setSelectedDate(long dateMillis) {
        if (dateMillis <= 0) return;
        selectedDate.setTimeInMillis(dateMillis);
        // If view is already created, refresh immediately, otherwise onResume will handle it.
        if (getView() != null && isAdded() && !isDetached()) {
            getView().post(this::refreshTransactions);
        }
    }

    private void loadTransactions(LinearLayout listIncome, LinearLayout listExpenses) {
        // Clear existing views
        if (listIncome != null) {
            listIncome.removeAllViews();
        }
        if (listExpenses != null) {
            listExpenses.removeAllViews();
        }
        
        // Filter by selected date (defaults to current date)
        Calendar dayStart = (Calendar) selectedDate.clone();
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        Date startDate = dayStart.getTime();
        
        Calendar dayEnd = (Calendar) selectedDate.clone();
        dayEnd.set(Calendar.HOUR_OF_DAY, 23);
        dayEnd.set(Calendar.MINUTE, 59);
        dayEnd.set(Calendar.SECOND, 59);
        dayEnd.set(Calendar.MILLISECOND, 999);
        Date endDate = dayEnd.getTime();
        
        // Get transactions for date range
        List<Transaction> allTransactions = TransactionManager.getTransactionsInRange(
            requireContext(), startDate, endDate);
        
        // Separate income and expenses
        List<Transaction> incomeTransactions = new ArrayList<>();
        List<Transaction> expenseTransactions = new ArrayList<>();
        
        for (Transaction transaction : allTransactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                incomeTransactions.add(transaction);
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                expenseTransactions.add(transaction);
            }
        }
        
        // Sort by date (newest first)
        incomeTransactions.sort((t1, t2) -> {
            Date d1 = t1.getDate() != null ? t1.getDate() : new Date(0);
            Date d2 = t2.getDate() != null ? t2.getDate() : new Date(0);
            return d2.compareTo(d1);
        });
        expenseTransactions.sort((t1, t2) -> {
            Date d1 = t1.getDate() != null ? t1.getDate() : new Date(0);
            Date d2 = t2.getDate() != null ? t2.getDate() : new Date(0);
            return d2.compareTo(d1);
        });
        
        // Show only latest 3 transactions of each type
        int maxItems = 3;
        List<Transaction> displayIncome = incomeTransactions.size() > maxItems 
            ? incomeTransactions.subList(0, maxItems) 
            : incomeTransactions;
        List<Transaction> displayExpenses = expenseTransactions.size() > maxItems 
            ? expenseTransactions.subList(0, maxItems) 
            : expenseTransactions;
                        
        // Populate income list
        populateTransactionList(listIncome, displayIncome, true);
        
        // Populate expenses list
        populateTransactionList(listExpenses, displayExpenses, false);
        
        // Show/hide empty state
        View rootView = getView();
        if (rootView != null) {
            TextView tvEmptyState = rootView.findViewById(R.id.tv_empty_state);
            if (tvEmptyState != null) {
                boolean isEmpty = displayIncome.isEmpty() && displayExpenses.isEmpty();
                if (isEmpty) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    if (listIncome != null) {
                        listIncome.setVisibility(View.GONE);
                    }
                    if (listExpenses != null) {
                        listExpenses.setVisibility(View.GONE);
                    }
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    if (listIncome != null) {
                        listIncome.setVisibility(View.VISIBLE);
                    }
                    if (listExpenses != null) {
                        listExpenses.setVisibility(View.VISIBLE);
                        }
                }
            }
        }
    }
    
    private void populateTransactionList(LinearLayout container, List<Transaction> transactions, boolean isIncome) {
        if (container == null || transactions == null) {
            return;
        }
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            
            // Create transaction row using the same layout as TransactionHistoryActivity
            View rowView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_transaction_row, container, false);
            
            ImageView ivIcon = rowView.findViewById(R.id.iv_merchant_icon);
            TextView tvMerchantName = rowView.findViewById(R.id.tv_merchant_name);
            TextView tvTime = rowView.findViewById(R.id.tv_transaction_time);
            TextView tvAmount = rowView.findViewById(R.id.tv_amount);
            View divider = rowView.findViewById(R.id.divider);
            
            // Set icon (prefer user-defined category icon, otherwise use default wallet icon)
            int iconResId = R.drawable.ic_wallet_24dp; // Default
            Integer categoryIconResId = transaction.getCategoryIconResId();
            if (categoryIconResId != null) {
                iconResId = categoryIconResId;
            }
            if (ivIcon != null) {
                ivIcon.setImageResource(iconResId);
            }
            
            // Set merchant name (title)
            String merchantName = transaction.getMerchantName();
            if (merchantName == null || merchantName.isEmpty()) {
                merchantName = isIncome ? getString(R.string.income) : getString(R.string.expense);
            }
            if (tvMerchantName != null) {
                tvMerchantName.setText(merchantName);
            }
            
            // Set time
            Date transDate = transaction.getDate();
            if (transDate != null && tvTime != null) {
                tvTime.setText(timeFormat.format(transDate));
            }
            
            // Set amount
            String amountText = isIncome
                ? "+" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount())
                : "-" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount());
            if (tvAmount != null) {
                tvAmount.setText(amountText);
                int colorResId = isIncome ? R.color.secondary_green : R.color.primary_red;
                tvAmount.setTextColor(ContextCompat.getColor(requireContext(), colorResId));
            }
            
            // Show/hide divider
            if (divider != null) {
                divider.setVisibility(i < transactions.size() - 1 ? View.VISIBLE : View.GONE);
            }
            
            // Make row clickable
            rowView.setOnClickListener(v -> {
                TransactionDetailFragment detailFragment = TransactionDetailFragment.newInstance(
                    transaction.getId()
                );
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment, "TRANSACTION_DETAIL_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
            });
            rowView.setClickable(true);
            rowView.setFocusable(true);
            
            container.addView(rowView);
        }
    }

}