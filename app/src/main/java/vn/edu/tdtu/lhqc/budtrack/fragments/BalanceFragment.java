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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BalanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BalanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BalanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BalanceFragment newInstance(String param1, String param2) {
        BalanceFragment fragment = new BalanceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        
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
        // Clean up Fragment Result listener
        requireActivity().getSupportFragmentManager().clearFragmentResultListener(
            TransactionCreateFragment.RESULT_KEY_TRANSACTION_CREATED);
    }

    private void setupBalanceView(View root) {
        TextView tvBalance = root.findViewById(R.id.tv_total_balance_amount);
        ImageButton btnVisibility = root.findViewById(R.id.btn_visibility);

        if (tvBalance != null && btnVisibility != null) {
            // Calculate total balance from actual wallet data
            long totalBalance = BalanceController.calculateTotalBalance(requireContext());
            final String originalBalanceText = CurrencyUtils.formatCurrency(totalBalance);
            
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