package vn.edu.tdtu.lhqc.budtrack.mockdata;

/**
 * Summary of mock data structure and connections.
 * 
 * This document shows how all mock data is connected together.
 * 
 * === WALLETS ===
 * ID 1: Cash - 4,000,000 VND
 * ID 2: Bank Card - 5,000,000 VND (Current Wallet)
 * ID 3: E-Wallet - 2,000,000 VND
 * 
 * === CATEGORIES ===
 * ID 1: Food (Personal Budget)
 * ID 2: Shopping (Personal Budget)
 * ID 3: Transport (Daily Budget)
 * ID 4: Home (Others Budget)
 * 
 * === BUDGETS ===
 * ID 1: Daily - 10,000,000 VND budget
 *   - Categories: Transport (ID: 3)
 *   - Spent: 380,000 VND (from Transport transactions)
 * 
 * ID 2: Personal - 5,000,000 VND budget
 *   - Categories: Food (ID: 1), Shopping (ID: 2)
 *   - Spent: 1,225,000 VND (425,000 Food + 800,000 Shopping)
 * 
 * ID 3: Others - 1,000,000 VND budget
 *   - Categories: Home (ID: 4)
 *   - Spent: 0 VND (no Home transactions)
 * 
 * === TRANSACTIONS ===
 * Expenses (using wallet ID from parameter, default: ID 1 or 2):
 *   - Transport (Daily Budget): 50,000 + 100,000 + 30,000 + 200,000 = 380,000 VND
 *   - Food (Personal Budget): 75,000 + 150,000 + 120,000 + 80,000 = 425,000 VND
 *   - Shopping (Personal Budget): 500,000 + 300,000 = 800,000 VND
 *   - Total Expenses: 1,605,000 VND
 * 
 * Income (using wallet ID from parameter, default: ID 1 or 2):
 *   - Salary: 5,000,000 VND (Day 1)
 *   - Freelance: 2,000,000 VND (Day 15)
 *   - Total Income: 7,000,000 VND
 * 
 * === BUDGET-CATEGORY RELATIONSHIPS ===
 * Daily Budget (ID: 1) -> Transport (ID: 3)
 * Personal Budget (ID: 2) -> Food (ID: 1), Shopping (ID: 2)
 * Others Budget (ID: 3) -> Home (ID: 4)
 * 
 * === CALCULATION SUMMARY ===
 * Daily Budget Spent: 380,000 VND (matches Transport transaction total)
 * Personal Budget Spent: 1,225,000 VND (matches Food + Shopping transaction totals)
 * Others Budget Spent: 0 VND (no Home category transactions)
 */
public class MockDataSummary {
    // This class serves as documentation for mock data connections
    // All data is properly connected and calculations match transaction totals
}

