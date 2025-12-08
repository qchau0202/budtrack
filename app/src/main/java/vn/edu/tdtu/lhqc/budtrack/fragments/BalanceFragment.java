package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.BalanceController;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

public class BalanceFragment extends Fragment {

    public BalanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Listen for transaction creation to refresh balance immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED,
            this,
            (requestKey, result) -> {
                if (TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED.equals(requestKey)) {
                    // Immediately refresh if fragment is visible
                    refreshBalance();
                }
            }
        );
        
        // Listen for currency changes to refresh UI immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            "currency_changed",
            this,
            (requestKey, result) -> {
                if ("currency_changed".equals(requestKey)) {
                    // Refresh balance when currency changes
                    refreshBalance();
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_balance, container, false);
        setupBalanceView(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always refresh when fragment becomes visible
        refreshBalance();
    }
    
    private void refreshBalance() {
        if (getView() != null && isAdded() && !isDetached()) {
            setupBalanceView(getView());
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listeners
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED);
        requireActivity().getSupportFragmentManager().clearFragmentResultListener("currency_changed");
    }

    private void setupBalanceView(View root) {
        TextView tvBalance = root.findViewById(R.id.tv_total_balance_amount);
        ImageButton btnVisibility = root.findViewById(R.id.btn_visibility);

        if (tvBalance != null && btnVisibility != null) {
            // Calculate total balance from actual wallet data
            long totalBalance = BalanceController.calculateTotalBalance(requireContext());
            final String originalBalanceText = CurrencyUtils.formatCurrency(requireContext(), totalBalance);
            
            boolean hidden = BalanceController.isHidden(requireContext());
            tvBalance.setText(BalanceController.formatDisplay(originalBalanceText, hidden));
            btnVisibility.setImageResource(hidden ? R.drawable.ic_visibility_off_24dp : R.drawable.ic_visibility_24dp);

            btnVisibility.setOnClickListener(v -> {
                boolean nowHidden = BalanceController.toggleHidden(requireContext());
                tvBalance.setText(BalanceController.formatDisplay(originalBalanceText, nowHidden));
                btnVisibility.setImageResource(nowHidden ? R.drawable.ic_visibility_off_24dp : R.drawable.ic_visibility_24dp);
            });
        }
    }
}