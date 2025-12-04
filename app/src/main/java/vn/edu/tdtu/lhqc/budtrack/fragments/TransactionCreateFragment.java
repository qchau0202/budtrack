package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import java.text.DecimalFormatSymbols;

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
    private String ocrTextFromArgs = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        // Read arguments passed from MainActivity
        Bundle args = getArguments();
        if (args != null) {
            selectedType = args.getString("transaction_type", TYPE_EXPENSE);
            isOCR = args.getBoolean("is_ocr", false);
            if (isOCR) {
                ocrTextFromArgs = args.getString("ocr_text", null);
            }
        }

        // Set up Fragment Result listener for location selection using activity's fragment manager
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
                        updateCurrencyText();
                    }
                }
        );
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


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If we have OCR text from the arguments, process it now
        if (isOCR && ocrTextFromArgs != null) {
            // Directly call the parser with the raw text string.
            findAndSetTransactionDetails(ocrTextFromArgs);

            // Also set the full text to the note for user reference
            if (editNote != null && editNote.getText().toString().isEmpty()) {
                editNote.setText("--- OCR Result ---\n" + ocrTextFromArgs);
            }
        }

        // Restore transaction state if reopening after location selection
        restoreTransactionState();
        // Check for stored location when view is created
        checkForStoredLocation();
    }

    /**
     * FINAL, ROBUST VERSION: Intelligently parses a raw OCR text string to find the total amount and a possible title.
     * This version uses a stricter Regex and smarter fallback logic to avoid picking phone numbers.
     *
     * @param ocrText The raw text string from the OCR process.
     */
    private void findAndSetTransactionDetails(String ocrText) {
        if (ocrText == null || ocrText.isEmpty()) {
            return;
        }

        List<String> totalKeywords = Arrays.asList(
                "grand total", "total", "thành tiền", "tổng cộng", "amount due", "balance"
        );
        String potentialTitle = "";
        double finalAmount = -1.0;

        // Stricter Regex: Won't match numbers with more than 7 digits unless they have separators.
        // This helps exclude phone numbers and order IDs.
        Pattern numberPattern = Pattern.compile(
                "\\b((?:\\d{1,3}(?:[,.]\\d{3})*[,.]\\d{1,2})|(?:\\d{1,7}[,.]\\d{1,2})|(?:\\d{1,7}))\\b"
        );

        java.text.NumberFormat numberFormat = java.text.NumberFormat.getInstance(Locale.ROOT);

        double keywordAmount = -1.0;
        int bestKeywordScore = -1;
        double largestFallbackAmount = -1.0;
        double lastFoundAmount = -1.0; // The very last number found

        // Iterate over each line of the raw text string
        for (String line : ocrText.split("\n")) {
            String lineText = line.toLowerCase(Locale.ROOT);

            // --- Title Finding Logic ---
            if (potentialTitle.isEmpty() && !lineText.matches(".*\\d.*") && lineText.length() > 3) {
                potentialTitle = line; // Use original casing
            }

            // --- Keyword Search Logic (Prioritized) ---
            for (int i = 0; i < totalKeywords.size(); i++) {
                String keyword = totalKeywords.get(i);
                if (lineText.contains(keyword)) {
                    Matcher matcher = numberPattern.matcher(line);
                    double lineMaxNumber = -1;

                    while (matcher.find()) {
                        try {
                            String standardizedStr = matcher.group().replaceAll(",", "");
                            double currentNumber = numberFormat.parse(standardizedStr).doubleValue();
                            lineMaxNumber = Math.max(lineMaxNumber, currentNumber);
                        } catch (java.text.ParseException | NumberFormatException ignored) {
                        }
                    }

                    if (lineMaxNumber != -1) {
                        int currentScore = totalKeywords.size() - i;
                        if (currentScore > bestKeywordScore) {
                            bestKeywordScore = currentScore;
                            keywordAmount = lineMaxNumber;
                        }
                    }
                }
            }

            // --- Find All Numbers for Fallback ---
            Matcher allNumbersMatcher = numberPattern.matcher(line);
            while (allNumbersMatcher.find()) {
                try {
                    String standardizedStr = allNumbersMatcher.group().replaceAll(",", "");
                    double currentNumber = numberFormat.parse(standardizedStr).doubleValue();
                    largestFallbackAmount = Math.max(largestFallbackAmount, currentNumber);
                    lastFoundAmount = currentNumber; // Always update with the latest number found
                } catch (java.text.ParseException | NumberFormatException ignored) {
                }
            }
        }

        // --- Determine Final Amount with Smarter Fallback ---
        if (keywordAmount != -1.0) {
            finalAmount = keywordAmount; // 1. Priority: Keyword match
        } else if (lastFoundAmount != -1.0) {
            finalAmount = lastFoundAmount; // 2. Fallback: The last number found on the receipt
        } else {
            finalAmount = largestFallbackAmount; // 3. Last resort: The largest number found
        }

        // --- Set the found amount in the EditText (CORRECTED LOGIC) ---
        if (finalAmount != -1.0 && editAmount != null) {
            // 1. Format the parsed number (e.g., 84.8) into a US-style string ("84.80")
            // This ensures two decimal places and uses a dot.
            DecimalFormat usFormatter = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.ROOT));
            String usFormattedString = usFormatter.format(finalAmount);

            // 2. Convert the US-style string ("84.80") into a VND/EU style string ("84,80")
            // that our NumberInputFormatter can correctly handle.
            String vndFormattedString = usFormattedString.replace('.', ',');
            editAmount.setText(vndFormattedString);
        }

        // Set a potential title
        if (!potentialTitle.isEmpty() && editTitle != null && editTitle.getText().toString().isEmpty()) {
            editTitle.setText(potentialTitle);
        }
    }


    // --- All other methods remain the same ---

    @Override
    public void onResume() {
        super.onResume();
        restoreTransactionState();
        checkForStoredLocation();
        updateCurrencyText();
    }

    private void checkForStoredLocation() {
        if (getContext() == null) return;
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("location_selection", android.content.Context.MODE_PRIVATE);
        if (prefs.getBoolean("has_location", false)) {
            String address = prefs.getString("address", "");
            float lat = prefs.getFloat("lat", 0f);
            float lng = prefs.getFloat("lng", 0f);
            prefs.edit().clear().apply();
            if (!address.isEmpty()) {
                selectLocation(address, lat, lng);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false);
            }
        }
    }

    private void initViews(View view) {
        tabExpense = view.findViewById(R.id.tabExpense);
        tabIncome = view.findViewById(R.id.tabIncome);
        tabOthers = view.findViewById(R.id.tabOthers);
        editAmount = view.findViewById(R.id.editAmount);
        editNote = view.findViewById(R.id.editNote);
        editTitle = view.findViewById(R.id.editTitle);
        tvDate = view.findViewById(R.id.tvDate);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvCancel = view.findViewById(R.id.tvCancel);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvWallet = view.findViewById(R.id.tvWallet);
        tvTime = view.findViewById(R.id.tvTime);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvCurrency = view.findViewById(R.id.tv_currency);
        cardDate = view.findViewById(R.id.cardDate);
        cardCategory = view.findViewById(R.id.cardCategory);
        cardWallet = view.findViewById(R.id.cardWallet);
        cardTime = view.findViewById(R.id.cardTime);
        cardLocation = view.findViewById(R.id.cardLocation);
        btnSave = view.findViewById(R.id.btnSave);
        ivCategoryIcon = view.findViewById(R.id.ivCategoryIcon);
        updateDateText();
        updateTimeText();
        updateTabSelection();
        updateWalletText();
        updateCategoryText();
        updateLocationText();
        updateCurrencyText();
        setupImeActions();
    }

    private void setupImeActions() {
        if (editTitle != null) {
            editTitle.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    editAmount.requestFocus();
                    return true;
                }
                return false;
            });
        }
        if (editAmount != null) {
            editAmount.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideKeyboard();
                    cardWallet.performClick();
                    return true;
                }
                return false;
            });
        }
        if (editNote != null) {
            editNote.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    btnSave.performClick();
                    return true;
                }
                return false;
            });
        }
    }

    private void hideKeyboard() {
        View currentFocus = (getView() != null) ? getView().findFocus() : null;
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
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
        TabStyleUtils.applyUnselectedStyle(requireContext(), tabExpense);
        TabStyleUtils.applyUnselectedStyle(requireContext(), tabIncome);
        TabStyleUtils.applyUnselectedStyle(requireContext(), tabOthers);
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
        tvDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void updateTimeText() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        tvTime.setText(sdf.format(selectedTime.getTime()));
    }

    private void showTimePicker() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View timePickerView = inflater.inflate(R.layout.dialog_time_picker, null, false);
        EditText etHour = timePickerView.findViewById(R.id.et_hour);
        EditText etMinute = timePickerView.findViewById(R.id.et_minute);
        MaterialButton btnCancel = timePickerView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = timePickerView.findViewById(R.id.btn_confirm);
        int currentHour = selectedTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = selectedTime.get(Calendar.MINUTE);
        etHour.setText(String.valueOf(currentHour));
        etMinute.setText(String.format(Locale.ROOT, "%02d", currentMinute));
        AlertDialog timeDialog = new MaterialAlertDialogBuilder(requireContext()).setView(timePickerView).create();
        btnCancel.setOnClickListener(v -> timeDialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            try {
                int hour = etHour.getText().toString().isEmpty() ? currentHour : Integer.parseInt(etHour.getText().toString());
                int minute = etMinute.getText().toString().isEmpty() ? currentMinute : Integer.parseInt(etMinute.getText().toString());
                selectedTime.set(Calendar.HOUR_OF_DAY, Math.max(0, Math.min(23, hour)));
                selectedTime.set(Calendar.MINUTE, Math.max(0, Math.min(59, minute)));
                updateTimeText();
            } catch (NumberFormatException e) {
                updateTimeText();
            }
            timeDialog.dismiss();
        });
        timeDialog.show();
        if (timeDialog.getWindow() != null) {
            timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void setupAmountFormatter() {
        NumberInputFormatter.attach(editAmount);
    }

    private void setupButtons() {
        tvCancel.setOnClickListener(v -> {
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
        if (selectedCategory != null && selectedCategoryIconResId != 0) {
            ivCategoryIcon.setImageResource(selectedCategoryIconResId);
            ivCategoryIcon.setVisibility(View.VISIBLE);
            tvCategory.setText(selectedCategory);
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        } else {
            ivCategoryIcon.setVisibility(View.GONE);
            tvCategory.setText(getString(R.string.none));
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
        }
    }

    private void showCategoryCreateSheet() {
        CategoryCreateBottomSheet createBottomSheet = CategoryCreateBottomSheet.newInstance();
        createBottomSheet.setOnCategoryCreateListener(this::selectCategory);
        createBottomSheet.show(getParentFragmentManager(), "CategoryCreateBottomSheet");
    }

    private void showWalletSelectionSheet() {
        WalletSelectBottomSheet sheet = WalletSelectBottomSheet.newInstance(selectedWallet);
        sheet.setOnWalletSelectedListener(new WalletSelectBottomSheet.OnWalletSelectedListener() {
            @Override
            public void onWalletSelected(Wallet wallet) { selectWallet(wallet); }
            @Override
            public void onNoneSelected() { selectWallet(null); }
            @Override
            public void onCreateWalletRequested() {
                saveTransactionState();
                showWalletTypeSelectionForTransaction();
            }
        });
        sheet.show(getParentFragmentManager(), WalletSelectBottomSheet.TAG);
    }

    private void showWalletTypeSelectionForTransaction() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.view_bottom_sheet_wallet_type_selection, null);
        dialog.setContentView(view);
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet).setDraggable(false);
        }
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

    private void openWalletCreateForTransaction(String walletTypeName, int iconResId) {
        WalletCreateFragment createFragment = WalletCreateFragment.newInstance(walletTypeName, iconResId);
        createFragment.show(getParentFragmentManager(), WalletCreateFragment.TAG);
        getParentFragmentManager().setFragmentResultListener(
                WalletCreateFragment.RESULT_KEY,
                this,
                (requestKey, result) -> {
                    if (WalletCreateFragment.RESULT_KEY.equals(requestKey)) {
                        handleWalletCreationResult(result);
                        getParentFragmentManager().clearFragmentResultListener(WalletCreateFragment.RESULT_KEY);
                    }
                }
        );
    }

    private void handleWalletCreationResult(Bundle result) {
        String walletName = result.getString(WalletCreateFragment.RESULT_WALLET_NAME);
        long balance = result.getLong(WalletCreateFragment.RESULT_WALLET_BALANCE);
        int iconResId = result.getInt(WalletCreateFragment.RESULT_WALLET_ICON);
        String walletType = result.getString(WalletCreateFragment.RESULT_WALLET_TYPE, walletName);
        if (walletName == null || walletName.isEmpty()) return;
        Wallet newWallet = new Wallet(walletName, balance, iconResId, walletType);
        vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager.addWallet(requireContext(), newWallet);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded() || getView() == null) return;
            restoreTransactionState();
            List<Wallet> wallets = vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager.getWallets(requireContext());
            Wallet createdWallet = null;
            for (Wallet wallet : wallets) {
                if (wallet.getName().equals(walletName) && wallet.getBalance() == balance) {
                    createdWallet = wallet;
                    break;
                }
            }
            if (createdWallet == null && !wallets.isEmpty()) {
                createdWallet = wallets.get(wallets.size() - 1);
            }
            if (createdWallet != null) {
                selectWallet(createdWallet);
            }
        }, 100);
    }

    private void selectWallet(Wallet wallet) {
        selectedWallet = wallet;
        updateWalletText();
    }

    private void updateWalletText() {
        if (selectedWallet != null) {
            tvWallet.setText(selectedWallet.getName());
            tvWallet.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        } else {
            tvWallet.setText(getString(R.string.none));
            tvWallet.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
        }
    }

    private void showLocationSelectionMap() {
        saveTransactionState();
        dismiss();
        MapFragment mapFragment = MapFragment.newInstanceForLocationSelection();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment, "MAP_FRAGMENT")
                .addToBackStack(null)
                .commit();
    }

    private void saveTransactionState() {
        if (getContext() == null) return;
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("transaction_state", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString("transaction_type", selectedType);
        editor.putBoolean("is_ocr", isOCR);
        editor.putBoolean("should_reopen", true);
        if (editAmount != null) editor.putString("amount", editAmount.getText().toString().trim());
        if (editTitle != null) editor.putString("title", editTitle.getText().toString().trim());
        if (editNote != null) editor.putString("note", editNote.getText().toString().trim());
        if (selectedWallet != null) {
            editor.putString("wallet_name", selectedWallet.getName());
            editor.putLong("wallet_id", selectedWallet.getId());
        } else {
            editor.remove("wallet_name");
            editor.remove("wallet_id");
        }
        if (selectedCategory != null) {
            editor.putString("category_name", selectedCategory);
            editor.putInt("category_icon_res_id", selectedCategoryIconResId);
        } else {
            editor.remove("category_name");
            editor.remove("category_icon_res_id");
        }
        editor.putLong("selected_date_millis", selectedDate.getTimeInMillis());
        editor.putLong("selected_time_millis", selectedTime.getTimeInMillis());
        if (selectedLocationAddress != null) editor.putString("location_address", selectedLocationAddress);
        if (selectedLocationLat != null) editor.putFloat("location_lat", selectedLocationLat.floatValue());
        if (selectedLocationLng != null) editor.putFloat("location_lng", selectedLocationLng.floatValue());
        editor.apply();
    }

    private void restoreTransactionState() {
        if (getContext() == null) return;
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("transaction_state", android.content.Context.MODE_PRIVATE);
        if (!prefs.getBoolean("should_reopen", false)) return;
        selectedType = prefs.getString("transaction_type", TYPE_EXPENSE);
        updateTabSelection();
        isOCR = prefs.getBoolean("is_ocr", false);
        String savedAmount = prefs.getString("amount", "");
        if (!savedAmount.isEmpty() && editAmount != null) editAmount.setText(savedAmount);
        String savedTitle = prefs.getString("title", "");
        if (!savedTitle.isEmpty() && editTitle != null) editTitle.setText(savedTitle);
        String savedNote = prefs.getString("note", "");
        if (!savedNote.isEmpty() && editNote != null) editNote.setText(savedNote);
        String savedWalletName = prefs.getString("wallet_name", null);
        if (savedWalletName != null) {
            long savedWalletId = prefs.getLong("wallet_id", -1);
            if (savedWalletId != -1) {
                List<Wallet> wallets = vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager.getWallets(requireContext());
                for (Wallet wallet : wallets) {
                    if (wallet.getId() == savedWalletId || wallet.getName().equals(savedWalletName)) {
                        selectedWallet = wallet;
                        updateWalletText();
                        break;
                    }
                }
            }
        }
        String savedCategoryName = prefs.getString("category_name", null);
        if (savedCategoryName != null) {
            int savedCategoryIcon = prefs.getInt("category_icon_res_id", 0);
            if (savedCategoryIcon != 0) {
                selectedCategory = savedCategoryName;
                selectedCategoryIconResId = savedCategoryIcon;
                updateCategoryText();
            }
        }
        long savedDateMillis = prefs.getLong("selected_date_millis", -1);
        if (savedDateMillis != -1) {
            selectedDate.setTimeInMillis(savedDateMillis);
            updateDateText();
        }
        long savedTimeMillis = prefs.getLong("selected_time_millis", -1);
        if (savedTimeMillis != -1) {
            selectedTime.setTimeInMillis(savedTimeMillis);
            updateTimeText();
        }
        android.content.SharedPreferences locationPrefs = requireContext().getSharedPreferences("location_selection", android.content.Context.MODE_PRIVATE);
        if (locationPrefs.getBoolean("has_location", false)) {
            String address = locationPrefs.getString("address", "");
            float lat = locationPrefs.getFloat("lat", 0f);
            float lng = locationPrefs.getFloat("lng", 0f);
            locationPrefs.edit().clear().apply();
            if (!address.isEmpty()) {
                selectLocation(address, lat, lng);
                saveTransactionState();
            }
        } else if (selectedLocationAddress == null) {
            String savedLocationAddress = prefs.getString("location_address", null);
            if (savedLocationAddress != null) {
                float savedLat = prefs.getFloat("location_lat", 0f);
                float savedLng = prefs.getFloat("location_lng", 0f);
                selectLocation(savedLocationAddress, savedLat, savedLng);
            }
        }
    }

    private void clearTransactionState() {
        if (getContext() == null) return;
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
        if (tvLocation == null) return;
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
            tvCurrency.setText(SettingsHandler.getCurrency(requireContext()));
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
            String currency = SettingsHandler.getCurrency(requireContext());
            double amountVnd;

            if ("USD".equals(currency)) {
                // For USD we allow decimals; parse robustly and convert to VND
                double amountUsd = CurrencyUtils.parseFormattedNumber(amountText);
                if (amountUsd <= 0) {
                    Toast.makeText(requireContext(), getString(R.string.error_amount_invalid), Toast.LENGTH_SHORT).show();
                    editAmount.requestFocus();
                    return;
                }
                float exchangeRate = SettingsHandler.getExchangeRate(requireContext());
                if (exchangeRate <= 0) {
                    Toast.makeText(requireContext(), getString(R.string.error_amount_invalid), Toast.LENGTH_SHORT).show();
                    editAmount.requestFocus();
                    return;
                }
                amountVnd = amountUsd * exchangeRate;
            } else {
                // For VND we treat the value as an integer amount with dot thousand separators
                long amountLong = CurrencyUtils.parseFormattedNumberLong(amountText);
                if (amountLong <= 0) {
                    Toast.makeText(requireContext(), getString(R.string.error_amount_invalid), Toast.LENGTH_SHORT).show();
                    editAmount.requestFocus();
                    return;
                }
                amountVnd = amountLong;
            }

            saveTransactionToDatabase(amountVnd, title, note, location);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), getString(R.string.error_amount_invalid), Toast.LENGTH_SHORT).show();
            editAmount.requestFocus();
        }
    }

    private void saveTransactionToDatabase(double amount, String title, String note, String location) {
        if (selectedWallet == null) {
            Toast.makeText(requireContext(), getString(R.string.error_wallet_required), Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar transactionDateTime = Calendar.getInstance();
        transactionDateTime.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedTime.get(Calendar.HOUR_OF_DAY), selectedTime.get(Calendar.MINUTE), 0);
        transactionDateTime.set(Calendar.MILLISECOND, 0);

        TransactionType transactionType;
        switch (selectedType) {
            case TYPE_INCOME: transactionType = TransactionType.INCOME; break;
            case TYPE_OTHERS: transactionType = TransactionType.OTHERS; break;
            default: transactionType = TransactionType.EXPENSE; break;
        }

        Transaction transaction = new Transaction(transactionType, (long) amount, selectedWallet.getId(), transactionDateTime.getTime());
        if (title != null && !title.isEmpty()) transaction.setMerchantName(title);
        if (selectedCategory != null && selectedCategoryIconResId != 0 && transactionType == TransactionType.EXPENSE) {
            transaction.setCategoryName(selectedCategory);
            transaction.setCategoryIconResId(selectedCategoryIconResId);
            transaction.setCategoryId((long) (selectedCategory.hashCode() * 31 + selectedCategoryIconResId));
        }
        if (!note.isEmpty()) transaction.setNote(note);
        String locationText = selectedLocationAddress != null ? selectedLocationAddress : location;
        if (!locationText.isEmpty()) {
            transaction.setAddress(locationText);
            if (selectedLocationLat != null && selectedLocationLng != null) {
                transaction.setLatitude(selectedLocationLat);
                transaction.setLongitude(selectedLocationLng);
            }
        }

        TransactionManager.addTransaction(requireContext(), transaction);

        if (transactionType == TransactionType.INCOME) {
            selectedWallet.setBalance(selectedWallet.getBalance() + (long) amount);
        } else {
            selectedWallet.setBalance(selectedWallet.getBalance() - (long) amount);
        }
        vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager.updateWallet(requireContext(), selectedWallet.getName(), selectedWallet);

        String message = getString(R.string.transaction_saved) + " " + getTransactionTypeText() + " - " + CurrencyUtils.formatNumberUS(amount) + " - " + (note.isEmpty() ? getString(R.string.no_note) : note);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        clearTransactionState();

        androidx.fragment.app.FragmentManager fm = requireActivity().getSupportFragmentManager();
        HomeFragment homeFragment = (HomeFragment) fm.findFragmentByTag("HOME_FRAGMENT");
        if (homeFragment != null && homeFragment.isAdded()) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (homeFragment.isAdded() && !homeFragment.isDetached()) {
                    homeFragment.refreshData();
                }
            });
        }

        Bundle result = new Bundle();
        result.putBoolean("transaction_created", true);
        requireActivity().getSupportFragmentManager().setFragmentResult(RESULT_KEY_TRANSACTION_CREATED, result);

        dismiss();
    }

    private String getTransactionTypeText() {
        switch (selectedType) {
            case TYPE_INCOME: return getString(R.string.income);
            case TYPE_OTHERS: return getString(R.string.others);
            default: return getString(R.string.expense);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(MapFragment.RESULT_KEY_LOCATION);
        requireActivity().getSupportFragmentManager().clearFragmentResultListener("currency_changed");
    }
}
