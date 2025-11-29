package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockWalletData;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.NumberInputFormatter;

public class WalletFragment extends Fragment {

    private List<Wallet> wallets;
    private Map<String, View> walletViews; // Map wallet name to its view
    private LinearLayout walletsContainer;
    private TextView tvTotalBalance;

    public WalletFragment() {
        // Required empty public constructor
    }

    public static WalletFragment newInstance() {
        return new WalletFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wallets = new ArrayList<>();
        walletViews = new HashMap<>();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set up Fragment Result listener for new wallet creation (from WalletCreateFragment)
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            WalletCreateFragment.RESULT_KEY,
            this,
            (requestKey, result) -> {
                if (requestKey.equals(WalletCreateFragment.RESULT_KEY)) {
                    String walletName = result.getString(WalletCreateFragment.RESULT_WALLET_NAME);
                    long balance = result.getLong(WalletCreateFragment.RESULT_WALLET_BALANCE);
                    int iconResId = result.getInt(WalletCreateFragment.RESULT_WALLET_ICON);
                    String walletType = result.getString(WalletCreateFragment.RESULT_WALLET_TYPE, walletName);
                    addNewWallet(walletName, balance, iconResId, walletType);
                }
            }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listener
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(WalletCreateFragment.RESULT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wallet, container, false);

        setupWalletList(root);
        setupButtons(root);

        return root;
    }

    private void setupButtons(View root) {
        // Close button (back)
        root.findViewById(R.id.btn_close).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Add wallet button → Show wallet type selection bottom sheet
        root.findViewById(R.id.btn_add_wallet).setOnClickListener(v -> showWalletTypeSelectionBottomSheet());
    }

    private void setupWalletList(View root) {
        walletsContainer = root.findViewById(R.id.wallets_container);
        tvTotalBalance = root.findViewById(R.id.tv_total_balance);

        if (walletsContainer == null) return;

        // Load sample wallet data from mockdata
        wallets = MockWalletData.getSampleWallets();

        // Find the add button index (it's already in the layout)
        View addButton = walletsContainer.findViewById(R.id.btn_add_wallet);
        int addButtonIndex = addButton != null ? walletsContainer.indexOfChild(addButton) : walletsContainer.getChildCount();

        // Add wallet items before the add button
        for (Wallet wallet : wallets) {
            if (!wallet.isCurrentWallet()) {
                View walletView = createWalletItem(walletsContainer, wallet);
                walletsContainer.addView(walletView, addButtonIndex);
                walletViews.put(wallet.getName(), walletView); // Store view for later updates
                addButtonIndex++; // Update index since we're inserting before add button
            }
        }

        // Calculate and display total balance
        updateTotalBalance();
    }

    private void updateWallet(String oldWalletName, String newWalletName, long newBalance) {
        // Find and update wallet data
        Wallet walletToUpdate = null;
        for (Wallet wallet : wallets) {
            if (wallet.getName().equals(oldWalletName)) {
                wallet.setName(newWalletName);
                wallet.setBalance(newBalance);
                walletToUpdate = wallet;
                break;
            }
        }

        if (walletToUpdate == null) return;

        // Update wallet item view
        View walletView = walletViews.get(oldWalletName);
        if (walletView != null) {
            // Update wallet name display
            TextView nameView = walletView.findViewById(R.id.tv_wallet_name);
            if (nameView != null) {
                nameView.setText(newWalletName);
            }

            // Update wallet balance display
            TextView balanceView = walletView.findViewById(R.id.tv_wallet_balance);
            if (balanceView != null) {
                balanceView.setText(CurrencyUtils.formatCurrency(newBalance));
                // Update color based on balance
                int colorRes = newBalance < 0 ? R.color.primary_red : R.color.primary_black;
                balanceView.setTextColor(getResources().getColor(colorRes, null));
            }

            // Update the map key if wallet name changed
            if (!oldWalletName.equals(newWalletName)) {
                walletViews.remove(oldWalletName);
                walletViews.put(newWalletName, walletView);
            }
        }

        // Recalculate and update total balance
        updateTotalBalance();
    }

    private void addNewWallet(String walletName, long balance, int iconResId, String walletType) {
        // Create new wallet using model
        Wallet newWallet = new Wallet(walletName, balance, iconResId, walletType);
        newWallet.setId(wallets.size() + 1); // Simple ID assignment (in real app, database will assign)
        newWallet.setCurrentWallet(false);
        wallets.add(newWallet);

        // Add wallet view to the container
        if (walletsContainer != null) {
            // Find the add button index
            View addButton = walletsContainer.findViewById(R.id.btn_add_wallet);
            int addButtonIndex = addButton != null ? walletsContainer.indexOfChild(addButton) : walletsContainer.getChildCount();

            // Create and add wallet item view
            View walletView = createWalletItem(walletsContainer, newWallet);
            walletsContainer.addView(walletView, addButtonIndex);
            walletViews.put(walletName, walletView); // Store view for later updates
        }

        // Update total balance
        updateTotalBalance();
    }

    private void updateTotalBalance() {
        long totalBalance = 0;
        for (Wallet wallet : wallets) {
            if (!wallet.isCurrentWallet()) {
                totalBalance += wallet.getBalance();
            }
        }

        if (tvTotalBalance != null) {
            tvTotalBalance.setText(CurrencyUtils.formatCurrency(totalBalance));
        }
    }

    private View createWalletItem(ViewGroup parent, Wallet wallet) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View walletView = inflater.inflate(R.layout.item_wallet, parent, false);

        ImageView iconView = walletView.findViewById(R.id.iv_wallet_icon);
        TextView nameView = walletView.findViewById(R.id.tv_wallet_name);
        TextView balanceView = walletView.findViewById(R.id.tv_wallet_balance);

