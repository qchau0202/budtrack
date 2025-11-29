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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;
import vn.edu.tdtu.lhqc.budtrack.utils.NumberInputFormatter;

public class WalletCreateFragment extends BottomSheetDialogFragment {

    public static final String TAG = "WalletCreateFragment";
    public static final String RESULT_KEY = "wallet_created";
    
    private static final String ARG_WALLET_TYPE_NAME = "wallet_type_name";
    private static final String ARG_WALLET_ICON = "wallet_icon";
    
    // Result bundle keys
    public static final String RESULT_WALLET_NAME = "wallet_name";
    public static final String RESULT_WALLET_BALANCE = "wallet_balance";
    public static final String RESULT_WALLET_ICON = "wallet_icon";
    public static final String RESULT_WALLET_TYPE = "wallet_type";

    private EditText editWalletName;
    private EditText editBalance;
    private ImageView ivWalletIcon;
    private TextView tvWalletType;
    private String walletTypeName;
    private int walletIconResId;

    public WalletCreateFragment() {}

    public static WalletCreateFragment newInstance(String walletTypeName, int iconResId) {
        WalletCreateFragment fragment = new WalletCreateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_WALLET_TYPE_NAME, walletTypeName);
        args.putInt(ARG_WALLET_ICON, iconResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        
        // Load arguments
        Bundle args = getArguments();
        if (args != null) {
            walletTypeName = args.getString(ARG_WALLET_TYPE_NAME, "");
            walletIconResId = args.getInt(ARG_WALLET_ICON, R.drawable.ic_wallet_cash);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_bottom_sheet_wallet_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views
        editWalletName = view.findViewById(R.id.edit_wallet_name);
        editBalance = view.findViewById(R.id.edit_balance);
        ivWalletIcon = view.findViewById(R.id.iv_wallet_icon);
        tvWalletType = view.findViewById(R.id.tv_wallet_type);

        // Set wallet icon
        if (ivWalletIcon != null) {
            ivWalletIcon.setImageResource(walletIconResId);
        }

        // Set wallet type title
        if (tvWalletType != null && walletTypeName != null && !walletTypeName.isEmpty()) {
            tvWalletType.setText(walletTypeName);
        }

        // Setup amount formatter for better UX with commas
        if (editBalance != null) {
            NumberInputFormatter.attachIntegerFormatter(editBalance, null);
        }

        // Done button
        view.findViewById(R.id.btn_done).setOnClickListener(v -> createWallet());
    }

    private void createWallet() {
        String walletName = editWalletName != null ? editWalletName.getText().toString().trim() : "";
        String balanceText = editBalance != null ? editBalance.getText().toString().trim() : "";

        // Validate wallet name
        if (walletName.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.wallet_name_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse balance
        long balance = 0;
        if (!balanceText.isEmpty()) {
            try {
                balance = CurrencyUtils.parseFormattedNumberLong(balanceText);
                
                // Validate: balance cannot be negative
                if (balance < 0) {
                    Toast.makeText(requireContext(), getString(R.string.wallet_balance_cannot_be_negative), Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), getString(R.string.wallet_balance_invalid), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Send result back to parent fragment
        Bundle result = new Bundle();
        result.putString(RESULT_WALLET_NAME, walletName);
        result.putLong(RESULT_WALLET_BALANCE, balance);
        result.putInt(RESULT_WALLET_ICON, walletIconResId);
        result.putString(RESULT_WALLET_TYPE, walletTypeName); // Include wallet type
        requireActivity().getSupportFragmentManager().setFragmentResult(RESULT_KEY, result);

        Toast.makeText(requireContext(), getString(R.string.wallet_created), Toast.LENGTH_SHORT).show();
        dismiss();
    }
}

