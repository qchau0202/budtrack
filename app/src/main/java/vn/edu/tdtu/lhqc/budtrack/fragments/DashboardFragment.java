package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.core.content.ContextCompat;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.widgets.PieChartView;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
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

        return root;
    }

    /**
     * Update category tabs with dynamic data.
     * This method can be called whenever data changes.
     */
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

    /**
     * Format currency amount with VND format
     */
    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setGroupingUsed(true);
        return formatter.format(amount) + " VND";
    }

    /**
     * Data class to hold category information
     * This makes it easy to extend with more fields in the future
     */
    private static class CategoryData {
        float percentage;
        double amount;

        CategoryData(float percentage, double amount) {
            this.percentage = percentage;
            this.amount = amount;
        }
    }
}