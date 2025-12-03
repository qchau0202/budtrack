package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager;
import vn.edu.tdtu.lhqc.budtrack.models.Category;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockCategoryData;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.NumberInputFormatter;
import vn.edu.tdtu.lhqc.budtrack.utils.TabStyleUtils;

/**
 * Full-screen Transaction Detail screen, similar to BudgetDetailFragment.
 * Shows all information of a single transaction.
 */
public class TransactionDetailFragment extends Fragment {

    private static final String ARG_TRANSACTION_ID = "transaction_id";

    private long transactionId = -1L;

    public static TransactionDetailFragment newInstance(long transactionId) {
        TransactionDetailFragment fragment = new TransactionDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TRANSACTION_ID, transactionId);
        fragment.setArguments(args);
        return fragment;
    }

    public TransactionDetailFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        GeneralHeaderController.setup(root, this);
        setupTransactionDetail(root);

        return root;
    }

    private void setupTransactionDetail(View root) {
        Bundle args = getArguments();
        if (args == null) return;

        transactionId = args.getLong(ARG_TRANSACTION_ID, -1L);
        if (transactionId <= 0) {
            return;
        }

        final Transaction transaction = TransactionManager.getTransactionById(requireContext(), transactionId);
        if (transaction == null) {
            return;
        }

        // Header: back + title + edit
        ImageButton btnBack = root.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager().popBackStack()
            );
        }

        TextView tvTitle = root.findViewById(R.id.tv_transaction_title);
        if (tvTitle != null) {
            String merchantName = transaction.getMerchantName();
            if (merchantName == null || merchantName.isEmpty()) {
                merchantName = getString(R.string.title);
            }
            tvTitle.setText(merchantName);
        }

        ImageButton btnEdit = root.findViewById(R.id.btn_edit);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> showEditTransactionDialog(transaction));
        }

        // Get transaction type
        boolean isIncome = transaction.getType() == TransactionType.INCOME;
        boolean isExpense = transaction.getType() == TransactionType.EXPENSE;

        // Amount display (prominent)
        TextView tvAmount = root.findViewById(R.id.tv_transaction_amount);
        TextView tvTransactionType = root.findViewById(R.id.tv_transaction_type);
        if (tvAmount != null) {
            String amountText = isIncome
                    ? "+" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount())
                    : "-" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount());
            tvAmount.setText(amountText);
            int colorRes = isIncome ? R.color.secondary_green : R.color.primary_red;
            tvAmount.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        }
        if (tvTransactionType != null) {
            String typeText = isIncome ? getString(R.string.income) 
                    : isExpense ? getString(R.string.expense) 
                    : getString(R.string.others);
            tvTransactionType.setText(typeText);
        }

        // Category icon and name
        ImageView ivIcon = root.findViewById(R.id.iv_transaction_icon);
        TextView tvCategory = root.findViewById(R.id.tv_transaction_category);
        if (ivIcon != null || tvCategory != null) {
            String categoryName = transaction.getCategoryName();
            Integer categoryIconResId = transaction.getCategoryIconResId();

            if (categoryName != null && categoryIconResId != null) {
                if (tvCategory != null) {
                    tvCategory.setText(categoryName);
                }
                if (ivIcon != null) {
                    ivIcon.setImageResource(categoryIconResId);
                    ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_green));
                }
            } else if (transaction.getCategoryId() != null) {
                Category category = findCategoryById(transaction.getCategoryId());
                if (category != null) {
                    if (tvCategory != null) {
                        tvCategory.setText(category.getName());
                    }
                    if (ivIcon != null) {
                        ivIcon.setImageResource(category.getIconResId());
                        ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_green));
                    }
                }
            } else {
                // No category - show default or hide
                if (tvCategory != null) {
                    tvCategory.setText(getString(R.string.category));
                }
            }
        }

        // Date
        TextView tvDate = root.findViewById(R.id.tv_transaction_date);
        if (tvDate != null) {
            Date transactionDate = transaction.getDate();
            if (transactionDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
                tvDate.setText(dateFormat.format(transactionDate));
            } else {
                tvDate.setText(getString(R.string.date));
            }
        }

        // Time
        TextView tvTime = root.findViewById(R.id.tv_transaction_time);
        View containerTime = root.findViewById(R.id.container_time);
        if (tvTime != null && containerTime != null) {
            Date transactionDate = transaction.getDate();
            if (transactionDate != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                tvTime.setText(timeFormat.format(transactionDate));
                containerTime.setVisibility(View.VISIBLE);
            } else {
                containerTime.setVisibility(View.GONE);
            }
        }

        // Wallet
        TextView tvWallet = root.findViewById(R.id.tv_transaction_wallet);
        View containerWallet = root.findViewById(R.id.container_wallet);
        if (tvWallet != null && containerWallet != null) {
            Wallet wallet = WalletManager.getWalletById(requireContext(), transaction.getWalletId());
            if (wallet != null) {
                tvWallet.setText(wallet.getName());
                containerWallet.setVisibility(View.VISIBLE);
            } else {
                containerWallet.setVisibility(View.GONE);
            }
        }

        // Location
        TextView tvLocation = root.findViewById(R.id.tv_transaction_location);
        View containerLocation = root.findViewById(R.id.container_location);
        if (tvLocation != null && containerLocation != null) {
            String address = transaction.getAddress();
            if (address != null && !address.isEmpty()) {
                tvLocation.setText(address);
                containerLocation.setVisibility(View.VISIBLE);
            } else {
                containerLocation.setVisibility(View.GONE);
            }
        }

        // Note
        TextView tvNote = root.findViewById(R.id.tv_transaction_note);
        View containerNote = root.findViewById(R.id.container_note);
        if (tvNote != null && containerNote != null) {
            String note = transaction.getNote();
            if (note != null && !note.isEmpty()) {
                tvNote.setText(note);
                containerNote.setVisibility(View.VISIBLE);
            } else {
                containerNote.setVisibility(View.GONE);
            }
        }

        // Delete button
        MaterialButton btnDelete = root.findViewById(R.id.btn_delete);
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog(transaction));
        }
    }

    private Category findCategoryById(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        for (Category category : MockCategoryData.getSampleCategories()) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }

    private void showDeleteConfirmationDialog(Transaction transaction) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_wallet_delete_confirmation, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete);

        if (dialogTitle != null) {
            dialogTitle.setText(R.string.transaction_delete_title);
        }
        if (dialogMessage != null) {
            dialogMessage.setText(R.string.transaction_delete_message);
        }
        if (btnDelete != null) {
            btnDelete.setText(R.string.transaction_delete_button);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                // Adjust wallet balance before removing transaction
                Wallet wallet = WalletManager.getWalletById(requireContext(), transaction.getWalletId());
                if (wallet != null) {
                    long amount = transaction.getAmount();
                    if (transaction.getType() == TransactionType.INCOME) {
                        wallet.setBalance(wallet.getBalance() - amount);
                    } else {
                        // EXPENSE or OTHERS increase wallet balance when deleted
                        wallet.setBalance(wallet.getBalance() + amount);
                    }
                    WalletManager.updateWallet(requireContext(), wallet.getName(), wallet);
                }

                // Remove transaction
                TransactionManager.removeTransaction(requireContext(), transaction.getId());

                Toast.makeText(requireContext(),
                        getString(R.string.transaction_deleted),
                        Toast.LENGTH_SHORT).show();

                dialog.dismiss();

                // Close detail screen
                if (isAdded()) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void showEditTransactionDialog(Transaction transaction) {
        if (getContext() == null) return;

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
            new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_transaction_edit, null, false);
        dialog.setContentView(dialogView);

        // Configure bottom sheet to expand fully
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = 
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
            behavior.setDraggable(false);
        }

        // Get views
        MaterialButton tabExpense = dialogView.findViewById(R.id.tabExpense);
        MaterialButton tabIncome = dialogView.findViewById(R.id.tabIncome);
        MaterialButton tabOthers = dialogView.findViewById(R.id.tabOthers);
        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        EditText editAmount = dialogView.findViewById(R.id.editAmount);
        EditText editNote = dialogView.findViewById(R.id.editNote);
        TextView tvDate = dialogView.findViewById(R.id.tvDate);
        TextView tvTime = dialogView.findViewById(R.id.tvTime);
        TextView tvCategory = dialogView.findViewById(R.id.tvCategory);
        TextView tvWallet = dialogView.findViewById(R.id.tvWallet);
        TextView tvLocation = dialogView.findViewById(R.id.tvLocation);
        TextView tvCurrency = dialogView.findViewById(R.id.tv_currency);
        ImageView ivCategoryIcon = dialogView.findViewById(R.id.ivCategoryIcon);
        View cardDate = dialogView.findViewById(R.id.cardDate);
        View cardTime = dialogView.findViewById(R.id.cardTime);
        View cardCategory = dialogView.findViewById(R.id.cardCategory);
        View cardWallet = dialogView.findViewById(R.id.cardWallet);
        View cardLocation = dialogView.findViewById(R.id.cardLocation);
        MaterialButton btnCancel = dialogView.findViewById(R.id.tvCancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

        // Initialize data from transaction
        final String[] selectedType = {transaction.getType() == TransactionType.INCOME ? "income"
                : transaction.getType() == TransactionType.EXPENSE ? "expense" : "others"};
        Calendar selectedDate = Calendar.getInstance();
        Calendar selectedTime = Calendar.getInstance();
        if (transaction.getDate() != null) {
            selectedDate.setTime(transaction.getDate());
            selectedTime.setTime(transaction.getDate());
        }
        final Wallet[] selectedWallet = {WalletManager.getWalletById(requireContext(), transaction.getWalletId())};
        final String[] selectedCategory = {transaction.getCategoryName()};
        final Integer[] selectedCategoryIconResId = {transaction.getCategoryIconResId()};
        AtomicReference<String> selectedLocationAddress = new AtomicReference<>(transaction.getAddress());
        AtomicReference<Double> selectedLocationLat = new AtomicReference<>(transaction.getLatitude());
        AtomicReference<Double> selectedLocationLng = new AtomicReference<>(transaction.getLongitude());

        // Pre-populate fields
        if (editTitle != null) {
            editTitle.setText(transaction.getMerchantName() != null ? transaction.getMerchantName() : "");
        }
        if (editAmount != null) {
            // Convert from VND to display currency for editing
            double amountVnd = transaction.getAmount();
            String selectedCurrency = SettingsHandler.getCurrency(requireContext());
            if ("USD".equals(selectedCurrency)) {
                double exchangeRate = SettingsHandler.getExchangeRate(requireContext());
                double amountUsd = amountVnd / exchangeRate;
                editAmount.setText(String.format(Locale.US, "%.2f", amountUsd));
            } else {
                editAmount.setText(String.valueOf(amountVnd));
            }
            NumberInputFormatter.attach(editAmount, null);
        }
        if (editNote != null) {
            editNote.setText(transaction.getNote() != null ? transaction.getNote() : "");
        }
        updateCurrencyText(tvCurrency);
        updateTabSelection(tabExpense, tabIncome, tabOthers, selectedType[0]);
        updateDateText(tvDate, selectedDate);
        updateTimeText(tvTime, selectedTime);
        updateCategoryText(tvCategory, ivCategoryIcon, selectedCategory[0], selectedCategoryIconResId[0]);
        updateWalletText(tvWallet, selectedWallet[0]);
        updateLocationText(tvLocation, selectedLocationAddress.get());

        // Setup tabs
        tabExpense.setOnClickListener(v -> {
            selectedType[0] = "expense";
            updateTabSelection(tabExpense, tabIncome, tabOthers, selectedType[0]);
        });
        tabIncome.setOnClickListener(v -> {
            selectedType[0] = "income";
            updateTabSelection(tabExpense, tabIncome, tabOthers, selectedType[0]);
        });
        tabOthers.setOnClickListener(v -> {
            selectedType[0] = "others";
            updateTabSelection(tabExpense, tabIncome, tabOthers, selectedType[0]);
        });

        // Setup date picker
        cardDate.setOnClickListener(v -> {
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
                    updateDateText(tvDate, selectedDate);
                }
            });
            picker.show(getParentFragmentManager(), "material_date_picker");
        });

        // Setup time picker
        cardTime.setOnClickListener(v -> showTimePickerDialog(tvTime, selectedTime));

        // Setup category selection
        cardCategory.setOnClickListener(v -> {
            CategorySelectBottomSheet sheet = CategorySelectBottomSheet.newInstance(R.string.select_category_title);
            sheet.setOnCategorySelectedListener(new CategorySelectBottomSheet.OnCategorySelectedListener() {
                @Override
                public void onCategorySelected(CategorySelectBottomSheet.CategoryOption option) {
                    selectedCategory[0] = option.name;
                    selectedCategoryIconResId[0] = option.iconResId;
                    updateCategoryText(tvCategory, ivCategoryIcon, selectedCategory[0], selectedCategoryIconResId[0]);
                }

                @Override
                public void onAddCategoryRequested() {
                    CategoryCreateBottomSheet createBottomSheet = CategoryCreateBottomSheet.newInstance();
                    createBottomSheet.setOnCategoryCreateListener((name, iconResId) -> {
                        selectedCategory[0] = name;
                        selectedCategoryIconResId[0] = iconResId;
                        updateCategoryText(tvCategory, ivCategoryIcon, selectedCategory[0], selectedCategoryIconResId[0]);
                    });
                    createBottomSheet.show(getParentFragmentManager(), "CategoryCreateBottomSheet");
                }
            });
            sheet.show(getParentFragmentManager(), CategorySelectBottomSheet.TAG);
        });

        // Setup wallet selection
        cardWallet.setOnClickListener(v -> {
            WalletSelectBottomSheet sheet = WalletSelectBottomSheet.newInstance(selectedWallet[0]);
            sheet.setOnWalletSelectedListener(new WalletSelectBottomSheet.OnWalletSelectedListener() {
                @Override
                public void onWalletSelected(Wallet wallet) {
                    selectedWallet[0] = wallet;
                    updateWalletText(tvWallet, selectedWallet[0]);
                }

                @Override
                public void onNoneSelected() {
                    selectedWallet[0] = null;
                    updateWalletText(tvWallet, null);
                }

                @Override
                public void onCreateWalletRequested() {
                    // Handle wallet creation if needed
                }
            });
            sheet.show(getParentFragmentManager(), WalletSelectBottomSheet.TAG);
        });

        // Setup location selection
        cardLocation.setOnClickListener(v -> {
            android.content.SharedPreferences locationPrefs = requireContext().getSharedPreferences("location_selection", android.content.Context.MODE_PRIVATE);
            locationPrefs.edit()
                    .putBoolean("has_location", false)
                    .apply();
            
            MapFragment mapFragment = MapFragment.newInstanceForLocationSelection();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mapFragment, "MAP_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
            
            // Listen for location result
            requireActivity().getSupportFragmentManager().setFragmentResultListener(
                MapFragment.RESULT_KEY_LOCATION,
                this,
                (requestKey, result) -> {
                    if (MapFragment.RESULT_KEY_LOCATION.equals(requestKey)) {
                        selectedLocationAddress.set(result.getString(MapFragment.RESULT_LOCATION_ADDRESS));
                        selectedLocationLat.set(result.getDouble(MapFragment.RESULT_LOCATION_LAT));
                        selectedLocationLng.set(result.getDouble(MapFragment.RESULT_LOCATION_LNG));
                        updateLocationText(tvLocation, selectedLocationAddress.get());
                        requireActivity().getSupportFragmentManager().clearFragmentResultListener(MapFragment.RESULT_KEY_LOCATION);
                    }
                }
            );
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            // Validate and save
            String title = editTitle != null ? editTitle.getText().toString().trim() : "";
            String amountText = editAmount != null ? editAmount.getText().toString().trim().replace(",", "") : "";
            String note = editNote != null ? editNote.getText().toString().trim() : "";

            if (amountText.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.error_amount_required), Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedWallet[0] == null) {
                Toast.makeText(requireContext(), getString(R.string.error_wallet_required), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);
                String selectedCurrency = SettingsHandler.getCurrency(requireContext());
                
                // Convert from display currency to VND for storage
                long amountVnd;
                if ("USD".equals(selectedCurrency)) {
                    double exchangeRate = SettingsHandler.getExchangeRate(requireContext());
                    amountVnd = (long) (amount * exchangeRate);
                } else {
                    amountVnd = (long) amount;
                }

                // Combine date and time
                Calendar transactionDateTime = Calendar.getInstance();
                transactionDateTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
                transactionDateTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
                transactionDateTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
                transactionDateTime.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY));
                transactionDateTime.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE));
                transactionDateTime.set(Calendar.SECOND, 0);
                transactionDateTime.set(Calendar.MILLISECOND, 0);

                // Update transaction
                TransactionType transactionType = "income".equals(selectedType[0]) ? TransactionType.INCOME
                        : "expense".equals(selectedType[0]) ? TransactionType.EXPENSE : TransactionType.OTHERS;

                // Get old wallet to update balance
                Wallet oldWallet = WalletManager.getWalletById(requireContext(), transaction.getWalletId());
                TransactionType oldType = transaction.getType();
                long oldAmount = transaction.getAmount();

                // Update transaction fields
                transaction.setType(transactionType);
                transaction.setAmount(amountVnd);
                transaction.setWalletId(selectedWallet[0].getId());
                transaction.setDate(transactionDateTime.getTime());
                transaction.setMerchantName(title.isEmpty() ? null : title);
                transaction.setNote(note.isEmpty() ? null : note);
                
                if (selectedCategory[0] != null && selectedCategoryIconResId[0] != null && transactionType == TransactionType.EXPENSE) {
                    transaction.setCategoryName(selectedCategory[0]);
                    transaction.setCategoryIconResId(selectedCategoryIconResId[0]);
                    long categoryId = (long) (selectedCategory[0].hashCode() * 31 + selectedCategoryIconResId[0]);
                    transaction.setCategoryId(categoryId);
                } else {
                    transaction.setCategoryName(null);
                    transaction.setCategoryIconResId(null);
                    transaction.setCategoryId(null);
                }

                if (selectedLocationAddress.get() != null && !selectedLocationAddress.get().isEmpty()) {
                    transaction.setAddress(selectedLocationAddress.get());
                    transaction.setLatitude(selectedLocationLat.get());
                    transaction.setLongitude(selectedLocationLng.get());
                } else {
                    transaction.setAddress(null);
                    transaction.setLatitude(null);
                    transaction.setLongitude(null);
                }

                // Update wallet balances
                if (oldWallet != null) {
                    // Revert old transaction effect
                    if (oldType == TransactionType.INCOME) {
                        oldWallet.setBalance(oldWallet.getBalance() - oldAmount);
                    } else {
                        oldWallet.setBalance(oldWallet.getBalance() + oldAmount);
                    }
                    WalletManager.updateWallet(requireContext(), oldWallet.getName(), oldWallet);
                }

                // Apply new transaction effect
                if (transactionType == TransactionType.INCOME) {
                    selectedWallet[0].setBalance(selectedWallet[0].getBalance() + amountVnd);
                } else {
                    selectedWallet[0].setBalance(selectedWallet[0].getBalance() - amountVnd);
                }
                WalletManager.updateWallet(requireContext(), selectedWallet[0].getName(), selectedWallet[0]);

                // Save updated transaction
                TransactionManager.updateTransaction(requireContext(), transaction);

                Toast.makeText(requireContext(), getString(R.string.transaction_updated), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                
                // Refresh the detail view
                if (getView() != null) {
                    setupTransactionDetail(getView());
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), getString(R.string.error_amount_invalid), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateTabSelection(MaterialButton tabExpense, MaterialButton tabIncome, MaterialButton tabOthers, String selectedType) {
        TabStyleUtils.applyUnselectedStyle(requireContext(), tabExpense);
        TabStyleUtils.applyUnselectedStyle(requireContext(), tabIncome);
        TabStyleUtils.applyUnselectedStyle(requireContext(), tabOthers);

        MaterialButton selectedTab = null;
        switch (selectedType) {
            case "expense":
                selectedTab = tabExpense;
                break;
            case "income":
                selectedTab = tabIncome;
                break;
            case "others":
                selectedTab = tabOthers;
                break;
        }
        if (selectedTab != null) {
            TabStyleUtils.applySelectedStyle(requireContext(), selectedTab);
        }
    }

    private void updateDateText(TextView tvDate, Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(date.getTime()));
    }

    private void updateTimeText(TextView tvTime, Calendar time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvTime.setText(sdf.format(time.getTime()));
    }

    private void updateCategoryText(TextView tvCategory, ImageView ivCategoryIcon, String category, Integer iconResId) {
        if (category != null && iconResId != null) {
            ivCategoryIcon.setImageResource(iconResId);
            ivCategoryIcon.setVisibility(View.VISIBLE);
            tvCategory.setText(category);
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        } else {
            ivCategoryIcon.setVisibility(View.GONE);
            tvCategory.setText(getString(R.string.none));
            tvCategory.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
        }
    }

    private void updateWalletText(TextView tvWallet, Wallet wallet) {
        if (wallet != null) {
            tvWallet.setText(wallet.getName());
            tvWallet.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        } else {
            tvWallet.setText(getString(R.string.none));
            tvWallet.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
        }
    }

    private void updateLocationText(TextView tvLocation, String address) {
        if (address != null && !address.isEmpty()) {
            tvLocation.setText(address);
            tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_black));
        } else {
            tvLocation.setText(getString(R.string.none));
            tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_grey));
        }
    }

    private void updateCurrencyText(TextView tvCurrency) {
        if (tvCurrency != null) {
            String currency = SettingsHandler.getCurrency(requireContext());
            tvCurrency.setText(currency);
        }
    }

    private void showTimePickerDialog(TextView tvTime, Calendar selectedTime) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View timePickerView = inflater.inflate(R.layout.dialog_time_picker, null, false);

        EditText etHour = timePickerView.findViewById(R.id.et_hour);
        EditText etMinute = timePickerView.findViewById(R.id.et_minute);
        MaterialButton btnCancel = timePickerView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = timePickerView.findViewById(R.id.btn_confirm);

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

                if (hour < 0) hour = 0;
                if (hour > 23) hour = 23;
                if (minute < 0) minute = 0;
                if (minute > 59) minute = 59;

                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                updateTimeText(tvTime, selectedTime);
                timeDialog.dismiss();
            } catch (NumberFormatException e) {
                updateTimeText(tvTime, selectedTime);
                timeDialog.dismiss();
            }
        });

        timeDialog.show();
        if (timeDialog.getWindow() != null) {
            timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}


