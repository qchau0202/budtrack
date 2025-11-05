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

public class MainActivity extends AppCompatActivity {

    private View navHome;
    private View navWallet;
    private View navDashboard;
    private View navProfile;
    private String currentFragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            setNavSelected(R.id.nav_home);
        }

        // Set up click listeners
        navHome.setOnClickListener(v -> setNavSelected(R.id.nav_home));
        navWallet.setOnClickListener(v -> setNavSelected(R.id.nav_wallet));
        navDashboard.setOnClickListener(v -> setNavSelected(R.id.nav_dashboard));
        navProfile.setOnClickListener(v -> setNavSelected(R.id.nav_profile));
        fabAdd.setOnClickListener(v -> {
            // Handle FAB click - add new item
            // TODO: Implement add functionality
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

        // Load corresponding fragment
        String fragmentTag = null;
        Fragment fragment = null;
        
        if (navId == R.id.nav_home) {
            fragmentTag = "HOME_FRAGMENT";
            fragment = new HomeFragment();
        } else if (navId == R.id.nav_wallet) {
            fragmentTag = "WALLET_FRAGMENT";
            fragment = new WalletFragment();
        } else if (navId == R.id.nav_dashboard) {
            fragmentTag = "DASHBOARD_FRAGMENT";
            fragment = new DashboardFragment();
        } else if (navId == R.id.nav_profile) {
            fragmentTag = "PROFILE_FRAGMENT";
            fragment = new ProfileFragment();
        }

        if (fragment != null && !fragmentTag.equals(currentFragmentTag)) {
            loadFragment(fragment, fragmentTag);
            currentFragmentTag = fragmentTag;
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        
        if (existingFragment == null || !existingFragment.isAdded()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment, tag);
            transaction.commit();
        }
    }
}