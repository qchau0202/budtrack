package vn.edu.tdtu.lhqc.budtrack.database.wallet;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.models.Wallet;

public class WalletConverter {
    public static WalletEntity toEntity(Wallet wallet) {
        WalletEntity entity = new WalletEntity();
        entity.id = wallet.getId();
        entity.name = wallet.getName();
        entity.balance = wallet.getBalance();
        entity.iconResId = wallet.getIconResId();
        entity.walletType = wallet.getWalletType();
        entity.isCurrentWallet = wallet.isCurrentWallet();
        entity.isArchived = wallet.isArchived();
        entity.excludeFromTotal = wallet.isExcludeFromTotal();
        return entity;
    }

    public static Wallet toModel(WalletEntity entity) {
        if (entity == null) {
            return null;
        }
        Wallet wallet = new Wallet();
        wallet.setId(entity.id);
        wallet.setName(entity.name);
        wallet.setBalance(entity.balance);
        wallet.setIconResId(entity.iconResId);
        wallet.setWalletType(entity.walletType);
        wallet.setCurrentWallet(entity.isCurrentWallet);
        wallet.setArchived(entity.isArchived);
        wallet.setExcludeFromTotal(entity.excludeFromTotal);
        return wallet;
    }

    public static List<Wallet> toModelList(List<WalletEntity> entities) {
        List<Wallet> wallets = new ArrayList<>();
        for (WalletEntity entity : entities) {
            wallets.add(toModel(entity));
        }
        return wallets;
    }
}

