package vn.edu.tdtu.lhqc.budtrack.controllers.wallet;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.database.wallet.WalletConverter;
import vn.edu.tdtu.lhqc.budtrack.database.wallet.WalletEntity;
import vn.edu.tdtu.lhqc.budtrack.database.wallet.WalletRepository;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;

/**
 * WalletManager manages wallet data with persistence.
 * Stores wallet data in Room database.
 */
public final class WalletManager {

    private static final String PREFS_NAME = "wallet_prefs";
    private static final String KEY_MIGRATED = "wallet_migrated_to_room"; // Flag to track migration from SharedPreferences
    private static final String KEY_WALLET_IDS = "wallet_ids"; // Set of wallet IDs (for migration)
    private static final String PREFIX_ID = "wallet_id_";
    private static final String PREFIX_NAME = "wallet_name_";
    private static final String PREFIX_BALANCE = "wallet_balance_";
    private static final String PREFIX_ICON = "wallet_icon_";
    private static final String PREFIX_TYPE = "wallet_type_";
    private static final String PREFIX_CURRENT = "wallet_current_";
    private static final String PREFIX_ARCHIVED = "wallet_archived_";
    private static final String PREFIX_EXCLUDE = "wallet_exclude_";

    private WalletManager() {
    }

    /**
     * Migrate wallets from SharedPreferences to Room database if needed.
     */
    private static void migrateIfNeeded(Context context) {
        SharedPreferences prefs = getPrefs(context);
        boolean migrated = prefs.getBoolean(KEY_MIGRATED, false);
        
        if (migrated) {
            return; // Already migrated
        }

        WalletRepository repository = new WalletRepository(context);
        
        // Check if Room database already has wallets
        List<WalletEntity> existingWallets = repository.getAllWallets();
        if (!existingWallets.isEmpty()) {
            // Room already has data, mark as migrated
            prefs.edit().putBoolean(KEY_MIGRATED, true).apply();
            return;
        }

        // Migrate from SharedPreferences to Room
        Set<String> walletIds = prefs.getStringSet(KEY_WALLET_IDS, new HashSet<>());
        if (!walletIds.isEmpty()) {
            for (String idStr : walletIds) {
                WalletEntity entity = new WalletEntity();
                entity.id = prefs.getLong(PREFIX_ID + idStr, 0);
                entity.name = prefs.getString(PREFIX_NAME + idStr, "");
                entity.balance = prefs.getLong(PREFIX_BALANCE + idStr, 0);
                entity.iconResId = prefs.getInt(PREFIX_ICON + idStr, R.drawable.ic_wallet_cash);
                entity.walletType = prefs.getString(PREFIX_TYPE + idStr, "Basic Wallet");
                entity.isCurrentWallet = prefs.getBoolean(PREFIX_CURRENT + idStr, false);
                entity.isArchived = prefs.getBoolean(PREFIX_ARCHIVED + idStr, false);
                entity.excludeFromTotal = prefs.getBoolean(PREFIX_EXCLUDE + idStr, false);
                
                // Use insertOrReplace to preserve IDs during migration
                repository.insertOrReplaceWallet(entity);
            }
        }

        // Mark as migrated
        prefs.edit().putBoolean(KEY_MIGRATED, true).apply();
    }

    /**
     * Get all wallets. Migrates from SharedPreferences if needed.
     * Always reloads from Room database to ensure fresh data.
     */
    public static List<Wallet> getWallets(Context context) {
        migrateIfNeeded(context);
        WalletRepository repository = new WalletRepository(context);
        List<WalletEntity> entities = repository.getAllWallets();
        return WalletConverter.toModelList(entities);
    }

    /**
     * Get a wallet by ID.
     */
    public static Wallet getWalletById(Context context, long id) {
        migrateIfNeeded(context);
        WalletRepository repository = new WalletRepository(context);
        WalletEntity entity = repository.getWalletById(id);
        return WalletConverter.toModel(entity);
    }

    /**
     * Get a wallet by name.
     */
    public static Wallet getWalletByName(Context context, String name) {
        migrateIfNeeded(context);
        WalletRepository repository = new WalletRepository(context);
        WalletEntity entity = repository.getWalletByName(name);
        return WalletConverter.toModel(entity);
    }

    /**
     * Update a wallet by name.
     */
    public static void updateWallet(Context context, String oldName, Wallet updatedWallet) {
        migrateIfNeeded(context);
        WalletRepository repository = new WalletRepository(context);
        WalletEntity existingEntity = repository.getWalletByName(oldName);
        
        if (existingEntity != null) {
            WalletEntity updatedEntity = WalletConverter.toEntity(updatedWallet);
            updatedEntity.id = existingEntity.id; // Preserve the ID
            repository.updateWallet(updatedEntity);
        }
    }

    /**
     * Add a new wallet.
     */
    public static void addWallet(Context context, Wallet wallet) {
        migrateIfNeeded(context);
        WalletRepository repository = new WalletRepository(context);
        WalletEntity entity = WalletConverter.toEntity(wallet);
        
        // Generate ID if not set
        if (wallet.getId() == 0) {
            List<WalletEntity> allWallets = repository.getAllWallets();
            long maxId = 0;
            for (WalletEntity w : allWallets) {
                if (w.id > maxId) {
                    maxId = w.id;
                }
            }
            entity.id = maxId + 1;
            wallet.setId(entity.id);
        }
        
        repository.insertWallet(entity);
    }

    /**
     * Remove a wallet by name.
     */
    public static void removeWallet(Context context, String walletName) {
        migrateIfNeeded(context);
        WalletRepository repository = new WalletRepository(context);
        WalletEntity entity = repository.getWalletByName(walletName);
        
        if (entity != null) {
            repository.deleteWallet(entity);
        }
    }


    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

