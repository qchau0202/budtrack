package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.mockdata.BudgetDisplayData;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockBudgetData;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockBudgetHelper;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.ProgressBarUtils;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;

/**
 * Budget Fragment - Displays budget information with budgets
 */
public class BudgetFragment extends Fragment {

    public BudgetFragment() {
        // Required empty public constructor
    }

    public static BudgetFragment newInstance() {
        return new BudgetFragment();
    }

    public static BudgetFragment newInstance(String param1, String param2) {
        BudgetFragment fragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_budget, container, false);

        GeneralHeaderController.setup(root, this);
        setupBudgetData(root);

        return root;
    }

    private void setupBudgetData(View root) {
        // Load budgets from mockdata
        List<vn.edu.tdtu.lhqc.budtrack.models.Budget> budgets = MockBudgetData.getSampleBudgets();
        
        // Create BudgetDisplayData with spent amounts
        List<BudgetDisplayData> budgetDisplayList = new ArrayList<>();
        long totalBudget = 0;
        long totalSpent = 0;
        
        for (vn.edu.tdtu.lhqc.budtrack.models.Budget budget : budgets) {
            long spentAmount = MockBudgetHelper.getMockSpentAmount(budget);
            BudgetDisplayData displayData = new BudgetDisplayData(budget, spentAmount);
            budgetDisplayList.add(displayData);
            
            totalBudget += budget.getBudgetAmount();
            totalSpent += spentAmount;
        }

        // Setup Total Budget
        setupTotalBudget(root, totalBudget, totalSpent);

        // Setup budget cards - match by name
        for (BudgetDisplayData displayData : budgetDisplayList) {
            String budgetName = displayData.getName();
            int cardId = -1;
            
            if (budgetName.equals(getString(R.string.budget_daily))) {
                cardId = R.id.card_daily_budget;
            } else if (budgetName.equals(getString(R.string.budget_personal))) {
                cardId = R.id.card_personal_budget;
            } else if (budgetName.equals(getString(R.string.budget_others))) {
                cardId = R.id.card_others_budget;
            }
            
            if (cardId != -1) {
                setupBudgetCard(root, displayData, cardId);
            }
        }
    }

    private void setupTotalBudget(View root, long totalBudget, long totalSpent) {
        TextView tvAmount = root.findViewById(R.id.tv_total_budget_amount);
        TextView tvPercent = root.findViewById(R.id.tv_total_budget_percent);
        ProgressBar progressBar = root.findViewById(R.id.progress_total_budget);
        TextView tvSpent = root.findViewById(R.id.tv_total_spent);
        TextView tvLeft = root.findViewById(R.id.tv_total_left);

        if (tvAmount != null) {
            tvAmount.setText(CurrencyUtils.formatCurrency(totalBudget));
        }

        long remaining = totalBudget - totalSpent;
        int percentage = totalBudget > 0 ? (int) Math.round(((double) totalSpent / totalBudget) * 100) : 0;

        if (tvPercent != null) {
            tvPercent.setText(percentage + "%");
        }

        if (progressBar != null) {
            progressBar.setMax(100);
            progressBar.setProgress(Math.min(percentage, 100));
        }

        if (tvSpent != null) {
            tvSpent.setText("-" + CurrencyUtils.formatCurrency(totalSpent) + " " + getString(R.string.spent));
        }

        if (tvLeft != null) {
            if (remaining >= 0) {
                tvLeft.setText(CurrencyUtils.formatCurrency(remaining) + " " + getString(R.string.left));
                tvLeft.setTextColor(getResources().getColor(R.color.primary_black, null));
            } else {
                tvLeft.setText(CurrencyUtils.formatCurrency(Math.abs(remaining)) + " " + getString(R.string.overspending));
                tvLeft.setTextColor(getResources().getColor(R.color.primary_red, null));
            }
        }
    }

    private void setupBudgetCard(View root, BudgetDisplayData data, int cardId) {
        View card = root.findViewById(cardId);
        if (card == null) return;

        // Find views using generic IDs from view_budget_card.xml
        TextView tvLabel = card.findViewById(R.id.tv_budget_label);
        TextView tvAmount = card.findViewById(R.id.tv_budget_amount);
        TextView tvPercent = card.findViewById(R.id.tv_budget_percent);
        ProgressBar progressBar = card.findViewById(R.id.progress_budget);
        TextView tvSpent = card.findViewById(R.id.tv_budget_spent);
        TextView tvLeft = card.findViewById(R.id.tv_budget_left);
        ImageButton btnMenu = card.findViewById(R.id.btn_budget_menu);

        // Set label text and color
        if (tvLabel != null) {
            tvLabel.setText(data.getName());
            tvLabel.setTextColor(getResources().getColor(data.getColorResId(), null));
        }

        if (tvAmount != null) {
            tvAmount.setText(CurrencyUtils.formatCurrency(data.getBudgetAmount()));
        }

        int percentage = data.getPercentage();
        boolean overspending = data.isOverspending();

        if (tvPercent != null) {
            if (overspending) {
                tvPercent.setText("-" + percentage + "%");
                tvPercent.setTextColor(getResources().getColor(R.color.primary_red, null));
            } else {
                tvPercent.setText(percentage + "%");
                tvPercent.setTextColor(getResources().getColor(R.color.primary_black, null));
            }
        }

        if (progressBar != null) {
            ProgressBarUtils.setProgressBarColor(requireContext(), progressBar, data.getColorResId());
            progressBar.setMax(100);
            // For overspending, show 100% filled
            progressBar.setProgress(Math.min(percentage, 100));
        }

        if (tvSpent != null) {
            tvSpent.setText("-" + CurrencyUtils.formatCurrency(data.getSpentAmount()) + " " + getString(R.string.spent));
        }

        if (tvLeft != null) {
            long remaining = data.getRemaining();
            if (remaining >= 0) {
                tvLeft.setText(CurrencyUtils.formatCurrency(remaining) + " " + getString(R.string.left));
                tvLeft.setTextColor(getResources().getColor(R.color.primary_black, null));
            } else {
                tvLeft.setText(CurrencyUtils.formatCurrency(Math.abs(remaining)) + " " + getString(R.string.overspending));
                tvLeft.setTextColor(getResources().getColor(R.color.primary_red, null));
            }
        }

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                Toast.makeText(requireContext(), data.getName() + " menu", Toast.LENGTH_SHORT).show();
                // TODO: Implement budget menu functionality
            });
        }

        // Make the card clickable to navigate to detail
        if (card != null) {
            card.setOnClickListener(v -> {
                BudgetDetailFragment detailFragment = BudgetDetailFragment.newInstance(
                    data.getName(),
                    data.getBudgetAmount(),
                    data.getSpentAmount(),
                    data.getColorResId()
                );
                
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment, "BUDGET_DETAIL_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
            });
            card.setClickable(true);
            card.setFocusable(true);
        }
    }

}
