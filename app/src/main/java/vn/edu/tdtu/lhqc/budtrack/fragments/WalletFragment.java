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
import com.google.android.material.switchmaterial.SwitchMaterial;
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
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.NumberInputFormatter;

public class WalletFragment extends Fragment {

    private List<Wallet> wallets;
    private Map<String, View> walletViews; // Map wallet name to its view
    private LinearLayout walletsContainer;
    private LinearLayout availableWalletsContainer;
    private LinearLayout archivedWalletsContainer;
    private TextView tvTotalBalance;
    private TextView tvAvailableWalletsHeader;
    private TextView tvArchivedWalletsHeader;

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
        
        // Listen for transaction creation to refresh wallet balances immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED,
            this,
            (requestKey, result) -> {
                if (TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED.equals(requestKey)) {
                    // Refresh wallet list when a transaction is created (to update balances)
                    refreshWalletList();
                }
            }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always refresh when fragment becomes visible
        if (getView() != null) {
            refreshWalletList();
        }
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
        availableWalletsContainer = root.findViewById(R.id.available_wallets_container);
        archivedWalletsContainer = root.findViewById(R.id.archived_wallets_container);
        tvTotalBalance = root.findViewById(R.id.tv_total_balance);
        tvAvailableWalletsHeader = root.findViewById(R.id.tv_available_wallets_header);
        tvArchivedWalletsHeader = root.findViewById(R.id.tv_archived_wallets_header);

        if (walletsContainer == null || availableWalletsContainer == null || archivedWalletsContainer == null) return;

