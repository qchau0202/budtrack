package vn.edu.tdtu.lhqc.budtrack.models;

/**
 * Wallet model - represents a user's wallet/account.
 * Simple model with essential fields only.
 */
public class Wallet {
    private long id;
    private String name;
    private long balance; // Balance in VND (stored as long to avoid decimal precision issues)
    private int iconResId; // Drawable resource ID for wallet icon
    private String walletType; // e.g., "Basic Wallet", "Investment Wallet", "Savings Wallet"
    private boolean isCurrentWallet; // Whether this is the active wallet
    private boolean isArchived; // Whether wallet is archived
    private boolean excludeFromTotal; // Whether to exclude this wallet from total balance calculation

    // Default constructor for database
    public Wallet() {
    }

    // Constructor for creating new wallets
    public Wallet(String name, long balance, int iconResId, String walletType) {
        this.name = name;
        this.balance = balance;
        this.iconResId = iconResId;
        this.walletType = walletType;
        this.isCurrentWallet = false;
        this.isArchived = false;
        this.excludeFromTotal = false;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getWalletType() {
        return walletType;
    }

    public void setWalletType(String walletType) {
        this.walletType = walletType;
    }

    public boolean isCurrentWallet() {
        return isCurrentWallet;
    }

    public void setCurrentWallet(boolean currentWallet) {
        isCurrentWallet = currentWallet;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public boolean isExcludeFromTotal() {
        return excludeFromTotal;
    }

    public void setExcludeFromTotal(boolean excludeFromTotal) {
        this.excludeFromTotal = excludeFromTotal;
    }
}

