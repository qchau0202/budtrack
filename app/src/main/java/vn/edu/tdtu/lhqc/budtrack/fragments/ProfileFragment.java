package vn.edu.tdtu.lhqc.budtrack.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import android.content.Intent;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.activities.LoginActivity;
import vn.edu.tdtu.lhqc.budtrack.controllers.auth.AuthController;
import vn.edu.tdtu.lhqc.budtrack.controllers.notifications.ReminderNotificationController;
import vn.edu.tdtu.lhqc.budtrack.controllers.settings.SettingsHandler;
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

    // Permission launcher for notification permission
    private ActivityResultLauncher<String> notificationPermissionLauncher;

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

        // Initialize permission launcher
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(requireContext(), R.string.notification_permission_granted, Toast.LENGTH_SHORT).show();
                        // Create notification channel
                        ReminderNotificationController.createNotificationChannel(requireContext());
                        // Show reminder dialog after permission is granted
                        View root = getView();
                        if (root != null) {
                            TextView tvReminderValue = root.findViewById(R.id.tv_reminder_value);
                            if (tvReminderValue != null) {
                                showReminderDialog(tvReminderValue);
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.notification_permission_denied, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Personal info values
        TextView tvName = root.findViewById(R.id.tv_name_value);
        TextView tvEmail = root.findViewById(R.id.tv_email_value);
        TextView tvAddress = root.findViewById(R.id.tv_address_value);
        updateProfileValues(tvName, tvEmail, tvAddress);

        ImageButton btnEditPersonalInfo = root.findViewById(R.id.btn_edit_personal_info);
        if (btnEditPersonalInfo != null) {
            btnEditPersonalInfo.setOnClickListener(v -> showEditProfileDialog(tvName, tvEmail, tvAddress));
        }

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

        TextView tvReminderValue = root.findViewById(R.id.tv_reminder_value);
        View reminderRow = root.findViewById(R.id.row_reminder);
        if (reminderRow != null && tvReminderValue != null) {
            updateReminderValue(tvReminderValue);
            reminderRow.setOnClickListener(v -> {
                // Check notification permission first
                if (!SettingsHandler.isNotificationPermissionGranted(getContext())) {
                    requestNotificationPermission();
                } else {
                    showReminderDialog(tvReminderValue);
                }
            });
        }

        // Set up sign out button
        MaterialButton btnLogout = root.findViewById(R.id.btn_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showSignOutConfirmationDialog());
        }

        return root;
    }

    private void updateProfileValues(TextView tvName, TextView tvEmail, TextView tvAddress) {
        if (getContext() == null) return;
        String name = AuthController.getCurrentUserName(requireContext());
        String email = AuthController.getCurrentUserEmail(requireContext());
        String address = AuthController.getCurrentUserAddress(requireContext());

        if (tvName != null) {
            tvName.setText(name != null && !name.isEmpty()
                    ? name
                    : getString(R.string.none));
        }
        if (tvEmail != null) {
            tvEmail.setText(email != null && !email.isEmpty()
                    ? email
                    : getString(R.string.none));
        }
        if (tvAddress != null) {
            tvAddress.setText(address != null && !address.isEmpty()
                    ? address
                    : getString(R.string.none));
        }
    }

    private void showEditProfileDialog(TextView tvName, TextView tvEmail, TextView tvAddress) {
        if (getContext() == null) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null, false);

        EditText editName = dialogView.findViewById(R.id.edit_name);
        EditText editEmail = dialogView.findViewById(R.id.edit_email);
        EditText editAddress = dialogView.findViewById(R.id.edit_address);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        String currentName = AuthController.getCurrentUserName(requireContext());
        String currentEmail = AuthController.getCurrentUserEmail(requireContext());
        String currentAddress = AuthController.getCurrentUserAddress(requireContext());

        if (editName != null && currentName != null) editName.setText(currentName);
        if (editEmail != null && currentEmail != null) editEmail.setText(currentEmail);
        if (editAddress != null && currentAddress != null) editAddress.setText(currentAddress);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newName = editName != null ? editName.getText().toString().trim() : "";
            String newEmail = editEmail != null ? editEmail.getText().toString().trim() : "";
            String newAddress = editAddress != null ? editAddress.getText().toString().trim() : "";

            AuthController.RegistrationResult result =
                    AuthController.updateProfile(requireContext(), newName, newEmail, newAddress);

            if (!result.isSuccess()) {
                String key = result.getErrorKey();
                int msgRes = 0;
                if ("full_name_required".equals(key)) msgRes = R.string.error_full_name_required;
                else if ("full_name_min_length".equals(key)) msgRes = R.string.error_full_name_min_length;
                else if ("email_required".equals(key)) msgRes = R.string.error_email_required;
                else if ("email_invalid".equals(key)) msgRes = R.string.error_email_invalid;

                if (msgRes != 0) {
                    Toast.makeText(requireContext(), getString(msgRes), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            updateProfileValues(tvName, tvEmail, tvAddress);
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
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

    private void updateReminderValue(TextView textView) {
        if (getContext() == null) return;
        boolean isEnabled = SettingsHandler.isReminderEnabled(getContext());
        if (isEnabled) {
            String time = SettingsHandler.formatReminderTime(getContext());
            textView.setText(time);
        } else {
            textView.setText(R.string.reminder_off);
        }
    }

    private void showReminderDialog(TextView tvReminderValue) {
        if (getContext() == null) return;

        final boolean[] pendingEnabled = {SettingsHandler.isReminderEnabled(getContext())};
        final int[] pendingHour = {SettingsHandler.getReminderHour(getContext())};
        final int[] pendingMinute = {SettingsHandler.getReminderMinute(getContext())};

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_reminder_settings, null, false);

        SwitchMaterial swReminderEnabled = dialogView.findViewById(R.id.sw_reminder_enabled);
        View containerReminderOptions = dialogView.findViewById(R.id.container_reminder_options);
        MaterialCardView cardTimePicker = dialogView.findViewById(R.id.card_time_picker);
        TextView tvReminderTimeValue = dialogView.findViewById(R.id.tv_reminder_time_value);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        // Initialize UI state
        swReminderEnabled.setChecked(pendingEnabled[0]);
        containerReminderOptions.setVisibility(pendingEnabled[0] ? View.VISIBLE : View.GONE);
        tvReminderTimeValue.setText(SettingsHandler.formatReminderTime(getContext()));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        // Toggle switch listener
        swReminderEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && pendingEnabled[0]) {
                // User is trying to disable - show confirmation dialog
                buttonView.setChecked(true); // Revert the switch
                showDisableReminderConfirmationDialog(dialog, swReminderEnabled, containerReminderOptions, pendingEnabled);
            } else {
                pendingEnabled[0] = isChecked;
                containerReminderOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        // Time picker click listener
        cardTimePicker.setOnClickListener(v -> showTimePickerDialog(pendingHour, pendingMinute, tvReminderTimeValue));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            boolean wasEnabled = SettingsHandler.isReminderEnabled(getContext());
            SettingsHandler.setReminderEnabled(getContext(), pendingEnabled[0]);
            if (pendingEnabled[0]) {
                SettingsHandler.setReminderTime(getContext(), pendingHour[0], pendingMinute[0]);
                // Schedule reminder notification
                ReminderNotificationController.createNotificationChannel(getContext());
                ReminderNotificationController.scheduleReminder(getContext());
                if (!wasEnabled) {
                    Toast.makeText(getContext(), R.string.reminder_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.reminder_time_updated, Toast.LENGTH_SHORT).show();
                }
            } else {
                // Cancel reminder
                ReminderNotificationController.cancelReminder(getContext());
                Toast.makeText(getContext(), R.string.reminder_disabled, Toast.LENGTH_SHORT).show();
            }
            updateReminderValue(tvReminderValue);
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void showTimePickerDialog(int[] pendingHour, int[] pendingMinute, TextView tvReminderTimeValue) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View timePickerView = inflater.inflate(R.layout.dialog_time_picker, null, false);

        EditText etHour = timePickerView.findViewById(R.id.et_hour);
        EditText etMinute = timePickerView.findViewById(R.id.et_minute);
        MaterialButton btnCancel = timePickerView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = timePickerView.findViewById(R.id.btn_confirm);

        // Initialize time values (24-hour format)
        int currentHour = pendingHour[0];
        int currentMinute = pendingMinute[0];

        etHour.setText(String.valueOf(currentHour));
        etMinute.setText(String.format("%02d", currentMinute));

        // Validate hour input (0-23)
        etHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        int hour = Integer.parseInt(s.toString());
                        if (hour > 23) {
                            s.replace(0, s.length(), "23");
                        } else if (hour < 0) {
                            s.replace(0, s.length(), "0");
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        });

        // Validate minute input (0-59)
        etMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    try {
                        int minute = Integer.parseInt(s.toString());
                        if (minute > 59) {
                            s.replace(0, s.length(), "59");
                        } else if (minute < 0) {
                            s.replace(0, s.length(), "0");
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        });

        AlertDialog timeDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(timePickerView)
                .create();

        btnCancel.setOnClickListener(v -> timeDialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            try {
                int hour = etHour.getText().toString().isEmpty() ? 0 : Integer.parseInt(etHour.getText().toString());
                int minute = etMinute.getText().toString().isEmpty() ? 0 : Integer.parseInt(etMinute.getText().toString());

                // Validate ranges
                if (hour < 0) hour = 0;
                if (hour > 23) hour = 23;
                if (minute < 0) minute = 0;
                if (minute > 59) minute = 59;

                pendingHour[0] = hour;
                pendingMinute[0] = minute;

                // Update display time in 24-hour format
                tvReminderTimeValue.setText(String.format("%02d:%02d", hour, minute));
                Toast.makeText(requireContext(), R.string.reminder_time_updated, Toast.LENGTH_SHORT).show();
                timeDialog.dismiss();
            } catch (NumberFormatException e) {
                // If invalid input, use default values
                pendingHour[0] = 20;
                pendingMinute[0] = 0;
                tvReminderTimeValue.setText("20:00");
                timeDialog.dismiss();
            }
        });

        timeDialog.show();
        if (timeDialog.getWindow() != null) {
            timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Already granted
                ReminderNotificationController.createNotificationChannel(requireContext());
                View root = getView();
                if (root != null) {
                    TextView tvReminderValue = root.findViewById(R.id.tv_reminder_value);
                    if (tvReminderValue != null) {
                        showReminderDialog(tvReminderValue);
                    }
                }
            }
        } else {
            // For older versions, permission is granted by default
            ReminderNotificationController.createNotificationChannel(requireContext());
            View root = getView();
            if (root != null) {
                TextView tvReminderValue = root.findViewById(R.id.tv_reminder_value);
                if (tvReminderValue != null) {
                    showReminderDialog(tvReminderValue);
                }
            }
        }
    }

    private void showDisableReminderConfirmationDialog(AlertDialog parentDialog, SwitchMaterial switchMaterial,
                                                       View containerReminderOptions, boolean[] pendingEnabled) {
        if (getContext() == null) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_disable_reminder, null, false);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnDisable = dialogView.findViewById(R.id.btn_disable);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDisable.setOnClickListener(v -> {
            // Disable reminder
            pendingEnabled[0] = false;
            switchMaterial.setChecked(false);
            containerReminderOptions.setVisibility(View.GONE);
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void showSignOutConfirmationDialog() {
        if (getContext() == null) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_sign_out_confirmation, null, false);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSignOut = dialogView.findViewById(R.id.btn_sign_out);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSignOut.setOnClickListener(v -> {
            // Sign out user
            AuthController.logout(requireContext());
            dialog.dismiss();
            
            // Show logout success toast
            Toast.makeText(requireContext(), getString(R.string.logout_successful), Toast.LENGTH_SHORT).show();
            
            // Navigate to LoginActivity and clear back stack
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
