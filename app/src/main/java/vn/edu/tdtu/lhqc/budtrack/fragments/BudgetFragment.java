package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;
import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCalculator;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCategoryManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.category.CategoryManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;
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
        
        // Set up SharedPreferences listener for currency changes (more reliable than FragmentResult)
        currencyPreferenceListener = (sharedPrefs, key) -> {
            if (SettingsHandler.KEY_CURRENCY.equals(key)) {
                // Currency changed - refresh UI immediately
                if (rootView != null && isAdded() && !isDetached()) {
                    rootView.post(() -> setupBudgetData(rootView));
                }
            }
        };
    }
    
    private android.content.SharedPreferences.OnSharedPreferenceChangeListener currencyPreferenceListener;

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
        // Register currency preference change listener
        if (currencyPreferenceListener != null) {
            SettingsHandler.getPrefs(requireContext()).registerOnSharedPreferenceChangeListener(currencyPreferenceListener);
        }
        // Always refresh when fragment becomes visible
        if (rootView != null) {
            setupBudgetData(rootView);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Unregister currency preference change listener
        if (currencyPreferenceListener != null) {
            SettingsHandler.getPrefs(requireContext()).unregisterOnSharedPreferenceChangeListener(currencyPreferenceListener);
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listeners
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED);
        requireActivity().getSupportFragmentManager().clearFragmentResultListener("currency_changed");
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
                    // Budget is already saved by BudgetCreateFragment
                    // Just refresh the budget list
        setupBudgetData(root);
                }
            }
        );
        
        // Set up Fragment Result listener for budget updates
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            BudgetCreateFragment.RESULT_KEY_UPDATED,
            this,
            (requestKey, result) -> {
                if (BudgetCreateFragment.RESULT_KEY_UPDATED.equals(requestKey)) {
                    // Budget is already updated by BudgetCreateFragment
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

        // Remove old budget cards (both hardcoded and dynamically created)
        LinearLayout budgetContainer = root.findViewById(R.id.budget_content);
        if (budgetContainer != null) { 
            // Remove all dynamically created budget cards (they have a specific tag)
            List<View> viewsToRemove = new ArrayList<>();
            for (int i = 0; i < budgetContainer.getChildCount(); i++) {
                View child = budgetContainer.getChildAt(i);
                // Check if this is a dynamically created budget card by checking for the tag
                if (child.getTag() != null && child.getTag().equals("DYNAMIC_BUDGET_CARD")) {
                    viewsToRemove.add(child);
            }
            }
            // Remove all tagged views
            for (View view : viewsToRemove) {
                budgetContainer.removeView(view);
            }
        }

        // Dynamically create budget cards
        if (budgetContainer != null) {
            // Find the add budget button to insert cards after it
            View addBudgetButton = root.findViewById(R.id.btn_add_budget);
            int insertIndex = budgetContainer.indexOfChild(addBudgetButton);
            
            // Insert budget cards after the add button
            for (Budget budget : budgets) {
                long spentAmount = BudgetCalculator.calculateSpentAmount(requireContext(), budget);
                View budgetCard = createBudgetCard(budget, spentAmount, budgetContainer);
                // Tag the card so we can identify and remove it later
                budgetCard.setTag("DYNAMIC_BUDGET_CARD");
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
            tvAmount.setText(CurrencyUtils.formatCurrency(requireContext(), totalBudget));
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
            tvSpent.setText(String.format("%s %s", CurrencyUtils.formatCurrency(requireContext(), totalSpent), getString(R.string.spent)));
        }

        if (tvLeft != null) {
            if (remaining >= 0) {
                tvLeft.setText(String.format("%s %s", CurrencyUtils.formatCurrency(requireContext(), remaining), getString(R.string.left)));
                tvLeft.setTextColor(getResources().getColor(R.color.primary_black, null));
            } else {
                tvLeft.setText(String.format("%s %s", CurrencyUtils.formatCurrency(requireContext(), Math.abs(remaining)), getString(R.string.overspending)));
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
        LinearLayout containerCategoryIcons = card.findViewById(R.id.container_category_icons);
        
        // Display category icons
        setupCategoryIcons(containerCategoryIcons, budget);

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
            tvAmount.setText(CurrencyUtils.formatCurrency(requireContext(), budget.getBudgetAmount()));
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
            progressBar.setMax(100);
            progressBar.setProgress(Math.min(percentage, 100));
            
            // Only apply user's color when there's spending (percentage > 0)
            // When percentage is 0, keep it grey (default)
            if (percentage > 0) {
                if (budget.getCustomColor() != null) {
                    // For custom colors, create a proper drawable with the custom color
                    setProgressBarCustomColor(progressBar, budget.getCustomColor());
                } else if (budget.getColorResId() != 0) {
                    ProgressBarUtils.setProgressBarColor(requireContext(), progressBar, budget.getColorResId());
                }
            } else {
                // When no spending, ensure it's grey (reset to default)
                // Reset to default drawable from XML
                progressBar.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.progress_bar_budget));
            }
        }

        if (tvSpent != null) {
            tvSpent.setText(String.format("%s %s", CurrencyUtils.formatCurrency(requireContext(), spentAmount), getString(R.string.spent)));
        }

        if (tvLeft != null) {
            if (remaining >= 0) {
                tvLeft.setText(String.format("%s %s", CurrencyUtils.formatCurrency(requireContext(), remaining), getString(R.string.left)));
                tvLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
            } else {
                tvLeft.setText(String.format("%s %s", CurrencyUtils.formatCurrency(requireContext(), Math.abs(remaining)), getString(R.string.overspending)));
                tvLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_red));
            }
        }

        // Make the card clickable to navigate to detail
            card.setOnClickListener(v -> {
            int colorResId = budget.getCustomColor() != null ? 0 : budget.getColorResId();
            Integer customColor = budget.getCustomColor();
                BudgetDetailFragment detailFragment = BudgetDetailFragment.newInstance(
                budget.getId(),
                budget.getName(),
                budget.getBudgetAmount(),
                spentAmount,
                colorResId,
                customColor
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

    /**
     * Sets a custom color for the progress bar (similar to ProgressBarUtils but for custom colors).
     * Creates a proper drawable with grey background and custom color for progress.
     */
    private void setProgressBarCustomColor(ProgressBar progressBar, int customColor) {
        if (progressBar == null) {
            return;
        }

        // Convert dp to pixels for corner radius (4dp)
        float density = requireContext().getResources().getDisplayMetrics().density;
        float cornerRadius = 4.0f * density;

        // Create background shape (grey)
        ShapeDrawable backgroundShape = new ShapeDrawable();
        backgroundShape.setShape(new RoundRectShape(
            new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                       cornerRadius, cornerRadius, cornerRadius, cornerRadius}, null, null));
        backgroundShape.getPaint().setColor(ContextCompat.getColor(requireContext(), R.color.secondary_grey));

        // Create progress shape with the custom color
        ShapeDrawable progressShape = new ShapeDrawable();
        progressShape.setShape(new RoundRectShape(
            new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                       cornerRadius, cornerRadius, cornerRadius, cornerRadius}, null, null));
        progressShape.getPaint().setColor(customColor);

        // Create clip drawable for progress
        ClipDrawable clipDrawable = new ClipDrawable(progressShape,
            Gravity.START, ClipDrawable.HORIZONTAL);

        // Create layer drawable
        LayerDrawable layerDrawable = new LayerDrawable(
            new android.graphics.drawable.Drawable[]{backgroundShape, clipDrawable});
        layerDrawable.setId(0, android.R.id.background);
        layerDrawable.setId(1, android.R.id.progress);

        progressBar.setProgressDrawable(layerDrawable);
    }

    /**
     * Sets up category icons for a budget card.
     * Displays icons for all categories associated with this budget.
     */
    private void setupCategoryIcons(LinearLayout container, Budget budget) {
        if (container == null || budget == null) {
            return;
        }

        // Clear existing icons
        container.removeAllViews();

        // Get category IDs for this budget
        List<Long> categoryIds = BudgetCategoryManager.getCategoryIdsForBudget(requireContext(), budget.getId());
        if (categoryIds.isEmpty()) {
            container.setVisibility(View.GONE);
            return;
        }

        container.setVisibility(View.VISIBLE);

        // Build a lookup map of categoryId -> iconResId based on user-defined categories
        List<CategoryManager.CategoryItem> userCategories = CategoryManager.getCategories(requireContext());
        java.util.Map<Long, Integer> categoryIdToIcon = new java.util.HashMap<>();
        for (CategoryManager.CategoryItem item : userCategories) {
            long id = item.name.hashCode() * 31L + item.iconResId;
            categoryIdToIcon.put(id, item.iconResId);
        }

        // Create icon views for each category associated with this budget
        for (Long categoryId : categoryIds) {
            Integer iconResId = categoryIdToIcon.get(categoryId);
            if (iconResId == null || iconResId == 0) {
                continue;
            }

            ImageView iconView = new ImageView(requireContext());
            int iconSize = (int) (32 * requireContext().getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
            params.setMarginEnd((int) (8 * requireContext().getResources().getDisplayMetrics().density));
            iconView.setLayoutParams(params);
            iconView.setImageResource(iconResId);
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iconView.setPadding(
                (int) (4 * requireContext().getResources().getDisplayMetrics().density),
                (int) (4 * requireContext().getResources().getDisplayMetrics().density),
                (int) (4 * requireContext().getResources().getDisplayMetrics().density),
                (int) (4 * requireContext().getResources().getDisplayMetrics().density)
            );

            // Set icon color based on budget color
            int colorValue = 0;
            if (budget.getCustomColor() != null) {
                colorValue = budget.getCustomColor();
            } else if (budget.getColorResId() != 0) {
                colorValue = ContextCompat.getColor(requireContext(), budget.getColorResId());
            }
            if (colorValue != 0) {
                iconView.setColorFilter(colorValue);
            }

            container.addView(iconView);
        }
    }

}
