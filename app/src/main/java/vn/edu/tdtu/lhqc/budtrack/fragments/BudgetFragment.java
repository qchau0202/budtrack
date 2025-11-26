package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.text.NumberFormat;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;

/**
 * Budget Fragment - Displays budget information with budgets
 */
public class BudgetFragment extends Fragment {

    // Mock data - budgets: Daily, Personal, Others
    private static class BudgetData {
        String budgetName;
        double budgetAmount;
        double spentAmount;
        int colorResId;

        BudgetData(String budgetName, double budgetAmount, double spentAmount, int colorResId) {
            this.budgetName = budgetName;
            this.budgetAmount = budgetAmount;
            this.spentAmount = spentAmount;
            this.colorResId = colorResId;
        }

        double getRemaining() {
            return budgetAmount - spentAmount;
        }

        int getPercentage() {
            if (budgetAmount == 0) return 0;
            return (int) Math.round((spentAmount / budgetAmount) * 100);
        }

        boolean isOverspending() {
            return spentAmount > budgetAmount;
        }
    }

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

        setupBudgetData(root);

        return root;
    }

    private void setupBudgetData(View root) {
        // Mock data - budgets: Daily, Personal, Others
        // Total budget: 25,000,000 VND
        double totalBudget = 25000000.0;
        
        // Daily: 10,000,000 budget, 1,900,000 spent (19%)
        BudgetData daily = new BudgetData(
            getString(R.string.budget_daily),
            10000000.0,
            1900000.0,
            R.color.primary_green
        );

        // Personal: 5,000,000 budget, 6,150,000 spent (123% - overspending)
        BudgetData personal = new BudgetData(
            getString(R.string.budget_personal),
            5000000.0,
            6150000.0,
            R.color.primary_yellow
        );

        // Others: 1,000,000 budget, 0 spent (0%)
        BudgetData others = new BudgetData(
            getString(R.string.budget_others),
            1000000.0,
            0.0,
            R.color.primary_red
        );

        // Calculate total spent
        double totalSpent = daily.spentAmount + personal.spentAmount + others.spentAmount;

        // Setup Total Budget
        setupTotalBudget(root, totalBudget, totalSpent);

        // Setup budget cards
        setupBudgetCard(root, daily, R.id.card_daily_budget);
        setupBudgetCard(root, personal, R.id.card_personal_budget);
        setupBudgetCard(root, others, R.id.card_others_budget);
    }

    private void setupTotalBudget(View root, double totalBudget, double totalSpent) {
        TextView tvAmount = root.findViewById(R.id.tv_total_budget_amount);
        TextView tvPercent = root.findViewById(R.id.tv_total_budget_percent);
        ProgressBar progressBar = root.findViewById(R.id.progress_total_budget);
        TextView tvSpent = root.findViewById(R.id.tv_total_spent);
        TextView tvLeft = root.findViewById(R.id.tv_total_left);

        if (tvAmount != null) {
            tvAmount.setText(formatCurrency(totalBudget));
        }

        double remaining = totalBudget - totalSpent;
        int percentage = totalBudget > 0 ? (int) Math.round((totalSpent / totalBudget) * 100) : 0;

        if (tvPercent != null) {
            tvPercent.setText(percentage + "%");
        }

        if (progressBar != null) {
            progressBar.setMax(100);
            progressBar.setProgress(Math.min(percentage, 100));
        }

        if (tvSpent != null) {
            tvSpent.setText("-" + formatCurrency(totalSpent) + " " + getString(R.string.spent));
        }

        if (tvLeft != null) {
            if (remaining >= 0) {
                tvLeft.setText(formatCurrency(remaining) + " " + getString(R.string.left));
                tvLeft.setTextColor(getResources().getColor(R.color.primary_black, null));
            } else {
                tvLeft.setText(formatCurrency(Math.abs(remaining)) + " " + getString(R.string.overspending));
                tvLeft.setTextColor(getResources().getColor(R.color.primary_red, null));
            }
        }
    }

    private void setupBudgetCard(View root, BudgetData data, int cardId) {
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
            tvLabel.setText(data.budgetName);
            tvLabel.setTextColor(getResources().getColor(data.colorResId, null));
        }

        if (tvAmount != null) {
            tvAmount.setText(formatCurrency(data.budgetAmount));
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
            setProgressBarColor(progressBar, data.colorResId);
            progressBar.setMax(100);
            // For overspending, show 100% filled
            progressBar.setProgress(Math.min(percentage, 100));
        }

        if (tvSpent != null) {
            tvSpent.setText("-" + formatCurrency(data.spentAmount) + " " + getString(R.string.spent));
        }

        if (tvLeft != null) {
            double remaining = data.getRemaining();
            if (remaining >= 0) {
                tvLeft.setText(formatCurrency(remaining) + " " + getString(R.string.left));
                tvLeft.setTextColor(getResources().getColor(R.color.primary_black, null));
            } else {
                tvLeft.setText(formatCurrency(Math.abs(remaining)) + " " + getString(R.string.overspending));
                tvLeft.setTextColor(getResources().getColor(R.color.primary_red, null));
            }
        }

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                Toast.makeText(requireContext(), data.budgetName + " menu", Toast.LENGTH_SHORT).show();
                // TODO: Implement budget menu functionality
            });
        }
    }

    private void setProgressBarColor(ProgressBar progressBar, int colorResId) {
        // Convert 4dp to pixels for corner radius
        float density = getResources().getDisplayMetrics().density;
        float cornerRadius = 4 * density;
        
        // Create background shape
        ShapeDrawable backgroundShape = new ShapeDrawable();
        backgroundShape.setShape(new RoundRectShape(
            new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, 
                       cornerRadius, cornerRadius, cornerRadius, cornerRadius}, null, null));
        backgroundShape.getPaint().setColor(ContextCompat.getColor(requireContext(), R.color.secondary_grey));
        
        // Create progress shape with the specified color
        ShapeDrawable progressShape = new ShapeDrawable();
        progressShape.setShape(new RoundRectShape(
            new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, 
                       cornerRadius, cornerRadius, cornerRadius, cornerRadius}, null, null));
        progressShape.getPaint().setColor(ContextCompat.getColor(requireContext(), colorResId));
        
        // Create clip drawable for progress
        ClipDrawable clipDrawable = new ClipDrawable(progressShape, 
            android.view.Gravity.START, ClipDrawable.HORIZONTAL);
        
        // Create layer drawable
        LayerDrawable layerDrawable = new LayerDrawable(
            new android.graphics.drawable.Drawable[]{backgroundShape, clipDrawable});
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);
        
        progressBar.setProgressDrawable(layerDrawable);
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setGroupingUsed(true);
        return formatter.format(amount) + " VND";
    }
}
