package vn.edu.tdtu.lhqc.budtrack.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
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

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.adapters.TransactionHistoryAdapter;
import vn.edu.tdtu.lhqc.budtrack.utils.LanguageManager;
import vn.edu.tdtu.lhqc.budtrack.utils.ThemeManager;

public class TransactionHistoryActivity extends AppCompatActivity {

    private final Calendar selectedMonth = Calendar.getInstance();

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
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        // Setup back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup search button (placeholder for now)
        ImageButton btnSearch = findViewById(R.id.btn_search);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                // TODO: Implement search functionality
            });
        }

        setupFilters();

        // Setup RecyclerView with sample data
        RecyclerView recyclerView = findViewById(R.id.recycler_transactions);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            // Create sample data
            List<TransactionHistoryAdapter.Transaction> transactions = new ArrayList<>();
            transactions.add(new TransactionHistoryAdapter.Transaction(
                    "Starbucks Coffee",
                    "09:00 AM",
                    "-$12.00",
                    R.drawable.ic_wallet_24dp
            ));
            transactions.add(new TransactionHistoryAdapter.Transaction(
                    "Dunkin' Donuts",
                    "02:00 PM",
                    "-$44.80",
                    R.drawable.ic_wallet_24dp
            ));
            transactions.add(new TransactionHistoryAdapter.Transaction(
                    "Amazon",
                    "12:00 AM",
                    "-$123.00",
                    R.drawable.ic_wallet_24dp
            ));

            TransactionHistoryAdapter adapter = getTransactionHistoryAdapter(transactions);
            recyclerView.setAdapter(adapter);
        }
    }

    @NonNull
    private static TransactionHistoryAdapter getTransactionHistoryAdapter(List<TransactionHistoryAdapter.Transaction> transactions) {
        List<TransactionHistoryAdapter.Transaction> novemberTransactions = new ArrayList<>();
        novemberTransactions.add(new TransactionHistoryAdapter.Transaction(
                "Uber Ride",
                "08:15 PM",
                "-$18.50",
                R.drawable.ic_wallet_24dp
        ));
        novemberTransactions.add(new TransactionHistoryAdapter.Transaction(
                "Whole Foods",
                "11:30 AM",
                "-$67.00",
                R.drawable.ic_wallet_24dp
        ));

        List<TransactionHistoryAdapter.DailyTransactionGroup> dailyGroups = new ArrayList<>();
        dailyGroups.add(new TransactionHistoryAdapter.DailyTransactionGroup(
                "October 2025",
                "October 17, Thu",
                "-$278.30",
                transactions
        ));
        dailyGroups.add(new TransactionHistoryAdapter.DailyTransactionGroup(
                "November 2025",
                "November 3, Sun",
                "-$85.50",
                novemberTransactions
        ));

        return new TransactionHistoryAdapter(dailyGroups);
    }

    private void setupFilters() {

        MaterialButton tabIncome = findViewById(R.id.tabIncome);
        MaterialButton tabExpenses = findViewById(R.id.tabExpense);

        if (tabIncome == null || tabExpenses == null) {
            return;
        }

        tabIncome.setOnClickListener(v -> selectTab(true, tabIncome, tabExpenses));
        tabExpenses.setOnClickListener(v -> selectTab(false, tabIncome, tabExpenses));
        selectTab(false, tabIncome, tabExpenses);
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
        });

        picker.show(getSupportFragmentManager(), "transaction_history_month_picker");
    }

    private void selectTab(boolean incomeSelected,
                           MaterialButton tabIncome,
                           MaterialButton tabExpenses) {
        applyTabStyle(tabIncome, incomeSelected);
        applyTabStyle(tabExpenses, !incomeSelected);
    }

    private void applyTabStyle(MaterialButton button, boolean selected) {
        if (button == null) return;
        int bgColor = selected ? R.color.primary_green : R.color.secondary_grey;
        int textColor = selected ? R.color.primary_white : R.color.primary_black;
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, bgColor));
        button.setTextColor(ContextCompat.getColor(this, textColor));
    }
}

