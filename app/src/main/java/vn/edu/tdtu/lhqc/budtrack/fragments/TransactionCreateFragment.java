package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.category.CategoryManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.NumberInputFormatter;
import vn.edu.tdtu.lhqc.budtrack.utils.TabStyleUtils;

public class TransactionCreateFragment extends BottomSheetDialogFragment {

    public static final String TAG = "TransactionFragment";
    public static final String RESULT_KEY_TRANSACTION_CREATED = "transaction_created";
    
    // Transaction types
    private static final String TYPE_EXPENSE = "expense";
    private static final String TYPE_INCOME = "income";
    private static final String TYPE_OTHERS = "others";
    
    private MaterialButton tabExpense, tabIncome, tabOthers, btnSave;
    private TextView tvDate, tvCategory, tvCancel, tvTitle, tvWallet, tvTime, tvLocation, tvCurrency;
    private EditText editAmount, editNote, editTitle;
    private View cardDate, cardCategory, cardWallet, cardTime, cardLocation;
    private ImageView ivCategoryIcon;
    
    // Location data
    private String selectedLocationAddress = null;
    private Double selectedLocationLat = null;
    private Double selectedLocationLng = null;

    private final Calendar selectedDate = Calendar.getInstance();
    private final Calendar selectedTime = Calendar.getInstance();
    private String selectedType = TYPE_EXPENSE;
    private String selectedCategory = null;
    private int selectedCategoryIconResId = 0;
    private Wallet selectedWallet = null;
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
        
