package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;

public class TransactionFragment extends Fragment {

    private TabLayout tabLayout;
    private EditText editAmount, editNote;
    private TextView tvDate, tvCategory, tvCancel;
    private CardView cardDate, cardCategory;
    private Button btnSave;

    private Calendar selectedDate = Calendar.getInstance();
    private String selectedType = "chi";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        initViews(view);
        setupTabs();
        setupDatePicker();
        setupAmountFormatter();
        setupButtons();
        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        editAmount = view.findViewById(R.id.editAmount);
        editNote = view.findViewById(R.id.editNote);
        tvDate = view.findViewById(R.id.tvDate);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvCancel = view.findViewById(R.id.tvCancel);
        cardDate = view.findViewById(R.id.cardDate);
        cardCategory = view.findViewById(R.id.cardCategory);
        btnSave = view.findViewById(R.id.btnSave);

        updateDateText();
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: selectedType = "chi"; break;
                    case 1: selectedType = "thu"; break;
                    case 2: selectedType = "vayno"; break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupDatePicker() {
        cardDate.setOnClickListener(v -> showDatePicker());
        updateDateText();
    }

    private void showDatePicker() {
        new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    selectedDate.set(year, month, day);
                    updateDateText();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        tvDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void setupAmountFormatter() {
        editAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editAmount.removeTextChangedListener(this);
                    String clean = s.toString().replaceAll("[,.]", "");
                    if (!clean.isEmpty()) {
                        double parsed = Double.parseDouble(clean);
                        String formatted = NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(parsed);
                        current = formatted;
                        editAmount.setText(formatted);
                        editAmount.setSelection(formatted.length());
                    }
                    editAmount.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupButtons() {
        tvCancel.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnSave.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().replaceAll("[,.]", "");
            String note = editNote.getText().toString().trim();

            if (amountStr.isEmpty() || Double.parseDouble(amountStr) == 0) {
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }
            if (note.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập ghi chú", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gửi dữ liệu về Activity hoặc ViewModel
            Toast.makeText(requireContext(), "Đã thêm giao dịch!", Toast.LENGTH_SHORT).show();

            // Đóng fragment
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}