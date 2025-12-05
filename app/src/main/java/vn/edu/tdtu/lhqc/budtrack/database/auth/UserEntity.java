package vn.edu.tdtu.lhqc.budtrack.database.auth;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String fullName;
    public String email;
    public String password; // plain for now (existing behavior); TODO: hash
    public String address;
    public String photoUrl;
    public boolean isGoogleSignIn;
}

