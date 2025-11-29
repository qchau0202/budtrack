package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.ui.GeneralHeaderController;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * Search Fragment - Provides search functionality across the app
 */
public class SearchFragment extends Fragment {

    private AppCompatEditText etSearch;
    private ImageButton btnClearSearch;
    private LinearLayout searchResultsContainer;
    private LinearLayout emptyState;
    private String currentQuery = "";

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        GeneralHeaderController.setup(root, this);
        setupSearchView(root);

        return root;
    }

    private void setupSearchView(View root) {
        etSearch = root.findViewById(R.id.et_search);
        btnClearSearch = root.findViewById(R.id.btn_clear_search);
        searchResultsContainer = root.findViewById(R.id.search_results_container);
        emptyState = root.findViewById(R.id.empty_state);

        // Auto-focus and show keyboard when fragment opens
        if (etSearch != null) {
            etSearch.requestFocus();
            // Show keyboard
            if (getActivity() != null) {
                android.view.inputmethod.InputMethodManager imm = 
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }

        // Search text change listener
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s.toString().trim();
                    updateClearButtonVisibility(currentQuery);
                    performSearch(currentQuery);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Handle search action from keyboard
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = etSearch.getText().toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                    }
                    hideKeyboard();
                    return true;
                }
                return false;
            });
        }

        // Clear button click listener
        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (etSearch != null) {
                    etSearch.setText("");
                    etSearch.requestFocus();
                }
            });
        }
    }

    private void updateClearButtonVisibility(String query) {
        if (btnClearSearch != null) {
            btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            // Show empty state
            showEmptyState(true);
            clearSearchResults();
        } else {
            // Hide empty state
            showEmptyState(false);
            // TODO: Implement actual search functionality
            // For now, show a placeholder
            showSearchResults(query);
        }
    }

    private void showEmptyState(boolean show) {
        if (emptyState != null) {
            emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void clearSearchResults() {
        if (searchResultsContainer != null) {
            searchResultsContainer.removeAllViews();
        }
    }

    private void showSearchResults(String query) {
        if (searchResultsContainer == null) {
            return;
        }

        // Clear existing results
        clearSearchResults();

        // TODO: Implement actual search logic here
        // This is a placeholder - you would search through:
        // - Transactions
        // - Categories
        // - Budgets
        // - Wallets
        // etc.

        // For now, show a placeholder message
        TextView placeholderText = new TextView(requireContext());
        placeholderText.setText(getString(R.string.search_no_results, query));
        placeholderText.setTextColor(getResources().getColor(R.color.primary_grey, null));
        placeholderText.setTextSize(16f);
        placeholderText.setPadding(0, 24, 0, 24);
        placeholderText.setGravity(android.view.Gravity.CENTER);
        
        searchResultsContainer.addView(placeholderText);
    }

    private void hideKeyboard() {
        if (getActivity() != null && etSearch != null) {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        }
    }
}
