package vn.edu.tdtu.lhqc.budtrack.mockdata;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.tdtu.lhqc.budtrack.adapters.TransactionHistoryAdapter;
import vn.edu.tdtu.lhqc.budtrack.models.Category;
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
     * @param transactions List of Transaction models
     * @return List of TransactionHistoryAdapter.Transaction
     */
    public static List<TransactionHistoryAdapter.Transaction> convertToAdapterTransactions(
            List<Transaction> transactions) {
        List<TransactionHistoryAdapter.Transaction> adapterTransactions = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        
        for (Transaction transaction : transactions) {
            String timeStr = transaction.getDate() != null 
                    ? timeFormat.format(transaction.getDate())
                    : "12:00 PM";
            
            String merchantName = transaction.getMerchantName() != null 
                    ? transaction.getMerchantName() 
                    : "Transaction";
            
            String amountText = transaction.getType() == TransactionType.INCOME
                    ? "+" + CurrencyUtils.formatNumber(transaction.getAmount()) + " VND"
                    : "-" + CurrencyUtils.formatNumber(transaction.getAmount()) + " VND";
            
            // Look up category icon or use default wallet icon
            int iconResId = vn.edu.tdtu.lhqc.budtrack.R.drawable.ic_wallet_24dp; // Default
            if (transaction.getCategoryId() != null) {
                Category category = findCategoryById(transaction.getCategoryId());
                if (category != null) {
                    iconResId = category.getIconResId();
                }
            }
            
            TransactionHistoryAdapter.Transaction adapterTransaction =
                    new TransactionHistoryAdapter.Transaction(
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
     * @param transactions List of Transaction models
     * @param isIncome Whether these are income transactions (for formatting)
     * @return List of DailyTransactionGroup
     */
    public static List<TransactionHistoryAdapter.DailyTransactionGroup> convertToDailyGroups(
            List<Transaction> transactions, boolean isIncome) {
        // Group transactions by date
        Map<String, List<TransactionHistoryAdapter.Transaction>> groupedByDate = new HashMap<>();
        
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
            }
            
            String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(transactionDate);
            String merchantName = transaction.getMerchantName() != null 
                    ? transaction.getMerchantName() 
                    : "Transaction";
            
            String amountText = isIncome
                    ? "+" + CurrencyUtils.formatNumber(transaction.getAmount()) + " VND"
                    : "-" + CurrencyUtils.formatNumber(transaction.getAmount()) + " VND";
            
            int iconResId = vn.edu.tdtu.lhqc.budtrack.R.drawable.ic_wallet_24dp; // Default
            if (transaction.getCategoryId() != null) {
                Category category = findCategoryById(transaction.getCategoryId());
                if (category != null) {
                    iconResId = category.getIconResId();
                }
            }
            
            TransactionHistoryAdapter.Transaction adapterTransaction =
                    new TransactionHistoryAdapter.Transaction(
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
            
            // Calculate daily total
            long dailyTotal = 0;
            for (TransactionHistoryAdapter.Transaction t : adapterTransactions) {
                String amountStr = t.getAmount().replaceAll("[.,\\s]", "").replace("VND", "").trim();
                boolean isPositive = !amountStr.startsWith("-");
                if (amountStr.startsWith("+") || amountStr.startsWith("-")) {
                    amountStr = amountStr.substring(1);
                }
                try {
                    long amount = Long.parseLong(amountStr);
                    dailyTotal += isPositive ? amount : -amount;
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
            
            String totalText = dailyTotal >= 0
                    ? "+" + CurrencyUtils.formatNumber(dailyTotal) + " VND"
                    : "-" + CurrencyUtils.formatNumber(Math.abs(dailyTotal)) + " VND";
            
            // Extract month from day key
            String monthLabel = dayKey.split(",")[0].split(" ")[0] + " " +
                    new SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.getTime());
            
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

    /**
     * Helper method to find category by ID.
     * 
     * @param categoryId Category ID
     * @return Category object or null if not found
     */
    private static Category findCategoryById(Long categoryId) {
        for (Category category : MockCategoryData.getSampleCategories()) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }
}

