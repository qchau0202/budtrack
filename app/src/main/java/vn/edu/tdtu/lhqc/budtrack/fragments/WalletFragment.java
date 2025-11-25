package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

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

        // Top bar actions
        root.findViewById(R.id.btn_close).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        root.findViewById(R.id.btn_edit).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Chỉnh sửa ví", Toast.LENGTH_SHORT).show());

        // Add wallet
        root.findViewById(R.id.btn_add_wallet).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Thêm ví mới", Toast.LENGTH_SHORT).show());

        // Link service
        root.findViewById(R.id.btn_link_service).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Liên kết dịch vụ", Toast.LENGTH_SHORT).show());

        // You can later replace Toasts with navigation to AddWalletFragment, etc.

        return root;
    }
}