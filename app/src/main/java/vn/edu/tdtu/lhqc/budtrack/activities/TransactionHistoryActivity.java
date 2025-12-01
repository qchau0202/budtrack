package vn.edu.tdtu.lhqc.budtrack.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import java.util.Date;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.mockdata.TransactionAdapterHelper;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.utils.TabStyleUtils;
import vn.edu.tdtu.lhqc.budtrack.adapters.TransactionHistoryAdapter;
import vn.edu.tdtu.lhqc.budtrack.fragments.TransactionDetailBottomSheet;
import vn.edu.tdtu.lhqc.budtrack.utils.LanguageManager;
import vn.edu.tdtu.lhqc.budtrack.utils.ThemeManager;

public class TransactionHistoryActivity extends AppCompatActivity {

    private final Calendar selectedMonth = Calendar.getInstance();
    private boolean isIncomeSelected = false;
    private RecyclerView recyclerView;
    private boolean filtersSetup = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager.applySavedLanguage(this);
        ThemeManager.applySavedTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_history);

        // Handle window insets properly for EdgeToEdge
        View mainLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(48, systemBars.top, 48, systemBars.bottom);
            return insets;
        });

        // Setup back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup title
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText(R.string.transactions);
        }

        // Setup search button (placeholder for now)
        ImageButton btnSearch = findViewById(R.id.btn_search);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                // TODO: Implement search functionality
            });
        }

        setupFilters();

        // Setup RecyclerView with real data
        recyclerView = findViewById(R.id.recycler_transactions);
        if (recyclerView != null) {
            // Enable nested scrolling for proper scrolling in activity context
            recyclerView.setNestedScrollingEnabled(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            // Load and display transactions
            loadTransactions();
            
            // Set max height for RecyclerView to enable scrolling in activity context
            recyclerView.post(() -> {
                if (recyclerView == null) return;
                View rootView = findViewById(R.id.main);
                View filtersContainer = findViewById(R.id.filters_container);
                View headerBar = findViewById(R.id.header_bar);
                if (rootView != null && filtersContainer != null && headerBar != null) {
                    int screenHeight = rootView.getHeight();
                    int filtersHeight = filtersContainer.getHeight();
                    int headerHeight = headerBar.getHeight();
                    if (screenHeight > 0 && filtersHeight > 0 && headerHeight > 0) {
                    int maxHeight = screenHeight - headerHeight - filtersHeight - (int) (48 * getResources().getDisplayMetrics().density); // 48dp padding
                    ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                        if (params != null) {
                    params.height = Math.max(maxHeight, 400); // Minimum 400dp for scrolling
                    recyclerView.setLayoutParams(params);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload transactions when returning to this activity
        if (recyclerView != null) {
            loadTransactions();
        }
    }
    
    private void loadTransactions() {
        if (recyclerView == null) {
            return;
        }
        
        try {
            // Get ALL transactions (not filtered by month)
            // This allows users to see all their transaction history when clicking "View All"
            List<Transaction> allTransactions = TransactionManager.getTransactions(this);
            
            // Filter by type
            List<Transaction> filteredTransactions = new ArrayList<>();
            for (Transaction transaction : allTransactions) {
                if (transaction != null) {
                    if (isIncomeSelected && transaction.getType() == TransactionType.INCOME) {
                        filteredTransactions.add(transaction);
                    } else if (!isIncomeSelected && transaction.getType() == TransactionType.EXPENSE) {
                        filteredTransactions.add(transaction);
                    }
                }
            }
            
            // Convert to adapter format and group by date
            List<TransactionHistoryAdapter.DailyTransactionGroup> dailyGroups = 
                TransactionAdapterHelper.convertToDailyGroups(filteredTransactions, isIncomeSelected);
            
            if (dailyGroups == null) {
                dailyGroups = new ArrayList<>();
            }
            
            // Sort by date (newest first) - parse dates for proper sorting
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, EEE", Locale.getDefault());
            dailyGroups.sort((g1, g2) -> {
                try {
                    if (g1 != null && g2 != null && g1.getDate() != null && g2.getDate() != null) {
                        Date d1 = dateFormat.parse(g1.getDate());
                        Date d2 = dateFormat.parse(g2.getDate());
                        if (d1 != null && d2 != null) {
                            return d2.compareTo(d1); // Newest first
                        }
                    }
                } catch (Exception e) {
                    // Fallback to string comparison
                }
                if (g1 != null && g2 != null && g1.getDate() != null && g2.getDate() != null) {
                    return g2.getDate().compareTo(g1.getDate());
                }
                return 0;
            });

            TransactionHistoryAdapter adapter = new TransactionHistoryAdapter(dailyGroups);
            adapter.setOnTransactionClickListener(transaction -> {
                if (transaction != null) {
                    TransactionDetailBottomSheet bottomSheet = TransactionDetailBottomSheet.newInstance(
                            transaction.getTransactionId()
                    );
                    bottomSheet.show(getSupportFragmentManager(), TransactionDetailBottomSheet.TAG);
                }
            });
            recyclerView.setAdapter(adapter);
            
            // Show/hide empty state
            TextView tvEmptyState = findViewById(R.id.tv_empty_state);
            if (tvEmptyState != null) {
                if (dailyGroups.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Set empty adapter on error
            recyclerView.setAdapter(new TransactionHistoryAdapter(new ArrayList<>()));
            // Show empty state on error
            TextView tvEmptyState = findViewById(R.id.tv_empty_state);
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }


    private void setupFilters() {
        if (filtersSetup) {
            return; // Already set up, avoid recursion
        }

        try {
        MaterialButton tabIncome = findViewById(R.id.tabIncome);
        MaterialButton tabExpenses = findViewById(R.id.tabExpense);

        if (tabIncome == null || tabExpenses == null) {
                // Views might not be ready yet, try again after layout (only once)
                if (recyclerView != null && !filtersSetup) {
                    recyclerView.post(() -> {
                        if (!filtersSetup) {
                            setupFilters();
                        }
                    });
                }
            return;
        }

            filtersSetup = true; // Mark as set up to prevent recursion
            
            tabIncome.setOnClickListener(v -> {
                if (tabIncome != null && tabExpenses != null) {
                    selectTab(true, tabIncome, tabExpenses);
                }
            });
            tabExpenses.setOnClickListener(v -> {
                if (tabIncome != null && tabExpenses != null) {
                    selectTab(false, tabIncome, tabExpenses);
                }
            });
        selectTab(false, tabIncome, tabExpenses);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMonthPicker(MaterialButton btnFilterDate, TextView filterDescription) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(getString(R.string.date))
                .setSelection(selectedMonth.getTimeInMillis())
                .setTheme(R.style.ThemeOverlay_Budtrack_DatePicker)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) {
                return;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            selectedMonth.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
            selectedMonth.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            // Reload transactions for new month
            loadTransactions();
        });

        picker.show(getSupportFragmentManager(), "transaction_history_month_picker");
    }

    private void selectTab(boolean incomeSelected,
                           MaterialButton tabIncome,
                           MaterialButton tabExpenses) {
        isIncomeSelected = incomeSelected;
        if (incomeSelected) {
            TabStyleUtils.selectTab(this, tabIncome, tabExpenses);
        } else {
            TabStyleUtils.selectTab(this, tabExpenses, tabIncome);
        }
        // Reload transactions when tab changes
        loadTransactions();
    }
}

