package vn.edu.tdtu.lhqc.budtrack.database.wallet;

import android.content.Context;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.AppDatabase;
import vn.edu.tdtu.lhqc.budtrack.database.wallet.dao.WalletDao;

public class WalletRepository {
    private WalletDao walletDao;

    public WalletRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        walletDao = db.walletDao();
    }

    public long insertWallet(WalletEntity wallet) {
        return walletDao.insert(wallet);
    }

    public long insertOrReplaceWallet(WalletEntity wallet) {
        return walletDao.insertOrReplace(wallet);
    }

    public void updateWallet(WalletEntity wallet) {
        walletDao.update(wallet);
    }

    public void deleteWallet(WalletEntity wallet) {
        walletDao.delete(wallet);
    }

    public void deleteWalletById(long id) {
        walletDao.deleteById(id);
    }

    public List<WalletEntity> getAllWallets() {
        return walletDao.getAllWallets();
    }

    public WalletEntity getWalletById(long id) {
        return walletDao.getWalletById(id);
    }

    public WalletEntity getWalletByName(String name) {
        return walletDao.getWalletByName(name);
    }

    public List<WalletEntity> getActiveWallets() {
        return walletDao.getActiveWallets();
    }
}

