package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.utils.LanguageManager;
import vn.edu.tdtu.lhqc.budtrack.utils.ThemeManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Dark mode switch wiring
        SwitchMaterial sw = root.findViewById(R.id.sw_dark_mode);
        if (sw != null && getContext() != null) {
            boolean isDark = ThemeManager.isDarkEnabled(getContext());
            sw.setChecked(isDark);
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ThemeManager.setTheme(
                            requireContext(),
                            isChecked
                                    ? ThemeManager.ThemeMode.DARK
                                    : ThemeManager.ThemeMode.LIGHT
                    );
                }
            });
        }

        TextView tvLanguageValue = root.findViewById(R.id.tv_language_value);
        View languageRow = root.findViewById(R.id.row_language);
        if (languageRow != null && tvLanguageValue != null) {
            updateLanguageValue(tvLanguageValue);
            languageRow.setOnClickListener(v -> showLanguageDialog(tvLanguageValue));
        }

        return root;
    }

    private void updateLanguageValue(TextView textView) {
        LanguageManager.Language language = LanguageManager.getCurrentLanguage(requireContext());
        if (language == LanguageManager.Language.VIETNAMESE) {
            textView.setText(R.string.language_option_vietnamese);
        } else {
            textView.setText(R.string.language_option_english);
        }
    }

    private void showLanguageDialog(TextView tvLanguageValue) {
        final LanguageManager.Language current = LanguageManager.getCurrentLanguage(requireContext());
        final LanguageManager.Language[] pendingSelection = {current};

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_language_selection, null, false);

        MaterialCardView cardVi = dialogView.findViewById(R.id.card_language_vi);
        MaterialCardView cardEn = dialogView.findViewById(R.id.card_language_en);
        View containerVi = dialogView.findViewById(R.id.container_language_vi);
        View containerEn = dialogView.findViewById(R.id.container_language_en);
        ImageView iconVi = dialogView.findViewById(R.id.icon_check_vi);
        ImageView iconEn = dialogView.findViewById(R.id.icon_check_en);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        Runnable refreshState = () -> {
            styleLanguageOption(cardVi, iconVi, pendingSelection[0] == LanguageManager.Language.VIETNAMESE);
            styleLanguageOption(cardEn, iconEn, pendingSelection[0] == LanguageManager.Language.ENGLISH);
        };

        View.OnClickListener vietnameseClickListener = v -> {
            pendingSelection[0] = LanguageManager.Language.VIETNAMESE;
            refreshState.run();
        };

        View.OnClickListener englishClickListener = v -> {
            pendingSelection[0] = LanguageManager.Language.ENGLISH;
            refreshState.run();
        };

        cardVi.setOnClickListener(vietnameseClickListener);
        containerVi.setOnClickListener(vietnameseClickListener);
        cardEn.setOnClickListener(englishClickListener);
        containerEn.setOnClickListener(englishClickListener);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            LanguageManager.Language selected = pendingSelection[0];
            if (selected != current) {
                LanguageManager.setLanguage(requireContext(), selected);
                updateLanguageValue(tvLanguageValue);
            }
            dialog.dismiss();
            if (selected != current) {
                requireActivity().recreate();
            }
        });

        refreshState.run();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void styleLanguageOption(MaterialCardView cardView, ImageView checkIcon, boolean selected) {
        if (cardView == null || checkIcon == null) {
            return;
        }
        int strokeColor = ContextCompat.getColor(requireContext(),
                selected ? R.color.primary_green : R.color.secondary_grey);

        cardView.setStrokeColor(strokeColor);
        checkIcon.setVisibility(selected ? View.VISIBLE : View.GONE);
    }
}