package vn.edu.tdtu.lhqc.budtrack.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.fragments.DashboardFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.HomeFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.ProfileFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.WalletFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.TransactionFragment;

public class MainActivity extends AppCompatActivity {

    private View navHome;
    private View navWallet;
    private View navDashboard;
    private View navProfile;
	private String currentFragmentTag;

	// Cache fragments to avoid re-creating on each tab switch
	private Fragment homeFragment;
	private Fragment walletFragment;
	private Fragment dashboardFragment;
	private Fragment profileFragment;
	private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vn.edu.tdtu.lhqc.budtrack.utils.ThemeManager.applySavedTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize navigation views
        navHome = findViewById(R.id.nav_home);
        navWallet = findViewById(R.id.nav_wallet);
        navDashboard = findViewById(R.id.nav_dashboard);
        navProfile = findViewById(R.id.nav_profile);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);



        // Set Home as active by default and load HomeFragment
		if (savedInstanceState == null) {
			FragmentManager fm = getSupportFragmentManager();
			homeFragment = new HomeFragment();
			walletFragment = new WalletFragment();
			dashboardFragment = new DashboardFragment();
			profileFragment = new ProfileFragment();

			FragmentTransaction ft = fm.beginTransaction();
			ft.setReorderingAllowed(true);
			ft.add(R.id.fragment_container, homeFragment, "HOME_FRAGMENT");
			ft.add(R.id.fragment_container, walletFragment, "WALLET_FRAGMENT").hide(walletFragment);
			ft.add(R.id.fragment_container, dashboardFragment, "DASHBOARD_FRAGMENT").hide(dashboardFragment);
			ft.add(R.id.fragment_container, profileFragment, "PROFILE_FRAGMENT").hide(profileFragment);
			ft.commit();

			activeFragment = homeFragment;
			currentFragmentTag = "HOME_FRAGMENT";
			navHome.setSelected(true);
		}

        // Set up click listeners
        navHome.setOnClickListener(v -> setNavSelected(R.id.nav_home));
        navWallet.setOnClickListener(v -> setNavSelected(R.id.nav_wallet));
        navDashboard.setOnClickListener(v -> setNavSelected(R.id.nav_dashboard));
        navProfile.setOnClickListener(v -> setNavSelected(R.id.nav_profile));
        fabAdd.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TransactionFragment(), "TRANSACTION_FRAGMENT")
                    .addToBackStack("add_transaction")
                    .commit();
        });
    }

	private void setNavSelected(int navId) {
        // Deselect all
        navHome.setSelected(false);
        navWallet.setSelected(false);
        navDashboard.setSelected(false);
        navProfile.setSelected(false);

        // Select the clicked one
        View selectedView = findViewById(navId);
        if (selectedView != null) {
            selectedView.setSelected(true);
        }

		// Determine target fragment
		Fragment target = null;
		String fragmentTag = null;
		if (navId == R.id.nav_home) {
			fragmentTag = "HOME_FRAGMENT";
			target = homeFragment != null ? homeFragment : getSupportFragmentManager().findFragmentByTag("HOME_FRAGMENT");
		} else if (navId == R.id.nav_wallet) {
			fragmentTag = "WALLET_FRAGMENT";
			target = walletFragment != null ? walletFragment : getSupportFragmentManager().findFragmentByTag("WALLET_FRAGMENT");
		} else if (navId == R.id.nav_dashboard) {
			fragmentTag = "DASHBOARD_FRAGMENT";
			target = dashboardFragment != null ? dashboardFragment : getSupportFragmentManager().findFragmentByTag("DASHBOARD_FRAGMENT");
		} else if (navId == R.id.nav_profile) {
			fragmentTag = "PROFILE_FRAGMENT";
			target = profileFragment != null ? profileFragment : getSupportFragmentManager().findFragmentByTag("PROFILE_FRAGMENT");
		}

		if (target != null && target != activeFragment) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setReorderingAllowed(true);
			if (activeFragment != null) {
				ft.hide(activeFragment);
			}
			ft.show(target);
			ft.commit();
			activeFragment = target;
			currentFragmentTag = fragmentTag;
		}
    }

	private void loadFragment(Fragment fragment, String tag) {
		// Deprecated by show/hide pattern; kept for backward compatibility if needed.
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
		if (existingFragment == null) {
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.setReorderingAllowed(true);
			transaction.add(R.id.fragment_container, fragment, tag);
			transaction.commit();
		}
	}
}