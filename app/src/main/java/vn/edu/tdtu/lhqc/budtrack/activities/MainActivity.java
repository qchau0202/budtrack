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

import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.content.Intent;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.controllers.auth.AuthController;
import vn.edu.tdtu.lhqc.budtrack.fragments.DashboardFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.HomeFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.ProfileFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.BudgetFragment;
import vn.edu.tdtu.lhqc.budtrack.fragments.TransactionCreateFragment;
import vn.edu.tdtu.lhqc.budtrack.utils.LanguageManager;
import vn.edu.tdtu.lhqc.budtrack.utils.ThemeManager;

public class MainActivity extends AppCompatActivity {

    private View navHome;
    private View navBudget;
    private View navDashboard;
    private View navProfile;
	private String currentFragmentTag;
	private View bottomBarContainer;

	// Cache fragments to avoid re-creating on each tab switch
	private Fragment homeFragment;
	private Fragment budgetFragment;
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
        
        // Check if user is logged in, redirect to LoginActivity if not
        if (!AuthController.isLoggedIn(this)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
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
        navBudget = findViewById(R.id.nav_budget);
        navDashboard = findViewById(R.id.nav_dashboard);
        navProfile = findViewById(R.id.nav_profile);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);

        // Set Home as active by default and load HomeFragment
		FragmentManager fm = getSupportFragmentManager();
		Fragment existingHome = fm.findFragmentByTag("HOME_FRAGMENT");

