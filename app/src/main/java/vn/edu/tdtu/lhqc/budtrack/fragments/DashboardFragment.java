package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
    private List<String> datesWithExpenses; // Dates that have expenses (format: "yyyy-MM-dd")

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        
        // Initialize calendar
        currentDate = Calendar.getInstance();
        selectedDate = Calendar.getInstance();
        todayDate = Calendar.getInstance(); // Track today's date
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateCells = new ArrayList<>();
        datesWithExpenses = new ArrayList<>();
        
        // Sample data: dates with expenses (can be replaced with actual data from database)
        // Format: "yyyy-MM-dd" for unique date identification
        Calendar today = Calendar.getInstance();
        
        // Add some sample dates with expenses (today and a few days before)
        datesWithExpenses.add(dateKeyFormat.format(today.getTime()));
        today.add(Calendar.DAY_OF_MONTH, -1);
        datesWithExpenses.add(dateKeyFormat.format(today.getTime()));
        today.add(Calendar.DAY_OF_MONTH, -2);
        datesWithExpenses.add(dateKeyFormat.format(today.getTime()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        GeneralHeaderController.setup(root, this);

        // Initialize calendar
        initializeCalendar(root);
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
        
        // Check if this date has expenses
        String dateKey = dateKeyFormat.format(date.getTime());
        boolean hasExpenses = datesWithExpenses.contains(dateKey);
        
        // Show event indicator if date has expenses
        if (hasExpenses && isCurrentMonth) {
            eventIndicator.setVisibility(View.VISIBLE);
        } else {
            eventIndicator.setVisibility(View.GONE);
        }
        
        // Check if this date is selected or is today
        boolean isSelected = isSameDay(date, selectedDate);
        boolean isToday = isSameDay(date, todayDate) && isCurrentMonth;
        
        // Update appearance based on selection and month
        if (!isCurrentMonth) {
            // Dates from other months: grey text, no background
            tvDate.setBackgroundResource(R.drawable.bg_date_cell);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.third_grey));
            tvDate.setAlpha(0.5f);
        } else if (isSelected) {
            // Selected date: green background with white text
            tvDate.setBackgroundResource(R.drawable.bg_date_cell_selected);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_white));
            tvDate.setAlpha(1.0f);
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
        
        // Set click listener (only for current month dates)
        if (isCurrentMonth) {
            cell.setOnClickListener(v -> {
                selectedDate = (Calendar) date.clone();
                updateCalendar(); // Refresh to show new selection
                // TODO: Update expense data based on selected date
            });
        } else {
            cell.setOnClickListener(null);
            cell.setClickable(false);
        }
        
        return cell;
    }

    // Check if two Calendar instances represent the same day
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

}