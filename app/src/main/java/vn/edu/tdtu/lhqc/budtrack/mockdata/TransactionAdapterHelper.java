package vn.edu.tdtu.lhqc.budtrack.mockdata;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.tdtu.lhqc.budtrack.adapters.TransactionHistoryAdapter;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

/**
 * Helper class to convert Transaction models to adapter-compatible format.
 */
public class TransactionAdapterHelper {

    /**
     * Converts a list of Transaction models to TransactionHistoryAdapter.Transaction format.
     * 
     * @param context Application context for currency formatting
     * @param transactions List of Transaction models
     * @return List of TransactionHistoryAdapter.Transaction
     */
    public static List<TransactionHistoryAdapter.Transaction> convertToAdapterTransactions(
            Context context, List<Transaction> transactions) {
        List<TransactionHistoryAdapter.Transaction> adapterTransactions = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        for (Transaction transaction : transactions) {
            String timeStr = transaction.getDate() != null 
                    ? timeFormat.format(transaction.getDate())
                    : "12:00";
            
            String merchantName = transaction.getMerchantName() != null 
                    ? transaction.getMerchantName() 
                    : "Transaction";
            
            String amountText = transaction.getType() == TransactionType.INCOME
                    ? "+" + CurrencyUtils.formatCurrency(context, transaction.getAmount())
                    : "-" + CurrencyUtils.formatCurrency(context, transaction.getAmount());
            
            // Determine category icon (prefer user-defined icon, otherwise use default wallet icon)
            int iconResId = vn.edu.tdtu.lhqc.budtrack.R.drawable.ic_wallet_24dp; // Default
            Integer categoryIconResId = transaction.getCategoryIconResId();
            if (categoryIconResId != null) {
                iconResId = categoryIconResId;
            }
            
            TransactionHistoryAdapter.Transaction adapterTransaction =
                    new TransactionHistoryAdapter.Transaction(
                            transaction.getId(),
                            merchantName,
                            timeStr,
                            amountText,
                            iconResId
                    );
            adapterTransactions.add(adapterTransaction);
        }
        
        return adapterTransactions;
    }

    /**
     * Groups transactions by date and converts to DailyTransactionGroup format.
     * 
     * @param context Application context for currency formatting
     * @param transactions List of Transaction models
     * @param isIncome Whether these are income transactions (for formatting)
     * @return List of DailyTransactionGroup
     */
    public static List<TransactionHistoryAdapter.DailyTransactionGroup> convertToDailyGroups(
            Context context, List<Transaction> transactions, boolean isIncome) {
        // Group transactions by date
        Map<String, List<TransactionHistoryAdapter.Transaction>> groupedByDate = new HashMap<>();
        Map<String, Long> dailyTotals = new HashMap<>(); // Store daily totals
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("MMMM d, EEE", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        for (Transaction transaction : transactions) {
            Date transactionDate = transaction.getDate() != null 
                    ? transaction.getDate() 
                    : calendar.getTime();
            
            String monthKey = dateFormat.format(transactionDate);
            String dayKey = dayFormat.format(transactionDate);
            
            if (!groupedByDate.containsKey(dayKey)) {
                groupedByDate.put(dayKey, new ArrayList<>());
                dailyTotals.put(dayKey, 0L);
            }
            
            // Add to daily total
            long currentTotal = dailyTotals.get(dayKey);
            if (isIncome) {
                dailyTotals.put(dayKey, currentTotal + transaction.getAmount());
            } else {
                dailyTotals.put(dayKey, currentTotal - transaction.getAmount());
            }
            
            String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(transactionDate);
            String merchantName = transaction.getMerchantName() != null 
                    ? transaction.getMerchantName() 
                    : "Transaction";
            
            String amountText = isIncome
                    ? "+" + CurrencyUtils.formatCurrency(context, transaction.getAmount())
                    : "-" + CurrencyUtils.formatCurrency(context, transaction.getAmount());
            
            // Determine category icon (prefer user-defined icon, otherwise use default wallet icon)
            int iconResId = vn.edu.tdtu.lhqc.budtrack.R.drawable.ic_wallet_24dp; // Default
            Integer categoryIconResId = transaction.getCategoryIconResId();
            if (categoryIconResId != null) {
                iconResId = categoryIconResId;
            }
            
            TransactionHistoryAdapter.Transaction adapterTransaction =
                    new TransactionHistoryAdapter.Transaction(
                            transaction.getId(),
                            merchantName,
                            timeStr,
                            amountText,
                            iconResId
                    );
            
            groupedByDate.get(dayKey).add(adapterTransaction);
        }
        
        // Convert to DailyTransactionGroup list
        List<TransactionHistoryAdapter.DailyTransactionGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<TransactionHistoryAdapter.Transaction>> entry : groupedByDate.entrySet()) {
            String dayKey = entry.getKey();
            List<TransactionHistoryAdapter.Transaction> adapterTransactions = entry.getValue();
            
            // Get daily total from map
            long dailyTotal = dailyTotals.getOrDefault(dayKey, 0L);
            
            String totalText = dailyTotal >= 0
                    ? "+" + CurrencyUtils.formatCurrency(context, dailyTotal)
                    : "-" + CurrencyUtils.formatCurrency(context, Math.abs(dailyTotal));
            
            // Extract month and year from first transaction of the day
            String monthLabel = "";
            if (!adapterTransactions.isEmpty()) {
                // Get date from first transaction
                for (Transaction transaction : transactions) {
                    Date transactionDate = transaction.getDate() != null 
                            ? transaction.getDate() 
                            : calendar.getTime();
                    String transactionDayKey = dayFormat.format(transactionDate);
                    if (transactionDayKey.equals(dayKey)) {
                        monthLabel = dateFormat.format(transactionDate);
                        break;
                    }
                }
            }
            if (monthLabel.isEmpty()) {
                monthLabel = dateFormat.format(calendar.getTime());
            }
            
            TransactionHistoryAdapter.DailyTransactionGroup group =
                    new TransactionHistoryAdapter.DailyTransactionGroup(
                            monthLabel,
                            dayKey,
                            totalText,
                            adapterTransactions
                    );
            
            groups.add(group);
        }
        
        return groups;
    }

}