		if (savedInstanceState == null && existingHome == null) {
			// First time creation - create new fragments
			homeFragment = new HomeFragment();
			budgetFragment = new BudgetFragment();
			dashboardFragment = new DashboardFragment();
			profileFragment = new ProfileFragment();

			FragmentTransaction ft = fm.beginTransaction();
			ft.setReorderingAllowed(true);
			ft.add(R.id.fragment_container, homeFragment, "HOME_FRAGMENT");
			ft.add(R.id.fragment_container, budgetFragment, "BUDGET_FRAGMENT").hide(budgetFragment);
			ft.add(R.id.fragment_container, dashboardFragment, "DASHBOARD_FRAGMENT").hide(dashboardFragment);
			ft.add(R.id.fragment_container, profileFragment, "PROFILE_FRAGMENT").hide(profileFragment);
			ft.commit();

			activeFragment = homeFragment;
			currentFragmentTag = "HOME_FRAGMENT";
			highlightNavigation(currentFragmentTag);
		} else {
			// Restore fragments from saved state
			homeFragment = fm.findFragmentByTag("HOME_FRAGMENT");
			budgetFragment = fm.findFragmentByTag("BUDGET_FRAGMENT");
			dashboardFragment = fm.findFragmentByTag("DASHBOARD_FRAGMENT");
			profileFragment = fm.findFragmentByTag("PROFILE_FRAGMENT");

			// Determine which fragment is currently visible
			if (homeFragment != null && !homeFragment.isHidden()) {
				activeFragment = homeFragment;
				currentFragmentTag = "HOME_FRAGMENT";
			} else if (budgetFragment != null && !budgetFragment.isHidden()) {
				activeFragment = budgetFragment;
				currentFragmentTag = "BUDGET_FRAGMENT";
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
        navBudget.setOnClickListener(v -> setNavSelected(R.id.nav_budget));
        navDashboard.setOnClickListener(v -> setNavSelected(R.id.nav_dashboard));
        navProfile.setOnClickListener(v -> setNavSelected(R.id.nav_profile));
        fabAdd.setOnClickListener(v -> showInputMethodSelectionBottomSheet());
    }

	private void setNavSelected(int navId) {
		FragmentManager fm = getSupportFragmentManager();

		// Determine target fragment
		Fragment target = null;
		String fragmentTag = null;
		if (navId == R.id.nav_home) {
			fragmentTag = "HOME_FRAGMENT";
			target = homeFragment != null ? homeFragment : fm.findFragmentByTag("HOME_FRAGMENT");
		} else if (navId == R.id.nav_budget) {
			fragmentTag = "BUDGET_FRAGMENT";
			target = budgetFragment != null ? budgetFragment : fm.findFragmentByTag("BUDGET_FRAGMENT");
		} else if (navId == R.id.nav_dashboard) {
			fragmentTag = "DASHBOARD_FRAGMENT";
			target = dashboardFragment != null ? dashboardFragment : fm.findFragmentByTag("DASHBOARD_FRAGMENT");
		} else if (navId == R.id.nav_profile) {
			fragmentTag = "PROFILE_FRAGMENT";
			target = profileFragment != null ? profileFragment : fm.findFragmentByTag("PROFILE_FRAGMENT");
		}

		// If target is null or already active and visible, do nothing
		if (target == null) {
			return;
		}

		// Check if target is already the active fragment and not hidden
		if (target == activeFragment && target.isAdded() && !target.isHidden()) {
			// Already showing this fragment, just update navigation highlight
			highlightNavigation(fragmentTag);
			if (fragmentTag != null) {
				currentFragmentTag = fragmentTag;
			}
			return;
		}

		highlightNavigation(fragmentTag);
		if (fragmentTag != null) {
			currentFragmentTag = fragmentTag;
		}

		FragmentTransaction ft = fm.beginTransaction();
		ft.setReorderingAllowed(true);

		// Remove any overlay fragments (Search, Notification, Map, etc.)
		for (Fragment fragment : fm.getFragments()) {
			if (fragment == null) continue;

			boolean isMainFragment = fragment == homeFragment
					|| fragment == budgetFragment
					|| fragment == dashboardFragment
					|| fragment == profileFragment;

			if (!isMainFragment && fragment.isAdded()) {
				ft.remove(fragment);
			}
		}

		// Hide all main fragments except the target
		if (homeFragment != null && homeFragment != target && !homeFragment.isHidden()) {
			ft.hide(homeFragment);
		}
		if (budgetFragment != null && budgetFragment != target && !budgetFragment.isHidden()) {
			ft.hide(budgetFragment);
		}
		if (dashboardFragment != null && dashboardFragment != target && !dashboardFragment.isHidden()) {
			ft.hide(dashboardFragment);
		}
		if (profileFragment != null && profileFragment != target && !profileFragment.isHidden()) {
			ft.hide(profileFragment);
		}

		// Show the target fragment if it's hidden
		if (target.isHidden() || !target.isAdded()) {
			ft.show(target);
		}

		ft.commit();
		activeFragment = target;

		// Clear any back stack entries related to overlays to avoid them
		// being resurrected when the user presses the system back button.
		if (fm.getBackStackEntryCount() > 0) {
			fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}


	private void highlightNavigation(String fragmentTag) {
		navHome.setSelected("HOME_FRAGMENT".equals(fragmentTag));
		navBudget.setSelected("BUDGET_FRAGMENT".equals(fragmentTag));
		navDashboard.setSelected("DASHBOARD_FRAGMENT".equals(fragmentTag));
		navProfile.setSelected("PROFILE_FRAGMENT".equals(fragmentTag));
	}

    public void setBottomBarVisible(boolean visible) {
        if (bottomBarContainer != null) {
            bottomBarContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
	}

	private void showInputMethodSelectionBottomSheet() {
		BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
		View view = LayoutInflater.from(this).inflate(R.layout.view_bottom_sheet_input_method, null);
		dialog.setContentView(view);

		// Configure bottom sheet to expand fully and disable dragging to prevent accidental dismissal
		View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (bottomSheet != null) {
			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			behavior.setSkipCollapsed(true);
			behavior.setDraggable(false); // Disable dragging to prevent accidental dismissal
		}

		// Manual input button
		view.findViewById(R.id.card_manual_input).setOnClickListener(v -> {
			dialog.dismiss();
			showTransactionTypeSelectionBottomSheet(false); // false = manual input
		});

		// OCR scan button
		view.findViewById(R.id.card_ocr_scan).setOnClickListener(v -> {
			dialog.dismiss();
			showTransactionTypeSelectionBottomSheet(true); // true = OCR scan
		});

		dialog.show();
	}

	private void showTransactionTypeSelectionBottomSheet(boolean isOCR) {
		BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
		View view = LayoutInflater.from(this).inflate(R.layout.view_bottom_sheet_transaction_type, null);
		dialog.setContentView(view);

		// Configure bottom sheet to expand fully and disable dragging to prevent accidental dismissal
		View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (bottomSheet != null) {
			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			behavior.setSkipCollapsed(true);
			behavior.setDraggable(false); // Disable dragging to prevent accidental dismissal
		}

		// Add Income button
		view.findViewById(R.id.card_add_income).setOnClickListener(v -> {
			dialog.dismiss();
			openTransactionCreate("income", isOCR);
		});

		// Add Expense button
		view.findViewById(R.id.card_add_expense).setOnClickListener(v -> {
			dialog.dismiss();
			openTransactionCreate("expense", isOCR);
		});

		dialog.show();
	}

	private void openTransactionCreate(String transactionType, boolean isOCR) {
		// TODO: If OCR, handle OCR flow separately
		// For now, just open TransactionFragmentCreate with the selected type
		TransactionCreateFragment transactionCreateFragment = new TransactionCreateFragment();
		
		// Pass transaction type via bundle
		Bundle args = new Bundle();
		args.putString("transaction_type", transactionType);
		args.putBoolean("is_ocr", isOCR);
		transactionCreateFragment.setArguments(args);
		
		transactionCreateFragment.show(getSupportFragmentManager(), TransactionCreateFragment.TAG);
	}
}