package vn.edu.tdtu.lhqc.budtrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.DailyGroupViewHolder> {

    private List<DailyTransactionGroup> dailyGroups;

    public TransactionHistoryAdapter(List<DailyTransactionGroup> dailyGroups) {
        this.dailyGroups = dailyGroups;
    }

    @NonNull
    @Override
    public DailyGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_transaction_group, parent, false);
        return new DailyGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyGroupViewHolder holder, int position) {
        DailyTransactionGroup group = dailyGroups.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return dailyGroups != null ? dailyGroups.size() : 0;
    }

    static class DailyGroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMonth;
        private final TextView tvDate;
        private final TextView tvDailyTotal;
        private final LinearLayout transactionsContainer;

        DailyGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tv_month);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDailyTotal = itemView.findViewById(R.id.tv_daily_total);
            transactionsContainer = itemView.findViewById(R.id.transactions_container);
        }

        void bind(DailyTransactionGroup group) {
            if (tvMonth != null) {
                tvMonth.setText(group.getMonthLabel());
            }
            tvDate.setText(group.getDate());
            tvDailyTotal.setText(group.getTotal());

            // Clear existing transaction views
            transactionsContainer.removeAllViews();

            // Add transaction rows
            List<Transaction> transactions = group.getTransactions();
            for (int index = 0; index < transactions.size(); index++) {
                Transaction transaction = transactions.get(index);
                View transactionView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.item_transaction_row, transactionsContainer, false);
                
                ImageView ivIcon = transactionView.findViewById(R.id.iv_merchant_icon);
                TextView tvMerchantName = transactionView.findViewById(R.id.tv_merchant_name);
                TextView tvTime = transactionView.findViewById(R.id.tv_transaction_time);
                TextView tvAmount = transactionView.findViewById(R.id.tv_amount);
                View divider = transactionView.findViewById(R.id.divider);

                // Set icon (using default wallet icon for now)
                ivIcon.setImageResource(transaction.getIconResId());
                
                tvMerchantName.setText(transaction.getMerchantName());
                tvTime.setText(transaction.getTime());
                tvAmount.setText(transaction.getAmount());
                tintAmount(itemView.getContext(), tvAmount, transaction.getAmount());
                
                if (divider != null) {
                    divider.setVisibility(index < transactions.size() - 1 ? View.VISIBLE : View.GONE);
                }

                transactionsContainer.addView(transactionView);
            }
        }

        private void tintAmount(Context context, TextView tvAmount, String amount) {
            if (amount == null || tvAmount == null || context == null) {
                return;
            }
            int colorRes = amount.trim().startsWith("-")
                    ? R.color.primary_red
                    : R.color.secondary_green;
            tvAmount.setTextColor(ContextCompat.getColor(context, colorRes));
        }
    }

    // Data model classes
    public static class Transaction {
        private final String merchantName;
        private final String time;
        private final String amount;
        private final int iconResId;

        public Transaction(String merchantName, String time, String amount, int iconResId) {
            this.merchantName = merchantName;
            this.time = time;
            this.amount = amount;
            this.iconResId = iconResId;
        }

        public String getMerchantName() { return merchantName; }
        public String getTime() { return time; }
        public String getAmount() { return amount; }
        public int getIconResId() { return iconResId; }
    }

    public static class DailyTransactionGroup {
        private final String monthLabel;
        private final String date;
        private final String total;
        private final List<Transaction> transactions;

        public DailyTransactionGroup(String monthLabel, String date, String total, List<Transaction> transactions) {
            this.monthLabel = monthLabel;
            this.date = date;
            this.total = total;
            this.transactions = transactions;
        }

        public String getMonthLabel() { return monthLabel; }
        public String getDate() { return date; }
        public String getTotal() { return total; }
        public List<Transaction> getTransactions() { return transactions; }
    }
}

