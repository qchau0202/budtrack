package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.activities.TransactionHistoryActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionHistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TransactionHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TransactionHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TransactionHistoryFragment newInstance(String param1, String param2) {
        TransactionHistoryFragment fragment = new TransactionHistoryFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transaction_history, container, false);

        // Get the card container and make it clickable to open fullscreen view
        LinearLayout cardTransactions = root.findViewById(R.id.card_transactions);
        if (cardTransactions != null) {
            cardTransactions.setOnClickListener(v -> {
                // Only navigate if the click is not on an interactive element (tabs, buttons, etc.)
                // Child views that handle clicks will consume the event, so this will only fire
                // when clicking on non-interactive areas of the card
                Intent intent = new Intent(requireContext(), TransactionHistoryActivity.class);
                startActivity(intent);
            });
            // Make it focusable and clickable
            cardTransactions.setClickable(true);
            cardTransactions.setFocusable(true);
        }

        MaterialButton btnViewAll = root.findViewById(R.id.btn_view_all_transactions);
        LinearLayout listIncome = root.findViewById(R.id.list_income);
        LinearLayout listExpenses = root.findViewById(R.id.list_expenses);

        colorizeAmounts(listIncome, R.color.secondary_green);
        colorizeAmounts(listExpenses, R.color.primary_red);

        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), TransactionHistoryActivity.class);
                startActivity(intent);
            });
        }

        return root;
    }

    private void colorizeAmounts(LinearLayout listContainer, int colorResId) {
        if (listContainer == null) return;
        int color = getResources().getColor(colorResId);
        final int childCount = listContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View row = listContainer.getChildAt(i);
            if (!(row instanceof LinearLayout)) continue;

            LinearLayout rowLayout = (LinearLayout) row;
            final int rowChildCount = rowLayout.getChildCount();
            // Heuristic: the last child is the end-aligned amount/date container
            if (rowChildCount == 0) continue;
            View last = rowLayout.getChildAt(rowChildCount - 1);
            if (last instanceof LinearLayout) {
                LinearLayout amountContainer = (LinearLayout) last;
                // First TextView in this container is the amount
                for (int j = 0; j < amountContainer.getChildCount(); j++) {
                    View v = amountContainer.getChildAt(j);
                    if (v instanceof TextView) {
                        ((TextView) v).setTextColor(color);
                        break;
                    }
                }
            }
        }
    }
}