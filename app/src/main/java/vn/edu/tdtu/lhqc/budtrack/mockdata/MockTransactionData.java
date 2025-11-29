package vn.edu.tdtu.lhqc.budtrack.mockdata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;

/**
 * Mock data generator for Transaction objects.
 * Provides sample transaction data for testing and development.
 * All transactions are properly connected with valid wallet IDs and category IDs.
 */
public class MockTransactionData {

    // Transaction ID counter - starts from 1
    private static long transactionIdCounter = 1;

    /**
     * Generates a list of sample expense transactions.
     * Transactions are spread across different dates and use valid category IDs.
     * 
     * @param walletId The wallet ID for transactions
     * @return List of sample expense Transaction objects
     */
    public static List<Transaction> getSampleExpenseTransactions(long walletId) {
        List<Transaction> transactions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        
        // Set to beginning of current month for consistent dates
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Day 1: Transport expense - 50,000 VND (Daily Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 30);
        Transaction transport1 = new Transaction(TransactionType.EXPENSE, 50000L, walletId, calendar.getTime());
        transport1.setId(transactionIdCounter++);
        transport1.setMerchantName("Taxi Ride");
        transport1.setCategoryId(3L); // Transport category (ID: 3) - Daily Budget
        transport1.setLatitude(10.8231);
        transport1.setLongitude(106.6297);
        transport1.setAddress("123 Nguyễn Huệ, Bến Nghé, Quận 1, Hồ Chí Minh");
        transport1.setNote("Morning commute");
        transactions.add(transport1);

        // Day 2: Food expense - Coffee 75,000 VND (Personal Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        Transaction coffee = new Transaction(TransactionType.EXPENSE, 75000L, walletId, calendar.getTime());
        coffee.setId(transactionIdCounter++);
        coffee.setMerchantName("Coffee Shop");
        coffee.setCategoryId(1L); // Food category (ID: 1) - Personal Budget
        coffee.setNote("Morning coffee");
        transactions.add(coffee);

        // Day 2: Transport expense - 100,000 VND (Daily Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 20);
        Transaction transport2 = new Transaction(TransactionType.EXPENSE, 100000L, walletId, calendar.getTime());
        transport2.setId(transactionIdCounter++);
        transport2.setMerchantName("Grab Ride");
        transport2.setCategoryId(3L); // Transport category (ID: 3) - Daily Budget
        transport2.setLatitude(10.7769);
        transport2.setLongitude(106.7009);
        transport2.setAddress("72 Lê Thánh Tôn, Bến Nghé, Quận 1, Hồ Chí Minh");
        transport2.setNote("Evening ride home");
        transactions.add(transport2);

        // Day 3: Food expense - Restaurant 150,000 VND (Personal Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        Transaction restaurant = new Transaction(TransactionType.EXPENSE, 150000L, walletId, calendar.getTime());
        restaurant.setId(transactionIdCounter++);
        restaurant.setMerchantName("Nhà Hàng Ngon");
        restaurant.setCategoryId(1L); // Food category (ID: 1) - Personal Budget
        restaurant.setLatitude(10.762622);
        restaurant.setLongitude(106.660172);
        restaurant.setAddress("160 Pasteur, Bến Nghé, Quận 1, Hồ Chí Minh");
        restaurant.setNote("Lunch with team");
        transactions.add(restaurant);

        // Day 3: Shopping expense - Grocery 500,000 VND (Personal Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 30);
        Transaction grocery = new Transaction(TransactionType.EXPENSE, 500000L, walletId, calendar.getTime());
        grocery.setId(transactionIdCounter++);
        grocery.setMerchantName("Grocery Store");
        grocery.setCategoryId(2L); // Shopping category (ID: 2) - Personal Budget
        grocery.setNote("Weekly groceries");
        transactions.add(grocery);

        // Day 5: Transport expense - Parking 30,000 VND (Daily Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        Transaction parking = new Transaction(TransactionType.EXPENSE, 30000L, walletId, calendar.getTime());
        parking.setId(transactionIdCounter++);
        parking.setMerchantName("Parking Fee");
        parking.setCategoryId(3L); // Transport category (ID: 3) - Daily Budget
        parking.setNote("Parking at mall");
        transactions.add(parking);

        // Day 5: Shopping expense - Mall 300,000 VND (Personal Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 0);
        Transaction shopping = new Transaction(TransactionType.EXPENSE, 300000L, walletId, calendar.getTime());
        shopping.setId(transactionIdCounter++);
        shopping.setMerchantName("Vincom Center");
        shopping.setCategoryId(2L); // Shopping category (ID: 2) - Personal Budget
        shopping.setLatitude(10.7769);
        shopping.setLongitude(106.7009);
        shopping.setAddress("72 Lê Thánh Tôn, Bến Nghé, Quận 1, Hồ Chí Minh");
        shopping.setNote("Clothing purchase");
        transactions.add(shopping);

        // Day 7: Food expense - Starbucks 120,000 VND (Personal Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 16);
        calendar.set(Calendar.MINUTE, 0);
        Transaction starbucks = new Transaction(TransactionType.EXPENSE, 120000L, walletId, calendar.getTime());
        starbucks.setId(transactionIdCounter++);
        starbucks.setMerchantName("Starbucks Coffee");
        starbucks.setCategoryId(1L); // Food category (ID: 1) - Personal Budget
        starbucks.setLatitude(10.762622);
        starbucks.setLongitude(106.660172);
        starbucks.setAddress("160 Pasteur, Bến Nghé, Quận 1, Hồ Chí Minh");
        starbucks.setNote("Afternoon coffee");
        transactions.add(starbucks);

        // Day 10: Transport expense - Gas 200,000 VND (Daily Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 10);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 0);
        Transaction gas = new Transaction(TransactionType.EXPENSE, 200000L, walletId, calendar.getTime());
        gas.setId(transactionIdCounter++);
        gas.setMerchantName("Gas Station");
        gas.setCategoryId(3L); // Transport category (ID: 3) - Daily Budget
        gas.setLatitude(10.8231);
        gas.setLongitude(106.6297);
        gas.setAddress("123 Nguyễn Huệ, Bến Nghé, Quận 1, Hồ Chí Minh");
        gas.setNote("Fuel refill");
        transactions.add(gas);

        // Day 12: Food expense - Lunch 80,000 VND (Personal Budget)
        calendar.set(Calendar.DAY_OF_MONTH, 12);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 30);
        Transaction lunch = new Transaction(TransactionType.EXPENSE, 80000L, walletId, calendar.getTime());
        lunch.setId(transactionIdCounter++);
        lunch.setMerchantName("Fast Food");
        lunch.setCategoryId(1L); // Food category (ID: 1) - Personal Budget
        lunch.setNote("Quick lunch");
        transactions.add(lunch);

