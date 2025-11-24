package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.services.BalanceService;
import vn.edu.tdtu.lhqc.budtrack.widgets.PieChartView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Setup map button click listener
        ImageButton btnMap = root.findViewById(R.id.btn_map);
        if (btnMap != null) {
            btnMap.setOnClickListener(v -> showMapFragment());
        }
        
        // Setup balance view
        setupBalanceView(root);
        
        // Setup analytics view
        setupAnalyticsView(root);
        
        // Initialize pie chart
        setupPieChart(root);
        
        return root;
    }
    
    private void setupBalanceView(View root) {
        TextView tvBalance = root.findViewById(R.id.tv_total_balance_amount);
        ImageButton btnVisibility = root.findViewById(R.id.btn_visibility);

        if (tvBalance != null && btnVisibility != null) {
            // Preserve the original unmasked balance text
            final String originalBalanceText = tvBalance.getText() != null ? tvBalance.getText().toString() : "";
            boolean hidden = BalanceService.isHidden(requireContext());
            tvBalance.setText(BalanceService.formatDisplay(originalBalanceText, hidden));
            btnVisibility.setImageResource(hidden ? R.drawable.ic_visibility_off_24dp : R.drawable.ic_visibility_24dp);

            btnVisibility.setOnClickListener(v -> {
                boolean nowHidden = BalanceService.toggleHidden(requireContext());
                tvBalance.setText(BalanceService.formatDisplay(originalBalanceText, nowHidden));
                btnVisibility.setImageResource(nowHidden ? R.drawable.ic_visibility_off_24dp : R.drawable.ic_visibility_24dp);
            });
        }
    }
    
    private void setupAnalyticsView(View root) {
        // Tabs inside analytics card control title only
        MaterialButton tabIncome = root.findViewById(R.id.tab_income);
        MaterialButton tabExpenses = root.findViewById(R.id.tab_expenses);
        TextView weeklyTitle = root.findViewById(R.id.tv_weekly_title);

        if (tabIncome != null && tabExpenses != null) {
            tabIncome.setOnClickListener(v -> selectAnalyticsTab(true, tabIncome, tabExpenses, weeklyTitle));
            tabExpenses.setOnClickListener(v -> selectAnalyticsTab(false, tabIncome, tabExpenses, weeklyTitle));
            // default selection
            selectAnalyticsTab(true, tabIncome, tabExpenses, weeklyTitle);
        }
    }
    
    private void selectAnalyticsTab(boolean incomeSelected,
                                   MaterialButton tabIncome,
                                   MaterialButton tabExpenses,
                                   TextView weeklyTitle) {
        if (incomeSelected) {
            tabIncome.setBackgroundTintList(getResources().getColorStateList(R.color.primary_green));
            tabIncome.setTextColor(getResources().getColor(R.color.primary_white));
            tabExpenses.setBackgroundTintList(getResources().getColorStateList(R.color.secondary_grey));
            tabExpenses.setTextColor(getResources().getColor(R.color.primary_black));
            if (weeklyTitle != null) weeklyTitle.setText(R.string.total_income);
        } else {
            tabExpenses.setBackgroundTintList(getResources().getColorStateList(R.color.primary_green));
            tabExpenses.setTextColor(getResources().getColor(R.color.primary_white));
            tabIncome.setBackgroundTintList(getResources().getColorStateList(R.color.secondary_grey));
            tabIncome.setTextColor(getResources().getColor(R.color.primary_black));
            if (weeklyTitle != null) weeklyTitle.setText(R.string.total_expenses);
        }
    }
    
    private void setupPieChart(View root) {
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
    
    private void showMapFragment() {
        MapFragment mapFragment = MapFragment.newInstance();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mapFragment, "MAP_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}