        if (iconView != null) {
            iconView.setImageResource(wallet.getIconResId());
        }

        if (nameView != null) {
            nameView.setText(wallet.getName());
        }

        if (balanceView != null) {
            balanceView.setText(CurrencyUtils.formatCurrency(wallet.getBalance()));
            // Color based on balance (negative = red, positive = black)
            int colorRes = wallet.getBalance() < 0 ? R.color.primary_red : R.color.primary_black;
            balanceView.setTextColor(getResources().getColor(colorRes, null));
        }

        // Make wallet item clickable to edit
        View card = walletView.findViewById(R.id.card_wallet_item);
        if (card != null) {
            card.setOnClickListener(v -> showWalletEditBottomSheet(wallet));
        }

        return walletView;
    }

    private void showWalletTypeSelectionBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.view_bottom_sheet_wallet_type_selection, null);
        dialog.setContentView(view);

        // Configure bottom sheet to expand fully
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }

        // Setup click listeners
        view.findViewById(R.id.card_basic_wallet).setOnClickListener(v -> {
            dialog.dismiss();
            openWalletCreate(getString(R.string.wallet_type_basic), R.drawable.ic_wallet_cash);
        });

        view.findViewById(R.id.card_investment_wallet).setOnClickListener(v -> {
            dialog.dismiss();
            openWalletCreate(getString(R.string.wallet_type_investment), R.drawable.ic_wallet_cash);
        });

        view.findViewById(R.id.card_savings_wallet).setOnClickListener(v -> {
            dialog.dismiss();
            openWalletCreate(getString(R.string.wallet_type_savings), R.drawable.ic_wallet_cash);
        });

        dialog.show();
    }

    private void openWalletCreate(String walletTypeName, int iconResId) {
        WalletCreateFragment createFragment = WalletCreateFragment.newInstance(walletTypeName, iconResId);
        createFragment.show(requireActivity().getSupportFragmentManager(), WalletCreateFragment.TAG);
    }

    private void showWalletEditBottomSheet(Wallet wallet) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.view_bottom_sheet_wallet_edit, null);
        dialog.setContentView(view);

        // Configure bottom sheet to expand fully
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }

        // Get views
        EditText editWalletName = view.findViewById(R.id.edit_wallet_name);
        TextView tvWalletType = view.findViewById(R.id.tv_wallet_type);
        EditText editBalance = view.findViewById(R.id.edit_balance);
        ImageView ivWalletIcon = view.findViewById(R.id.iv_wallet_icon);
        SwitchCompat switchNotification = view.findViewById(R.id.switch_notification);
        SwitchCompat switchExcludeTotal = view.findViewById(R.id.switch_exclude_total);
        SwitchCompat switchArchive = view.findViewById(R.id.switch_archive);

        // Set wallet type
        if (tvWalletType != null) {
            tvWalletType.setText(wallet.getWalletType() != null ? wallet.getWalletType() : wallet.getName());
        }

        // Set wallet name in the edit field
        if (editWalletName != null) {
            editWalletName.setText(wallet.getName());
            // Move cursor to end so user can edit
            editWalletName.setSelection(wallet.getName().length());
        }

        // Set wallet icon
        if (ivWalletIcon != null) {
            ivWalletIcon.setImageResource(wallet.getIconResId());
        }

        // Set current balance in the edit field with formatted value
        if (editBalance != null) {
            String formattedBalance = CurrencyUtils.formatNumberUS(wallet.getBalance());
            
            // Setup amount formatter for better UX with commas
            NumberInputFormatter.attachIntegerFormatter(editBalance, formattedBalance);
            
            editBalance.setText(formattedBalance);
            // Move cursor to end so user can edit
            editBalance.setSelection(formattedBalance.length());
        }

        // Store original wallet name for update
        String oldWalletName = wallet.getName();

        // Done button
        view.findViewById(R.id.btn_done).setOnClickListener(v -> {
            saveWalletChanges(dialog, editWalletName, editBalance, oldWalletName);
        });

        // Delete button
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            showDeleteConfirmationDialog(dialog, editWalletName != null ? editWalletName.getText().toString().trim() : wallet.getName());
        });

        dialog.show();
    }

    private void saveWalletChanges(BottomSheetDialog dialog, EditText editWalletName, EditText editBalance, String oldWalletName) {
        String walletName = editWalletName != null ? editWalletName.getText().toString().trim() : "";
        String balanceText = editBalance != null ? editBalance.getText().toString().trim() : "";

        // Validate wallet name
        if (walletName.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.wallet_name_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Save balance if entered (parse formatted number)
        long newBalance = 0;
        if (!balanceText.isEmpty()) {
            try {
                newBalance = CurrencyUtils.parseFormattedNumberLong(balanceText);

                // Validate: balance cannot be negative
                if (newBalance < 0) {
                    Toast.makeText(requireContext(), getString(R.string.wallet_balance_cannot_be_negative), Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), getString(R.string.wallet_balance_invalid), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Update wallet
        updateWallet(oldWalletName, walletName, newBalance);

        Toast.makeText(requireContext(), getString(R.string.wallet_changes_saved), Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    private void showDeleteConfirmationDialog(BottomSheetDialog parentDialog, String walletName) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_wallet_delete_confirmation, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = dialogView.findViewById(R.id.btn_delete);

        // Set message with wallet name
        dialogMessage.setText(getString(R.string.wallet_delete_message, walletName));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
                    // TODO: Delete wallet from database
                    Toast.makeText(requireContext(), 
                        getString(R.string.wallet_deleted, walletName), 
                        Toast.LENGTH_SHORT).show();
            dialog.dismiss();
                    parentDialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

}