        // Set up Fragment Result listener for location selection using activity's fragment manager
        // This ensures it works even if the bottom sheet is dismissed
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            MapFragment.RESULT_KEY_LOCATION,
            this,
            (requestKey, result) -> {
                if (MapFragment.RESULT_KEY_LOCATION.equals(requestKey)) {
                    String address = result.getString(MapFragment.RESULT_LOCATION_ADDRESS);
                    double lat = result.getDouble(MapFragment.RESULT_LOCATION_LAT);
                    double lng = result.getDouble(MapFragment.RESULT_LOCATION_LNG);
                    selectLocation(address, lat, lng);
                }
            }
        );
        
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Restore transaction state if reopening after location selection
        restoreTransactionState();
        // Check for stored location when view is created
        checkForStoredLocation();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Restore transaction state if reopening after location selection
        restoreTransactionState();
        // Also check in onResume in case view was already created
        checkForStoredLocation();
        // Update currency text in case it changed
        updateCurrencyText();
    }
    
    private void checkForStoredLocation() {
        if (getContext() == null) {
            return;
        }
        
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("location_selection", android.content.Context.MODE_PRIVATE);
        if (prefs.getBoolean("has_location", false)) {
            String address = prefs.getString("address", "");
            float lat = prefs.getFloat("lat", 0f);
            float lng = prefs.getFloat("lng", 0f);
            
            // Clear the stored location
            prefs.edit().clear().apply();
            
            // Apply the location
            if (!address.isEmpty()) {
                selectLocation(address, lat, lng);
            }
        }
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_bottom_sheet_transaction_create, container, false);
        initViews(view);
        setupTabs();
        setupDatePicker();
        setupTimePicker();
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
        editTitle = view.findViewById(R.id.editTitle);

        // Text views
        tvDate = view.findViewById(R.id.tvDate);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvCancel = view.findViewById(R.id.tvCancel);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvWallet = view.findViewById(R.id.tvWallet);
        tvTime = view.findViewById(R.id.tvTime);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvCurrency = view.findViewById(R.id.tv_currency);

        // Cards
        cardDate = view.findViewById(R.id.cardDate);
        cardCategory = view.findViewById(R.id.cardCategory);
        cardWallet = view.findViewById(R.id.cardWallet);
        cardTime = view.findViewById(R.id.cardTime);
        cardLocation = view.findViewById(R.id.cardLocation);

        // Buttons
        btnSave = view.findViewById(R.id.btnSave);

        // Category selection
        ivCategoryIcon = view.findViewById(R.id.ivCategoryIcon);

        updateDateText();
        updateTimeText(); // Initialize with current time
        updateTabSelection(); // Set initial tab selection
        updateWalletText(); // Initialize wallet text to "None"
        updateCategoryText(); // Initialize category text to "None"
        updateLocationText(); // Initialize location text to "None"
        updateCurrencyText(); // Initialize currency text dynamically
        
        // Setup IME action handlers for Enter key navigation
        setupImeActions();
    }
    
    /**
     * Setup IME action handlers to navigate through form fields when Enter is pressed.
     */
    private void setupImeActions() {
        // Title field: Enter -> focus on Amount field
        if (editTitle != null) {
            editTitle.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (editAmount != null) {
                        editAmount.requestFocus();
                        // Show keyboard for amount field
                        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(editAmount, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                    return true;
                }
                return false;
            });
        }
        
        // Amount field: Enter -> open Wallet selection
        if (editAmount != null) {
            editAmount.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    // Hide keyboard first
                    hideKeyboard();
                    // Open wallet selection
                    if (cardWallet != null) {
                        cardWallet.performClick();
                    }
                    return true;
                }
                return false;
            });
        }
        
        // Note field: Enter -> Save transaction (if all required fields are filled)
        if (editNote != null) {
            editNote.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    // Hide keyboard
                    hideKeyboard();
                    // Try to save if all required fields are filled
                    if (btnSave != null) {
                        btnSave.performClick();
                    }
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Hide the soft keyboard.
     */
    private void hideKeyboard() {
        View currentFocus = getView() != null ? getView().findFocus() : null;
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
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

    private void setupTimePicker() {
        cardTime.setOnClickListener(v -> showTimePicker());
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

    private void updateTimeText() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        String timeText = sdf.format(selectedTime.getTime());
        tvTime.setText(timeText);
    }

    private void showTimePicker() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View timePickerView = inflater.inflate(R.layout.dialog_time_picker, null, false);

        EditText etHour = timePickerView.findViewById(R.id.et_hour);
        EditText etMinute = timePickerView.findViewById(R.id.et_minute);
        MaterialButton btnCancel = timePickerView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = timePickerView.findViewById(R.id.btn_confirm);

        // Initialize with current selected time
        int currentHour = selectedTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = selectedTime.get(Calendar.MINUTE);

        etHour.setText(String.valueOf(currentHour));
        etMinute.setText(String.format("%02d", currentMinute));

        // Validate hour input (0-23)
        etHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        int hour = Integer.parseInt(s.toString());
                        if (hour > 23) {
                            s.replace(0, s.length(), "23");
                        } else if (hour < 0) {
                            s.replace(0, s.length(), "0");
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        });

        // Validate minute input (0-59)
        etMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        int minute = Integer.parseInt(s.toString());
                        if (minute > 59) {
                            s.replace(0, s.length(), "59");
                        } else if (minute < 0) {
                            s.replace(0, s.length(), "0");
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        });

        AlertDialog timeDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(timePickerView)
                .create();

        btnCancel.setOnClickListener(v -> timeDialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            try {
                int hour = etHour.getText().toString().isEmpty() ? currentHour : Integer.parseInt(etHour.getText().toString());
                int minute = etMinute.getText().toString().isEmpty() ? currentMinute : Integer.parseInt(etMinute.getText().toString());

                // Validate ranges
                if (hour < 0) hour = 0;
                if (hour > 23) hour = 23;
                if (minute < 0) minute = 0;
                if (minute > 59) minute = 59;

                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                updateTimeText();
                timeDialog.dismiss();
            } catch (NumberFormatException e) {
                // If invalid input, keep current time
                updateTimeText();
                timeDialog.dismiss();
            }
        });

        timeDialog.show();
        if (timeDialog.getWindow() != null) {
            timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void setupAmountFormatter() {
        // Use utility class for formatting number inputs with commas
        NumberInputFormatter.attach(editAmount, null);
    }

    private void setupButtons() {
        tvCancel.setOnClickListener(v -> {
            // Clear saved state when user cancels
            clearTransactionState();
            dismiss();
        });

        cardCategory.setOnClickListener(v -> showCategorySelectionSheet());

        cardWallet.setOnClickListener(v -> showWalletSelectionSheet());
        
        cardLocation.setOnClickListener(v -> showLocationSelectionMap());

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
                showCategoryCreateSheet();
            }
        });
        sheet.show(getParentFragmentManager(), CategorySelectBottomSheet.TAG);
    }

    private void selectCategory(String categoryName, int iconResId) {
        selectedCategory = categoryName;
        selectedCategoryIconResId = iconResId;
        updateCategoryText();
    }

    private void updateCategoryText() {
        // Update category display
        if (selectedCategory != null && selectedCategoryIconResId != 0) {
            // Show category with icon
            ivCategoryIcon.setImageResource(selectedCategoryIconResId);
        ivCategoryIcon.setVisibility(View.VISIBLE);
            tvCategory.setText(selectedCategory);
        tvCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        } else {
            // Show "None" when no category selected
            ivCategoryIcon.setVisibility(View.GONE);
            tvCategory.setText(getString(R.string.none));
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
        }
    }

    private void showCategoryCreateSheet() {
        CategoryCreateBottomSheet createBottomSheet = CategoryCreateBottomSheet.newInstance();
        createBottomSheet.setOnCategoryCreateListener((name, iconResId) -> selectCategory(name, iconResId));
        createBottomSheet.show(getParentFragmentManager(), "CategoryCreateBottomSheet");
    }

    private void showWalletSelectionSheet() {
        WalletSelectBottomSheet sheet = WalletSelectBottomSheet.newInstance(selectedWallet);
        sheet.setOnWalletSelectedListener(new WalletSelectBottomSheet.OnWalletSelectedListener() {
            @Override
            public void onWalletSelected(Wallet wallet) {
                selectWallet(wallet);
            }

            @Override
            public void onNoneSelected() {
                selectWallet(null);
            }

            @Override
            public void onCreateWalletRequested() {
                // Save transaction state before opening wallet creation
                saveTransactionState();
                
                // Open wallet type selection (same flow as WalletFragment)
                showWalletTypeSelectionForTransaction();
            }
        });
        sheet.show(getParentFragmentManager(), WalletSelectBottomSheet.TAG);
    }
    
    /**
     * Show wallet type selection bottom sheet for creating a wallet from transaction create.
     * Follows the same flow as WalletFragment.
     */
    private void showWalletTypeSelectionForTransaction() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.view_bottom_sheet_wallet_type_selection, null);
        dialog.setContentView(view);

        // Configure bottom sheet to expand fully and disable dragging
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = 
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
            behavior.setDraggable(false);
        }

        // Setup click listeners
        view.findViewById(R.id.card_basic_wallet).setOnClickListener(v -> {
            dialog.dismiss();
            openWalletCreateForTransaction(getString(R.string.wallet_type_basic), R.drawable.ic_wallet_cash);
        });

        view.findViewById(R.id.card_investment_wallet).setOnClickListener(v -> {
            dialog.dismiss();
            openWalletCreateForTransaction(getString(R.string.wallet_type_investment), R.drawable.ic_wallet_cash);
        });

        view.findViewById(R.id.card_savings_wallet).setOnClickListener(v -> {
            dialog.dismiss();
            openWalletCreateForTransaction(getString(R.string.wallet_type_savings), R.drawable.ic_wallet_cash);
        });

        dialog.show();
    }
    
    /**
     * Open wallet creation dialog from transaction create.
     */
    private void openWalletCreateForTransaction(String walletTypeName, int iconResId) {
        WalletCreateFragment createFragment = WalletCreateFragment.newInstance(walletTypeName, iconResId);
        createFragment.show(getParentFragmentManager(), WalletCreateFragment.TAG);
        
        // Listen for wallet creation result
        getParentFragmentManager().setFragmentResultListener(
            WalletCreateFragment.RESULT_KEY,
            this,
            (requestKey, result) -> {
                if (WalletCreateFragment.RESULT_KEY.equals(requestKey)) {
                    handleWalletCreationResult(result);
                    // Remove listener after handling
                    getParentFragmentManager().clearFragmentResultListener(WalletCreateFragment.RESULT_KEY);
                }
            }
        );
    }
    
    /**
     * Handle wallet creation result - create wallet and select it.
     */
    private void handleWalletCreationResult(Bundle result) {
        // Get wallet details from result
        String walletName = result.getString(WalletCreateFragment.RESULT_WALLET_NAME);
        long balance = result.getLong(WalletCreateFragment.RESULT_WALLET_BALANCE);
        int iconResId = result.getInt(WalletCreateFragment.RESULT_WALLET_ICON);
        String walletType = result.getString(WalletCreateFragment.RESULT_WALLET_TYPE, walletName);
        
        if (walletName == null || walletName.isEmpty()) {
            return; // Invalid result
        }
        
        // Create the wallet using WalletManager (same as WalletFragment.addNewWallet)
        Wallet newWallet = new Wallet(walletName, balance, iconResId, walletType);
        vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager.addWallet(requireContext(), newWallet);
        
        // Small delay to ensure wallet is saved, then reload and select
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded() || getView() == null) {
                return;
            }
            
            // Restore transaction state
            restoreTransactionState();
            
            // Reload wallets to get the wallet with assigned ID
            java.util.List<Wallet> wallets = vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager.getWallets(requireContext());
            Wallet createdWallet = null;
            for (Wallet wallet : wallets) {
                if (wallet.getName().equals(walletName) && wallet.getBalance() == balance) {
                    createdWallet = wallet;
                    break;
                }
            }
            
            // If wallet not found by name, try to get the last one (most recently created)
            if (createdWallet == null && !wallets.isEmpty()) {
                createdWallet = wallets.get(wallets.size() - 1);
            }
            
            // Select the newly created wallet
            if (createdWallet != null) {
                selectWallet(createdWallet);
            }
        }, 100); // Small delay to ensure wallet is persisted
    }

    private void selectWallet(Wallet wallet) {
        selectedWallet = wallet;
        updateWalletText();
    }
        
    private void updateWalletText() {
        // Update wallet text
        if (selectedWallet != null) {
            tvWallet.setText(selectedWallet.getName());
            tvWallet.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        } else {
            tvWallet.setText(getString(R.string.none));
            tvWallet.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
        }
    }

    private void showLocationSelectionMap() {
        // Save ALL transaction state before dismissing
        saveTransactionState();
        
        // Dismiss bottom sheet first so map is visible
        dismiss();
        
        // Navigate to MapFragment in location selection mode
        MapFragment mapFragment = MapFragment.newInstanceForLocationSelection();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment, "MAP_FRAGMENT")
                .addToBackStack(null)
                .commit();
    }
    
    private void saveTransactionState() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("transaction_state", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        
        // Save transaction type and OCR flag
        editor.putString("transaction_type", selectedType);
        editor.putBoolean("is_ocr", isOCR);
        editor.putBoolean("should_reopen", true);
        
        // Save amount
        if (editAmount != null) {
            String amountText = editAmount.getText().toString().trim();
            editor.putString("amount", amountText);
        }
        
        // Save title
        if (editTitle != null) {
            String titleText = editTitle.getText().toString().trim();
            editor.putString("title", titleText);
        }
        
        // Save note
        if (editNote != null) {
            String noteText = editNote.getText().toString().trim();
            editor.putString("note", noteText);
        }
        
        // Save wallet
        if (selectedWallet != null) {
            editor.putString("wallet_name", selectedWallet.getName());
            editor.putLong("wallet_id", selectedWallet.getId());
        } else {
            editor.remove("wallet_name");
            editor.remove("wallet_id");
        }
        
        // Save category
        if (selectedCategory != null) {
            editor.putString("category_name", selectedCategory);
            editor.putInt("category_icon_res_id", selectedCategoryIconResId);
        } else {
            editor.remove("category_name");
            editor.remove("category_icon_res_id");
        }
        
        // Save date
        editor.putLong("selected_date_millis", selectedDate.getTimeInMillis());
        
        // Save time
        editor.putLong("selected_time_millis", selectedTime.getTimeInMillis());
        
        // Save location (if already selected)
        if (selectedLocationAddress != null) {
            editor.putString("location_address", selectedLocationAddress);
        }
        if (selectedLocationLat != null) {
            editor.putFloat("location_lat", selectedLocationLat.floatValue());
        }
        if (selectedLocationLng != null) {
            editor.putFloat("location_lng", selectedLocationLng.floatValue());
        }
        
        editor.apply();
    }
    
    private void restoreTransactionState() {
        if (getContext() == null) {
            return;
        }
        
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("transaction_state", android.content.Context.MODE_PRIVATE);
        
        // Check if we should restore state
        if (!prefs.getBoolean("should_reopen", false)) {
            return;
        }
        
        // Clear the flag after checking (we'll restore it if needed)
        // But don't clear all state yet - only clear when transaction is saved or cancelled
        
        // Restore transaction type
        String savedType = prefs.getString("transaction_type", TYPE_EXPENSE);
        selectedType = savedType;
        updateTabSelection();
        
        // Restore OCR flag
        isOCR = prefs.getBoolean("is_ocr", false);
        
        // Restore amount
        String savedAmount = prefs.getString("amount", "");
        if (!savedAmount.isEmpty() && editAmount != null) {
            editAmount.setText(savedAmount);
        }
        
        // Restore title
        String savedTitle = prefs.getString("title", "");
        if (!savedTitle.isEmpty() && editTitle != null) {
            editTitle.setText(savedTitle);
        }
        
        // Restore note
        String savedNote = prefs.getString("note", "");
        if (!savedNote.isEmpty() && editNote != null) {
            editNote.setText(savedNote);
        }
        
        // Restore wallet
        String savedWalletName = prefs.getString("wallet_name", null);
        if (savedWalletName != null) {
            long savedWalletId = prefs.getLong("wallet_id", -1);
            if (savedWalletId != -1) {
                // Try to find the wallet by ID
                java.util.List<Wallet> wallets = vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager.getWallets(requireContext());
                for (Wallet wallet : wallets) {
                    if (wallet.getId() == savedWalletId || wallet.getName().equals(savedWalletName)) {
                        selectedWallet = wallet;
                        updateWalletText();
                        break;
                    }
                }
            }
        }
        
        // Restore category
        String savedCategoryName = prefs.getString("category_name", null);
        if (savedCategoryName != null) {
            int savedCategoryIcon = prefs.getInt("category_icon_res_id", 0);
            if (savedCategoryIcon != 0) {
                selectedCategory = savedCategoryName;
                selectedCategoryIconResId = savedCategoryIcon;
                updateCategoryText();
            }
        }
        
        // Restore date
        long savedDateMillis = prefs.getLong("selected_date_millis", -1);
        if (savedDateMillis != -1) {
            selectedDate.setTimeInMillis(savedDateMillis);
            updateDateText();
        }
        
        // Restore time
        long savedTimeMillis = prefs.getLong("selected_time_millis", -1);
        if (savedTimeMillis != -1) {
            selectedTime.setTimeInMillis(savedTimeMillis);
            updateTimeText();
        }
        
        // Restore location (if not already set from location selection)
        // First check if location was selected from map (has_location flag)
        android.content.SharedPreferences locationPrefs = requireContext().getSharedPreferences("location_selection", android.content.Context.MODE_PRIVATE);
        if (locationPrefs.getBoolean("has_location", false)) {
            // Location was just selected from map, use that
            String address = locationPrefs.getString("address", "");
            float lat = locationPrefs.getFloat("lat", 0f);
            float lng = locationPrefs.getFloat("lng", 0f);
            locationPrefs.edit().clear().apply(); // Clear location selection prefs
            
            if (!address.isEmpty() && lat != 0f && lng != 0f) {
                selectedLocationAddress = address;
                selectedLocationLat = (double) lat;
                selectedLocationLng = (double) lng;
                updateLocationText();
                // Save the new location to transaction state
                saveTransactionState();
            }
        } else if (selectedLocationAddress == null) {
            // No new location from map, restore from saved state
            String savedLocationAddress = prefs.getString("location_address", null);
            if (savedLocationAddress != null) {
                float savedLat = prefs.getFloat("location_lat", 0f);
                float savedLng = prefs.getFloat("location_lng", 0f);
                if (savedLat != 0f && savedLng != 0f) {
                    selectedLocationAddress = savedLocationAddress;
                    selectedLocationLat = (double) savedLat;
                    selectedLocationLng = (double) savedLng;
                    updateLocationText();
                }
            }
        }
    }
    
    private void clearTransactionState() {
        if (getContext() == null) {
            return;
        }
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("transaction_state", android.content.Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private void selectLocation(String address, double lat, double lng) {
        selectedLocationAddress = address;
        selectedLocationLat = lat;
        selectedLocationLng = lng;
        updateLocationText();
    }

    private void updateLocationText() {
        // Update location text - check if view is still available
        if (tvLocation == null) {
            return;
        }
        
        if (selectedLocationAddress != null && !selectedLocationAddress.isEmpty()) {
            tvLocation.setText(selectedLocationAddress);
            tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        } else {
            tvLocation.setText(getString(R.string.none));
            tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
        }
    }

    private void updateCurrencyText() {
        if (tvCurrency != null && getContext() != null) {
            String currency = SettingsHandler.getCurrency(requireContext());
            tvCurrency.setText(currency);
        }
    }
    
    private void saveTransaction() {
        String amountText = editAmount.getText().toString().trim();
        String title = editTitle != null ? editTitle.getText().toString().trim() : "";
        String note = editNote.getText().toString().trim();
        String location = selectedLocationAddress != null ? selectedLocationAddress : "";

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

            // Convert amount to VND if currency is USD (amounts are always stored in VND)
            String currentCurrency = SettingsHandler.getCurrency(requireContext());
            if ("USD".equals(currentCurrency)) {
                // User entered amount in USD, convert to VND for storage
                float exchangeRate = SettingsHandler.getExchangeRate(requireContext());
                if (exchangeRate > 0) {
                    amount = amount * exchangeRate; // Convert USD to VND
                }
            }
            // If currency is VND, amount is already in VND, no conversion needed

            // Save transaction to database
            saveTransactionToDatabase(amount, title, note, location);

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), getString(R.string.error_amount_invalid), Toast.LENGTH_SHORT).show();
            editAmount.requestFocus();
        }
    }

    private void saveTransactionToDatabase(double amount, String title, String note, String location) {
        // Validate wallet selection
        if (selectedWallet == null) {
            Toast.makeText(requireContext(), getString(R.string.error_wallet_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Combine date and time for the transaction timestamp
        Calendar transactionDateTime = Calendar.getInstance();
        transactionDateTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
        transactionDateTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
        transactionDateTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
        transactionDateTime.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY));
        transactionDateTime.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE));
        transactionDateTime.set(Calendar.SECOND, 0);
        transactionDateTime.set(Calendar.MILLISECOND, 0);

        // Convert transaction type
        TransactionType transactionType;
        switch (selectedType) {
            case TYPE_INCOME:
                transactionType = TransactionType.INCOME;
                break;
            case TYPE_OTHERS:
                transactionType = TransactionType.OTHERS;
                break;
            default:
                transactionType = TransactionType.EXPENSE;
                break;
        }

        // Create transaction
        Transaction transaction = new Transaction(
            transactionType,
            (long) amount,
            selectedWallet.getId(),
            transactionDateTime.getTime()
        );

        // Set title (merchantName)
        if (title != null && !title.isEmpty()) {
            transaction.setMerchantName(title);
        }

        // Set category if selected (only for expenses)
        // Store category name and icon directly (user-defined categories from CategoryManager)
        // Also set categoryId for compatibility with budget system (generated from name+icon hash)
        if (selectedCategory != null && selectedCategoryIconResId != 0 && transactionType == TransactionType.EXPENSE) {
            transaction.setCategoryName(selectedCategory);
            transaction.setCategoryIconResId(selectedCategoryIconResId);
            // Generate categoryId using same formula as BudgetCreateFragment for budget matching
            long categoryId = (long) (selectedCategory.hashCode() * 31 + selectedCategoryIconResId);
            transaction.setCategoryId(categoryId);
        }

        // Set note
        if (!note.isEmpty()) {
            transaction.setNote(note);
        }

        // Set location
        String locationText = selectedLocationAddress != null ? selectedLocationAddress : location;
        if (!locationText.isEmpty()) {
            transaction.setAddress(locationText);
            if (selectedLocationLat != null && selectedLocationLng != null) {
                transaction.setLatitude(selectedLocationLat);
                transaction.setLongitude(selectedLocationLng);
            }
        }

        // Save transaction using TransactionManager
        TransactionManager.addTransaction(requireContext(), transaction);

        // Update wallet balance
        if (transactionType == TransactionType.INCOME) {
            selectedWallet.setBalance(selectedWallet.getBalance() + (long) amount);
        } else {
            selectedWallet.setBalance(selectedWallet.getBalance() - (long) amount);
        }
        vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager.updateWallet(
            requireContext(), selectedWallet.getName(), selectedWallet);

        String typeText = getTransactionTypeText();
        String formattedAmount = CurrencyUtils.formatNumberUS(amount);
        String noteText = note.isEmpty() ? getString(R.string.no_note) : note;

        String message = getString(R.string.transaction_saved)
                + " " + typeText + " - " + formattedAmount + " - " + noteText;
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // Clear saved transaction state since transaction is saved
        clearTransactionState();

        // Directly refresh HomeFragment if it exists (for immediate UI update)
        // This ensures the UI updates immediately, even if the fragment is hidden
        androidx.fragment.app.FragmentManager fm = requireActivity().getSupportFragmentManager();
        HomeFragment homeFragment = (HomeFragment) fm.findFragmentByTag("HOME_FRAGMENT");
        if (homeFragment != null && homeFragment.isAdded()) {
            // Post to main thread to ensure UI updates happen on the correct thread
            // This works even if the fragment is currently hidden
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (homeFragment.isAdded() && !homeFragment.isDetached()) {
                    homeFragment.refreshData();
                }
            });
        }

        // Notify all fragments that a transaction was created (backup mechanism)
        // Data is already saved synchronously via commit(), so we can notify immediately
        Bundle result = new Bundle();
        result.putBoolean("transaction_created", true);
        requireActivity().getSupportFragmentManager().setFragmentResult(RESULT_KEY_TRANSACTION_CREATED, result);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listeners
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            MapFragment.RESULT_KEY_LOCATION);
        requireActivity().getSupportFragmentManager().clearFragmentResultListener("currency_changed");
    }
}