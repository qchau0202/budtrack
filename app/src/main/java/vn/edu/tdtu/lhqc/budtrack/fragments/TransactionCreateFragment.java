package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
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
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.NumberInputFormatter;
import vn.edu.tdtu.lhqc.budtrack.utils.TabStyleUtils;

public class TransactionCreateFragment extends BottomSheetDialogFragment {

    public static final String TAG = "TransactionFragment";
    
    // Transaction types
    private static final String TYPE_EXPENSE = "expense";
    private static final String TYPE_INCOME = "income";
    private static final String TYPE_OTHERS = "others";
    
    private MaterialButton tabExpense, tabIncome, tabOthers, btnSave;
    private TextView tvDate, tvCategory, tvCancel, tvTitle;
    private EditText editAmount, editNote;
    private View cardDate, cardCategory, cardWallet;
    private ImageView ivCategoryIcon;

    private final Calendar selectedDate = Calendar.getInstance();
    private String selectedType = TYPE_EXPENSE;
    private String selectedCategory = null;
    private int selectedCategoryIconResId = 0;
    private boolean isOCR = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        
        // Read transaction type from arguments
        Bundle args = getArguments();
        if (args != null) {
            String type = args.getString("transaction_type", TYPE_EXPENSE);
            if (TYPE_INCOME.equals(type)) {
                selectedType = TYPE_INCOME;
            } else if (TYPE_EXPENSE.equals(type)) {
                selectedType = TYPE_EXPENSE;
            } else {
                selectedType = TYPE_EXPENSE; // Default
            }
            isOCR = args.getBoolean("is_ocr", false);
        }
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
        tvCancel = view.findViewById(R.id.tvCancel);
        tvTitle = view.findViewById(R.id.tvTitle);

        // Cards
        cardDate = view.findViewById(R.id.cardDate);
        cardCategory = view.findViewById(R.id.cardCategory);
        cardWallet = view.findViewById(R.id.cardWallet);

        // Buttons
        btnSave = view.findViewById(R.id.btnSave);

        // Category selection
        ivCategoryIcon = view.findViewById(R.id.ivCategoryIcon);

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
        TabStyleUtils.applyUnselectedStyle(requireContext(), tabExpense);
        TabStyleUtils.applyUnselectedStyle(requireContext(), tabIncome);
        TabStyleUtils.applyUnselectedStyle(requireContext(), tabOthers);

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
            TabStyleUtils.applySelectedStyle(requireContext(), selectedTab);
        }
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
        // Use utility class for formatting number inputs with commas
        NumberInputFormatter.attach(editAmount, null);
    }

    private void setupButtons() {
        tvCancel.setOnClickListener(v -> dismiss());

        cardCategory.setOnClickListener(v -> showCategorySelectionSheet());

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
    
    private void saveTransaction() {
        String amountText = editAmount.getText().toString().trim();
        String note = editNote.getText().toString().trim();

        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_amount_required), Toast.LENGTH_SHORT).show();
            editAmount.requestFocus();
            return;
        }

        try {
            double amount = CurrencyUtils.parseFormattedNumber(amountText);
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
        String formattedAmount = CurrencyUtils.formatNumberUS(amount);
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