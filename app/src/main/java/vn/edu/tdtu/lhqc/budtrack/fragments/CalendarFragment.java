package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView tvMonthYear;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private LinearLayout datesContainer;
    
    private Calendar currentDate;
    private Calendar selectedDate;
    private SimpleDateFormat monthYearFormat;
    private List<View> dateCells;
    private List<Integer> datesWithEvents; // Example: dates that have events

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
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
        
        currentDate = Calendar.getInstance();
        selectedDate = Calendar.getInstance();
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        dateCells = new ArrayList<>();
        datesWithEvents = new ArrayList<>();
        // Example: Add some dates with events (21, 22)
        datesWithEvents.add(1);
        datesWithEvents.add(2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        
        tvMonthYear = root.findViewById(R.id.tv_month_year);
        btnPrevMonth = root.findViewById(R.id.btn_prev_month);
        btnNextMonth = root.findViewById(R.id.btn_next_month);
        datesContainer = root.findViewById(R.id.dates_container);
        
        setupListeners();
        updateCalendar();
        
        return root;
    }
    
    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentDate.add(Calendar.WEEK_OF_YEAR, -1);
            updateCalendar();
        });
        
        btnNextMonth.setOnClickListener(v -> {
            currentDate.add(Calendar.WEEK_OF_YEAR, 1);
            updateCalendar();
        });
    }
    
    private void updateCalendar() {
        // Update month/year display
        tvMonthYear.setText(monthYearFormat.format(currentDate.getTime()));
        
        // Clear existing date cells
        datesContainer.removeAllViews();
        dateCells.clear();
        
        // Get the current week's dates
        Calendar weekStart = (Calendar) currentDate.clone();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        // Create date cells for the week (7 days)
        for (int i = 0; i < 7; i++) {
            Calendar date = (Calendar) weekStart.clone();
            date.add(Calendar.DAY_OF_MONTH, i);
            
            View dateCell = createDateCell(date.get(Calendar.DAY_OF_MONTH), date);
            datesContainer.addView(dateCell);
            dateCells.add(dateCell);
        }
    }
    
    private View createDateCell(int day, Calendar date) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        LinearLayout cell = (LinearLayout) inflater.inflate(R.layout.item_date_cell, datesContainer, false);
        
        TextView tvDate = cell.findViewById(R.id.tv_date);
        View eventIndicator = cell.findViewById(R.id.event_indicator);
        
        tvDate.setText(String.valueOf(day));
        
        // Check if this date is selected
        boolean isSelected = isSameDay(date, selectedDate);
        boolean hasEvent = datesWithEvents.contains(day);
        
        // Update appearance based on selection
        if (isSelected) {
            tvDate.setBackgroundResource(R.drawable.bg_date_cell_selected);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_white));
            if (hasEvent) {
                eventIndicator.setVisibility(View.VISIBLE);
                eventIndicator.setBackgroundResource(R.drawable.bg_event_dot);
            } else {
                eventIndicator.setVisibility(View.GONE);
            }
        } else {
            tvDate.setBackgroundResource(R.drawable.bg_date_cell);
            tvDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
            if (hasEvent) {
                eventIndicator.setVisibility(View.VISIBLE);
                eventIndicator.setBackgroundResource(R.drawable.bg_event_dot_grey);
            } else {
                eventIndicator.setVisibility(View.GONE);
            }
        }
        
        // Set click listener
        cell.setOnClickListener(v -> {
            selectedDate = (Calendar) date.clone();
            updateCalendar(); // Refresh to show new selection
        });
        
        return cell;
    }
    
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}