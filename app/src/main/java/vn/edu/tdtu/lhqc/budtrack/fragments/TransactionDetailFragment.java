package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.transaction.TransactionManager;
import vn.edu.tdtu.lhqc.budtrack.models.Category;
import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;
import vn.edu.tdtu.lhqc.budtrack.mockdata.MockCategoryData;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import vn.edu.tdtu.lhqc.budtrack.utils.CurrencyUtils;

/**
 * Full-screen Transaction Detail screen, similar to BudgetDetailFragment.
 * Shows all information of a single transaction.
 */
public class TransactionDetailFragment extends Fragment {

    private static final String ARG_TRANSACTION_ID = "transaction_id";

    private long transactionId = -1L;

    public static TransactionDetailFragment newInstance(long transactionId) {
        TransactionDetailFragment fragment = new TransactionDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TRANSACTION_ID, transactionId);
        fragment.setArguments(args);
        return fragment;
    }

    public TransactionDetailFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        GeneralHeaderController.setup(root, this);
        setupTransactionDetail(root);

        return root;
    }

    private void setupTransactionDetail(View root) {
        Bundle args = getArguments();
        if (args == null) return;

        transactionId = args.getLong(ARG_TRANSACTION_ID, -1L);
        if (transactionId <= 0) {
            return;
        }

        final Transaction transaction = TransactionManager.getTransactionById(requireContext(), transactionId);
        if (transaction == null) {
            return;
        }

        // Header: back + title + edit
        ImageButton btnBack = root.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager().popBackStack()
            );
        }

        TextView tvTitle = root.findViewById(R.id.tv_transaction_title);
        if (tvTitle != null) {
            String merchantName = transaction.getMerchantName();
            if (merchantName == null || merchantName.isEmpty()) {
                merchantName = getString(R.string.title);
            }
            tvTitle.setText(merchantName);
        }

        ImageButton btnEdit = root.findViewById(R.id.btn_edit);
        if (btnEdit != null) {
            // UI only: open TransactionCreateFragment bottom sheet pre-populated can be wired later
            btnEdit.setOnClickListener(v -> {
                // TODO: wire this to open TransactionCreateFragment in edit mode
                TransactionCreateFragment sheet = new TransactionCreateFragment();
                sheet.show(requireActivity().getSupportFragmentManager(), TransactionCreateFragment.TAG);
            });
        }

        // Main info section
        ImageView ivIcon = root.findViewById(R.id.iv_transaction_icon);
        TextView tvCategory = root.findViewById(R.id.tv_transaction_category);
        TextView tvAmount = root.findViewById(R.id.tv_transaction_amount);
        TextView tvDate = root.findViewById(R.id.tv_transaction_date);
        TextView tvNote = root.findViewById(R.id.tv_transaction_note);

        // Icon & category
        if (ivIcon != null || tvCategory != null) {
            String categoryName = transaction.getCategoryName();
            Integer categoryIconResId = transaction.getCategoryIconResId();

            if (categoryName != null && categoryIconResId != null) {
                if (tvCategory != null) {
                    tvCategory.setText(categoryName);
                }
                if (ivIcon != null) {
                    ivIcon.setImageResource(categoryIconResId);
                    ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_green));
                }
            } else if (transaction.getCategoryId() != null) {
                Category category = findCategoryById(transaction.getCategoryId());
                if (category != null) {
                    if (tvCategory != null) {
                        tvCategory.setText(category.getName());
                    }
                    if (ivIcon != null) {
                        ivIcon.setImageResource(category.getIconResId());
                        ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_green));
                    }
                }
            }
        }

        // Amount
        if (tvAmount != null) {
            boolean isIncome = transaction.getType() == TransactionType.INCOME;
            String amountText = isIncome
                    ? "+" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount())
                    : "-" + CurrencyUtils.formatCurrency(requireContext(), transaction.getAmount());
            tvAmount.setText(amountText);
            int colorRes = isIncome ? R.color.secondary_green : R.color.primary_red;
            tvAmount.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        }

        // Date
        if (tvDate != null) {
            Date transactionDate = transaction.getDate();
            if (transactionDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                tvDate.setText(dateFormat.format(transactionDate));
            }
        }

        // Note
        if (tvNote != null) {
            String note = transaction.getNote();
            if (note != null && !note.isEmpty()) {
                tvNote.setText(note);
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }
        }

        // Edit / Delete buttons (UI only)
        MaterialButton btnDelete = root.findViewById(R.id.btn_delete);
        MaterialButton btnEditBottom = root.findViewById(R.id.btn_edit_bottom);
        if (btnDelete != null) {
            // TODO: wire delete logic later
            btnDelete.setOnClickListener(v -> {
                // no-op for now
            });
        }
        if (btnEditBottom != null) {
            btnEditBottom.setOnClickListener(v -> {
                // TODO: wire this to open TransactionCreateFragment in edit mode
                TransactionCreateFragment sheet = new TransactionCreateFragment();
                sheet.show(requireActivity().getSupportFragmentManager(), TransactionCreateFragment.TAG);
            });
        }
    }

    private Category findCategoryById(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        for (Category category : MockCategoryData.getSampleCategories()) {
            if (category.getId() == categoryId) {
                return category;
            }
        }
        return null;
    }
}


