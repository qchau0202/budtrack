package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

public class TransactionSearchBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "TransactionSearchBottomSheet";
    private static final String ARG_SEARCH_QUERY = "search_query";
    private static final String ARG_SEARCH_RESULTS = "search_results";

    private List<Transaction> searchResults;
    private String searchQuery;
    private RecyclerView rvSearchResults;
    private TextView tvNoResults;
    private TextView tvResultsCount;
    private TransactionSearchAdapter adapter;

    public static TransactionSearchBottomSheet newInstance(String query, List<Transaction> results) {
        TransactionSearchBottomSheet sheet = new TransactionSearchBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_QUERY, query);
        // Store transaction IDs instead of full objects
        long[] transactionIds = new long[results.size()];
        for (int i = 0; i < results.size(); i++) {
            transactionIds[i] = results.get(i).getId();
        }
        args.putLongArray(ARG_SEARCH_RESULTS, transactionIds);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        searchResults = new ArrayList<>();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Configure bottom sheet to expand fully
        if (getDialog() != null && getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_bottom_sheet_transaction_search, container, false);

        ImageButton btnClose = view.findViewById(R.id.btn_close_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        tvNoResults = view.findViewById(R.id.tv_no_results);
        tvResultsCount = view.findViewById(R.id.tv_results_count);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        // Load transactions from IDs now that we have a context
        Bundle args = getArguments();
        if (args != null) {
            searchQuery = args.getString(ARG_SEARCH_QUERY, "");
            long[] transactionIds = args.getLongArray(ARG_SEARCH_RESULTS);
            if (transactionIds != null && requireContext() != null) {
                searchResults.clear();
                for (long id : transactionIds) {
                    Transaction transaction = TransactionManager.getTransactionById(requireContext(), id);
                    if (transaction != null) {
                        searchResults.add(transaction);
                    }
                }
            }
        }

        setupRecyclerView();
        updateUI();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new TransactionSearchAdapter(searchResults);
        adapter.setOnTransactionClickListener(transaction -> {
            // Show transaction detail bottom sheet
            dismiss();
            if (getActivity() != null) {
                TransactionDetailBottomSheet detailSheet =
                        TransactionDetailBottomSheet.newInstance(transaction.getId());
                detailSheet.show(getActivity().getSupportFragmentManager(), TransactionDetailBottomSheet.TAG);
            }
        });

        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(adapter);
    }

    private void updateUI() {
        if (searchResults == null || searchResults.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
            tvResultsCount.setText("");
        } else {
            tvNoResults.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            String countText = getResources().getQuantityString(
                    R.plurals.search_results_count,
                    searchResults.size(),
                    searchResults.size()
            );
            tvResultsCount.setText(countText);
        }
    }

    private static class TransactionSearchAdapter extends RecyclerView.Adapter<TransactionSearchAdapter.ViewHolder> {

        private List<Transaction> transactions;
        private OnTransactionClickListener listener;

        interface OnTransactionClickListener {
            void onTransactionClick(Transaction transaction);
        }

        TransactionSearchAdapter(List<Transaction> transactions) {
            this.transactions = transactions != null ? transactions : new ArrayList<>();
        }

        void setOnTransactionClickListener(OnTransactionClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Transaction transaction = transactions.get(position);
            holder.bind(transaction);
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.ImageView ivIcon;
            private final TextView tvMerchantName;
            private final TextView tvTime;
            private final TextView tvAmount;
            private final View divider;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.iv_merchant_icon);
                tvMerchantName = itemView.findViewById(R.id.tv_merchant_name);
                tvTime = itemView.findViewById(R.id.tv_transaction_time);
                tvAmount = itemView.findViewById(R.id.tv_amount);
                divider = itemView.findViewById(R.id.divider);
            }

            void bind(Transaction transaction) {
                // Set icon
                int iconResId = R.drawable.ic_wallet_24dp;
                Integer categoryIconResId = transaction.getCategoryIconResId();
                if (categoryIconResId != null) {
                    iconResId = categoryIconResId;
                }
                ivIcon.setImageResource(iconResId);

                // Set merchant name
                String merchantName = transaction.getMerchantName();
                if (merchantName == null || merchantName.isEmpty()) {
                    merchantName = transaction.getCategoryName();
                    if (merchantName == null || merchantName.isEmpty()) {
                        merchantName = itemView.getContext().getString(R.string.unknown);
                    }
                }
                tvMerchantName.setText(merchantName);

                // Set time
                Date date = transaction.getDate();
                if (date != null) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    tvTime.setText(timeFormat.format(date));
                } else {
                    tvTime.setText("");
                }

                // Set amount
                boolean isIncome = transaction.getType() == vn.edu.tdtu.lhqc.budtrack.models.TransactionType.INCOME;
                String amountText = isIncome
                        ? "+" + CurrencyUtils.formatCurrency(itemView.getContext(), transaction.getAmount())
                        : "-" + CurrencyUtils.formatCurrency(itemView.getContext(), transaction.getAmount());
                tvAmount.setText(amountText);
                int colorRes = isIncome ? R.color.secondary_green : R.color.primary_red;
                tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));

                // Hide divider for last item
                if (divider != null) {
                    divider.setVisibility(getAdapterPosition() < transactions.size() - 1 ? View.VISIBLE : View.GONE);
                }

                // Set click listener
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTransactionClick(transaction);
                    }
                });
            }
        }
    }
}

