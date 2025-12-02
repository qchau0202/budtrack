package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.category.CategoryManager;

public class CategoryCreateBottomSheet extends BottomSheetDialogFragment {

    public interface OnCategoryCreateListener {
        void onCategoryCreated(String name, int iconResId);
    }

    private static final String ARG_OLD_NAME = "old_name";
    private static final String ARG_OLD_ICON = "old_icon";
    private static final String ARG_IS_EDIT = "is_edit";

    private EditText editCategoryName;
    private MaterialCardView cardIconHome;
    private MaterialCardView cardIconFood;
    private MaterialCardView cardIconTransport;
    private int selectedIconResId = 0;
    private boolean isEditMode = false;
    private String oldName;
    private int oldIconResId;
    private OnCategoryCreateListener listener;

    public static CategoryCreateBottomSheet newInstance() {
        return new CategoryCreateBottomSheet();
    }

    public static CategoryCreateBottomSheet newInstanceForEdit(String name, int iconResId) {
        CategoryCreateBottomSheet sheet = new CategoryCreateBottomSheet();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_EDIT, true);
        args.putString(ARG_OLD_NAME, name);
        args.putInt(ARG_OLD_ICON, iconResId);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnCategoryCreateListener(OnCategoryCreateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        Bundle args = getArguments();
        if (args != null) {
            isEditMode = args.getBoolean(ARG_IS_EDIT, false);
            oldName = args.getString(ARG_OLD_NAME, null);
            oldIconResId = args.getInt(ARG_OLD_ICON, 0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_bottom_sheet_category_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editCategoryName = view.findViewById(R.id.edit_category_name);
        cardIconHome = view.findViewById(R.id.card_icon_home);
        cardIconFood = view.findViewById(R.id.card_icon_food);
        cardIconTransport = view.findViewById(R.id.card_icon_transport);
        View btnDone = view.findViewById(R.id.btn_done);
        TextView tvTitle = view.findViewById(R.id.tv_title);

        setupIconCard(cardIconHome, R.drawable.ic_home_24dp);
        setupIconCard(cardIconFood, R.drawable.ic_food_24dp);
        setupIconCard(cardIconTransport, R.drawable.ic_transport_24dp);

        if (isEditMode) {
            if (tvTitle != null) {
                tvTitle.setText(R.string.category_edit_title);
            }
            if (editCategoryName != null && oldName != null) {
                editCategoryName.setText(oldName);
                editCategoryName.setSelection(oldName.length());
            }
            // Pre-select the existing icon
            if (oldIconResId != 0) {
                if (oldIconResId == R.drawable.ic_home_24dp && cardIconHome != null) {
                    selectIcon(cardIconHome, oldIconResId);
                } else if (oldIconResId == R.drawable.ic_food_24dp && cardIconFood != null) {
                    selectIcon(cardIconFood, oldIconResId);
                } else if (oldIconResId == R.drawable.ic_transport_24dp && cardIconTransport != null) {
                    selectIcon(cardIconTransport, oldIconResId);
                } else {
                    selectedIconResId = oldIconResId;
                }
            }
        }

        btnDone.setOnClickListener(v -> handleSave());
    }

    private void setupIconCard(MaterialCardView cardView, int iconResId) {
        if (cardView == null) return;
        cardView.setOnClickListener(v -> selectIcon(cardView, iconResId));
    }

    private void selectIcon(MaterialCardView selectedCard, int iconResId) {
        selectedIconResId = iconResId;
        // Reset all cards
        resetCardSelection(cardIconHome);
        resetCardSelection(cardIconFood);
        resetCardSelection(cardIconTransport);

        if (selectedCard != null) {
            selectedCard.setStrokeColor(requireContext().getColor(R.color.primary_green));
        }
    }

    private void resetCardSelection(MaterialCardView card) {
        if (card == null) return;
        card.setStrokeColor(requireContext().getColor(R.color.secondary_grey));
    }

    private void handleSave() {
        String categoryName = editCategoryName != null ? editCategoryName.getText().toString().trim() : "";

        if (TextUtils.isEmpty(categoryName)) {
            if (editCategoryName != null) {
                editCategoryName.setError(getString(R.string.category_name_required));
                editCategoryName.requestFocus();
            }
            return;
        }

        if (selectedIconResId == 0) {
            Toast.makeText(requireContext(), getString(R.string.category_icon_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            // Check if editing to match another existing category (excluding the current one)
            if (CategoryManager.categoryExistsExcluding(requireContext(), categoryName, selectedIconResId, 
                    oldName, oldIconResId)) {
                Toast.makeText(requireContext(), 
                    getString(R.string.category_duplicate_exists, categoryName), 
                    Toast.LENGTH_SHORT).show();
                return;
            }
            // Update existing category (no duplicate found)
            CategoryManager.updateCategory(requireContext(), oldName, oldIconResId, categoryName, selectedIconResId);
        } else {
            // Check for duplicate before adding
            if (CategoryManager.categoryExists(requireContext(), categoryName, selectedIconResId)) {
                Toast.makeText(requireContext(), 
                    getString(R.string.category_duplicate_exists, categoryName), 
                    Toast.LENGTH_SHORT).show();
                return;
            }
            // Persist the new category for future selections
            try {
                CategoryManager.addCategory(requireContext(), categoryName, selectedIconResId);
            } catch (IllegalArgumentException e) {
                Toast.makeText(requireContext(), 
                    getString(R.string.category_duplicate_exists, categoryName), 
                    Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (listener != null) {
            listener.onCategoryCreated(categoryName, selectedIconResId);
        }
        dismiss();
    }
}


