package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetCategoryManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.budget.BudgetManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;
import vn.edu.tdtu.lhqc.budtrack.models.Budget;
import vn.edu.tdtu.lhqc.budtrack.models.Category;
import vn.edu.tdtu.lhqc.budtrack.controllers.category.CategoryManager;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.NumberInputFormatter;

public class BudgetCreateFragment extends BottomSheetDialogFragment {

    public static final String TAG = "BudgetCreateFragment";
    public static final String RESULT_KEY = "budget_created";
    public static final String RESULT_KEY_UPDATED = "budget_updated";
    
    // Arguments for edit mode
    private static final String ARG_BUDGET_ID = "budget_id";
    private static final String ARG_IS_EDIT_MODE = "is_edit_mode";
    
    // Result bundle keys
    public static final String RESULT_BUDGET_NAME = "budget_name";
    public static final String RESULT_BUDGET_AMOUNT = "budget_amount";
    public static final String RESULT_BUDGET_COLOR = "budget_color";
    public static final String RESULT_BUDGET_PERIOD = "budget_period";
    public static final String RESULT_BUDGET_CATEGORIES = "budget_categories";

    private EditText editBudgetName;
    private EditText editBudgetAmount;
    private TextView tvPeriod;
    private TextView tvColor;
    private TextView tvCategories;
    private TextView tvTitle;
    private TextView tvCurrency;
    private View cardPeriod;
    private View cardColor;
    private View cardCategories;
    private View viewColorIndicator;
    private LinearLayout containerCategoryIcons;
    private TextView btnDone;
    
    private long budgetId = 0; // 0 means new budget
    private boolean isEditMode = false;
    private String selectedPeriod = "monthly";
    private int selectedColorResId = R.color.primary_green;
    private Integer selectedCustomColor = null; // Store custom color as ARGB integer
    private boolean isCustomColor = false;
    private Set<Long> selectedCategoryIds = new HashSet<>();
    private List<Category> availableCategories;

    /**
     * Create a new instance for creating a budget.
     */
    public static BudgetCreateFragment newInstance() {
        return new BudgetCreateFragment();
    }

    /**
     * Create a new instance for editing a budget.
     */
    public static BudgetCreateFragment newInstanceForEdit(long budgetId) {
        BudgetCreateFragment fragment = new BudgetCreateFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_BUDGET_ID, budgetId);
        args.putBoolean(ARG_IS_EDIT_MODE, true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        availableCategories = new ArrayList<>(); // Will be loaded in onViewCreated when context is available
        
        // Check if we're in edit mode
        Bundle args = getArguments();
        if (args != null) {
            isEditMode = args.getBoolean(ARG_IS_EDIT_MODE, false);
            budgetId = args.getLong(ARG_BUDGET_ID, 0);
        }
        
        // Listen for currency changes to refresh UI immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            "currency_changed",
            this,
            (requestKey, result) -> {
                if ("currency_changed".equals(requestKey)) {
                    // Update currency text when currency changes
                    updateCurrencyText();
                }
            }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        // Configure bottom sheet to expand fully and disable dragging to prevent accidental dismissal while scrolling
        if (getDialog() != null && getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false); // Disable dragging to prevent accidental dismissal while scrolling
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_bottom_sheet_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views
        editBudgetName = view.findViewById(R.id.edit_budget_name);
        editBudgetAmount = view.findViewById(R.id.edit_budget_amount);
        tvPeriod = view.findViewById(R.id.tv_period);
        tvColor = view.findViewById(R.id.tv_color);
        tvCategories = view.findViewById(R.id.tv_categories);
        tvTitle = view.findViewById(R.id.tv_title);
        tvCurrency = view.findViewById(R.id.tv_currency);
        cardPeriod = view.findViewById(R.id.card_period);
        cardColor = view.findViewById(R.id.card_color);
        cardCategories = view.findViewById(R.id.card_categories);
        viewColorIndicator = view.findViewById(R.id.view_color_indicator);
        containerCategoryIcons = view.findViewById(R.id.container_category_icons);
        btnDone = view.findViewById(R.id.btn_done);

        // Load user-defined categories from CategoryManager (instead of MockCategoryData)
        loadAvailableCategories();

        // Update title based on mode
        if (tvTitle != null) {
            tvTitle.setText(isEditMode ? getString(R.string.budget_edit_title) : getString(R.string.budget_create_title));
        }

        // Setup amount formatter
        setupAmountFormatter();
        
        // Setup click listeners
        setupClickListeners();
        
        // Load budget data if in edit mode
        if (isEditMode && budgetId > 0) {
            loadBudgetData();
        }
        
        // Update initial UI
        updatePeriodText();
        updateColorText();
        updateCategoriesText();
        updateCurrencyText();
    }
    
