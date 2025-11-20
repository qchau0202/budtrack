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
import androidx.fragment.app.FragmentTransaction;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.widgets.PieChartView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
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
    private ImageButton btnPrevWeek;
    private ImageButton btnNextWeek;
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

        // Sample data - can be replaced with dynamic data from database/ViewModel
        double totalExpense = 4000000.0; // Total expense amount
        LinkedHashMap<String, CategoryData> categoryData = new LinkedHashMap<>();
        categoryData.put("Transport", new CategoryData(20f, totalExpense * 0.20));
        categoryData.put("Food", new CategoryData(50f, totalExpense * 0.50));
        categoryData.put("Shopping", new CategoryData(30f, totalExpense * 0.30));

        // Initialize pie chart
        PieChartView pieChart = root.findViewById(R.id.pieChart);
        if (pieChart != null) {
            LinkedHashMap<String, Float> pieData = new LinkedHashMap<>();
            for (String key : categoryData.keySet()) {
                pieData.put(key, categoryData.get(key).percentage);
            }

            pieChart.setData(pieData, Arrays.asList(
                    ContextCompat.getColor(requireContext(), R.color.primary_green),
                    ContextCompat.getColor(requireContext(), R.color.primary_yellow),
                    ContextCompat.getColor(requireContext(), R.color.primary_red)
            ));
            float density = getResources().getDisplayMetrics().density;
            pieChart.setRingThicknessPx(12f * density);
            pieChart.setSegmentGapDegrees(14f);
            pieChart.setCenterTexts(getString(R.string.expense), formatCurrency(totalExpense));
        }

        // Update category tabs with amounts and percentages
        updateCategoryTabs(root, categoryData);

        // Initialize calendar
        initializeCalendar(root);
        
        // Setup map button click listener
        ImageButton btnMap = root.findViewById(R.id.btn_map);
        if (btnMap != null) {
            btnMap.setOnClickListener(v -> showMapFragment());
        }
        return root;
    }
    
    private void showMapFragment() {
        MapFragment mapFragment = MapFragment.newInstance();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mapFragment, "MAP_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Initialize the calendar week view
    private void initializeCalendar(View root) {
        tvMonthYear = root.findViewById(R.id.tv_month_year);
        btnPrevWeek = root.findViewById(R.id.btn_prev_week);
        btnNextWeek = root.findViewById(R.id.btn_next_week);
        datesContainer = root.findViewById(R.id.dates_container);
        
        if (tvMonthYear != null && btnPrevWeek != null && btnNextWeek != null && datesContainer != null) {
            setupCalendarListeners();
            updateCalendar();
        }
    }

    // Setup calendar navigation listeners
    
    private void setupCalendarListeners() {
        btnPrevWeek.setOnClickListener(v -> {
            currentDate.add(Calendar.WEEK_OF_YEAR, -1);
            updateCalendar();
        });
        
        btnNextWeek.setOnClickListener(v -> {
            currentDate.add(Calendar.WEEK_OF_YEAR, 1);
            updateCalendar();
        });
    }

    // Update the calendar display with current week
    private void updateCalendar() {
        // Update month/year display
        if (tvMonthYear != null) {
            tvMonthYear.setText(monthYearFormat.format(currentDate.getTime()));
        }
        
        // Clear existing date cells
        if (datesContainer != null) {
            datesContainer.removeAllViews();
            dateCells.clear();
            
            // Get the current week's dates (starting from Monday)
            Calendar weekStart = (Calendar) currentDate.clone();
            int dayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
            weekStart.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
            
            // Create date cells for the week (7 days)
            for (int i = 0; i < 7; i++) {
                Calendar date = (Calendar) weekStart.clone();
                date.add(Calendar.DAY_OF_MONTH, i);
                
                View dateCell = createDateCell(date.get(Calendar.DAY_OF_MONTH), date);
                datesContainer.addView(dateCell);
                dateCells.add(dateCell);
            }
        }
    }

    // Create a date cell view
    private View createDateCell(int day, Calendar date) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        LinearLayout cell = (LinearLayout) inflater.inflate(R.layout.item_date_cell, datesContainer, false);
        
        TextView tvDate = cell.findViewById(R.id.tv_date);
        View eventIndicator = cell.findViewById(R.id.event_indicator);
        
        // Hide the event indicator dot
        eventIndicator.setVisibility(View.GONE);
        
        tvDate.setText(String.valueOf(day));
        
        // Check if this date is selected or is today
        boolean isSelected = isSameDay(date, selectedDate);
        boolean isToday = isSameDay(date, todayDate);
        
        // Update appearance based on selection (selected takes priority over today)
        if (isSelected) {
            // Selected date: green background with white text
            tvDate.setBackgroundResource(R.drawable.bg_date_cell_selected);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_white));
        } else if (isToday) {
            // Today's date: grey background with black text
            tvDate.setBackgroundResource(R.drawable.bg_date_cell_today);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        } else {
            // Other dates: transparent background with black text
            tvDate.setBackgroundResource(R.drawable.bg_date_cell);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        }
        
        // Set click listener
        cell.setOnClickListener(v -> {
            selectedDate = (Calendar) date.clone();
            updateCalendar(); // Refresh to show new selection
            // TODO: Update expense data based on selected date
        });
        
        return cell;
    }

    // Check if two Calendar instances represent the same day
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // Update category tabs with dynamic data. This method can be called whenever data changes.
    private void updateCategoryTabs(View root, LinkedHashMap<String, CategoryData> categoryData) {
        // Transport
        TextView tvAmountTransport = root.findViewById(R.id.tv_amount_transport);
        TextView tvPercentTransport = root.findViewById(R.id.tv_percent_transport);
        if (tvAmountTransport != null && tvPercentTransport != null && categoryData.containsKey("Transport")) {
            CategoryData transport = categoryData.get("Transport");
            tvAmountTransport.setText(formatCurrency(transport.amount));
            tvPercentTransport.setText(String.format(Locale.getDefault(), "%.0f%%", transport.percentage));
        }

        // Food
        TextView tvAmountFood = root.findViewById(R.id.tv_amount_food);
        TextView tvPercentFood = root.findViewById(R.id.tv_percent_food);
        if (tvAmountFood != null && tvPercentFood != null && categoryData.containsKey("Food")) {
            CategoryData food = categoryData.get("Food");
            tvAmountFood.setText(formatCurrency(food.amount));
            tvPercentFood.setText(String.format(Locale.getDefault(), "%.0f%%", food.percentage));
        }

        // Shopping
        TextView tvAmountShopping = root.findViewById(R.id.tv_amount_shopping);
        TextView tvPercentShopping = root.findViewById(R.id.tv_percent_shopping);
        if (tvAmountShopping != null && tvPercentShopping != null && categoryData.containsKey("Shopping")) {
            CategoryData shopping = categoryData.get("Shopping");
            tvAmountShopping.setText(formatCurrency(shopping.amount));
            tvPercentShopping.setText(String.format(Locale.getDefault(), "%.0f%%", shopping.percentage));
        }
    }

    // Format currency amount with VND format
    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setGroupingUsed(true);
        return formatter.format(amount) + " VND";
    }

    // Data class to hold category information This makes it easy to extend with more fields in the future
    private static class CategoryData {
        float percentage;
        double amount;

        CategoryData(float percentage, double amount) {
            this.percentage = percentage;
            this.amount = amount;
        }
    }
}