package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;

public class TransactionFragmentCreate extends BottomSheetDialogFragment {

    public static final String TAG = "TransactionFragment";
    
    // Transaction types
    private static final String TYPE_EXPENSE = "expense";
    private static final String TYPE_INCOME = "income";
    private static final String TYPE_OTHERS = "others";
    
    private MaterialButton tabExpense, tabIncome, tabOthers, btnSave;
    private TextView tvDate, tvCategory, tvCancel, tvTitle, tvSubCategory;
    private EditText editAmount, editNote;
    private View cardDate, cardCategory, cardWallet, cardSubCategory;
    private ImageView ivCategoryIcon, ivSubCategoryIcon;

    private final Calendar selectedDate = Calendar.getInstance();
    private String selectedType = TYPE_EXPENSE;
    private String currentAmount = "";
    private String selectedCategory = null;
    private int selectedCategoryIconResId = 0;
    private String selectedSubCategory = null;
    private int selectedSubCategoryIconResId = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Configure bottom sheet to expand fully
        if (getDialog() != null && getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_create, container, false);
        initViews(view);
        setupTabs();
        setupDatePicker();
        setupAmountFormatter();
        setupButtons();
        return view;
    }

    private void initViews(View view) {
        // Tabs
        tabExpense = view.findViewById(R.id.tabExpense);
        tabIncome = view.findViewById(R.id.tabIncome);
        tabOthers = view.findViewById(R.id.tabOthers);

        // Input fields
        editAmount = view.findViewById(R.id.editAmount);
        editNote = view.findViewById(R.id.editNote);

        // Text views
        tvDate = view.findViewById(R.id.tvDate);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvSubCategory = view.findViewById(R.id.tvSubCategory);
        tvCancel = view.findViewById(R.id.tvCancel);
        tvTitle = view.findViewById(R.id.tvTitle);

        // Cards
        cardDate = view.findViewById(R.id.cardDate);
        cardCategory = view.findViewById(R.id.cardCategory);
        cardSubCategory = view.findViewById(R.id.cardSubCategory);
        cardWallet = view.findViewById(R.id.cardWallet);

        // Buttons
        btnSave = view.findViewById(R.id.btnSave);

        // Category selection
        ivCategoryIcon = view.findViewById(R.id.ivCategoryIcon);
        ivSubCategoryIcon = view.findViewById(R.id.ivSubCategoryIcon);

        updateDateText();
        updateTabSelection(); // Set initial tab selection
    }

    private void setupTabs() {
        tabExpense.setOnClickListener(v -> {
            selectedType = TYPE_EXPENSE;
            updateTabSelection();
        });

        tabIncome.setOnClickListener(v -> {
            selectedType = TYPE_INCOME;
            updateTabSelection();
        });

        tabOthers.setOnClickListener(v -> {
            selectedType = TYPE_OTHERS;
            updateTabSelection();
        });
    }

    private void updateTabSelection() {
        // Reset all tabs to unselected state
        setTabUnselected(tabExpense);
        setTabUnselected(tabIncome);
        setTabUnselected(tabOthers);

        // Set selected tab based on type
        MaterialButton selectedTab = null;
        switch (selectedType) {
            case TYPE_EXPENSE:
                selectedTab = tabExpense;
                break;
            case TYPE_INCOME:
                selectedTab = tabIncome;
                break;
            case TYPE_OTHERS:
                selectedTab = tabOthers;
                break;
        }
        
        if (selectedTab != null) {
            setTabSelected(selectedTab);
        }
    }
    
    private void setTabSelected(MaterialButton button) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary_green));
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_white));
    }
    
    private void setTabUnselected(MaterialButton button) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.secondary_grey));
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
    }

    private void setupDatePicker() {
        cardDate.setOnClickListener(v -> showDatePicker());
        updateDateText();
    }

    private void showDatePicker() {
        // Use MaterialDatePicker with our theme overlay to apply app styling/colors
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(getString(R.string.date))
                .setSelection(selectedDate.getTimeInMillis())
                .setTheme(R.style.ThemeOverlay_Budtrack_DatePicker)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(selection);
                selectedDate.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                updateDateText();
            }
        });

        picker.show(getParentFragmentManager(), "material_date_picker");
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.ENGLISH);
        String dateText = sdf.format(selectedDate.getTime());
        tvDate.setText(dateText);
    }

    private void setupAmountFormatter() {
        editAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(currentAmount)) {
                    editAmount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[.,\\s]", "");

                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = NumberFormat.getNumberInstance(Locale.US).format(parsed);
                            currentAmount = formatted;
                            editAmount.setText(formatted);
                            editAmount.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            // Invalid number format, keep current value
                            currentAmount = s.toString();
                        }
                    } else {
                        currentAmount = "";
                        editAmount.setText("");
                    }

                    editAmount.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupButtons() {
        tvCancel.setOnClickListener(v -> dismiss());

        cardCategory.setOnClickListener(v -> showCategorySelectionSheet());
        cardSubCategory.setOnClickListener(v -> showSubCategorySelectionSheet());

        cardWallet.setOnClickListener(v -> {
            // TODO: Implement wallet selection dialog
            Toast.makeText(requireContext(), getString(R.string.select_wallet), Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void showCategorySelectionSheet() {
        CategorySelectBottomSheet sheet = CategorySelectBottomSheet.newInstance(R.string.select_category_title);
        sheet.setOnCategorySelectedListener(new CategorySelectBottomSheet.OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(CategorySelectBottomSheet.CategoryOption option) {
                selectCategory(option.name, option.iconResId);
            }

            @Override
            public void onAddCategoryRequested() {
                Toast.makeText(requireContext(), getString(R.string.add_new_category), Toast.LENGTH_SHORT).show();
            }
        });
        sheet.show(getParentFragmentManager(), CategorySelectBottomSheet.TAG);
    }

    private void selectCategory(String categoryName, int iconResId) {
        selectedCategory = categoryName;
        selectedCategoryIconResId = iconResId;
        
        // Update category icon
        ivCategoryIcon.setImageResource(iconResId);
        ivCategoryIcon.setVisibility(View.VISIBLE);
        
        // Update category text
        tvCategory.setText(categoryName);
        tvCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
    }

    private void showSubCategorySelectionSheet() {
        CategorySelectBottomSheet sheet = CategorySelectBottomSheet.newInstance(R.string.select_subcategory_title);
        sheet.setOnCategorySelectedListener(new CategorySelectBottomSheet.OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(CategorySelectBottomSheet.CategoryOption option) {
                selectSubCategory(option.name, option.iconResId);
            }

            @Override
            public void onAddCategoryRequested() {
                Toast.makeText(requireContext(), getString(R.string.add_new_subcategory), Toast.LENGTH_SHORT).show();
            }
        });
        sheet.show(getParentFragmentManager(), CategorySelectBottomSheet.TAG + "_SUB");
    }

    private void selectSubCategory(String subCategoryName, int iconResId) {
        selectedSubCategory = subCategoryName;
        selectedSubCategoryIconResId = iconResId;

        if (iconResId != 0) {
            ivSubCategoryIcon.setImageResource(iconResId);
            ivSubCategoryIcon.setVisibility(View.VISIBLE);
        } else {
            ivSubCategoryIcon.setVisibility(View.GONE);
        }

        tvSubCategory.setText(subCategoryName);
        tvSubCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
    }
    
    private void saveTransaction() {
        String amountStr = editAmount.getText().toString().replaceAll("[.,\\s]", "");
        String note = editNote.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_amount_required), Toast.LENGTH_SHORT).show();
            editAmount.requestFocus();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(requireContext(), getString(R.string.error_amount_invalid), Toast.LENGTH_SHORT).show();
                editAmount.requestFocus();
                return;
            }

            // TODO: Save transaction to database
            saveTransactionToDatabase(amount, note);

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), getString(R.string.error_amount_invalid), Toast.LENGTH_SHORT).show();
            editAmount.requestFocus();
        }
    }

    private void saveTransactionToDatabase(double amount, String note) {
        // TODO: Implement transaction saving logic
        // This should connect to your database or ViewModel

        String typeText = getTransactionTypeText();
        String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(amount);
        String noteText = note.isEmpty() ? getString(R.string.no_note) : note;

        String message = getString(R.string.transaction_saved, typeText, formattedAmount, noteText);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // Close bottom sheet
        dismiss();
    }
    
    private String getTransactionTypeText() {
        switch (selectedType) {
            case TYPE_EXPENSE:
                return getString(R.string.expense);
            case TYPE_INCOME:
                return getString(R.string.income);
            case TYPE_OTHERS:
                return getString(R.string.others);
            default:
                return getString(R.string.expense);
        }
    }

}