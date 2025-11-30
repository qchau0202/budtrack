package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

public class WalletSelectBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "WalletSelectBottomSheet";

    public interface OnWalletSelectedListener {
        void onWalletSelected(Wallet wallet);
        void onNoneSelected();
    }

    private OnWalletSelectedListener listener;
    private Wallet selectedWallet;
    private List<Wallet> wallets;
    private LinearLayout containerWallets;
    private View cardWalletNone;
    private ImageView ivWalletNoneCheck;

    public static WalletSelectBottomSheet newInstance() {
        return new WalletSelectBottomSheet();
    }

    public static WalletSelectBottomSheet newInstance(Wallet currentWallet) {
        WalletSelectBottomSheet sheet = new WalletSelectBottomSheet();
        Bundle args = new Bundle();
        if (currentWallet != null) {
            args.putLong("wallet_id", currentWallet.getId());
        }
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnWalletSelectedListener(OnWalletSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        wallets = WalletManager.getWallets(requireContext());
        
        // Get current selected wallet from arguments
        if (getArguments() != null) {
            long walletId = getArguments().getLong("wallet_id", -1);
            if (walletId != -1) {
                for (Wallet wallet : wallets) {
                    if (wallet.getId() == walletId) {
                        selectedWallet = wallet;
                        break;
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_bottom_sheet_wallet_select, container, false);
        initViews(view);
        setupWallets();
        return view;
    }

    private void initViews(View view) {
        ImageButton btnBack = view.findViewById(R.id.btnWalletBack);
        containerWallets = view.findViewById(R.id.containerWallets);
        cardWalletNone = view.findViewById(R.id.cardWalletNone);
        ivWalletNoneCheck = view.findViewById(R.id.ivWalletNoneCheck);

        btnBack.setOnClickListener(v -> dismiss());

        // Handle "None" selection
        cardWalletNone.setOnClickListener(v -> {
            selectedWallet = null;
            updateSelectionUI();
            if (listener != null) {
                listener.onNoneSelected();
            }
            dismiss();
        });
    }

    private void setupWallets() {
        if (containerWallets == null) return;

        // Add wallet items dynamically (exclude archived wallets)
        for (Wallet wallet : wallets) {
            if (!wallet.isArchived()) {
            View walletView = createWalletItem(wallet);
            containerWallets.addView(walletView);
            }
        }

        updateSelectionUI();
    }

    private View createWalletItem(Wallet wallet) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        LinearLayout walletCard = (LinearLayout) inflater.inflate(
            R.layout.item_wallet_select, containerWallets, false);

        TextView tvWalletName = walletCard.findViewById(R.id.tvWalletName);
        ImageView ivWalletCheck = walletCard.findViewById(R.id.ivWalletCheck);

        // Display wallet name with balance
        String walletName = wallet.getName();
        String balance = CurrencyUtils.formatCurrency(wallet.getBalance());
        tvWalletName.setText(walletName + " - " + balance);

        // Set click listener
        walletCard.setOnClickListener(v -> {
            selectedWallet = wallet;
            updateSelectionUI();
            if (listener != null) {
                listener.onWalletSelected(wallet);
            }
            dismiss();
        });

        // Store wallet reference in tag
        walletCard.setTag(wallet);

        return walletCard;
    }

    private void updateSelectionUI() {
        if (cardWalletNone == null || ivWalletNoneCheck == null || containerWallets == null) {
            return;
        }

        // Update "None" check
        if (selectedWallet == null) {
            ivWalletNoneCheck.setVisibility(View.VISIBLE);
            cardWalletNone.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_selected));
        } else {
            ivWalletNoneCheck.setVisibility(View.GONE);
            cardWalletNone.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_card));
        }

        // Update wallet items (skip the "None" card which is at index 0)
        for (int i = 1; i < containerWallets.getChildCount(); i++) {
            View child = containerWallets.getChildAt(i);
            if (child.getTag() instanceof Wallet) {
                Wallet wallet = (Wallet) child.getTag();
                ImageView ivCheck = child.findViewById(R.id.ivWalletCheck);
                if (ivCheck != null) {
                    if (selectedWallet != null && selectedWallet.getId() == wallet.getId()) {
                        ivCheck.setVisibility(View.VISIBLE);
                        child.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_selected));
                    } else {
                        ivCheck.setVisibility(View.GONE);
                        child.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_card));
                    }
                }
            }
        }
    }
}