    /**
     * Load available categories from CategoryManager and convert to Category objects.
     * Generates consistent IDs based on name+icon hash for compatibility with budget system.
     */
    private void loadAvailableCategories() {
        availableCategories = new ArrayList<>();
        List<CategoryManager.CategoryItem> userCategories = CategoryManager.getCategories(requireContext());
        for (CategoryManager.CategoryItem item : userCategories) {
            Category category = new Category(item.name, item.iconResId);
            // Generate a unique ID based on name+icon hash (for compatibility with existing budget system)
            // This same formula must be used when matching transactions to budgets
            category.setId((long) (item.name.hashCode() * 31 + item.iconResId));
            availableCategories.add(category);
        }
    }
    
    private void updateCurrencyText() {
        if (tvCurrency != null && getContext() != null) {
            String currency = SettingsHandler.getCurrency(requireContext());
            tvCurrency.setText(currency);
        }
    }

    private void setupAmountFormatter() {
        if (editBudgetAmount != null) {
            NumberInputFormatter.attachIntegerFormatter(editBudgetAmount, null);
        }
    }

    private void setupClickListeners() {
        if (btnDone != null) {
            btnDone.setOnClickListener(v -> saveBudget());
        }

        if (cardPeriod != null) {
            cardPeriod.setOnClickListener(v -> showPeriodSelectionDialog());
        }

        if (cardColor != null) {
            cardColor.setOnClickListener(v -> showColorSelectionDialog());
        }

        if (cardCategories != null) {
            cardCategories.setOnClickListener(v -> showCategorySelectionDialog());
        }
    }

    private void showPeriodSelectionDialog() {
        final String[] periodValues = {"daily", "weekly", "monthly", "yearly"};
        final String[] pendingSelection = {selectedPeriod};

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_period_selection, null, false);

        MaterialCardView cardDaily = dialogView.findViewById(R.id.card_period_daily);
        MaterialCardView cardWeekly = dialogView.findViewById(R.id.card_period_weekly);
        MaterialCardView cardMonthly = dialogView.findViewById(R.id.card_period_monthly);
        MaterialCardView cardYearly = dialogView.findViewById(R.id.card_period_yearly);
        View containerDaily = dialogView.findViewById(R.id.container_period_daily);
        View containerWeekly = dialogView.findViewById(R.id.container_period_weekly);
        View containerMonthly = dialogView.findViewById(R.id.container_period_monthly);
        View containerYearly = dialogView.findViewById(R.id.container_period_yearly);
        ImageView iconDaily = dialogView.findViewById(R.id.icon_check_daily);
        ImageView iconWeekly = dialogView.findViewById(R.id.icon_check_weekly);
        ImageView iconMonthly = dialogView.findViewById(R.id.icon_check_monthly);
        ImageView iconYearly = dialogView.findViewById(R.id.icon_check_yearly);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        Runnable refreshState = () -> {
            styleSelectionOption(cardDaily, iconDaily, pendingSelection[0].equals("daily"));
            styleSelectionOption(cardWeekly, iconWeekly, pendingSelection[0].equals("weekly"));
            styleSelectionOption(cardMonthly, iconMonthly, pendingSelection[0].equals("monthly"));
            styleSelectionOption(cardYearly, iconYearly, pendingSelection[0].equals("yearly"));
        };