        refreshWalletList();
    }

    private void refreshWalletList() {
        if (availableWalletsContainer == null || archivedWalletsContainer == null) return;

        // Clear existing views
        availableWalletsContainer.removeAllViews();
        archivedWalletsContainer.removeAllViews();
        walletViews.clear();

        // Load wallet data from WalletManager
        wallets = WalletManager.getWallets(requireContext());

        // Separate wallets into available and archived
        List<Wallet> availableWallets = new ArrayList<>();
        List<Wallet> archivedWallets = new ArrayList<>();

        for (Wallet wallet : wallets) {
            if (!wallet.isCurrentWallet()) {
                if (wallet.isArchived()) {
                    archivedWallets.add(wallet);
                } else {
                    availableWallets.add(wallet);
                }
            }
        }

        // Add available wallets
        for (Wallet wallet : availableWallets) {
            View walletView = createWalletItem(availableWalletsContainer, wallet);
            availableWalletsContainer.addView(walletView);
                walletViews.put(wallet.getName(), walletView); // Store view for later updates
        }

        // Add archived wallets
        for (Wallet wallet : archivedWallets) {
            View walletView = createWalletItem(archivedWalletsContainer, wallet);
            archivedWalletsContainer.addView(walletView);
            walletViews.put(wallet.getName(), walletView); // Store view for later updates
        }

        // Show/hide headers based on whether there are wallets in each section
        if (tvAvailableWalletsHeader != null) {
            tvAvailableWalletsHeader.setVisibility(availableWallets.isEmpty() ? View.GONE : View.VISIBLE);
        }
        if (tvArchivedWalletsHeader != null) {
            tvArchivedWalletsHeader.setVisibility(archivedWallets.isEmpty() ? View.GONE : View.VISIBLE);
        }

        // Calculate and display total balance
        updateTotalBalance();
    }

    private void updateWallet(String oldWalletName, String newWalletName, long newBalance, boolean excludeFromTotal, boolean isArchived) {
        // Find and update wallet data
        Wallet walletToUpdate = null;
        boolean wasArchived = false;
        for (Wallet wallet : wallets) {
            if (wallet.getName().equals(oldWalletName)) {
                wasArchived = wallet.isArchived();
                wallet.setName(newWalletName);
                wallet.setBalance(newBalance);
                wallet.setExcludeFromTotal(excludeFromTotal);
                wallet.setArchived(isArchived);
                walletToUpdate = wallet;
                break;
            }
        }

        if (walletToUpdate == null) return;

        // Save to WalletManager
        WalletManager.updateWallet(requireContext(), oldWalletName, walletToUpdate);

        // Get the existing wallet view
        View walletView = walletViews.get(oldWalletName);
        
        // If archived status changed, move wallet between sections
        if (wasArchived != isArchived && walletView != null) {
            // Remove from old container
            ViewGroup oldParent = (ViewGroup) walletView.getParent();
            if (oldParent != null) {
                oldParent.removeView(walletView);
            }
            
            // Add to new container
            if (isArchived) {
                if (archivedWalletsContainer != null) {
                    archivedWalletsContainer.addView(walletView);
                }
            } else {
                if (availableWalletsContainer != null) {
                    availableWalletsContainer.addView(walletView);
                }
            }
        }

        // Update wallet item view
        if (walletView != null) {
            // Update wallet name display
            TextView nameView = walletView.findViewById(R.id.tv_wallet_name);
            if (nameView != null) {
                nameView.setText(newWalletName);
                // Update text color for archived wallets
                int nameColorRes = isArchived ? R.color.primary_grey : R.color.primary_black;
                nameView.setTextColor(getResources().getColor(nameColorRes, null));
            }

            // Update wallet balance display
            TextView balanceView = walletView.findViewById(R.id.tv_wallet_balance);
            if (balanceView != null) {
                balanceView.setText(CurrencyUtils.formatCurrency(newBalance));
                // Update color based on balance and archived state
                int colorRes;
                if (isArchived) {
                    colorRes = R.color.primary_grey;
                } else {
                    colorRes = newBalance < 0 ? R.color.primary_red : R.color.primary_black;
                }
                balanceView.setTextColor(getResources().getColor(colorRes, null));
            }

            // Update card background for archived wallets
            View card = walletView.findViewById(R.id.card_wallet_item);
            if (card != null) {
                if (isArchived) {
                    card.setAlpha(0.5f); // Make archived wallets appear faded
                } else {
                    card.setAlpha(1.0f);
                }
            }

            // Update the map key if wallet name changed
            if (!oldWalletName.equals(newWalletName)) {
                walletViews.remove(oldWalletName);
                walletViews.put(newWalletName, walletView);
            }
        }

        // Reload wallet list to reflect changes (especially if archived status changed)
        refreshWalletList();
    }

    private void addNewWallet(String walletName, long balance, int iconResId, String walletType) {
        // Create new wallet using model
        Wallet newWallet = new Wallet(walletName, balance, iconResId, walletType);
        newWallet.setId(wallets.size() + 1); // Simple ID assignment (in real app, database will assign)
        newWallet.setCurrentWallet(false);
        wallets.add(newWallet);

        // Save to WalletManager
        WalletManager.addWallet(requireContext(), newWallet);

        // Add wallet view to the appropriate container (always available, never archived when created)
        if (availableWalletsContainer != null) {
            // Create and add wallet item view
            View walletView = createWalletItem(availableWalletsContainer, newWallet);
            availableWalletsContainer.addView(walletView);
            walletViews.put(walletName, walletView); // Store view for later updates
        }

        // Reload wallet list to reflect new wallet
        refreshWalletList();
    }

    private void updateTotalBalance() {
        long totalBalance = 0;
        for (Wallet wallet : wallets) {
            if (!wallet.isCurrentWallet() && !wallet.isExcludeFromTotal()) {
                totalBalance += wallet.getBalance();
            }
        }

        if (tvTotalBalance != null) {
            tvTotalBalance.setText(CurrencyUtils.formatCurrency(totalBalance));
        }
    }

    private void updateSectionHeaders() {
        // Count available and archived wallets
        int availableCount = 0;
        int archivedCount = 0;
        
        for (Wallet wallet : wallets) {
            if (!wallet.isCurrentWallet()) {
                if (wallet.isArchived()) {
                    archivedCount++;
                } else {
                    availableCount++;
                }
            }
        }

        // Show/hide headers based on whether there are wallets in each section
        if (tvAvailableWalletsHeader != null) {
            tvAvailableWalletsHeader.setVisibility(availableCount > 0 ? View.VISIBLE : View.GONE);
        }
        if (tvArchivedWalletsHeader != null) {
            tvArchivedWalletsHeader.setVisibility(archivedCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private View createWalletItem(ViewGroup parent, Wallet wallet) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View walletView = inflater.inflate(R.layout.item_wallet, parent, false);

        ImageView iconView = walletView.findViewById(R.id.iv_wallet_icon);
        TextView nameView = walletView.findViewById(R.id.tv_wallet_name);
        TextView balanceView = walletView.findViewById(R.id.tv_wallet_balance);
        View card = walletView.findViewById(R.id.card_wallet_item);

        if (iconView != null) {
            iconView.setImageResource(wallet.getIconResId());
        }

        if (nameView != null) {
            nameView.setText(wallet.getName());
            // Set text color based on archived state
            int nameColorRes = wallet.isArchived() ? R.color.primary_grey : R.color.primary_black;
            nameView.setTextColor(getResources().getColor(nameColorRes, null));
        }

        if (balanceView != null) {
            balanceView.setText(CurrencyUtils.formatCurrency(wallet.getBalance()));
            // Color based on balance and archived state
            int colorRes;
            if (wallet.isArchived()) {
                colorRes = R.color.primary_grey;
            } else {
                colorRes = wallet.getBalance() < 0 ? R.color.primary_red : R.color.primary_black;
            }
            balanceView.setTextColor(getResources().getColor(colorRes, null));
        }

        // Update card appearance for archived wallets
        if (card != null) {
            if (wallet.isArchived()) {
                card.setAlpha(0.5f); // Make archived wallets appear faded
            } else {
                card.setAlpha(1.0f);
            }
            // Make wallet item clickable to edit
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
        SwitchMaterial switchExcludeTotal = view.findViewById(R.id.switch_exclude_total);
        SwitchMaterial switchArchive = view.findViewById(R.id.switch_archive);

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

        // Set switch states
        if (switchExcludeTotal != null) {
            switchExcludeTotal.setChecked(wallet.isExcludeFromTotal());
        }
        if (switchArchive != null) {
            switchArchive.setChecked(wallet.isArchived());
        }

        // Store original wallet name for update
        String oldWalletName = wallet.getName();

        // Done button
        view.findViewById(R.id.btn_done).setOnClickListener(v -> {
            saveWalletChanges(dialog, editWalletName, editBalance, switchExcludeTotal, switchArchive, oldWalletName);
        });

        // Delete button
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            showDeleteConfirmationDialog(dialog, editWalletName != null ? editWalletName.getText().toString().trim() : wallet.getName());
        });

        dialog.show();
    }

    private void saveWalletChanges(BottomSheetDialog dialog, EditText editWalletName, EditText editBalance, 
                                   SwitchMaterial switchExcludeTotal, SwitchMaterial switchArchive, String oldWalletName) {
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

        // Get switch states
        boolean excludeFromTotal = switchExcludeTotal != null && switchExcludeTotal.isChecked();
        boolean isArchived = switchArchive != null && switchArchive.isChecked();

        // Update wallet
        updateWallet(oldWalletName, walletName, newBalance, excludeFromTotal, isArchived);

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
            // Delete wallet from WalletManager
            WalletManager.removeWallet(requireContext(), walletName);
            
                    Toast.makeText(requireContext(), 
                        getString(R.string.wallet_deleted, walletName), 
                        Toast.LENGTH_SHORT).show();
            
            dialog.dismiss();
                    parentDialog.dismiss();
            
            // Reload wallet list to reflect deletion
            refreshWalletList();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

}