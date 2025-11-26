package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import vn.edu.tdtu.lhqc.budtrack.R;

public class WalletTypeSelectionFragment extends Fragment {

    public WalletTypeSelectionFragment() {
        // Required empty public constructor
    }

    public static WalletTypeSelectionFragment newInstance() {
        return new WalletTypeSelectionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wallet_type_selection, container, false);

        // Close button
        root.findViewById(R.id.tvClose).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Basic wallet card
        CardView cardBasic = root.findViewById(R.id.card_basic_wallet);
        cardBasic.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tạo ví cơ bản", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to wallet creation form with type = BASIC
        });

        // Linked wallet card
        CardView cardLinked = root.findViewById(R.id.card_linked_wallet);
        cardLinked.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tạo ví liên kết", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to wallet creation form with type = LINKED
        });

        // Credit wallet card
        CardView cardCredit = root.findViewById(R.id.card_credit_wallet);
        cardCredit.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tạo ví tín dụng", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to wallet creation form with type = CREDIT
        });

        // Savings wallet card
        CardView cardSavings = root.findViewById(R.id.card_savings_wallet);
        cardSavings.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tạo ví tiết kiệm", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to wallet creation form with type = SAVINGS
        });

        return root;
    }
}
