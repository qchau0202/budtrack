package vn.edu.tdtu.lhqc.budtrack.controllers.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Patterns;

import vn.edu.tdtu.lhqc.budtrack.database.auth.UserEntity;
import vn.edu.tdtu.lhqc.budtrack.database.auth.UserLocalRepository;

/**
 * AuthController handles user authentication and registration.
 * Simple implementation using SharedPreferences for demo purposes.
 */
public final class AuthController {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_GOOGLE_SIGN_IN = "is_google_sign_in";
    private static final String KEY_PROFILE_PHOTO = "";

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

        UserLocalRepository repo = UserLocalRepository.getInstance(context);
        UserEntity existing = repo.findByEmail(email);
        if (existing != null) {
            return new RegistrationResult(false, "email_already_exists");
        }

        UserEntity user = new UserEntity();
        user.fullName = fullName;
        user.email = email;
        user.password = password; // TODO: hash in production
        user.address = "";
        user.isGoogleSignIn = false;
        long userId = repo.insert(user);

        // Not logged in yet; do not set is_logged_in
        SharedPreferences prefs = getPrefs(context);
        prefs.edit()
                .putLong(KEY_CURRENT_USER_ID, userId)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .putBoolean(KEY_IS_GOOGLE_SIGN_IN, false)
                .apply();

        return new RegistrationResult(true, null);
    }

    /**
     * Save or login an externally authenticated user (e.g. Google Sign-In).
     * This will mark the user as registered and logged in and store basic profile info.
     * Also marks the account as Google-authenticated (email cannot be changed).
     */
    public static void loginWithExternalAccount(Context context, String email, String fullName, String photoUrl) {
        UserLocalRepository repo = UserLocalRepository.getInstance(context);
        UserEntity existing = repo.findByEmail(email);
        if (existing == null) {
            existing = new UserEntity();
            existing.email = email;
            existing.fullName = fullName != null ? fullName : "";
            existing.password = ""; // Google sign-in users have no local password
            existing.address = "";
            existing.photoUrl = photoUrl;
            existing.isGoogleSignIn = true;
            long id = repo.insert(existing);
            setCurrentUserPrefs(context, id, true, true);
        } else {
            existing.fullName = fullName != null ? fullName : existing.fullName;
            existing.photoUrl = photoUrl != null ? photoUrl : existing.photoUrl;
            existing.isGoogleSignIn = true;
            repo.update(existing);
            setCurrentUserPrefs(context, existing.id, true, true);
        }
    }

    /**
     * Get stored profile photo URL for current user
     */
    public static String getCurrentUserPhotoUrl(Context context) {
        if (!isLoggedIn(context)) return null;
        SharedPreferences prefs = getPrefs(context);
        return prefs.getString(KEY_PROFILE_PHOTO, null);
    }

    /**
     * Check if the current logged-in user authenticated via Google Sign-In.
     * Google-authenticated users cannot change their email.
     */
    public static boolean isGoogleSignInUser(Context context) {
        if (!isLoggedIn(context)) return false;
        SharedPreferences prefs = getPrefs(context);
        return prefs.getBoolean(KEY_IS_GOOGLE_SIGN_IN, false);
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
        UserLocalRepository repo = UserLocalRepository.getInstance(context);
        UserEntity user = repo.findByEmail(email);
        if (user == null || user.password == null || !user.password.equals(password)) {
            return new LoginResult(false, "invalid_credentials");
        }

        setCurrentUserPrefs(context, user.id, true, user.isGoogleSignIn);

        return new LoginResult(true, null);
    }

    /**
     * Check if user is currently logged in
     * @param context Application context
     * @return true if user is logged in, false otherwise
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = getPrefs(context);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && prefs.getLong(KEY_CURRENT_USER_ID, -1) > 0;
    }

    /**
     * Logout the current user
     * @param context Application context
     */
    public static void logout(Context context) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putBoolean(KEY_IS_GOOGLE_SIGN_IN, false);
        editor.putLong(KEY_CURRENT_USER_ID, -1);
        editor.apply();
    }

    /**
     * Get current logged in user's email
     * @param context Application context
     * @return User's email or null if not logged in
     */
    public static String getCurrentUserEmail(Context context) {
        UserEntity user = getCurrentUser(context);
        return user != null ? user.email : null;
    }

    /**
     * Get current logged in user's full name
     * @param context Application context
     * @return User's full name or null if not logged in
     */
    public static String getCurrentUserName(Context context) {
        UserEntity user = getCurrentUser(context);
        return user != null ? user.fullName : null;
    }

    /**
     * Get current logged in user's address (optional).
     */
    public static String getCurrentUserAddress(Context context) {
        UserEntity user = getCurrentUser(context);
        return user != null ? user.address : null;
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

        UserEntity user = getCurrentUser(context);
        if (user == null) {
            return new RegistrationResult(false, "invalid_credentials");
        }
        user.fullName = fullName;
        user.email = email;
        if (address != null) {
            user.address = address.trim();
        }
        UserLocalRepository.getInstance(context).update(user);
        return new RegistrationResult(true, null);
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static void setCurrentUserPrefs(Context context, long userId, boolean isLoggedIn, boolean isGoogle) {
        getPrefs(context).edit()
                .putLong(KEY_CURRENT_USER_ID, userId)
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putBoolean(KEY_IS_GOOGLE_SIGN_IN, isGoogle)
                .apply();
    }

    private static UserEntity getCurrentUser(Context context) {
        SharedPreferences prefs = getPrefs(context);
        long id = prefs.getLong(KEY_CURRENT_USER_ID, -1);
        if (id <= 0) return null;
        return UserLocalRepository.getInstance(context).findById(id);
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

