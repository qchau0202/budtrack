package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.category.CategoryManager;

public class CategorySelectBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CategorySelectBottomSheet";
    private static final String ARG_TITLE = "titleRes";

    public interface OnCategorySelectedListener {
        void onCategorySelected(CategoryOption option);
        void onAddCategoryRequested();
    }

    public static class CategoryOption {
        public final String name;
        public final int iconResId;

        public CategoryOption(String name, int iconResId) {
            this.name = name;
            this.iconResId = iconResId;
        }
    }

    private OnCategorySelectedListener listener;
    private List<CategoryManager.CategoryItem> allCategories;
    private int titleResId = R.string.select_category_title;
    private GridLayout gridCategories;
    private androidx.appcompat.widget.AppCompatEditText etSearch;

    public static CategorySelectBottomSheet newInstance(@StringRes int titleResId) {
        CategorySelectBottomSheet sheet = new CategorySelectBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, titleResId);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        if (getArguments() != null) {
            titleResId = getArguments().getInt(ARG_TITLE, R.string.select_category_title);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_bottom_sheet_category_select, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        ImageButton btnBack = view.findViewById(R.id.btnCategoryBack);
        TextView title = view.findViewById(R.id.tvCategoryTitle);
        etSearch = view.findViewById(R.id.etSearchCategories);
        gridCategories = view.findViewById(R.id.gridCategories);

        if (title != null) {
            title.setText(getString(titleResId));
        }

        btnBack.setOnClickListener(v -> dismiss());

        // Load all categories once
        allCategories = CategoryManager.getCategories(requireContext());
        populateCategoryGrid(true, allCategories);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim().toLowerCase();
                    List<CategoryManager.CategoryItem> filtered = new java.util.ArrayList<>();
                    if (allCategories != null) {
                        for (CategoryManager.CategoryItem item : allCategories) {
                            if (item.name != null && item.name.toLowerCase().contains(query)) {
                                filtered.add(item);
                            }
                        }
                    }
                    boolean showAdd = query.isEmpty();
                    populateCategoryGrid(showAdd, filtered);
                }

                @Override
                public void afterTextChanged(android.text.Editable s) { }
            });
        }
    }

    private void populateCategoryGrid(boolean showAdd, List<CategoryManager.CategoryItem> categories) {
        if (gridCategories == null) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        // Clear all current items
        gridCategories.removeAllViews();

        // Optionally add the Add button as the first cell
        if (showAdd) {
            View addView = inflater.inflate(R.layout.item_category_add_option, gridCategories, false);
            GridLayout.LayoutParams addParams = new GridLayout.LayoutParams();
            addParams.width = 0;
            addParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            addParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            addView.setLayoutParams(addParams);

            MaterialCardView addCard = addView.findViewById(R.id.cardCategoryAdd);
            if (addCard != null) {
                addCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddCategoryRequested();
            }
            dismiss();
        });
            }
            gridCategories.addView(addView);
        }

        // Then user-defined categories
        for (CategoryManager.CategoryItem category : categories) {
            View optionView = inflater.inflate(R.layout.item_category_grid_option, gridCategories, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            optionView.setLayoutParams(params);

            MaterialCardView card = optionView.findViewById(R.id.card_category_option);
            ImageView icon = optionView.findViewById(R.id.icon_category_option);
            ImageView btnRemove = optionView.findViewById(R.id.btn_remove_category);
            TextView label = optionView.findViewById(R.id.tv_category_option);

            if (icon != null) {
                icon.setImageResource(category.iconResId);
            }
            if (label != null) {
                label.setText(category.name);
            }
            // Hide remove icon in selection sheet
            if (btnRemove != null) {
                btnRemove.setVisibility(View.GONE);
            }
            if (card != null) {
                card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategorySelected(new CategoryOption(
                                category.name,
                                category.iconResId
                ));
            }
            dismiss();
        });
            }

            gridCategories.addView(optionView);
        }
    }
}

