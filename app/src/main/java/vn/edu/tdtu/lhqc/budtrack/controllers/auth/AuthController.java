package vn.edu.tdtu.lhqc.budtrack.controllers.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Patterns;

/**
 * AuthController handles user authentication and registration.
 * Simple implementation using SharedPreferences for demo purposes.
 */
public final class AuthController {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PASSWORD = "user_password";
    private static final String KEY_FULL_NAME = "user_full_name";
    private static final String KEY_ADDRESS = "user_address";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_REGISTERED = "is_registered";

    private AuthController() {
    }

    /**
     * Register a new user
     * @param context Application context
     * @param fullName User's full name
     * @param email User's email
     * @param password User's password
     * @return RegistrationResult with success status and error message if any
     */
    public static RegistrationResult register(Context context, String fullName, String email, String password) {
        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            return new RegistrationResult(false, "full_name_required");
        }

        if (fullName.length() < 2) {
            return new RegistrationResult(false, "full_name_min_length");
        }

        if (TextUtils.isEmpty(email)) {
            return new RegistrationResult(false, "email_required");
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new RegistrationResult(false, "email_invalid");
        }

        if (TextUtils.isEmpty(password)) {
            return new RegistrationResult(false, "password_required");
        }

        if (password.length() < 6) {
            return new RegistrationResult(false, "password_min_length");
        }

        // Check if user already exists
        SharedPreferences prefs = getPrefs(context);
        if (prefs.getBoolean(KEY_IS_REGISTERED, false)) {
            String existingEmail = prefs.getString(KEY_EMAIL, "");
            if (existingEmail.equalsIgnoreCase(email)) {
                return new RegistrationResult(false, "email_already_exists");
            }
        }

        // Save user data
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password); // In production, this should be hashed
        editor.putString(KEY_ADDRESS, ""); // no address yet
        editor.putBoolean(KEY_IS_REGISTERED, true);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();

        return new RegistrationResult(true, null);
    }

    /**
     * Login with email and password
     * @param context Application context
     * @param email User's email
     * @param password User's password
     * @return LoginResult with success status and error message if any
     */
    public static LoginResult login(Context context, String email, String password) {
        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            return new LoginResult(false, "email_required");
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new LoginResult(false, "email_invalid");
        }

        if (TextUtils.isEmpty(password)) {
            return new LoginResult(false, "password_required");
        }

        if (password.length() < 6) {
            return new LoginResult(false, "password_min_length");
        }

        // Check credentials
        SharedPreferences prefs = getPrefs(context);
        String storedEmail = prefs.getString(KEY_EMAIL, "");
        String storedPassword = prefs.getString(KEY_PASSWORD, "");

        if (storedEmail.isEmpty() || !storedEmail.equalsIgnoreCase(email)) {
            return new LoginResult(false, "invalid_credentials");
        }

        if (!storedPassword.equals(password)) {
            return new LoginResult(false, "invalid_credentials");
        }

        // Mark as logged in
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();

        return new LoginResult(true, null);
    }

    /**
     * Check if user is currently logged in
     * @param context Application context
     * @return true if user is logged in, false otherwise
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = getPrefs(context);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Logout the current user
     * @param context Application context
     */
    public static void logout(Context context) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    /**
     * Get current logged in user's email
     * @param context Application context
     * @return User's email or null if not logged in
     */
    public static String getCurrentUserEmail(Context context) {
        if (!isLoggedIn(context)) {
            return null;
        }
        SharedPreferences prefs = getPrefs(context);
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * Get current logged in user's full name
     * @param context Application context
     * @return User's full name or null if not logged in
     */
    public static String getCurrentUserName(Context context) {
        if (!isLoggedIn(context)) {
            return null;
        }
        SharedPreferences prefs = getPrefs(context);
        return prefs.getString(KEY_FULL_NAME, null);
    }

    /**
     * Get current logged in user's address (optional).
     */
    public static String getCurrentUserAddress(Context context) {
        if (!isLoggedIn(context)) {
            return null;
        }
        SharedPreferences prefs = getPrefs(context);
        return prefs.getString(KEY_ADDRESS, null);
    }

    /**
     * Update current user's profile (name, email, address).
     * Password remains unchanged.
     */
    public static RegistrationResult updateProfile(Context context, String fullName, String email, String address) {
        // Reuse validation similar to register()
        if (TextUtils.isEmpty(fullName)) {
            return new RegistrationResult(false, "full_name_required");
        }

        if (fullName.length() < 2) {
            return new RegistrationResult(false, "full_name_min_length");
        }

        if (TextUtils.isEmpty(email)) {
            return new RegistrationResult(false, "email_required");
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new RegistrationResult(false, "email_invalid");
        }

        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        if (address != null) {
            editor.putString(KEY_ADDRESS, address.trim());
        }
        editor.apply();

        return new RegistrationResult(true, null);
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Result class for registration operations
     */
    public static class RegistrationResult {
        private final boolean success;
        private final String errorKey; // Key for error message string resource

        public RegistrationResult(boolean success, String errorKey) {
            this.success = success;
            this.errorKey = errorKey;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorKey() {
            return errorKey;
        }
    }

    /**
     * Result class for login operations
     */
    public static class LoginResult {
        private final boolean success;
        private final String errorKey; // Key for error message string resource

        public LoginResult(boolean success, String errorKey) {
            this.success = success;
            this.errorKey = errorKey;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorKey() {
            return errorKey;
        }
    }
}

