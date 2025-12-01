package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.content.Intent;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.activities.TransactionHistoryActivity;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockCategoryData;
import vn.edu.tdtu.lhqc.budtrack.models.Category;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionHistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    
    // Selected date for filtering transactions (defaults to current date)
    private Calendar selectedDate = Calendar.getInstance();

    public TransactionHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TransactionHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TransactionHistoryFragment newInstance(String param1, String param2) {
        TransactionHistoryFragment fragment = new TransactionHistoryFragment();
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
                            getView().post(() -> refreshTransactions());
                        } else {
                            // If view is not ready, refresh will happen in onResume
                        refreshTransactions();
                        }
                    }
                }
            }
        );
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
        // Always refresh when fragment becomes visible
        refreshTransactions();
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
            
            // Set icon
            int iconResId = R.drawable.ic_wallet_24dp; // Default
            if (transaction.getCategoryId() != null) {
                Category category = findCategoryById(transaction.getCategoryId());
                if (category != null) {
                    iconResId = category.getIconResId();
                        }
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
                ? "+" + CurrencyUtils.formatCurrency(transaction.getAmount())
                : "-" + CurrencyUtils.formatCurrency(transaction.getAmount());
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
            String finalMerchantName = merchantName;
            rowView.setOnClickListener(v -> {
                TransactionDetailBottomSheet bottomSheet = TransactionDetailBottomSheet.newInstance(
                        finalMerchantName,
                    transDate != null ? timeFormat.format(transDate) : "",
                    amountText
                );
                bottomSheet.show(getParentFragmentManager(), TransactionDetailBottomSheet.TAG);
            });
            rowView.setClickable(true);
            rowView.setFocusable(true);
            
            container.addView(rowView);
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

}