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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.controllers.wallet.WalletManager;
import vn.edu.tdtu.lhqc.budtrack.models.Category;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockCategoryData;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

public class TransactionDetailBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "TransactionDetailBottomSheet";
    private static final String ARG_TRANSACTION_ID = "transaction_id";

    public static TransactionDetailBottomSheet newInstance(long transactionId) {
        TransactionDetailBottomSheet sheet = new TransactionDetailBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_TRANSACTION_ID, transactionId);
        sheet.setArguments(args);
        return sheet;
    }
    
    // Legacy method for backward compatibility (deprecated)
    @Deprecated
    public static TransactionDetailBottomSheet newInstance(String merchantName, String time, String amount) {
        // This method is kept for backward compatibility but should not be used
        // It will create a bottom sheet with no data
        return new TransactionDetailBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        
        // Listen for currency changes to refresh UI immediately
        requireActivity().getSupportFragmentManager().setFragmentResultListener(
            "currency_changed",
            this,
            (requestKey, result) -> {
                if ("currency_changed".equals(requestKey) && getView() != null) {
                    // Refresh transaction detail when currency changes
                    Bundle args = getArguments();
                    if (args != null) {
                        long transactionId = args.getLong(ARG_TRANSACTION_ID, -1);
                        if (transactionId != -1) {
                            Transaction transaction = TransactionManager.getTransactionById(requireContext(), transactionId);
                            if (transaction != null) {
                                updateTransactionDisplay(getView(), transaction);
                            }
                        }
                    }
                }
            }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        // Configure bottom sheet to expand fully and disable dragging to prevent accidental dismissal while scrolling
        if (getDialog() != null && getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false); // Disable dragging to prevent accidental dismissal while scrolling
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_bottom_sheet_transaction_details_map, container, false);
        
        Bundle args = getArguments();
        if (args == null) {
            return view;
        }

        long transactionId = args.getLong(ARG_TRANSACTION_ID, -1);
        if (transactionId == -1) {
            // No transaction ID provided, return empty view
            return view;
        }

        // Load transaction data
        Transaction transaction = TransactionManager.getTransactionById(requireContext(), transactionId);
        if (transaction == null) {
            // Transaction not found, return empty view
            return view;
        }

        // Initialize views
        TextView tvExpenseName = view.findViewById(R.id.tv_expense_name);
        TextView tvExpenseAddress = view.findViewById(R.id.tv_expense_address);
        TextView tvExpenseCategory = view.findViewById(R.id.tv_expense_category);
        TextView tvExpenseAmount = view.findViewById(R.id.tv_expense_amount);
        TextView tvExpenseDate = view.findViewById(R.id.tv_expense_date);
        TextView tvExpenseNote = view.findViewById(R.id.tv_expense_note);
        ImageButton btnClose = view.findViewById(R.id.btn_close_details);

        // Update transaction display
        updateTransactionDisplay(view, transaction);

        // Close button
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

    private void updateTransactionDisplay(View view, Transaction transaction) {
        TextView tvExpenseName = view.findViewById(R.id.tv_expense_name);
        TextView tvExpenseAddress = view.findViewById(R.id.tv_expense_address);
        TextView tvExpenseCategory = view.findViewById(R.id.tv_expense_category);
        TextView tvExpenseAmount = view.findViewById(R.id.tv_expense_amount);
        TextView tvExpenseDate = view.findViewById(R.id.tv_expense_date);
        TextView tvExpenseNote = view.findViewById(R.id.tv_expense_note);

        // Set merchant name
        if (tvExpenseName != null) {
            String merchantName = transaction.getMerchantName();
            if (merchantName != null && !merchantName.isEmpty()) {
                tvExpenseName.setText(merchantName);
            } else {
                tvExpenseName.setText(getString(R.string.unknown_merchant));
            }
        }

        // Set address
        if (tvExpenseAddress != null) {
            String address = transaction.getAddress();
            if (address != null && !address.isEmpty()) {
                tvExpenseAddress.setText(address);
                tvExpenseAddress.setVisibility(View.VISIBLE);
            } else {
                tvExpenseAddress.setVisibility(View.GONE);
            }
        }

        // Set category (use categoryName and categoryIconResId from transaction, fallback to categoryId for legacy)
        if (tvExpenseCategory != null) {
            String categoryName = transaction.getCategoryName();
            Integer categoryIconResId = transaction.getCategoryIconResId();
            
            if (categoryName != null && categoryIconResId != null) {
                // Use user-defined category (name + icon)
                tvExpenseCategory.setText(categoryName);
                tvExpenseCategory.setVisibility(View.VISIBLE);
            } else if (transaction.getCategoryId() != null) {
                // Legacy: try to match categoryId to MockCategoryData (for backward compatibility)
                Category category = findCategoryById(transaction.getCategoryId());
                if (category != null) {
                    tvExpenseCategory.setText(category.getName());
                    tvExpenseCategory.setVisibility(View.VISIBLE);
                } else {
                    tvExpenseCategory.setVisibility(View.GONE);
                }
            } else {
                tvExpenseCategory.setVisibility(View.GONE);
            }
        }

        // Set amount
        if (tvExpenseAmount != null) {
            boolean isIncome = transaction.getType() == TransactionType.INCOME;
            String amountText = isIncome
                    ? "+" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount())
                    : "-" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount());
            tvExpenseAmount.setText(amountText);
            
            // Set color based on transaction type
            int colorRes = isIncome ? R.color.secondary_green : R.color.primary_red;
            tvExpenseAmount.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        }

        // Set date
        if (tvExpenseDate != null) {
            Date transactionDate = transaction.getDate();
            if (transactionDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                tvExpenseDate.setText(dateFormat.format(transactionDate));
            } else {
                tvExpenseDate.setVisibility(View.GONE);
            }
        }

        // Set note
        if (tvExpenseNote != null) {
            String note = transaction.getNote();
            if (note != null && !note.isEmpty()) {
                tvExpenseNote.setText(note);
                tvExpenseNote.setVisibility(View.VISIBLE);
            } else {
                tvExpenseNote.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Find category by ID from MockCategoryData.
     */
    private Category findCategoryById(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        List<Category> categories = MockCategoryData.getSampleCategories();
        for (Category category : categories) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Fragment Result listener
        requireActivity().getSupportFragmentManager().clearFragmentResultListener("currency_changed");
    }
}

