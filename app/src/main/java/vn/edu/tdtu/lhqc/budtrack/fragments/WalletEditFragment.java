package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;

import vn.edu.tdtu.lhqc.budtrack.R;

public class WalletEditFragment extends Fragment {

    private TextView tvWalletName;
    private SwitchCompat switchNotification;
    private SwitchCompat switchExcludeTotal;
    private SwitchCompat switchArchive;

    public WalletEditFragment() {}

    public static WalletEditFragment newInstance() {
        return new WalletEditFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Top bar
        view.findViewById(R.id.btn_close).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btn_done).setOnClickListener(v -> {
            saveWalletChanges();
            Toast.makeText(requireContext(), "Đã lưu thay đổi", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Views
        tvWalletName = view.findViewById(R.id.tv_wallet_name);
        switchNotification = view.findViewById(R.id.switch_notification);
        switchExcludeTotal = view.findViewById(R.id.switch_exclude_total);
        switchArchive = view.findViewById(R.id.switch_archive);

        // Action buttons
        view.findViewById(R.id.btn_transfer).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Chuyển tiền đến ví khác", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_edit_balance).setOnClickListener(v -> showEditBalanceDialog());

        view.findViewById(R.id.btn_delete).setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void saveWalletChanges() {
        String walletName = tvWalletName.getText().toString().trim();
        boolean notify = switchNotification.isChecked();
        boolean excludeFromTotal = switchExcludeTotal.isChecked();
        boolean archived = switchArchive.isChecked();

        // TODO: Save to Room / ViewModel
    }

    private void showEditBalanceDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setHint("Nhập số dư mới (ví dụ: 5000000 hoặc -1200050000)");
        input.setTextSize(18);
        input.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(requireContext())
                .setTitle("Điều chỉnh số dư")
                .setView(input)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        try {
                            long newBalance = Long.parseLong(text);
                            updateWalletBalance(newBalance);
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Số không hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateWalletBalance(long newBalance) {
        // TODO: Update balance in database (Room)
        // TODO: Update total balance in WalletFragment / ViewModel

        Toast.makeText(requireContext(),
                "Số dư đã cập nhật: " + formatCurrency(newBalance) + " đ",
                Toast.LENGTH_LONG).show();
    }

    private void showDeleteConfirmationDialog() {
        String walletName = tvWalletName.getText().toString();

        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa ví")
                .setMessage("Bạn có chắc chắn muốn xóa ví \"" + walletName + "\"?\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // TODO: Delete wallet from database
                    Toast.makeText(requireContext(), "Đã xóa ví \"" + walletName + "\"", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String formatCurrency(long amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formatted = formatter.format(Math.abs(amount));
        return amount < 0 ? "-" + formatted : formatted;
    }
}