        View.OnClickListener dailyClickListener = v -> {
            pendingSelection[0] = "daily";
            refreshState.run();
        };
        View.OnClickListener weeklyClickListener = v -> {
            pendingSelection[0] = "weekly";
            refreshState.run();
        };
        View.OnClickListener monthlyClickListener = v -> {
            pendingSelection[0] = "monthly";
            refreshState.run();
        };
        View.OnClickListener yearlyClickListener = v -> {
            pendingSelection[0] = "yearly";
            refreshState.run();
        };

        cardDaily.setOnClickListener(dailyClickListener);
        containerDaily.setOnClickListener(dailyClickListener);
        cardWeekly.setOnClickListener(weeklyClickListener);
        containerWeekly.setOnClickListener(weeklyClickListener);
        cardMonthly.setOnClickListener(monthlyClickListener);
        containerMonthly.setOnClickListener(monthlyClickListener);
        cardYearly.setOnClickListener(yearlyClickListener);
        containerYearly.setOnClickListener(yearlyClickListener);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            selectedPeriod = pendingSelection[0];
            updatePeriodText();
            dialog.dismiss();
        });

        refreshState.run();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void showColorSelectionDialog() {
        final int[] colorResIds = {R.color.primary_green, R.color.primary_yellow, R.color.primary_red};
        final int[] pendingSelection = {selectedColorResId};
        final boolean[] pendingIsCustom = {isCustomColor};
        final Integer[] pendingCustomColor = {selectedCustomColor};

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_color_selection, null, false);

        MaterialCardView cardGreen = dialogView.findViewById(R.id.card_color_green);
        MaterialCardView cardYellow = dialogView.findViewById(R.id.card_color_yellow);
        MaterialCardView cardRed = dialogView.findViewById(R.id.card_color_red);
        MaterialCardView cardCustom = dialogView.findViewById(R.id.card_color_custom);
        View containerGreen = dialogView.findViewById(R.id.container_color_green);
        View containerYellow = dialogView.findViewById(R.id.container_color_yellow);
        View containerRed = dialogView.findViewById(R.id.container_color_red);
        View containerCustom = dialogView.findViewById(R.id.container_color_custom);
        ImageView iconGreen = dialogView.findViewById(R.id.icon_check_green);
        ImageView iconYellow = dialogView.findViewById(R.id.icon_check_yellow);
        ImageView iconRed = dialogView.findViewById(R.id.icon_check_red);
        ImageView iconCustom = dialogView.findViewById(R.id.icon_check_custom);
        View viewCustomColor = dialogView.findViewById(R.id.view_color_custom);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        Runnable refreshState = () -> {
            boolean isCustom = pendingIsCustom[0];
            styleSelectionOption(cardGreen, iconGreen, !isCustom && pendingSelection[0] == R.color.primary_green);
            styleSelectionOption(cardYellow, iconYellow, !isCustom && pendingSelection[0] == R.color.primary_yellow);
            styleSelectionOption(cardRed, iconRed, !isCustom && pendingSelection[0] == R.color.primary_red);
            styleSelectionOption(cardCustom, iconCustom, isCustom);
            
            // Update custom color indicator
            if (viewCustomColor != null) {
                if (isCustom && pendingCustomColor[0] != null) {
                    ViewCompat.setBackgroundTintList(viewCustomColor, 
                        android.content.res.ColorStateList.valueOf(pendingCustomColor[0]));
                } else {
                    // Reset to grey if not custom
                    ViewCompat.setBackgroundTintList(viewCustomColor, 
                        android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.primary_grey)));
                }
            }
        };

        View.OnClickListener greenClickListener = v -> {
            pendingSelection[0] = R.color.primary_green;
            pendingIsCustom[0] = false;
            refreshState.run();
        };
        View.OnClickListener yellowClickListener = v -> {
            pendingSelection[0] = R.color.primary_yellow;
            pendingIsCustom[0] = false;
            refreshState.run();
        };
        View.OnClickListener redClickListener = v -> {
            pendingSelection[0] = R.color.primary_red;
            pendingIsCustom[0] = false;
            refreshState.run();
        };
        View.OnClickListener customClickListener = v -> {
            // Toggle custom color selection
            if (pendingIsCustom[0] && pendingCustomColor[0] != null) {
                // If already selected, deselect it
                pendingIsCustom[0] = false;
                pendingCustomColor[0] = null;
            } else {
                // Show color picker dialog
                showCustomColorPickerDialog(pendingCustomColor, () -> {
                    if (pendingCustomColor[0] != null) {
                        pendingIsCustom[0] = true;
                    }
                    refreshState.run();
                });
            }
            refreshState.run();
        };

        cardGreen.setOnClickListener(greenClickListener);
        containerGreen.setOnClickListener(greenClickListener);
        cardYellow.setOnClickListener(yellowClickListener);
        containerYellow.setOnClickListener(yellowClickListener);
        cardRed.setOnClickListener(redClickListener);
        containerRed.setOnClickListener(redClickListener);
        cardCustom.setOnClickListener(customClickListener);
        containerCustom.setOnClickListener(customClickListener);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            if (pendingIsCustom[0] && pendingCustomColor[0] != null) {
                isCustomColor = true;
                selectedCustomColor = pendingCustomColor[0];
                selectedColorResId = 0; // Not used for custom colors
            } else {
                isCustomColor = false;
                selectedColorResId = pendingSelection[0];
                selectedCustomColor = null;
            }
            updateColorText();
            dialog.dismiss();
        });

        refreshState.run();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    
    private void showCustomColorPickerDialog(Integer[] pendingCustomColor, Runnable refreshCallback) {
        // Color resource IDs in order (9 colors, 3 rows x 3 columns)
        final int[] colorResIds = {
            R.color.custom_color_blue,        // Row 1
            R.color.custom_color_purple,
            R.color.custom_color_orange,
            R.color.custom_color_cyan,        // Row 2
            R.color.custom_color_pink,
            R.color.custom_color_indigo,
            R.color.custom_color_teal,        // Row 3
            R.color.custom_color_amber,
            R.color.custom_color_lime
        };
        
        // Convert to ARGB integers
        final int[] colorValues = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorValues[i] = ContextCompat.getColor(requireContext(), colorResIds[i]);
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_custom_color_picker, null, false);

        LinearLayout colorGrid = dialogView.findViewById(R.id.color_grid);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        final Integer[] selectedColor = {pendingCustomColor[0]};
        List<MaterialCardView> colorCards = new ArrayList<>();
        List<ImageView> checkIcons = new ArrayList<>();

        // Create 3 rows with 3 colors each
        for (int row = 0; row < 3; row++) {
            LinearLayout rowLayout = new LinearLayout(requireContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            if (row < 2) {
                rowParams.setMargins(0, 0, 0, (int) (12 * getResources().getDisplayMetrics().density));
            }
            rowLayout.setLayoutParams(rowParams);

            for (int col = 0; col < 3; col++) {
                int colorIndex = row * 3 + col;
                if (colorIndex >= colorValues.length) break;

                View itemView = inflater.inflate(R.layout.item_color_swatch, rowLayout, false);
                MaterialCardView card = itemView.findViewById(R.id.card_color_swatch);
                View colorSwatch = itemView.findViewById(R.id.view_color_swatch);
                ImageView iconCheck = itemView.findViewById(R.id.icon_check);

                if (card != null && colorSwatch != null && iconCheck != null) {
                    // Set color
                    ViewCompat.setBackgroundTintList(colorSwatch, 
                        android.content.res.ColorStateList.valueOf(colorValues[colorIndex]));

                    // Check if this color is currently selected
                    boolean isSelected = selectedColor[0] != null && selectedColor[0] == colorValues[colorIndex];
                    styleColorSwatch(card, iconCheck, isSelected);

                    final int finalColorIndex = colorIndex;
                    final int colorValue = colorValues[colorIndex];
                    
                    // Set click listener - toggle selection
                    View.OnClickListener colorClickListener = v -> {
                        if (selectedColor[0] != null && selectedColor[0] == colorValue) {
                            // Deselect if already selected
                            selectedColor[0] = null;
                        } else {
                            // Select this color
                            selectedColor[0] = colorValue;
                        }
                        // Update all cards
                        for (int j = 0; j < colorCards.size(); j++) {
                            boolean isThisSelected = selectedColor[0] != null && selectedColor[0] == colorValues[j];
                            styleColorSwatch(colorCards.get(j), checkIcons.get(j), isThisSelected);
                        }
                    };

                    card.setOnClickListener(colorClickListener);
                    itemView.setOnClickListener(colorClickListener);

                    colorCards.add(card);
                    checkIcons.add(iconCheck);
                    
                    // Set layout params
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemView.getLayoutParams();
                    params.weight = 1;
                    if (col < 2) {
                        params.setMargins(0, 0, (int) (12 * getResources().getDisplayMetrics().density), 0);
                    }
                    itemView.setLayoutParams(params);
                    
                    rowLayout.addView(itemView);
                }
            }
            
            colorGrid.addView(rowLayout);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            pendingCustomColor[0] = selectedColor[0];
            if (refreshCallback != null) {
                refreshCallback.run();
            }
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    
    private void styleColorSwatch(MaterialCardView cardView, ImageView checkIcon, boolean selected) {
        if (cardView == null || checkIcon == null) {
            return;
        }
        int strokeColor = ContextCompat.getColor(requireContext(),
                selected ? R.color.primary_green : R.color.secondary_grey);

        cardView.setStrokeColor(strokeColor);
        cardView.setStrokeWidth(selected ? 2 : 1);
        checkIcon.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    private void showCategorySelectionDialog() {
        // Reload categories in case new ones were added
        loadAvailableCategories();
        
        if (availableCategories == null || availableCategories.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_categories_available), Toast.LENGTH_SHORT).show();
            return;
        }

        final Set<Long> pendingSelection = new HashSet<>(selectedCategoryIds);
        
        // Get categories that are already used in other budgets
        // When editing, exclude the current budget so its categories can still be selected
        Set<Long> usedCategoryIds = BudgetCategoryManager.getCategoriesUsedByOtherBudgets(requireContext(), budgetId);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_categories_selection, null, false);

        LinearLayout containerCategories = dialogView.findViewById(R.id.container_categories);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        // Create category cards dynamically
        List<MaterialCardView> categoryCards = new ArrayList<>();
        List<ImageView> checkIcons = new ArrayList<>();
        List<View> containers = new ArrayList<>();

        for (Category category : availableCategories) {
            View categoryItemView = inflater.inflate(R.layout.item_category_selection, containerCategories, false);
            MaterialCardView card = categoryItemView.findViewById(R.id.card_category);
            View container = categoryItemView.findViewById(R.id.container_category);
            ImageView iconCheck = categoryItemView.findViewById(R.id.icon_check);
            ImageView iconCategory = categoryItemView.findViewById(R.id.icon_category);
            TextView tvCategoryName = categoryItemView.findViewById(R.id.tv_category_name);

            // Check if this category is already used in another budget
            boolean isUsedInOtherBudget = usedCategoryIds.contains(category.getId());
            boolean isCurrentlySelected = pendingSelection.contains(category.getId());

            // Set category icon
            if (iconCategory != null) {
                iconCategory.setImageResource(category.getIconResId());
                // Grey out icon if used in other budget and not currently selected
                if (isUsedInOtherBudget && !isCurrentlySelected) {
                    iconCategory.setAlpha(0.4f);
                } else {
                    iconCategory.setAlpha(1.0f);
                }
            }

            // Set category name
            if (tvCategoryName != null) {
                tvCategoryName.setText(category.getName());
                // Grey out text if used in other budget and not currently selected
                if (isUsedInOtherBudget && !isCurrentlySelected) {
                    tvCategoryName.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_grey));
                    tvCategoryName.setAlpha(0.6f);
                } else {
                    tvCategoryName.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
                    tvCategoryName.setAlpha(1.0f);
                }
            }

            // Set initial selection state
            boolean isSelected = isCurrentlySelected;
            styleSelectionOption(card, iconCheck, isSelected);

            // Disable card if used in other budget and not currently selected
            if (isUsedInOtherBudget && !isCurrentlySelected) {
                card.setEnabled(false);
                card.setAlpha(0.5f);
            } else {
                card.setEnabled(true);
                card.setAlpha(1.0f);
            }

            // Set click listener - prevent selection if already used in other budget
            View.OnClickListener categoryClickListener = v -> {
                if (isUsedInOtherBudget && !pendingSelection.contains(category.getId())) {
                    // Category is already used in another budget, show message
                    Toast.makeText(requireContext(), 
                        getString(R.string.category_already_in_use, category.getName()), 
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (pendingSelection.contains(category.getId())) {
                    pendingSelection.remove(category.getId());
                } else {
                    pendingSelection.add(category.getId());
                }
                styleSelectionOption(card, iconCheck, pendingSelection.contains(category.getId()));
            };

            card.setOnClickListener(categoryClickListener);
            container.setOnClickListener(categoryClickListener);

            categoryCards.add(card);
            checkIcons.add(iconCheck);
            containers.add(container);
            containerCategories.addView(categoryItemView);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            selectedCategoryIds.clear();
            selectedCategoryIds.addAll(pendingSelection);
            updateCategoriesText();
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    
    private void styleSelectionOption(MaterialCardView cardView, ImageView checkIcon, boolean selected) {
        if (cardView == null || checkIcon == null) {
            return;
        }
        int strokeColor = ContextCompat.getColor(requireContext(),
                selected ? R.color.primary_green : R.color.secondary_grey);

        cardView.setStrokeColor(strokeColor);
        checkIcon.setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    private void updatePeriodText() {
        if (tvPeriod != null) {
            String periodDisplay = selectedPeriod.substring(0, 1).toUpperCase() + selectedPeriod.substring(1);
            tvPeriod.setText(periodDisplay);
        }
    }

    private void updateColorText() {
        if (tvColor != null && viewColorIndicator != null) {
            String colorName = "";
            int colorTint = 0;
            
            if (isCustomColor && selectedCustomColor != null) {
                colorName = getString(R.string.custom_color);
                colorTint = selectedCustomColor;
            } else if (selectedColorResId == R.color.primary_green) {
                colorName = "Green";
                colorTint = ContextCompat.getColor(requireContext(), R.color.primary_green);
            } else if (selectedColorResId == R.color.primary_yellow) {
                colorName = "Yellow";
                colorTint = ContextCompat.getColor(requireContext(), R.color.primary_yellow);
            } else if (selectedColorResId == R.color.primary_red) {
                colorName = "Red";
                colorTint = ContextCompat.getColor(requireContext(), R.color.primary_red);
            }
            
            tvColor.setText(colorName);
            
            // Update color indicator
            if (colorTint != 0) {
                ViewCompat.setBackgroundTintList(viewColorIndicator, 
                    android.content.res.ColorStateList.valueOf(colorTint));
                viewColorIndicator.setVisibility(View.VISIBLE);
            } else {
                viewColorIndicator.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Load budget data when in edit mode.
     */
    private void loadBudgetData() {
        if (!isEditMode || budgetId <= 0) {
            return;
        }

        Budget budget = BudgetManager.getBudgetById(requireContext(), budgetId);
        if (budget == null) {
            return;
        }

        // Populate name
        if (editBudgetName != null) {
            editBudgetName.setText(budget.getName());
            editBudgetName.setSelection(budget.getName().length());
        }

        // Populate amount (use formatNumber to avoid "VND" suffix in input field)
        if (editBudgetAmount != null) {
            editBudgetAmount.setText(CurrencyUtils.formatNumber(budget.getBudgetAmount()));
        }

        // Populate period
        selectedPeriod = budget.getPeriod() != null ? budget.getPeriod() : "monthly";
        updatePeriodText();

        // Populate color
        if (budget.getCustomColor() != null) {
            isCustomColor = true;
            selectedCustomColor = budget.getCustomColor();
            selectedColorResId = 0;
        } else {
            isCustomColor = false;
            selectedColorResId = budget.getColorResId();
            selectedCustomColor = null;
        }
        updateColorText();

        // Populate categories
        List<Long> categoryIds = BudgetCategoryManager.getCategoryIdsForBudget(requireContext(), budgetId);
        selectedCategoryIds.clear();
        selectedCategoryIds.addAll(categoryIds);
        updateCategoriesText();
    }

    private void updateCategoriesText() {
        if (tvCategories != null && containerCategoryIcons != null && availableCategories != null) {
            // Clear existing icons
            containerCategoryIcons.removeAllViews();
            
            if (selectedCategoryIds.isEmpty()) {
                tvCategories.setText(getString(R.string.none));
                tvCategories.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
                containerCategoryIcons.setVisibility(View.GONE);
            } else {
                StringBuilder categoriesText = new StringBuilder();
                int count = 0;
                
                // Add category icons and text
                LayoutInflater inflater = LayoutInflater.from(requireContext());
                for (Category category : availableCategories) {
                    if (selectedCategoryIds.contains(category.getId())) {
                        // Add icon
                        ImageView iconView = new ImageView(requireContext());
                        int iconSize = (int) (24 * getResources().getDisplayMetrics().density);
                        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
                        iconParams.setMargins(0, 0, (int) (6 * getResources().getDisplayMetrics().density), 0);
                        iconView.setLayoutParams(iconParams);
                        iconView.setImageResource(category.getIconResId());
                        iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_green));
                        containerCategoryIcons.addView(iconView);
                        
                        // Add to text (only show text if 3 or fewer categories)
                        if (count < 3) {
                            if (count > 0) categoriesText.append(" ");
                            categoriesText.append(category.getName());
                        }
                        count++;
                    }
                }
                
                // Only show text if 3 or fewer categories selected
                if (count <= 3) {
                    tvCategories.setText(categoriesText.toString());
                    tvCategories.setVisibility(View.VISIBLE);
                } else {
                    // Hide text if more than 3 categories to prevent overflow
                    tvCategories.setVisibility(View.GONE);
                }
                
                tvCategories.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
                containerCategoryIcons.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void saveBudget() {
        // Validate inputs
        String budgetName = editBudgetName != null ? editBudgetName.getText().toString().trim() : "";
        String amountText = editBudgetAmount != null ? editBudgetAmount.getText().toString().trim() : "";

        if (budgetName.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.budget_name_required), Toast.LENGTH_SHORT).show();
            if (editBudgetName != null) editBudgetName.requestFocus();
            return;
        }

        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.budget_amount_required), Toast.LENGTH_SHORT).show();
            if (editBudgetAmount != null) editBudgetAmount.requestFocus();
            return;
        }

        try {
            // Parse user-entered amount in the current display currency
            double amount = CurrencyUtils.parseFormattedNumber(amountText);

            // Convert from display currency to stored currency (VND)
            String selectedCurrency = SettingsHandler.getCurrency(requireContext());
            long budgetAmount;
            if ("USD".equals(selectedCurrency)) {
                float exchangeRate = SettingsHandler.getExchangeRate(requireContext());
                if (exchangeRate <= 0f) {
                    // Fallback: avoid dividing/multiplying by zero; treat as 1:1 if misconfigured
                    exchangeRate = 1f;
                }
                budgetAmount = (long) (amount * exchangeRate);
            } else {
                // VND mode: amount is already in VND
                budgetAmount = (long) amount;
            }

            if (budgetAmount <= 0) {
                Toast.makeText(requireContext(), getString(R.string.budget_amount_invalid), Toast.LENGTH_SHORT).show();
                if (editBudgetAmount != null) editBudgetAmount.requestFocus();
                return;
            }

            if (selectedCategoryIds.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.budget_categories_required), Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditMode && budgetId > 0) {
                // Update existing budget
                Budget existingBudget = BudgetManager.getBudgetById(requireContext(), budgetId);
                if (existingBudget != null) {
                    existingBudget.setName(budgetName);
                    existingBudget.setBudgetAmount(budgetAmount);
                    existingBudget.setColorResId(selectedColorResId);
                    existingBudget.setPeriod(selectedPeriod);
                    if (isCustomColor && selectedCustomColor != null) {
                        existingBudget.setCustomColor(selectedCustomColor);
                    } else {
                        existingBudget.setCustomColor(null);
                    }
                    
                    // Update budget using BudgetManager
                    BudgetManager.updateBudget(requireContext(), existingBudget);
                    
                    // Update budget-category relationships
                    List<Long> categoryIdList = new ArrayList<>(selectedCategoryIds);
                    BudgetCategoryManager.setCategoriesForBudget(
                        requireContext(), budgetId, categoryIdList);

                    // Create result bundle
                    Bundle result = new Bundle();
                    result.putLong("budget_id", budgetId);
                    result.putString(RESULT_BUDGET_NAME, budgetName);
                    result.putLong(RESULT_BUDGET_AMOUNT, budgetAmount);
                    result.putInt(RESULT_BUDGET_COLOR, selectedColorResId);
                    result.putBoolean("is_custom_color", isCustomColor);
                    if (isCustomColor && selectedCustomColor != null) {
                        result.putInt("custom_color", selectedCustomColor);
                    }
                    result.putString(RESULT_BUDGET_PERIOD, selectedPeriod);
                    
                    // Convert category IDs to long array
                    long[] categoryIds = new long[selectedCategoryIds.size()];
                    int index = 0;
                    for (Long categoryId : selectedCategoryIds) {
                        categoryIds[index++] = categoryId;
                    }
                    result.putLongArray(RESULT_BUDGET_CATEGORIES, categoryIds);

                    // Send result
                    requireActivity().getSupportFragmentManager().setFragmentResult(RESULT_KEY_UPDATED, result);

                    Toast.makeText(requireContext(), getString(R.string.budget_updated), Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.budget_not_found), Toast.LENGTH_SHORT).show();
                }
            } else {
                // Create new budget
            Budget newBudget = new Budget(budgetName, budgetAmount, selectedColorResId, selectedPeriod);
            if (isCustomColor && selectedCustomColor != null) {
                newBudget.setCustomColor(selectedCustomColor);
            }
            
            // Save budget using BudgetManager
                BudgetManager.addBudget(requireContext(), newBudget);
            
            // Save budget-category relationships
            List<Long> categoryIdList = new ArrayList<>(selectedCategoryIds);
                BudgetCategoryManager.setCategoriesForBudget(
                requireContext(), newBudget.getId(), categoryIdList);

            // Create result bundle
            Bundle result = new Bundle();
            result.putString(RESULT_BUDGET_NAME, budgetName);
            result.putLong(RESULT_BUDGET_AMOUNT, budgetAmount);
            result.putInt(RESULT_BUDGET_COLOR, selectedColorResId);
            result.putBoolean("is_custom_color", isCustomColor);
            if (isCustomColor && selectedCustomColor != null) {
                result.putInt("custom_color", selectedCustomColor);
            }
            result.putString(RESULT_BUDGET_PERIOD, selectedPeriod);
            
            // Convert category IDs to long array
            long[] categoryIds = new long[selectedCategoryIds.size()];
            int index = 0;
            for (Long categoryId : selectedCategoryIds) {
                categoryIds[index++] = categoryId;
            }
            result.putLongArray(RESULT_BUDGET_CATEGORIES, categoryIds);

            // Send result
            requireActivity().getSupportFragmentManager().setFragmentResult(RESULT_KEY, result);

            Toast.makeText(requireContext(), getString(R.string.budget_created), Toast.LENGTH_SHORT).show();
            dismiss();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), getString(R.string.budget_amount_invalid), Toast.LENGTH_SHORT).show();
            if (editBudgetAmount != null) editBudgetAmount.requestFocus();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listener
        requireActivity().getSupportFragmentManager().clearFragmentResultListener("currency_changed");
    }
}

