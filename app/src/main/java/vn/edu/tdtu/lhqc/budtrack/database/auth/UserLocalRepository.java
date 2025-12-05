package vn.edu.tdtu.lhqc.budtrack.database.auth;

import android.content.Context;

import vn.edu.tdtu.lhqc.budtrack.database.auth.dao.UserDao;

/**
 * Simple repository layer for local (Room) auth data.
 */
public final class UserLocalRepository {

    private final UserDao userDao;

    private UserLocalRepository(Context context) {
        userDao = AuthDatabase.getInstance(context).userDao();
    }

    public static UserLocalRepository getInstance(Context context) {
        return new UserLocalRepository(context.getApplicationContext());
    }

    public UserEntity findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public UserEntity findById(long id) {
        return userDao.findById(id);
    }

    public long insert(UserEntity user) {
        return userDao.insert(user);
    }

    public void update(UserEntity user) {
        userDao.update(user);
    }
}

