package vn.edu.tdtu.lhqc.budtrack.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.auth.AuthController;
import vn.edu.tdtu.lhqc.budtrack.utils.LanguageManager;
import vn.edu.tdtu.lhqc.budtrack.utils.ThemeManager;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageManager.applySavedLanguage(this);
        ThemeManager.applySavedTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Initialize views
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        // Navigate to Login screen
        TextView loginTextView = findViewById(R.id.tv_login);
        if (loginTextView != null) {
            loginTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Close register screen when navigating to login
                }
            });
        }

        // Handle register button click
        MaterialButton registerButton = findViewById(R.id.btn_register);
        if (registerButton != null) {
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performRegistration();
                }
            });
        }
    }

    /**
     * Performs registration using AuthController
     */
    private void performRegistration() {
        String fullName = etFullName != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword != null ? etConfirmPassword.getText().toString().trim() : "";

        // Validate password match (UI-level validation)
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.error_passwords_not_match), Toast.LENGTH_SHORT).show();
            if (etConfirmPassword != null) {
                etConfirmPassword.requestFocus();
            }
            return;
        }

        // Use AuthController to handle registration logic
        AuthController.RegistrationResult result = AuthController.register(this, fullName, email, password);

        if (result.isSuccess()) {
            // Registration successful - show message and navigate to login
            Toast.makeText(this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close register screen
        } else {
            // Show error message
            String errorMessage = getErrorMessage(result.getErrorKey());
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            
            // Focus on appropriate field
            if (result.getErrorKey() != null) {
                if (result.getErrorKey().contains("full_name")) {
                    if (etFullName != null) {
                        etFullName.requestFocus();
                    }
                } else if (result.getErrorKey().contains("email")) {
                    if (etEmail != null) {
                        etEmail.requestFocus();
                    }
                } else if (result.getErrorKey().contains("password")) {
                    if (etPassword != null) {
                        etPassword.requestFocus();
                    }
                }
            }
        }
    }

    /**
     * Get error message from error key
     */
    private String getErrorMessage(String errorKey) {
        if (errorKey == null) {
            return getString(R.string.error_invalid_credentials);
        }

        switch (errorKey) {
            case "full_name_required":
                return getString(R.string.error_full_name_required);
            case "full_name_min_length":
                return getString(R.string.error_full_name_min_length);
            case "email_required":
                return getString(R.string.error_email_required);
            case "email_invalid":
                return getString(R.string.error_email_invalid);
            case "email_already_exists":
                return getString(R.string.error_email_already_exists);
            case "password_required":
                return getString(R.string.error_password_required);
            case "password_min_length":
                return getString(R.string.error_password_min_length);
            default:
                return getString(R.string.error_invalid_credentials);
        }
    }
}