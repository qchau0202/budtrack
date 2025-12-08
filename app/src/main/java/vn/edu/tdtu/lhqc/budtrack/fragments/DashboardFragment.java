package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    // Calendar components
    private TextView tvMonthYear;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private LinearLayout datesContainer;
    
    private Calendar currentDate;
    private Calendar selectedDate;
    private Calendar todayDate;
    private SimpleDateFormat monthYearFormat;
    private SimpleDateFormat dateKeyFormat;
    private List<View> dateCells;
    private Set<String> datesWithTransactions; // Dates that have transactions (format: "yyyy-MM-dd")
    
    public static final String RESULT_KEY_DATE_SELECTED = "date_selected";
    public static final String RESULT_SELECTED_DATE_MILLIS = "selected_date_millis";

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize calendar
        currentDate = Calendar.getInstance();
        selectedDate = Calendar.getInstance();
        todayDate = Calendar.getInstance(); // Track today's date
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateCells = new ArrayList<>();
        datesWithTransactions = new HashSet<>();
        
        // Listen for transaction creation to refresh calendar dots
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED,
            this,
            (requestKey, result) -> {
                if (TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED.equals(requestKey)) {
                    // Immediately refresh if fragment is visible
                    refreshCalendar();
                }
            }
        );
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Always refresh when fragment becomes visible
        refreshCalendar();
    }
    
    private void refreshCalendar() {
        if (getView() != null && isAdded() && !isDetached()) {
            loadTransactionDates();
            updateCalendar();
        }
    }
    
    private void loadTransactionDates() {
        if (getContext() == null) {
            return;
        }
        
        datesWithTransactions.clear();
        
        // Get ALL transactions from TransactionManager (not just current month)
        // This allows showing transaction indicators for dates in any month
        List<Transaction> allTransactions = TransactionManager.getTransactions(getContext());
        
        // Extract unique dates that have transactions
        for (Transaction transaction : allTransactions) {
            if (transaction.getDate() != null) {
                Calendar transCal = Calendar.getInstance();
                transCal.setTime(transaction.getDate());
                String dateKey = dateKeyFormat.format(transCal.getTime());
                datesWithTransactions.add(dateKey);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        GeneralHeaderController.setup(root, this);

        // Initialize calendar
        initializeCalendar(root);

        // Send initial date selection (current date) to TransactionHistoryFragment after view is created
        root.post(() -> {
            if (!isAdded()) {
                return;
            }
            FragmentActivity activity = getActivity();
            if (activity == null) {
                return;
            }
            long selectedMillis = selectedDate.getTimeInMillis();

            // Notify via FragmentResult so any listener (including TransactionHistoryFragment) can react
            Bundle result = new Bundle();
            result.putLong(RESULT_SELECTED_DATE_MILLIS, selectedMillis);
            activity.getSupportFragmentManager()
                    .setFragmentResult(RESULT_KEY_DATE_SELECTED, result);

            // Also directly notify the embedded TransactionHistoryFragment for extra reliability
            Fragment child = getChildFragmentManager()
                    .findFragmentById(R.id.transaction_history_fragment_container);
            if (child instanceof TransactionHistoryFragment) {
                ((TransactionHistoryFragment) child).setSelectedDate(selectedMillis);
            }
        });

        return root;
    }

    // Initialize the calendar month view
    private void initializeCalendar(View root) {
        tvMonthYear = root.findViewById(R.id.tv_month_year);
        btnPrevMonth = root.findViewById(R.id.btn_prev_week); // Reusing the same ID from layout
        btnNextMonth = root.findViewById(R.id.btn_next_week); // Reusing the same ID from layout
        datesContainer = root.findViewById(R.id.dates_container);

        if (tvMonthYear != null && btnPrevMonth != null && btnNextMonth != null && datesContainer != null) {
            setupCalendarListeners();
            updateCalendar();
        }
    }

    // Setup calendar navigation listeners

    private void setupCalendarListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    // Update the calendar display with current month
    private void updateCalendar() {
        // Load transaction dates for the current month
        loadTransactionDates();

        // Update month/year display
        if (tvMonthYear != null) {
            tvMonthYear.setText(monthYearFormat.format(currentDate.getTime()));
        }

        // Clear existing date cells
        if (datesContainer != null) {
            datesContainer.removeAllViews();
            dateCells.clear();

            // Set calendar to first day of the month
            Calendar monthStart = (Calendar) currentDate.clone();
            monthStart.set(Calendar.DAY_OF_MONTH, 1);

            // Get the first day of week for the first day of month
            int firstDayOfWeek = monthStart.get(Calendar.DAY_OF_WEEK);
            // Convert to Monday = 0 format (Sunday = 6, Monday = 0, ..., Saturday = 5)
            int daysFromMonday = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - Calendar.MONDAY;

            // Go back to the Monday of the week containing the first day of month
            monthStart.add(Calendar.DAY_OF_MONTH, -daysFromMonday);

            // Calculate total cells needed (including leading/trailing days from other months)
            // We need to show 6 weeks (42 days) to ensure full month display
            int totalCells = 42;
            int daysPerWeek = 7;

            // Create date cells for the month in a grid (6 weeks x 7 days)
            for (int week = 0; week < 6; week++) {
                LinearLayout weekRow = new LinearLayout(requireContext());
                weekRow.setOrientation(LinearLayout.HORIZONTAL);
                weekRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                for (int day = 0; day < daysPerWeek; day++) {
                    int cellIndex = week * daysPerWeek + day;
                    if (cellIndex >= totalCells) break;

                    Calendar date = (Calendar) monthStart.clone();
                    date.add(Calendar.DAY_OF_MONTH, cellIndex);

                    // Check if this date is in the current month
                    boolean isCurrentMonth = date.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                                           date.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR);

                    View dateCell = createDateCell(date.get(Calendar.DAY_OF_MONTH), date, isCurrentMonth);

                    // Set layout params for equal width cells
                    LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                    );
                    dateCell.setLayoutParams(cellParams);

                    weekRow.addView(dateCell);
                    dateCells.add(dateCell);
                }

                datesContainer.addView(weekRow);
            }
        }
    }

    // Create a date cell view
    private View createDateCell(int day, Calendar date, boolean isCurrentMonth) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        LinearLayout cell = (LinearLayout) inflater.inflate(R.layout.item_date_cell, datesContainer, false);

        TextView tvDate = cell.findViewById(R.id.tv_date);
        View eventIndicator = cell.findViewById(R.id.event_indicator);

        tvDate.setText(String.valueOf(day));

        // Check if this date has transactions (for any month, not just current)
        String dateKey = dateKeyFormat.format(date.getTime());
        boolean hasTransactions = datesWithTransactions.contains(dateKey);

        // Show event indicator if date has transactions (for any month)
        if (hasTransactions) {
            eventIndicator.setVisibility(View.VISIBLE);
        } else {
            eventIndicator.setVisibility(View.GONE);
        }

        // Check if this date is selected or is today
        boolean isSelected = isSameDay(date, selectedDate);
        boolean isToday = isSameDay(date, todayDate) && isCurrentMonth;

        // Update appearance based on selection and month
        if (isSelected) {
            // Selected date: green background with white text (works for any month)
            tvDate.setBackgroundResource(R.drawable.bg_date_cell_selected);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_white));
            tvDate.setAlpha(1.0f);
        } else if (!isCurrentMonth) {
            // Dates from other months: grey text, no background
            tvDate.setBackgroundResource(R.drawable.bg_date_cell);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.third_grey));
            tvDate.setAlpha(0.5f);
        } else if (isToday) {
            // Today's date: grey background with black text
            tvDate.setBackgroundResource(R.drawable.bg_date_cell_today);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
            tvDate.setAlpha(1.0f);
        } else {
            // Other dates: transparent background with black text
            tvDate.setBackgroundResource(R.drawable.bg_date_cell);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
            tvDate.setAlpha(1.0f);
        }

        // Set click listener for all dates (including dates from other months)
        // This allows users to select dates from previous/next months to view their transactions
        cell.setOnClickListener(v -> {
            if (!isAdded()) {
                return;
            }
            FragmentActivity activity = getActivity();
            if (activity == null) {
                return;
            }
            selectedDate = (Calendar) date.clone();
            long selectedMillis = selectedDate.getTimeInMillis();

            // If clicking a date from a different month, update currentDate to that month
            if (!isCurrentMonth) {
                currentDate.set(Calendar.YEAR, date.get(Calendar.YEAR));
                currentDate.set(Calendar.MONTH, date.get(Calendar.MONTH));
            }
            updateCalendar(); // Refresh to show new selection and month

            // Notify TransactionHistoryFragment about date selection via FragmentResult
            Bundle result = new Bundle();
            result.putLong(RESULT_SELECTED_DATE_MILLIS, selectedMillis);
            activity.getSupportFragmentManager().setFragmentResult(RESULT_KEY_DATE_SELECTED, result);

            // Also directly notify the embedded TransactionHistoryFragment for reliability
            Fragment child = getChildFragmentManager().findFragmentById(R.id.transaction_history_fragment_container);
            if (child instanceof TransactionHistoryFragment) {
                ((TransactionHistoryFragment) child).setSelectedDate(selectedMillis);
            }
        });

        return cell;
    }

    // Check if two Calendar instances represent the same day
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listener
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED);
    }

}