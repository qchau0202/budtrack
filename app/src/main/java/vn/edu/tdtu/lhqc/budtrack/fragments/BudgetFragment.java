package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCalculator;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetManager;
import vn.edu.tdtu.lhqc.budtrack.models.Budget;
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
        
        // Listen for transaction creation to refresh budget data immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED,
            this,
            (requestKey, result) -> {
                if (TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED.equals(requestKey)) {
                    // Refresh budget data when a transaction is created (to update spent amounts)
                    if (getView() != null) {
                        setupBudgetData(getView());
                    }
                }
            }
        );
    }

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_budget, container, false);

        GeneralHeaderController.setup(rootView, this);
        setupAddBudgetButton(rootView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always refresh when fragment becomes visible
        if (rootView != null) {
            setupBudgetData(rootView);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listener
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED);
    }
    
    private void setupAddBudgetButton(View root) {
        View btnAddBudget = root.findViewById(R.id.btn_add_budget);
        if (btnAddBudget != null) {
            btnAddBudget.setOnClickListener(v -> {
                BudgetCreateFragment budgetCreateFragment = new BudgetCreateFragment();
                budgetCreateFragment.show(getParentFragmentManager(), BudgetCreateFragment.TAG);
            });
        }
        
        // Set up Fragment Result listener for budget creation
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            BudgetCreateFragment.RESULT_KEY,
            this,
            (requestKey, result) -> {
                if (BudgetCreateFragment.RESULT_KEY.equals(requestKey)) {
                    String budgetName = result.getString(BudgetCreateFragment.RESULT_BUDGET_NAME);
                    long budgetAmount = result.getLong(BudgetCreateFragment.RESULT_BUDGET_AMOUNT);
                    int colorResId = result.getInt(BudgetCreateFragment.RESULT_BUDGET_COLOR);
                    String period = result.getString(BudgetCreateFragment.RESULT_BUDGET_PERIOD);
                    long[] categoryIds = result.getLongArray(BudgetCreateFragment.RESULT_BUDGET_CATEGORIES);
                    
                    // Budget is already saved by BudgetCreateFragment
                    // Just refresh the budget list
        setupBudgetData(root);
                }
            }
        );
    }

    private void setupBudgetData(View root) {
        // Load budgets from BudgetManager
        List<Budget> budgets = BudgetManager.getBudgets(requireContext());
        
        // Calculate total budget and spent
        long totalBudget = 0;
        long totalSpent = 0;
        
        for (Budget budget : budgets) {
            long spentAmount = BudgetCalculator.calculateSpentAmount(requireContext(), budget);
            totalBudget += budget.getBudgetAmount();
            totalSpent += spentAmount;
        }

        // Setup Total Budget
        setupTotalBudget(root, totalBudget, totalSpent);

        // Remove old hardcoded budget cards
        LinearLayout budgetContent = root.findViewById(R.id.budget_content);
        if (budgetContent != null) {
            // Remove hardcoded budget cards (they have specific IDs)
            View cardDaily = root.findViewById(R.id.card_daily_budget);
            View cardPersonal = root.findViewById(R.id.card_personal_budget);
            View cardOthers = root.findViewById(R.id.card_others_budget);
            
            if (cardDaily != null && cardDaily.getParent() != null) {
                ((ViewGroup) cardDaily.getParent()).removeView(cardDaily);
            }
            if (cardPersonal != null && cardPersonal.getParent() != null) {
                ((ViewGroup) cardPersonal.getParent()).removeView(cardPersonal);
            }
            if (cardOthers != null && cardOthers.getParent() != null) {
                ((ViewGroup) cardOthers.getParent()).removeView(cardOthers);
            }
        }

        // Dynamically create budget cards
        LinearLayout budgetContainer = root.findViewById(R.id.budget_content);
        if (budgetContainer != null) {
            // Find the add budget button to insert cards after it
            View addBudgetButton = root.findViewById(R.id.btn_add_budget);
            int insertIndex = budgetContainer.indexOfChild(addBudgetButton);
            
            // Insert budget cards after the add button
            for (Budget budget : budgets) {
                long spentAmount = BudgetCalculator.calculateSpentAmount(requireContext(), budget);
                View budgetCard = createBudgetCard(budget, spentAmount, budgetContainer);
                if (insertIndex >= 0) {
                    // Insert after the add button (insertIndex + 1)
                    budgetContainer.addView(budgetCard, insertIndex + 1);
                    insertIndex++; // Update index for next card
                } else {
                    budgetContainer.addView(budgetCard);
                }
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

    private View createBudgetCard(Budget budget, long spentAmount, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        // Inflate with parent to preserve layout parameters from XML
        View card = inflater.inflate(R.layout.view_budget_card, parent, false);

        // Find views
        TextView tvLabel = card.findViewById(R.id.tv_budget_label);
        TextView tvAmount = card.findViewById(R.id.tv_budget_amount);
        TextView tvPercent = card.findViewById(R.id.tv_budget_percent);
        ProgressBar progressBar = card.findViewById(R.id.progress_budget);
        TextView tvSpent = card.findViewById(R.id.tv_budget_spent);
        TextView tvLeft = card.findViewById(R.id.tv_budget_left);
        ImageButton btnMenu = card.findViewById(R.id.btn_budget_menu);

        // Get color (custom or resource)
        int colorValue = 0;
        if (budget.getCustomColor() != null) {
            colorValue = budget.getCustomColor();
        } else if (budget.getColorResId() != 0) {
            colorValue = ContextCompat.getColor(requireContext(), budget.getColorResId());
        }

        // Set label text and color
        if (tvLabel != null) {
            tvLabel.setText(budget.getName());
            if (colorValue != 0) {
                tvLabel.setTextColor(colorValue);
            }
        }

        if (tvAmount != null) {
            tvAmount.setText(CurrencyUtils.formatCurrency(budget.getBudgetAmount()));
        }

        long remaining = budget.getBudgetAmount() - spentAmount;
        int percentage = budget.getBudgetAmount() > 0 
            ? (int) Math.round(((double) spentAmount / budget.getBudgetAmount()) * 100) 
            : 0;
        boolean overspending = spentAmount > budget.getBudgetAmount();

        if (tvPercent != null) {
            if (overspending) {
                int overspendPercent = budget.getBudgetAmount() > 0
                    ? (int) Math.round(((double) (spentAmount - budget.getBudgetAmount()) / budget.getBudgetAmount()) * 100)
                    : 0;
                tvPercent.setText("+" + overspendPercent + "%");
                tvPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_red));
            } else {
                tvPercent.setText(percentage + "%");
                tvPercent.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
            }
        }

        if (progressBar != null) {
            if (budget.getCustomColor() != null) {
                // For custom colors, set tint directly
                progressBar.getProgressDrawable().setColorFilter(
                    budget.getCustomColor(), 
                    android.graphics.PorterDuff.Mode.SRC_IN);
            } else if (budget.getColorResId() != 0) {
                ProgressBarUtils.setProgressBarColor(requireContext(), progressBar, budget.getColorResId());
            }
            progressBar.setMax(100);
            progressBar.setProgress(Math.min(percentage, 100));
        }

        if (tvSpent != null) {
            tvSpent.setText("-" + CurrencyUtils.formatCurrency(spentAmount) + " " + getString(R.string.spent));
        }

        if (tvLeft != null) {
            if (remaining >= 0) {
                tvLeft.setText(CurrencyUtils.formatCurrency(remaining) + " " + getString(R.string.left));
                tvLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
            } else {
                tvLeft.setText(CurrencyUtils.formatCurrency(Math.abs(remaining)) + " " + getString(R.string.overspending));
                tvLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_red));
            }
        }

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                Toast.makeText(requireContext(), budget.getName() + " menu", Toast.LENGTH_SHORT).show();
                // TODO: Implement budget menu functionality (edit/delete)
            });
        }

        // Make the card clickable to navigate to detail
            card.setOnClickListener(v -> {
            int colorResId = budget.getCustomColor() != null ? 0 : budget.getColorResId();
                BudgetDetailFragment detailFragment = BudgetDetailFragment.newInstance(
                budget.getName(),
                budget.getBudgetAmount(),
                spentAmount,
                colorResId
                );
                
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment, "BUDGET_DETAIL_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
            });
            card.setClickable(true);
            card.setFocusable(true);

        return card;
    }

}
