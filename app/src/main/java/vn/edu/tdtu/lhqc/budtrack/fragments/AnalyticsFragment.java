package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import vn.edu.tdtu.lhqc.budtrack.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnalyticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnalyticsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AnalyticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AnalyticsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnalyticsFragment newInstance(String param1, String param2) {
        AnalyticsFragment fragment = new AnalyticsFragment();
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
        View root = inflater.inflate(R.layout.fragment_analytics, container, false);

        // Tabs inside analytics card control title only
        MaterialButton tabIncome = root.findViewById(R.id.tab_income);
        MaterialButton tabExpenses = root.findViewById(R.id.tab_expenses);
        TextView weeklyTitle = root.findViewById(R.id.tv_weekly_title);

        if (tabIncome != null && tabExpenses != null) {
            tabIncome.setOnClickListener(v -> selectTab(true, tabIncome, tabExpenses, weeklyTitle));
            tabExpenses.setOnClickListener(v -> selectTab(false, tabIncome, tabExpenses, weeklyTitle));
            // default selection
            selectTab(true, tabIncome, tabExpenses, weeklyTitle);
        }

        return root;
    }

    private void selectTab(boolean incomeSelected,
                           MaterialButton tabIncome,
                           MaterialButton tabExpenses,
                           TextView weeklyTitle) {
        if (incomeSelected) {
            tabIncome.setBackgroundTintList(getResources().getColorStateList(R.color.primary_green));
            tabIncome.setTextColor(getResources().getColor(R.color.primary_white));
            tabExpenses.setBackgroundTintList(getResources().getColorStateList(R.color.secondary_grey));
            tabExpenses.setTextColor(getResources().getColor(R.color.primary_black));
            if (weeklyTitle != null) weeklyTitle.setText(R.string.total_income);
        } else {
            tabExpenses.setBackgroundTintList(getResources().getColorStateList(R.color.primary_green));
            tabExpenses.setTextColor(getResources().getColor(R.color.primary_white));
            tabIncome.setBackgroundTintList(getResources().getColorStateList(R.color.secondary_grey));
            tabIncome.setTextColor(getResources().getColor(R.color.primary_black));
            if (weeklyTitle != null) weeklyTitle.setText(R.string.total_expenses);
        }
    }
}