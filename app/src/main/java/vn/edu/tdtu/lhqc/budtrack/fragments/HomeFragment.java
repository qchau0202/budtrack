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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.mockdata.BudgetDisplayData;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockBudgetData;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockBudgetHelper;
import vn.edu.tdtu.lhqc.budtrack.models.Budget;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.BalanceController;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.TabStyleUtils;
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

        GeneralHeaderController.setup(root, this);

        // Setup balance view
        setupBalanceView(root);
        
        // Initialize pie chart
        setupPieChart(root);

        // Setup analytics tabs inside included view
        setupAnalyticsTabs(root);
        
        return root;
    }
    
    private void setupBalanceView(View root) {
        TextView tvBalance = root.findViewById(R.id.tv_total_balance_amount);
        ImageButton btnVisibility = root.findViewById(R.id.btn_visibility);
        MaterialButton btnViewWallet = root.findViewById(R.id.btn_view_wallet);

        if (tvBalance != null && btnVisibility != null) {
            // Preserve the original unmasked balance text
            final String originalBalanceText = tvBalance.getText() != null ? tvBalance.getText().toString() : "";
            boolean hidden = BalanceController.isHidden(requireContext());
            tvBalance.setText(BalanceController.formatDisplay(originalBalanceText, hidden));
            btnVisibility.setImageResource(hidden ? R.drawable.ic_visibility_off_24dp : R.drawable.ic_visibility_24dp);

            btnVisibility.setOnClickListener(v -> {
                boolean nowHidden = BalanceController.toggleHidden(requireContext());
                tvBalance.setText(BalanceController.formatDisplay(originalBalanceText, nowHidden));
                btnVisibility.setImageResource(nowHidden ? R.drawable.ic_visibility_off_24dp : R.drawable.ic_visibility_24dp);
            });
        }

        // Setup view wallet button click listener
        if (btnViewWallet != null) {
            btnViewWallet.setOnClickListener(v -> showWalletFragment());
        }
    }
    
    private void setupPieChart(View root) {
        // Load budgets from mockdata
        List<Budget> budgets = MockBudgetData.getSampleBudgets();
        
        // Create BudgetDisplayData with spent amounts
        LinkedHashMap<String, BudgetDisplayData> budgetData = new LinkedHashMap<>();
        long totalSpent = 0;
        
        for (vn.edu.tdtu.lhqc.budtrack.models.Budget budget : budgets) {
            long spentAmount = MockBudgetHelper.getMockSpentAmount(budget);
            BudgetDisplayData displayData = new BudgetDisplayData(budget, spentAmount);
            budgetData.put(budget.getName(), displayData);
            totalSpent += spentAmount;
        }

        // Initialize pie chart
        PieChartView pieChart = root.findViewById(R.id.pieChart);
        if (pieChart != null && totalSpent > 0) {
            LinkedHashMap<String, Float> pieData = new LinkedHashMap<>();
            for (String key : budgetData.keySet()) {
                BudgetDisplayData budget = budgetData.get(key);
                float percentage = totalSpent > 0 ? (float) ((budget.getSpentAmount() / (double) totalSpent) * 100) : 0;
                pieData.put(key, percentage);
            }

            pieChart.setData(pieData, Arrays.asList(
                    ContextCompat.getColor(requireContext(), R.color.primary_green),
                    ContextCompat.getColor(requireContext(), R.color.primary_yellow),
                    ContextCompat.getColor(requireContext(), R.color.primary_red)
            ));
            float density = getResources().getDisplayMetrics().density;
            pieChart.setRingThicknessPx(12f * density);
            pieChart.setSegmentGapDegrees(14f);
            pieChart.setCenterTexts(getString(R.string.expense), CurrencyUtils.formatCurrency(totalSpent));
        } else if (pieChart != null) {
            // If no spending, show empty chart
            pieChart.setData(new LinkedHashMap<>(), Arrays.asList());
            pieChart.setCenterTexts(getString(R.string.expense), CurrencyUtils.formatCurrency(0));
        }

        // Update budget tabs with amounts and percentages
        updateBudgetTabs(root, budgetData, totalSpent);
    }

    private void setupAnalyticsTabs(View root) {
        View analyticsCard = root.findViewById(R.id.card_weekly_expenses);
        if (analyticsCard == null) {
            return;
        }

        MaterialButton tabIncome = analyticsCard.findViewById(R.id.tab_income);
        MaterialButton tabExpenses = analyticsCard.findViewById(R.id.tab_expenses);
        TextView title = analyticsCard.findViewById(R.id.tv_title);

        if (tabIncome == null || tabExpenses == null) {
            return;
        }

        tabIncome.setOnClickListener(v -> selectAnalyticsTab(true, tabIncome, tabExpenses, title));
        tabExpenses.setOnClickListener(v -> selectAnalyticsTab(false, tabIncome, tabExpenses, title));
        selectAnalyticsTab(false, tabIncome, tabExpenses, title);
    }

    private void selectAnalyticsTab(boolean incomeSelected,
                                    MaterialButton tabIncome,
                                    MaterialButton tabExpenses,
                                    TextView title) {
        applyAnalyticsTabStyle(tabIncome, incomeSelected);
        applyAnalyticsTabStyle(tabExpenses, !incomeSelected);
        if (title != null) {
            title.setText(incomeSelected ? R.string.total_income : R.string.total_expenses);
        }
    }

    private void applyAnalyticsTabStyle(MaterialButton button, boolean selected) {
        TabStyleUtils.applyStyle(button.getContext(), button, selected);
    }
    
    // Update budget tabs with dynamic data. This method can be called whenever data changes.
    private void updateBudgetTabs(View root, LinkedHashMap<String, BudgetDisplayData> budgetData, long totalSpent) {
        // Daily Budget (maps to Transport tab in layout)
        TextView tvCategoryTransport = root.findViewById(R.id.tv_category_transport);
        TextView tvAmountTransport = root.findViewById(R.id.tv_amount_transport);
        TextView tvPercentTransport = root.findViewById(R.id.tv_percent_transport);
        if (budgetData.containsKey("Daily")) {
            BudgetDisplayData daily = budgetData.get("Daily");
            if (tvCategoryTransport != null) {
                tvCategoryTransport.setText(daily.getName());
            }
            if (tvAmountTransport != null) {
                tvAmountTransport.setText(CurrencyUtils.formatCurrency(daily.getSpentAmount()));
            }
            if (tvPercentTransport != null && totalSpent > 0) {
                float percentage = (float) ((daily.getSpentAmount() / (double) totalSpent) * 100);
                tvPercentTransport.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
            }
        }

        // Personal Budget (maps to Food tab in layout)
        TextView tvCategoryFood = root.findViewById(R.id.tv_category_food);
        TextView tvAmountFood = root.findViewById(R.id.tv_amount_food);
        TextView tvPercentFood = root.findViewById(R.id.tv_percent_food);
        if (budgetData.containsKey("Personal")) {
            BudgetDisplayData personal = budgetData.get("Personal");
            if (tvCategoryFood != null) {
                tvCategoryFood.setText(personal.getName());
            }
            if (tvAmountFood != null) {
                tvAmountFood.setText(CurrencyUtils.formatCurrency(personal.getSpentAmount()));
            }
            if (tvPercentFood != null && totalSpent > 0) {
                float percentage = (float) ((personal.getSpentAmount() / (double) totalSpent) * 100);
                tvPercentFood.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
            }
        }

        // Others Budget (maps to Shopping tab in layout)
        TextView tvCategoryShopping = root.findViewById(R.id.tv_category_shopping);
        TextView tvAmountShopping = root.findViewById(R.id.tv_amount_shopping);
        TextView tvPercentShopping = root.findViewById(R.id.tv_percent_shopping);
        if (budgetData.containsKey("Others")) {
            BudgetDisplayData others = budgetData.get("Others");
            if (tvCategoryShopping != null) {
                tvCategoryShopping.setText(others.getName());
            }
            if (tvAmountShopping != null) {
                tvAmountShopping.setText(CurrencyUtils.formatCurrency(others.getSpentAmount()));
            }
            if (tvPercentShopping != null && totalSpent > 0) {
                float percentage = (float) ((others.getSpentAmount() / (double) totalSpent) * 100);
                tvPercentShopping.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
            }
        }
    }


    
    private void showWalletFragment() {
        WalletFragment walletFragment = WalletFragment.newInstance();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, walletFragment, "WALLET_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }
}