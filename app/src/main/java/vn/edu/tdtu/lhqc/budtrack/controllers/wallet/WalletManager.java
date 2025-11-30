package vn.edu.tdtu.lhqc.budtrack.controllers.wallet;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;

/**
 * WalletManager manages wallet data with persistence.
 * Stores wallet data in SharedPreferences.
 */
public final class WalletManager {

    private static final String PREFS_NAME = "wallet_prefs";
    private static final String KEY_WALLET_IDS = "wallet_ids"; // Set of wallet IDs
    private static final String KEY_INITIALIZED = "initialized";
    private static final String PREFIX_ID = "wallet_id_";
    private static final String PREFIX_NAME = "wallet_name_";
    private static final String PREFIX_BALANCE = "wallet_balance_";
    private static final String PREFIX_ICON = "wallet_icon_";
    private static final String PREFIX_TYPE = "wallet_type_";
    private static final String PREFIX_CURRENT = "wallet_current_";
    private static final String PREFIX_ARCHIVED = "wallet_archived_";
    private static final String PREFIX_EXCLUDE = "wallet_exclude_";

    private static List<Wallet> wallets = null;

    private WalletManager() {
    }

    /**
     * Initialize wallets from SharedPreferences or create default wallets if not initialized.
     */
    public static void initialize(Context context) {
        if (wallets != null) {
            return; // Already initialized
        }

        SharedPreferences prefs = getPrefs(context);
        boolean initialized = prefs.getBoolean(KEY_INITIALIZED, false);

        if (initialized) {
            // Load from SharedPreferences
            wallets = loadWallets(context);
        } else {
            // Create default wallets (empty list)
            wallets = createDefaultWallets();
            saveWallets(context);
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply();
        }
    }

    /**
     * Get all wallets. Initializes if not already done.
     * Automatically removes old hardcoded wallets if they exist.
     * Always reloads from SharedPreferences to ensure fresh data.
     */
    public static List<Wallet> getWallets(Context context) {
        if (wallets == null) {
            initialize(context);
        } else {
            // Reload from SharedPreferences to ensure we have the latest data
            wallets = loadWallets(context);
        }
        return new ArrayList<>(wallets); // Return a copy to prevent external modification
    }

    /**
     * Get a wallet by ID.
     */
    public static Wallet getWalletById(Context context, long id) {
        List<Wallet> allWallets = getWallets(context);
        for (Wallet wallet : allWallets) {
            if (wallet.getId() == id) {
                return wallet;
            }
        }
        return null;
    }

    /**
     * Get a wallet by name.
     */
    public static Wallet getWalletByName(Context context, String name) {
        List<Wallet> allWallets = getWallets(context);
        for (Wallet wallet : allWallets) {
            if (wallet.getName().equals(name)) {
                return wallet;
            }
        }
        return null;
    }

    /**
     * Update a wallet by name.
     */
    public static void updateWallet(Context context, String oldName, Wallet updatedWallet) {
        if (wallets == null) {
            initialize(context);
        }

        for (int i = 0; i < wallets.size(); i++) {
            if (wallets.get(i).getName().equals(oldName)) {
                wallets.set(i, updatedWallet);
                saveWallets(context);
                // Note: getWallets() always reloads from SharedPreferences, so cache is always fresh
                return;
            }
        }
    }

    /**
     * Add a new wallet.
     */
    public static void addWallet(Context context, Wallet wallet) {
        if (wallets == null) {
            initialize(context);
        }

        // Assign ID if not set
        if (wallet.getId() == 0) {
            long maxId = 0;
            for (Wallet w : wallets) {
                if (w.getId() > maxId) {
                    maxId = w.getId();
                }
            }
            wallet.setId(maxId + 1);
        }

        wallets.add(wallet);
        saveWallets(context);
    }

    /**
     * Remove a wallet by name.
     */
    public static void removeWallet(Context context, String walletName) {
        if (wallets == null) {
            initialize(context);
        }

        wallets.removeIf(wallet -> wallet.getName().equals(walletName));
        saveWallets(context);
    }

    /**
     * Save wallets to SharedPreferences.
     */
    private static void saveWallets(Context context) {
        if (wallets == null) {
            return;
        }

        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();

        // Store wallet IDs
        Set<String> walletIds = new HashSet<>();
        for (Wallet wallet : wallets) {
            String idStr = String.valueOf(wallet.getId());
            walletIds.add(idStr);

            // Save wallet properties
            editor.putLong(PREFIX_ID + idStr, wallet.getId());
            editor.putString(PREFIX_NAME + idStr, wallet.getName());
            editor.putLong(PREFIX_BALANCE + idStr, wallet.getBalance());
            editor.putInt(PREFIX_ICON + idStr, wallet.getIconResId());
            editor.putString(PREFIX_TYPE + idStr, wallet.getWalletType());
            editor.putBoolean(PREFIX_CURRENT + idStr, wallet.isCurrentWallet());
            editor.putBoolean(PREFIX_ARCHIVED + idStr, wallet.isArchived());
            editor.putBoolean(PREFIX_EXCLUDE + idStr, wallet.isExcludeFromTotal());
        }

        // Remove old wallets that no longer exist
        Set<String> existingIds = prefs.getStringSet(KEY_WALLET_IDS, new HashSet<>());
        for (String oldId : existingIds) {
            if (!walletIds.contains(oldId)) {
                editor.remove(PREFIX_ID + oldId);
                editor.remove(PREFIX_NAME + oldId);
                editor.remove(PREFIX_BALANCE + oldId);
                editor.remove(PREFIX_ICON + oldId);
                editor.remove(PREFIX_TYPE + oldId);
                editor.remove(PREFIX_CURRENT + oldId);
                editor.remove(PREFIX_ARCHIVED + oldId);
                editor.remove(PREFIX_EXCLUDE + oldId);
            }
        }

        editor.putStringSet(KEY_WALLET_IDS, walletIds);
        // Use commit() to ensure data is saved synchronously before notifying other fragments
        editor.commit();
    }

    /**
     * Load wallets from SharedPreferences.
     */
    private static List<Wallet> loadWallets(Context context) {
        SharedPreferences prefs = getPrefs(context);
        Set<String> walletIds = prefs.getStringSet(KEY_WALLET_IDS, new HashSet<>());
        List<Wallet> loadedWallets = new ArrayList<>();

        for (String idStr : walletIds) {
            Wallet wallet = new Wallet();
            wallet.setId(prefs.getLong(PREFIX_ID + idStr, 0));
            wallet.setName(prefs.getString(PREFIX_NAME + idStr, ""));
            wallet.setBalance(prefs.getLong(PREFIX_BALANCE + idStr, 0));
            wallet.setIconResId(prefs.getInt(PREFIX_ICON + idStr, R.drawable.ic_wallet_cash));
            wallet.setWalletType(prefs.getString(PREFIX_TYPE + idStr, "Basic Wallet"));
            wallet.setCurrentWallet(prefs.getBoolean(PREFIX_CURRENT + idStr, false));
            wallet.setArchived(prefs.getBoolean(PREFIX_ARCHIVED + idStr, false));
            wallet.setExcludeFromTotal(prefs.getBoolean(PREFIX_EXCLUDE + idStr, false));
            loadedWallets.add(wallet);
        }

        return loadedWallets;
    }

    /**
     * Create default wallets for first-time initialization.
     * Returns empty list - users must create their own wallets.
     */
    private static List<Wallet> createDefaultWallets() {
        return new ArrayList<>();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