        // Summary of expenses:
        // Transport (Daily Budget): 50,000 + 100,000 + 30,000 + 200,000 = 380,000 VND
        // Food (Personal Budget): 75,000 + 150,000 + 120,000 + 80,000 = 425,000 VND
        // Shopping (Personal Budget): 500,000 + 300,000 = 800,000 VND
        // Total Personal Budget spent: 425,000 + 800,000 = 1,225,000 VND

        return transactions;
    }

    /**
     * Generates a list of sample income transactions.
     * 
     * @param walletId The wallet ID for transactions
     * @return List of sample income Transaction objects
     */
    public static List<Transaction> getSampleIncomeTransactions(long walletId) {
        List<Transaction> transactions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        
        // Set to beginning of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Day 1: Salary - 5,000,000 VND (beginning of month)
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        Transaction salary = new Transaction(TransactionType.INCOME, 5000000L, walletId, calendar.getTime());
        salary.setId(transactionIdCounter++);
        salary.setMerchantName("Company Salary");
        salary.setNote("Monthly salary");
        transactions.add(salary);

        // Day 15: Freelance - 2,000,000 VND (mid-month)
        calendar.set(Calendar.DAY_OF_MONTH, 15);
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 30);
        Transaction freelance = new Transaction(TransactionType.INCOME, 2000000L, walletId, calendar.getTime());
        freelance.setId(transactionIdCounter++);
        freelance.setMerchantName("Freelance Project");
        freelance.setNote("Freelance project payment");
        transactions.add(freelance);

        return transactions;
    }

    /**
     * Generates a list of all sample transactions (expenses and income).
     * 
     * @param walletId The wallet ID for transactions
     * @return List of all sample Transaction objects
     */
    public static List<Transaction> getAllSampleTransactions(long walletId) {
        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(getSampleExpenseTransactions(walletId));
        allTransactions.addAll(getSampleIncomeTransactions(walletId));
        return allTransactions;
    }

    /**
     * Resets the transaction ID counter (useful for testing).
     */
    public static void resetTransactionIdCounter() {
        transactionIdCounter = 1;
    }
}
