package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import vn.edu.tdtu.lhqc.budtrack.R;

public class WalletFragment extends Fragment {

    public WalletFragment() {
        // Required empty public constructor
    }

    public static WalletFragment newInstance() {
        return new WalletFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wallet, container, false);

        // Close button (back)
        root.findViewById(R.id.btn_close).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Edit button → Open WalletEditFragment
        root.findViewById(R.id.btn_edit).setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();

            transaction.replace(R.id.fragment_container, WalletEditFragment.newInstance());
            transaction.addToBackStack(null); // Allows user to press back and return here
            transaction.commit();
        });

        // Add wallet button → Open WalletTypeSelectionFragment
        root.findViewById(R.id.btn_add_wallet).setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();

            transaction.replace(R.id.fragment_container, WalletTypeSelectionFragment.newInstance());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return root;
    }
}