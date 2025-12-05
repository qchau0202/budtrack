package vn.edu.tdtu.lhqc.budtrack.database.wallet;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets")
public class WalletEntity {
    @PrimaryKey
    public long id;

    @NonNull
    public String name;

    public long balance; // Balance in VND (stored as long to avoid decimal precision issues)

    public int iconResId; // Drawable resource ID for wallet icon

    @NonNull
    public String walletType; // e.g., "Basic Wallet", "Investment Wallet", "Savings Wallet"

    public boolean isCurrentWallet; // Whether this is the active wallet

    public boolean isArchived; // Whether wallet is archived

    public boolean excludeFromTotal; // Whether to exclude this wallet from total balance calculation

    public WalletEntity() {
    }

    public WalletEntity(@NonNull String name, long balance, int iconResId, @NonNull String walletType) {
        this.name = name;
        this.balance = balance;
        this.iconResId = iconResId;
        this.walletType = walletType;
        this.isCurrentWallet = false;
        this.isArchived = false;
        this.excludeFromTotal = false;
    }
}

