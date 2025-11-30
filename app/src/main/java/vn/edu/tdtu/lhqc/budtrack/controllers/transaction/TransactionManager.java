package vn.edu.tdtu.lhqc.budtrack.controllers.transaction;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;

/**
 * TransactionManager manages transaction data with persistence.
 * Stores transaction data in SharedPreferences using JSON serialization.
 */
public final class TransactionManager {

    private static final String PREFS_NAME = "transaction_prefs";
    private static final String KEY_TRANSACTIONS = "transactions";
    private static final String KEY_NEXT_ID = "next_id";

    private TransactionManager() {
    }

    /**
     * Get all transactions.
     */
    public static List<Transaction> getTransactions(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String transactionsJson = prefs.getString(KEY_TRANSACTIONS, "[]");
        
        List<Transaction> transactions = new ArrayList<>();
        
        try {
            JSONArray jsonArray = new JSONArray(transactionsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Transaction transaction = fromJson(jsonObject);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return transactions;
    }

    /**
     * Get transactions filtered by type.
     */
    public static List<Transaction> getTransactionsByType(Context context, TransactionType type) {
        List<Transaction> allTransactions = getTransactions(context);
        List<Transaction> filtered = new ArrayList<>();
        
        for (Transaction transaction : allTransactions) {
            if (transaction.getType() == type) {
                filtered.add(transaction);
            }
        }
        
        return filtered;
    }

    /**
     * Get transactions within a date range.
     */
    public static List<Transaction> getTransactionsInRange(Context context, Date startDate, Date endDate) {
        List<Transaction> allTransactions = getTransactions(context);
        List<Transaction> filtered = new ArrayList<>();
        
        for (Transaction transaction : allTransactions) {
            Date transactionDate = transaction.getDate();
            if (transactionDate != null && 
                !transactionDate.before(startDate) && 
                !transactionDate.after(endDate)) {
                filtered.add(transaction);
            }
        }
        
        return filtered;
    }

    /**
     * Get transactions by category ID.
     */
    public static List<Transaction> getTransactionsByCategory(Context context, long categoryId) {
        List<Transaction> allTransactions = getTransactions(context);
        List<Transaction> filtered = new ArrayList<>();
        
        for (Transaction transaction : allTransactions) {
            if (transaction.getCategoryId() != null && transaction.getCategoryId() == categoryId) {
                filtered.add(transaction);
            }
        }
        
        return filtered;
    }

    /**
     * Add a new transaction.
     */
    public static void addTransaction(Context context, Transaction transaction) {
        List<Transaction> transactions = getTransactions(context);
        
        // Assign ID if not set
        if (transaction.getId() == 0) {
            SharedPreferences prefs = getPrefs(context);
            long nextId = prefs.getLong(KEY_NEXT_ID, 1);
            transaction.setId(nextId);
            prefs.edit().putLong(KEY_NEXT_ID, nextId + 1).apply();
        }
        
        transactions.add(transaction);
        saveTransactions(context, transactions);
    }

    /**
     * Update a transaction.
     */
    public static void updateTransaction(Context context, Transaction transaction) {
        List<Transaction> transactions = getTransactions(context);
        
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId() == transaction.getId()) {
                transactions.set(i, transaction);
                saveTransactions(context, transactions);
                return;
            }
        }
    }

    /**
     * Remove a transaction by ID.
     */
    public static void removeTransaction(Context context, long transactionId) {
        List<Transaction> transactions = getTransactions(context);
        transactions.removeIf(t -> t.getId() == transactionId);
        saveTransactions(context, transactions);
    }

    /**
     * Save transactions to SharedPreferences.
     */
    private static void saveTransactions(Context context, List<Transaction> transactions) {
        SharedPreferences prefs = getPrefs(context);
        JSONArray jsonArray = new JSONArray();
        
        for (Transaction transaction : transactions) {
            try {
                jsonArray.put(toJson(transaction));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        // Use commit() to ensure data is saved synchronously before notifying other fragments
        prefs.edit().putString(KEY_TRANSACTIONS, jsonArray.toString()).commit();
    }

    /**
     * Convert Transaction to JSONObject.
     */
    private static JSONObject toJson(Transaction transaction) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", transaction.getId());
        json.put("type", transaction.getType() != null ? transaction.getType().name() : "EXPENSE");
        json.put("amount", transaction.getAmount());
        json.put("walletId", transaction.getWalletId());
        if (transaction.getCategoryId() != null) {
            json.put("categoryId", transaction.getCategoryId());
        }
        if (transaction.getMerchantName() != null) {
            json.put("merchantName", transaction.getMerchantName());
        }
        if (transaction.getNote() != null) {
            json.put("note", transaction.getNote());
        }
        if (transaction.getDate() != null) {
            json.put("date", transaction.getDate().getTime());
        }
        if (transaction.getLatitude() != null) {
            json.put("latitude", transaction.getLatitude());
        }
        if (transaction.getLongitude() != null) {
            json.put("longitude", transaction.getLongitude());
        }
        if (transaction.getAddress() != null) {
            json.put("address", transaction.getAddress());
        }
        return json;
    }

    /**
     * Convert JSONObject to Transaction.
     */
    private static Transaction fromJson(JSONObject json) throws JSONException {
        Transaction transaction = new Transaction();
        transaction.setId(json.getLong("id"));
        
        String typeStr = json.optString("type", "EXPENSE");
        try {
            transaction.setType(TransactionType.valueOf(typeStr));
        } catch (IllegalArgumentException e) {
            transaction.setType(TransactionType.EXPENSE);
        }
        
        transaction.setAmount(json.getLong("amount"));
        transaction.setWalletId(json.getLong("walletId"));
        
        if (json.has("categoryId") && !json.isNull("categoryId")) {
            transaction.setCategoryId(json.getLong("categoryId"));
        }
        if (json.has("merchantName")) {
            transaction.setMerchantName(json.getString("merchantName"));
        }
        if (json.has("note")) {
            transaction.setNote(json.getString("note"));
        }
        if (json.has("date")) {
            transaction.setDate(new Date(json.getLong("date")));
        }
        if (json.has("latitude") && !json.isNull("latitude")) {
            transaction.setLatitude(json.getDouble("latitude"));
        }
        if (json.has("longitude") && !json.isNull("longitude")) {
            transaction.setLongitude(json.getDouble("longitude"));
        }
        if (json.has("address")) {
            transaction.setAddress(json.getString("address"));
        }
        
        return transaction;
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

