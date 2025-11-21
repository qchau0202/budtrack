package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;

public class TransactionFragment extends Fragment {

    private TextView tabExpense, tabIncome, tabLoan, tvDate, tvCategory, tvCancel, tvTitle;
    private EditText editAmount, editNote;
    private CardView cardDate, cardCategory, cardWallet;
    private MaterialButton btnSave, btnAddDetails;

    private Calendar selectedDate = Calendar.getInstance();
    private String selectedType = "chi";
    private String currentAmount = "";

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
        // Tabs
        tabExpense = view.findViewById(R.id.tabExpense);
        tabIncome = view.findViewById(R.id.tabIncome);
        tabLoan = view.findViewById(R.id.tabLoan);

        // Input fields
        editAmount = view.findViewById(R.id.editAmount);
        editNote = view.findViewById(R.id.editNote);

        // Text views
        tvDate = view.findViewById(R.id.tvDate);
        tvCategory = view.findViewById(R.id.tvCategory);
        tvCancel = view.findViewById(R.id.tvCancel);
        tvTitle = view.findViewById(R.id.tvTitle);

        // Cards
        cardDate = view.findViewById(R.id.cardDate);
        cardCategory = view.findViewById(R.id.cardCategory);
        cardWallet = view.findViewById(R.id.cardWallet);

        // Buttons
        btnSave = view.findViewById(R.id.btnSave);
        btnAddDetails = view.findViewById(R.id.btnAddDetails);

        updateDateText();
        updateTabSelection(); // Set initial tab selection
    }

    private void setupTabs() {
        tabExpense.setOnClickListener(v -> {
            selectedType = "chi";
            updateTabSelection();
        });

        tabIncome.setOnClickListener(v -> {
            selectedType = "thu";
            updateTabSelection();
        });

        tabLoan.setOnClickListener(v -> {
            selectedType = "vayno";
            updateTabSelection();
        });
    }

    private void updateTabSelection() {
        // Reset all tabs to unselected state
        tabExpense.setSelected(false);
        tabExpense.setTextSize(14f);
        setTextStyle(tabExpense, false);

        tabIncome.setSelected(false);
        tabIncome.setTextSize(14f);
        setTextStyle(tabIncome, false);

        tabLoan.setSelected(false);
        tabLoan.setTextSize(14f);
        setTextStyle(tabLoan, false);

        // Set selected tab (selector drawable handles the background color change)
        switch (selectedType) {
            case "chi":
                tabExpense.setSelected(true);
                tabExpense.setTextSize(16f);
                setTextStyle(tabExpense, true);
                break;
            case "thu":
                tabIncome.setSelected(true);
                tabIncome.setTextSize(16f);
                setTextStyle(tabIncome, true);
                break;
            case "vayno":
                tabLoan.setSelected(true);
                tabLoan.setTextSize(16f);
                setTextStyle(tabLoan, true);
                break;
        }
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(currentAmount)) {
                    editAmount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[.,\\s]", "");

                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = NumberFormat.getNumberInstance(Locale.GERMANY).format(parsed);
                            currentAmount = formatted;
                            editAmount.setText(formatted);
                            editAmount.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else {
                        currentAmount = "";
                        editAmount.setText("");
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

        cardCategory.setOnClickListener(v -> {
            // TODO: Implement category selection dialog
            Toast.makeText(requireContext(), "Chọn nhóm", Toast.LENGTH_SHORT).show();
        });

        cardWallet.setOnClickListener(v -> {
            // TODO: Implement wallet selection dialog
            Toast.makeText(requireContext(), "Chọn ví tiền", Toast.LENGTH_SHORT).show();
        });

        btnAddDetails.setOnClickListener(v -> {
            // TODO: Implement add details functionality
            Toast.makeText(requireContext(), "Thêm chi tiết", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> {
            String amountStr = editAmount.getText().toString().replaceAll("[.,\\s]", "");
            String note = editNote.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount == 0) {
                    Toast.makeText(requireContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: Save transaction to database
                saveTransaction(amount, note);

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTransaction(double amount, String note) {
        // TODO: Implement transaction saving logic
        // This should connect to your database or ViewModel

        String typeText = "";
        switch (selectedType) {
            case "chi": typeText = "chi tiêu"; break;
            case "thu": typeText = "thu nhập"; break;
            case "vayno": typeText = "vay/nợ"; break;
        }

        String message = String.format("Đã thêm giao dịch %s: %s VND - %s",
                typeText,
                NumberFormat.getNumberInstance(Locale.GERMANY).format(amount),
                note.isEmpty() ? "Không có ghi chú" : note);

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

        // Close fragment
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    // Helper method to set text style (you might need to adjust this based on your actual implementation)
    private void setTextStyle(TextView textView, boolean isBold) {
        if (isBold) {
            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD);
        } else {
            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.NORMAL);
        }
    }
}