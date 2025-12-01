package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.category.CategoryManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GridLayout gridAllCategories;
    private AppCompatEditText etSearchCategories;
    private TextView tvEmptyCategories;
    private List<CategoryManager.CategoryItem> allCategories;
    private ImageButton btnEdit;
    private ImageButton btnAdd;
    private boolean isEditMode = false;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
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
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnAdd = view.findViewById(R.id.btn_add);
        btnEdit = view.findViewById(R.id.btn_edit);
        gridAllCategories = view.findViewById(R.id.grid_all_categories);
        etSearchCategories = view.findViewById(R.id.et_search_categories);
        tvEmptyCategories = view.findViewById(R.id.tv_empty_categories);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                CategoryCreateBottomSheet createSheet = CategoryCreateBottomSheet.newInstance();
                createSheet.setOnCategoryCreateListener((name, iconResId) -> {
                    allCategories = CategoryManager.getCategories(requireContext());
                    updateCategoryGrid(allCategories);
                });
                createSheet.show(requireActivity().getSupportFragmentManager(), "CreateCategoryBottomSheet");
            });
        }

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> toggleEditMode());
            updateEditButtonTint();
        }

        // Load all categories using CategoryManager
        allCategories = CategoryManager.getCategories(requireContext());
        updateCategoryGrid(allCategories);

        if (etSearchCategories != null) {
            etSearchCategories.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim().toLowerCase();
                    List<CategoryManager.CategoryItem> filtered = new ArrayList<>();
                    if (allCategories != null) {
                        for (CategoryManager.CategoryItem item : allCategories) {
                            if (item.name != null && item.name.toLowerCase().contains(query)) {
                                filtered.add(item);
                            }
                        }
                    }
                    updateCategoryGrid(filtered);
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        updateEditButtonTint();
        // Rebuild grid to show/hide delete icons
        updateCategoryGrid(allCategories);
    }

    private void updateEditButtonTint() {
        if (btnEdit == null || getContext() == null) return;
        int color = isEditMode
                ? getResources().getColor(R.color.primary_green, null)
                : getResources().getColor(R.color.primary_grey, null);
        btnEdit.setColorFilter(color);
    }

    private void updateCategoryGrid(List<CategoryManager.CategoryItem> categories) {
        if (gridAllCategories == null) {
            return;
        }

        gridAllCategories.removeAllViews();

        if (categories == null || categories.isEmpty()) {
            if (tvEmptyCategories != null) {
                tvEmptyCategories.setVisibility(View.VISIBLE);
            }
            return;
        } else if (tvEmptyCategories != null) {
            tvEmptyCategories.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (CategoryManager.CategoryItem category : categories) {
            View itemView = inflater.inflate(R.layout.item_category_grid_option, gridAllCategories, false);

            MaterialCardView card = itemView.findViewById(R.id.card_category_option);
            ImageView icon = itemView.findViewById(R.id.icon_category_option);
            ImageView btnRemove = itemView.findViewById(R.id.btn_remove_category);
            TextView label = itemView.findViewById(R.id.tv_category_option);

            if (icon != null) {
                icon.setImageResource(category.iconResId);
            }
            if (label != null) {
                label.setText(category.name);
            }

            if (btnRemove != null) {
                btnRemove.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
                btnRemove.setOnClickListener(v -> {
                    CategoryManager.removeCategory(requireContext(), category.name, category.iconResId);
                    allCategories = CategoryManager.getCategories(requireContext());
                    updateCategoryGrid(allCategories);
                });
            }

            if (card != null) {
                card.setOnClickListener(v -> {
                    if (!isEditMode) {
                        return;
                    }
                    CategoryCreateBottomSheet editSheet =
                            CategoryCreateBottomSheet.newInstanceForEdit(category.name, category.iconResId);
                    editSheet.setOnCategoryCreateListener((newName, newIconResId) -> {
                        allCategories = CategoryManager.getCategories(requireContext());
                        updateCategoryGrid(allCategories);
                    });
                    editSheet.show(requireActivity().getSupportFragmentManager(), "EditCategoryBottomSheet");
                });
            }

            gridAllCategories.addView(itemView);
        }
    }
}