package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import vn.edu.tdtu.lhqc.budtrack.R;

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
    private int titleResId = R.string.select_category_title;

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
        View view = inflater.inflate(R.layout.bottom_sheet_category_select, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        ImageButton btnBack = view.findViewById(R.id.btnCategoryBack);
        TextView title = view.findViewById(R.id.tvCategoryTitle);
        View cardAdd = view.findViewById(R.id.cardCategoryAdd);
        View cardFood = view.findViewById(R.id.cardCategoryFood);
        View cardShopping = view.findViewById(R.id.cardCategoryShopping);
        View cardTransport = view.findViewById(R.id.cardCategoryTransport);
        View cardHome = view.findViewById(R.id.cardCategoryHome);

        if (title != null) {
            title.setText(getString(titleResId));
        }

        btnBack.setOnClickListener(v -> dismiss());

        cardAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddCategoryRequested();
            }
            dismiss();
        });

        cardFood.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategorySelected(new CategoryOption(
                        getString(R.string.category_food),
                        R.drawable.ic_food_24dp
                ));
            }
            dismiss();
        });

        cardShopping.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategorySelected(new CategoryOption(
                        getString(R.string.category_shopping),
                        R.drawable.ic_shopping_24dp
                ));
            }
            dismiss();
        });

        cardTransport.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategorySelected(new CategoryOption(
                        getString(R.string.category_transport),
                        R.drawable.ic_transport_24dp
                ));
            }
            dismiss();
        });

        cardHome.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategorySelected(new CategoryOption(
                        getString(R.string.category_home),
                        R.drawable.ic_home_24dp
                ));
            }
            dismiss();
        });
    }
}

