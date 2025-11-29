package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import vn.edu.tdtu.lhqc.budtrack.R;

public class TransactionDetailBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "TransactionDetailBottomSheet";
    private static final String ARG_MERCHANT_NAME = "merchant_name";
    private static final String ARG_TIME = "time";
    private static final String ARG_AMOUNT = "amount";

    public static TransactionDetailBottomSheet newInstance(String merchantName, String time, String amount) {
        TransactionDetailBottomSheet sheet = new TransactionDetailBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_MERCHANT_NAME, merchantName);
        args.putString(ARG_TIME, time);
        args.putString(ARG_AMOUNT, amount);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_expense_details_bottom_sheet, container, false);
        
        Bundle args = getArguments();
        if (args == null) {
            return view;
        }

        String merchantName = args.getString(ARG_MERCHANT_NAME, "");
        String time = args.getString(ARG_TIME, "");
        String amount = args.getString(ARG_AMOUNT, "");

        TextView tvExpenseName = view.findViewById(R.id.tv_expense_name);
        TextView tvExpenseAmount = view.findViewById(R.id.tv_expense_amount);
        TextView tvExpenseDate = view.findViewById(R.id.tv_expense_date);
        ImageButton btnClose = view.findViewById(R.id.btn_close_details);

        if (tvExpenseName != null) {
            tvExpenseName.setText(merchantName);
        }

        if (tvExpenseAmount != null) {
            tvExpenseAmount.setText(amount);
            // Set color based on amount (negative = red, positive = green)
            int colorRes = amount.trim().startsWith("-") 
                    ? R.color.primary_red 
                    : R.color.secondary_green;
            tvExpenseAmount.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        }

        if (tvExpenseDate != null) {
            tvExpenseDate.setText(time);
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        // Hide view details button
        View btnViewDetails = view.findViewById(R.id.btn_view_details);
        if (btnViewDetails != null) {
            btnViewDetails.setVisibility(View.GONE);
        }

        return view;
    }
}

