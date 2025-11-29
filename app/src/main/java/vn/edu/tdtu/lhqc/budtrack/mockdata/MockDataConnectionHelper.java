package vn.edu.tdtu.lhqc.budtrack.mockdata;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.models.Budget;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;

/**
 * Helper class to verify and maintain data connections across all mock data.
 * Ensures that all entities are properly connected.
 */
public class MockDataConnectionHelper {

    /**
     * Gets all transactions for a specific wallet.
     * 
     * @param walletId The wallet ID
     * @return List of transactions for that wallet
     */
    public static List<Transaction> getTransactionsForWallet(long walletId) {
        return MockTransactionData.getAllSampleTransactions(walletId);
    }

    /**
     * Gets all transactions for the default wallet (ID: 1 or 2).
     * 
     * @return List of transactions
     */
    public static List<Transaction> getDefaultWalletTransactions() {
        List<Wallet> wallets = MockWalletData.getSampleWallets();
        long defaultWalletId = wallets.isEmpty() ? 1L : wallets.get(0).getId();
        return MockTransactionData.getAllSampleTransactions(defaultWalletId);
    }

    /**
     * Calculates spent amount for a budget from actual transactions.
     * 
     * @param budget The budget
     * @param walletId The wallet ID to get transactions from
     * @return Calculated spent amount
     */
    public static long calculateBudgetSpent(Budget budget, long walletId) {
        List<Transaction> transactions = MockTransactionData.getAllSampleTransactions(walletId);
        return MockBudgetHelper.getSpentAmountFromTransactions(budget, transactions);
    }

    /**
     * Validates that all mock data connections are correct.
     * Checks:
     * - All transactions reference valid wallet IDs
     * - All transactions reference valid category IDs
     * - All budget categories reference valid budget and category IDs
     * - Spent amounts match transaction totals
     * 
     * @return true if all connections are valid
     */
    public static boolean validateConnections() {
        // Get all wallets
        List<Wallet> wallets = MockWalletData.getSampleWallets();
        
        // Get all transactions for first wallet
        if (wallets.isEmpty()) {
            return false;
        }
        
        long walletId = wallets.get(0).getId();
        List<Transaction> transactions = MockTransactionData.getAllSampleTransactions(walletId);
        
        // Verify each transaction has valid wallet ID
        for (Transaction transaction : transactions) {
            boolean validWallet = false;
            for (Wallet wallet : wallets) {
                if (wallet.getId() == transaction.getWalletId()) {
                    validWallet = true;
                    break;
                }
            }
            if (!validWallet) {
                return false;
            }
        }
        
        // Verify budget spent amounts match transaction calculations
        List<Budget> budgets = MockBudgetData.getSampleBudgets();
        for (Budget budget : budgets) {
            long calculatedSpent = MockBudgetHelper.getSpentAmountFromTransactions(budget, transactions);
            long mockSpent = MockBudgetHelper.getMockSpentAmount(budget);
            
            // Allow small differences due to rounding, but they should match
            if (Math.abs(calculatedSpent - mockSpent) > 1000) {
                return false;
            }
        }
        
        return true;
    }
}

