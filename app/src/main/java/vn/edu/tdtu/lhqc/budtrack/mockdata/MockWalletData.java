package vn.edu.tdtu.lhqc.budtrack.mockdata;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.models.Wallet;

/**
 * Mock data generator for Wallet objects.
 * Provides sample wallet data for testing and development.
 */
public class MockWalletData {

    /**
     * Generates a list of sample wallets.
     * 
     * @return List of sample Wallet objects
     */
    public static List<Wallet> getSampleWallets() {
        List<Wallet> wallets = new ArrayList<>();

        // Cash wallet
        Wallet cashWallet = new Wallet("Cash", 4000000L, R.drawable.ic_wallet_cash, "Basic Wallet");
        cashWallet.setId(1);
        cashWallet.setCurrentWallet(false);
        wallets.add(cashWallet);

        // Bank card wallet
        Wallet bankWallet = new Wallet("Bank Card", 5000000L, R.drawable.ic_wallet_cash, "Basic Wallet");
        bankWallet.setId(2);
        bankWallet.setCurrentWallet(true);
        wallets.add(bankWallet);

        // E-wallet
        Wallet eWallet = new Wallet("E-Wallet", 2000000L, R.drawable.ic_wallet_cash, "Basic Wallet");
        eWallet.setId(3);
        eWallet.setCurrentWallet(false);
        wallets.add(eWallet);

        return wallets;
    }

    /**
     * Generates a single sample wallet (Cash).
     * 
     * @return Sample Wallet object
     */
    public static Wallet getSampleCashWallet() {
        Wallet wallet = new Wallet("Cash", 4000000L, R.drawable.ic_wallet_cash, "Basic Wallet");
        wallet.setId(1);
        wallet.setCurrentWallet(false);
        return wallet;
    }
}

