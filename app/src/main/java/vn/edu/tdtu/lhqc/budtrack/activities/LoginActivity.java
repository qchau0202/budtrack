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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

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
        
        // Check if user is already logged in, skip to MainActivity
        if (AuthController.isLoggedIn(this)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        // Navigate to Register screen
        TextView signUpTextView = findViewById(R.id.tv_sign_up);
        if (signUpTextView != null) {
            signUpTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Handle login button click
        MaterialButton loginButton = findViewById(R.id.btn_login);
        if (loginButton != null) {
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performLogin();
                }
            });
        }

        // Handle Google sign up button (UI only, no logic as requested)
        MaterialButton googleButton = findViewById(R.id.btn_sign_up_google);
        if (googleButton != null) {
            googleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Implement Google Sign-In logic
                    Toast.makeText(LoginActivity.this, getString(R.string.google_sign_in_coming_soon), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Performs login using AuthController
     */
    private void performLogin() {
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString().trim() : "";

        // Use AuthController to handle login logic
        AuthController.LoginResult result = AuthController.login(this, email, password);

        if (result.isSuccess()) {
            // Login successful - show toast and navigate to MainActivity
            Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Prevent going back to login screen
        } else {
            // Show error message
            String errorMessage = getErrorMessage(result.getErrorKey());
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            
            // Focus on appropriate field
            if (result.getErrorKey() != null && result.getErrorKey().contains("email")) {
                if (etEmail != null) {
                    etEmail.requestFocus();
                }
            } else if (result.getErrorKey() != null && result.getErrorKey().contains("password")) {
                if (etPassword != null) {
                    etPassword.requestFocus();
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
            case "email_required":
                return getString(R.string.error_email_required);
            case "email_invalid":
                return getString(R.string.error_email_invalid);
            case "password_required":
                return getString(R.string.error_password_required);
            case "password_min_length":
                return getString(R.string.error_password_min_length);
            case "invalid_credentials":
                return getString(R.string.error_invalid_credentials);
            default:
                return getString(R.string.error_invalid_credentials);
        }
    }
}