package vn.edu.tdtu.lhqc.budtrack.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.fragments.DashboardFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.HomeFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.ProfileFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.WalletFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.TransactionFragmentCreate;
import vn.edu.tdtu.lhqc.budtrack.utils.LanguageManager;
import vn.edu.tdtu.lhqc.budtrack.utils.ThemeManager;

public class MainActivity extends AppCompatActivity {

    private View navHome;
    private View navWallet;
    private View navDashboard;
    private View navProfile;
	private String currentFragmentTag;
	private View bottomBarContainer;

	// Cache fragments to avoid re-creating on each tab switch
	private Fragment homeFragment;
	private Fragment walletFragment;
	private Fragment dashboardFragment;
	private Fragment profileFragment;
	private Fragment activeFragment;

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(LanguageManager.wrapContext(newBase));
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		LanguageManager.applySavedLanguage(this);
		ThemeManager.applySavedTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        bottomBarContainer = findViewById(R.id.bottom_bar_container);
        
        // Handle window insets properly for EdgeToEdge
        View mainLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply padding only to top for status bar
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // Initialize navigation views
        navHome = findViewById(R.id.nav_home);
        navWallet = findViewById(R.id.nav_wallet);
        navDashboard = findViewById(R.id.nav_dashboard);
        navProfile = findViewById(R.id.nav_profile);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);

        // Set Home as active by default and load HomeFragment
		FragmentManager fm = getSupportFragmentManager();
		Fragment existingHome = fm.findFragmentByTag("HOME_FRAGMENT");

		if (savedInstanceState == null && existingHome == null) {
			// First time creation - create new fragments
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
			highlightNavigation(currentFragmentTag);
		} else {
			// Restore fragments from saved state
			homeFragment = fm.findFragmentByTag("HOME_FRAGMENT");
			walletFragment = fm.findFragmentByTag("WALLET_FRAGMENT");
			dashboardFragment = fm.findFragmentByTag("DASHBOARD_FRAGMENT");
			profileFragment = fm.findFragmentByTag("PROFILE_FRAGMENT");

			// Determine which fragment is currently visible
			if (homeFragment != null && !homeFragment.isHidden()) {
				activeFragment = homeFragment;
				currentFragmentTag = "HOME_FRAGMENT";
			} else if (walletFragment != null && !walletFragment.isHidden()) {
				activeFragment = walletFragment;
				currentFragmentTag = "WALLET_FRAGMENT";
			} else if (dashboardFragment != null && !dashboardFragment.isHidden()) {
				activeFragment = dashboardFragment;
				currentFragmentTag = "DASHBOARD_FRAGMENT";
			} else if (profileFragment != null && !profileFragment.isHidden()) {
				activeFragment = profileFragment;
				currentFragmentTag = "PROFILE_FRAGMENT";
			} else {
				// Fallback to home fragment
				activeFragment = homeFragment;
				currentFragmentTag = "HOME_FRAGMENT";
			}
			highlightNavigation(currentFragmentTag);
		}

        // Set up click listeners
        navHome.setOnClickListener(v -> setNavSelected(R.id.nav_home));
        navWallet.setOnClickListener(v -> setNavSelected(R.id.nav_wallet));
        navDashboard.setOnClickListener(v -> setNavSelected(R.id.nav_dashboard));
        navProfile.setOnClickListener(v -> setNavSelected(R.id.nav_profile));
        fabAdd.setOnClickListener(v -> {
            TransactionFragmentCreate transactionFragmentCreate = new TransactionFragmentCreate();
            transactionFragmentCreate.show(getSupportFragmentManager(), TransactionFragmentCreate.TAG);
        });
    }

	private void setNavSelected(int navId) {
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

		highlightNavigation(fragmentTag);
		if (fragmentTag != null) {
			currentFragmentTag = fragmentTag;
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
		}
	}


	private void highlightNavigation(String fragmentTag) {
		navHome.setSelected("HOME_FRAGMENT".equals(fragmentTag));
		navWallet.setSelected("WALLET_FRAGMENT".equals(fragmentTag));
		navDashboard.setSelected("DASHBOARD_FRAGMENT".equals(fragmentTag));
		navProfile.setSelected("PROFILE_FRAGMENT".equals(fragmentTag));
	}

    public void setBottomBarVisible(boolean visible) {
        if (bottomBarContainer != null) {
            bottomBarContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
	}
}