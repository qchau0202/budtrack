package vn.edu.tdtu.lhqc.budtrack.models;

import com.google.firebase.Timestamp;

/**
 * Plain Old Java Object representing a user document in Firestore.
 * Fields are public to allow Firestore automatic mapping via {@code DocumentSnapshot.toObject(User.class)}.
 */
public class User {
    public String address;
    public String avatar;
    public Timestamp createdAt;
    public String email;
    public String fullName;
    public Timestamp lastLoginAt;
    public Timestamp updatedAt;

    // Required empty constructor for Firestore
    public User() {}

    public User(String address, String avatar, Timestamp createdAt, String email, String fullName, Timestamp lastLoginAt, Timestamp updatedAt) {
        this.address = address;
        this.avatar = avatar;
        this.createdAt = createdAt;
        this.email = email;
        this.fullName = fullName;
        this.lastLoginAt = lastLoginAt;
        this.updatedAt = updatedAt;
    }
}
