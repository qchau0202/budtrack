package vn.edu.tdtu.lhqc.budtrack.database.auth;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import vn.edu.tdtu.lhqc.budtrack.database.auth.dao.UserDao;

@Database(entities = {UserEntity.class}, version = 1, exportSchema = false)
public abstract class AuthDatabase extends RoomDatabase {

    private static volatile AuthDatabase INSTANCE;

    public abstract UserDao userDao();

    public static AuthDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AuthDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AuthDatabase.class,
                                    "auth_db"
                            )
                            .allowMainThreadQueries() // simple usage for auth; consider moving off main thread later
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

