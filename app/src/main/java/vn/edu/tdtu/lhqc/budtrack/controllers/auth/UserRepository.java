package vn.edu.tdtu.lhqc.budtrack.controllers.auth;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.Timestamp;
import vn.edu.tdtu.lhqc.budtrack.models.User;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for Firestore user CRUD operations.
 */
public final class UserRepository {

    private static final String TAG = "UserRepository";
    // Firestore address field (migrating from legacy 'addres' if present)
    public static final String FIELD_ADDRESS = "address";
    public static final String FIELD_AVATAR = "avatar";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FULL_NAME = "fullName";
    public static final String FIELD_LAST_LOGIN = "lastLoginAt";
    public static final String FIELD_UPDATED_AT = "updatedAt";

    private UserRepository() { }

    /**
     * Create or update a user document during sign-in.
     * If the document exists, updates lastLoginAt/updatedAt and avatar (if provided).
     * If not, creates a new document with the required fields.
     */
    public static void createOrUpdateUserOnSignIn(Context context, com.google.firebase.auth.FirebaseUser user, String photoUrl) {
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();
        final com.google.firebase.firestore.DocumentReference docRef = db.collection("users").document(uid);

        docRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot != null && snapshot.exists()) {
                Map<String, Object> updates = new HashMap<>();
                updates.put(FIELD_LAST_LOGIN, FieldValue.serverTimestamp());
                updates.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
                if (photoUrl != null) updates.put(FIELD_AVATAR, photoUrl);

                // Migrate legacy 'addres' -> 'address' if present and new field missing
                try {
                    if (snapshot.contains("addres") && !snapshot.contains(FIELD_ADDRESS)) {
                        Object legacy = snapshot.get("addres");
                        if (legacy != null) {
                            updates.put(FIELD_ADDRESS, legacy);
                        } else {
                            updates.put(FIELD_ADDRESS, "");
                        }
                        // remove legacy field
                        updates.put("addres", FieldValue.delete());
                    }
                } catch (Exception e) {
                    // ignore migration errors
                }

                docRef.update(updates);
            } else {
                // Create a typed User object. Using client Timestamp.now() for initial timestamps.
                Timestamp now = Timestamp.now();
                User userObj = new User(
                        "", // address
                        photoUrl != null ? photoUrl : "",
                        now,
                        user.getEmail() != null ? user.getEmail() : "",
                        user.getDisplayName() != null ? user.getDisplayName() : "",
                        now,
                        now
                );

                // Write typed object to Firestore
                docRef.set(userObj).addOnFailureListener(e -> Log.w(TAG, "Failed to create user doc", e));
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Failed to read/create user doc", e));
    }

    /**
     * Update profile fields (fullName, email, address) in Firestore for current user.
     * Only non-null values are written.
     */
    public static void updateProfileFields(Context context, String fullName, String email, String address) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();
        final com.google.firebase.firestore.DocumentReference docRef = db.collection("users").document(uid);

        Map<String, Object> updates = new HashMap<>();
        if (fullName != null) updates.put(FIELD_FULL_NAME, fullName);
        if (email != null) updates.put(FIELD_EMAIL, email);
        if (address != null) updates.put(FIELD_ADDRESS, address);
        if (!updates.isEmpty()) {
            updates.put(FIELD_UPDATED_AT, FieldValue.serverTimestamp());
            docRef.update(updates).addOnFailureListener(e -> Log.w(TAG, "Failed to update profile fields", e));
        }
    }

    /**
     * Update only the address field for the current user.
     */
    public static void updateAddressForCurrentUser(Context context, String address) {
        updateProfileFields(context, null, null, address);
    }
}
