package vn.edu.tdtu.lhqc.budtrack.database.wallet.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.wallet.WalletEntity;

@Dao
public interface WalletDao {
    @Insert
    long insert(WalletEntity wallet);

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    long insertOrReplace(WalletEntity wallet);

    @Update
    void update(WalletEntity wallet);

    @Delete
    void delete(WalletEntity wallet);

    @Query("SELECT * FROM wallets ORDER BY id ASC")
    List<WalletEntity> getAllWallets();

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    WalletEntity getWalletById(long id);

    @Query("SELECT * FROM wallets WHERE name = :name LIMIT 1")
    WalletEntity getWalletByName(String name);

    @Query("SELECT * FROM wallets WHERE isArchived = 0 ORDER BY id ASC")
    List<WalletEntity> getActiveWallets();

    @Query("DELETE FROM wallets WHERE id = :id")
    void deleteById(long id);
}

